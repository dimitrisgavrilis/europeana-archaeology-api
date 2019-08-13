package gr.dcu.europeana.arch.api.resource.auth;

import gr.dcu.europeana.arch.api.resource.UserResource;
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
