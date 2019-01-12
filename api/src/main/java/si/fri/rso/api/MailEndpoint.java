package si.fri.rso.api;

import com.kumuluz.ee.common.config.EeConfig;
import com.kumuluz.ee.configuration.sources.FileConfigurationSource;
import com.kumuluz.ee.configuration.utils.ConfigurationUtil;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Path("forecast")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class MailEndpoint {
    @POST
    @ApplicationScoped
    @Path("/send")
    public Response get(Mail mail){
        return Response.ok(mail).build();
    }
}
