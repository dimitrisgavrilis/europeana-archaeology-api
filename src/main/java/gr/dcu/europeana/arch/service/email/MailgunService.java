package gr.dcu.europeana.arch.service.email;

import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

/**
 *
 * @author Vangelis Nomikos
 */
@Slf4j
@Service
public class MailgunService {
    
    // @Autowired
    // private AppConfig appConfig;
    
    private String domainName;
    private String apiKey;
    
    // private static final String domainName = "mailer.share3d.eu";
    // private static final String API_KEY = "c078aecbaeec4cf8ea31cb8e95b2d46e-1df6ec32-e856d932";
    
    /*
    public static JsonNode sendSimpleMessage(String domainName) throws UnirestException {
        
        HttpResponse<JsonNode> request = Unirest.post("https://api.mailgun.net/v3/" + domainName + "/messages"),
                .basicAuth("api", API_KEY)
                .queryString("from", "Excited User <USER@YOURDOMAIN.COM>")
                .queryString("to", "artemis@example.com")
                .queryString("subject", "hello")
                .queryString("text", "testing")
                .asJson();
        return request.getBody();
    }*/
    
    /**
     * 
     * @param domainName
     * @param apiKey 
     */
    public void configure(String domainName, String apiKey) {
        this.domainName = domainName;
        this.apiKey = apiKey;
    }
    /**
     * 
     * @param from
     * @param toRecipients
     * @param subject
     * @param body
     * @param isHtml
     * @return
     * @throws Exception 
     */
    public String sendMessage(String from, List<String> toRecipients, String subject,
            String body, boolean isHtml) throws Exception {

        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basicBuilder()
                .nonPreemptive()
                .credentials("api", this.apiKey)
                .build();

        ClientConfig config = new ClientConfig();
        config.register(feature);

        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target("https://api.eu.mailgun.net/v3/" + this.domainName + "/messages");

        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.add("from", from);
        
        for(String toRecipient : toRecipients) {
            formData.add("to", toRecipient);
        }
        
        formData.add("subject", subject);

        if(isHtml) {
            formData.add("html", body);
        } else {
            formData.add("text", body);
        }

        String response;
        try {
            response = target.request().post(Entity.form(formData), String.class);
        } catch (Exception ex) {
            log.error("", ex);
            throw ex;
        }

        return response;
    }
}
