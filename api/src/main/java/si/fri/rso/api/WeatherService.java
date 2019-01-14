package si.fri.rso.api;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.annotation.Timed;

import javax.enterprise.context.ApplicationScoped;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@ApplicationScoped
public class WeatherService {
    @Timed
    @CircuitBreaker(requestVolumeThreshold = 3)
    @Timeout(value = 3, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "GetForecastFallback")
    public String GetForecast(String city, Optional<String> weatherUrl) throws Exception{
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

    public String GetForecastFallback(String city, Optional<String> weatherUrl) throws Exception {
        System.out.println("Fallback executed");
        return "{\"reports\": {}}";
    }
}
