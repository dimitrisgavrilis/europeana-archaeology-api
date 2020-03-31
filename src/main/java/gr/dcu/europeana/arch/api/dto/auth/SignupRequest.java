package gr.dcu.europeana.arch.api.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Vangelis Nomikos
 */
@Getter
@Setter
@NoArgsConstructor
public class SignupRequest {
    
    private String username;
    
    private String password;
    
    private String name; 
    
    private String organization;
    
    private String email;
}
