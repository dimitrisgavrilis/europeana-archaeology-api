package gr.dcu.europeana.arch.api.dto.auth;

import gr.dcu.europeana.arch.api.dto.user.UserViewDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginResponse {

    private String token;
    private UserViewDto user;
}
