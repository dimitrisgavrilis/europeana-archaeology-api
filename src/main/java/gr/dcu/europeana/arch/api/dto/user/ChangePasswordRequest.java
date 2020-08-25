package gr.dcu.europeana.arch.api.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class ChangePasswordRequest {
    @NotBlank
    private String password;
}
