package gr.dcu.europeana.arch.api.resource.auth;

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
public class LogoutResponse {
    
    // private String status;
    
    private boolean success;
    
}