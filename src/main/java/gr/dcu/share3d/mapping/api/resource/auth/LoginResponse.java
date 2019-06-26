package gr.dcu.share3d.mapping.api.resource.auth;

import gr.dcu.share3d.mapping.api.resource.UserResource;
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
public class LoginResponse {

    private String token;
    private UserResource user;
}
