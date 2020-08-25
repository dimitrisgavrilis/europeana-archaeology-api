package gr.dcu.europeana.arch.api.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateRequest {

    @NotBlank
    private String name;
    @Email
    private String email;
    private String organization;
    private Boolean isAdmin;
}
