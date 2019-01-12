package si.fri.rso.api;


import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("v1")
public class MailerApplication  extends Application {
    public MailerApplication() {
        super();
    }
}
