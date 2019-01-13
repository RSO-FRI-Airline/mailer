package si.fri.rso.api;

import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import com.kumuluz.ee.discovery.annotations.DiscoverService;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;

@Path("mail")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class MailEndpoint {

    @Inject
    @DiscoverService(value = "rso-airline-booking")
    private Optional<String> bookingUrl;

    @Inject
    @DiscoverService(value = "rso-airline-search")
    private Optional<String> searchUrl;

    @Inject
    @DiscoverService(value = "rso-airline-weather")
    private Optional<String> weatherUrl;

    @POST
    @ApplicationScoped
    @Path("/send")
    public Response get(Mail mail){

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                        String password = ConfigurationUtil.getInstance().get("kumuluzee.env.gmail").get();
                        return new PasswordAuthentication("friairlines", password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("friairlines@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(mail.to));
            message.setSubject(mail.subject);
            message.setText(mail.content);

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        return Response.ok(mail).build();
    }

    @POST
    @ApplicationScoped
    @Path("/process")
    public Response get(){
        if(!searchUrl.isPresent() || !bookingUrl.isPresent() || !weatherUrl.isPresent())
            Response.serverError().build();

        try {
            JSONArray bookings = new JSONArray(GetBookings());
            for(int i = 0; i < bookings.length(); i++){
                JSONObject b = bookings.getJSONObject(i);
                JSONObject price = new JSONObject(GetPrice(b.getInt("price_id")));
                JSONObject customer = b.getJSONObject("customer");
                JSONObject schedule = price.getJSONObject("schedule");

                JSONObject forecast = new JSONObject(GetForecast(schedule.getJSONObject("destination").getString("id")));
                JSONObject f = GetDay(forecast, price.getLong("date"));

                Mail mail = new Mail();
                mail.to = customer.getString("mail");
                mail.subject = "Flight "+schedule.getString("id");
                mail.content = "Dear "+customer.getString("name")+"\n "
                    +"Important informations about your departure;\n "
                    +schedule.getJSONObject("origin").getString("city")+" - "
                        +schedule.getJSONObject("destination").getString("city")+"\n "
                    +"Flight number: "+schedule.getString("id")+"\n "
                    +"Departure: "+schedule.getString("start_time")+"\n "
                    +"Arrival: "+schedule.getString("end_time")+"\n "
                    +"Weather forecast: "+f.getString("summary")+"\n "
                    +"Temperatures: "+f.getString("temperatures")
                    +"\n\nHave a great flight!";
                get(mail);
            }


            return Response.ok().build();
        }
        catch(Exception e){
            return Response.serverError().build();
        }
    }

    private JSONObject GetDay(JSONObject forecast, long time) throws Exception{
        JSONObject reports = forecast.getJSONObject("reports");

        Date date = new java.util.Date(time);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+1"));
        String formattedDate = sdf.format(date);

        return reports.getJSONObject(formattedDate);
    }

    private String GetForecast(String city) throws Exception{
        URL url = new URL(weatherUrl.get()+"/v1/forecast/cities/"+city);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        int status = con.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();
        return content.toString();
    }


    private String GetPrice(int token) throws Exception{
        URL url = new URL(searchUrl.get()+"/v1/search/price/"+token);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        int status = con.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();
        return content.toString();
    }

    private String GetBookings() throws Exception{
        URL url = new URL(bookingUrl.get()+"/v1/book/bookings");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        int status = con.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();
        return content.toString();
    }
}
