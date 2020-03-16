package gr.dcu.europeana.arch.service.email;

import com.google.common.io.Resources;
import gr.dcu.europeana.arch.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.Charsets;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Vangelis Nomikos
 */
@Slf4j
@Service
public class EmailBuilderService {
    
    private final FileStorageService fileStorageService;
    
    private static final String SIGNUP_TEMPLATE                         = "templates/Signup.txt";
    private static final String RESET_PASSWORD_TEMPLATE                 = "templates/ResetPassword.txt";

    public static final String VAR_PROJECT_NAME     = "${PROJECT_NAME}";
    public static final String VAR_USER_NAME        = "${USER_NAME}";
    public static final String VAR_SERVICE_NAME     = "${SERVICE_NAME}";
    public static final String VAR_PASSWORD         = "${PASSWORD}";

    public EmailBuilderService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * Build signup email
     * @param defaultText
     * @param variables
     * @return 
     */
    public String buildSignupEmail(String defaultText, Map<String, String> variables) {
        
        String content;
        
        try {
            Resource resource = fileStorageService.loadFileAsResource(SIGNUP_TEMPLATE);
                    // fileStorageService.loadFileAsResource(Paths.get(SINGUP_TEMPLATE));
            content = Resources.toString(resource.getURL(), Charsets.UTF_8);
            
            for(Map.Entry<String, String> entry : variables.entrySet()) {
                content = content.replace(entry.getKey(), entry.getValue());
            }
            
        } catch (IOException ex) {
            log.error("Load template failed.");
            content = defaultText;
        }
        
        return content;
        
    }
    
    /**
     * Build reset password email
     * @param defaultText
     * @param variables
     * @return 
     */
    public String buildResetPasswordTemplate(String defaultText, Map<String, String> variables) {
        
        String content;
        
        try {
            Resource resource = fileStorageService.loadFileAsResource(RESET_PASSWORD_TEMPLATE);
            content = Resources.toString(resource.getURL(), Charsets.UTF_8);
            
            for(Map.Entry<String, String> entry : variables.entrySet()) {
                content = content.replace(entry.getKey(), entry.getValue());
            }
            
        } catch (IOException ex) {
            log.error("Load template failed.");
            content = defaultText;
        }
        
        return content;
        
    }
}
