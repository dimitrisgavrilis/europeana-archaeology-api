package gr.dcu.share3d.mapping.api.resource.auth;

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
public class ValidateTokenResponse {
    
    // private String token;
    
    private boolean isValid;
}
