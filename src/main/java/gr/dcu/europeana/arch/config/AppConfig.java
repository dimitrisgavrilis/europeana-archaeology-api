package gr.dcu.europeana.arch.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Vangelis Nomikos
 */
@Getter
@Setter
@NoArgsConstructor
@Configuration
public class AppConfig {

    @Value("${mailgun.domainName}")
    private String mailgunDomainName;
    
    @Value("${mailgun.apiKey}")
    private String mailgunApiKey;
    
    @Value("${mailgun.sender}")
    private String mailgunSender;
    
    // @Value("${mailgun.recipients}")
    // private String mailgunRecipients;
    
    // @Value("${api.baseUrl}")
    // private String apiBaseUrl;

    // @Value("${dashboard.url}")
    // private String dashboardUrl;

    public static final String PROJECT_NAME = "Europeana Archaeology";
    public static final String SERVICE_NAME = "Mapping Tool";
}
