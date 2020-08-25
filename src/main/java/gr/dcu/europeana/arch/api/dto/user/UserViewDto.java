package gr.dcu.europeana.arch.api.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserViewDto {
    private Integer id;
    private String name;
    private String email;
    private String organization;
    private boolean isAdmin;
}
