package gr.dcu.europeana.arch.api.dto;

import gr.dcu.europeana.arch.domain.entity.UserEntity;
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
public class UserResource {
    
    private Integer id;
    
    private String name;
    
    private String email;
    
    // private int favoriteCount;
    
    // private int viewCount;
    
    public static UserResource toResource(UserEntity userEntity) {
        
        if(userEntity == null) {
            return null;
        }
        
        UserResource resource = new UserResource();
        resource.setId(userEntity.getId());
        resource.setName(userEntity.getName());
        resource.setEmail(userEntity.getEmail());
        
        return resource;
        
    }
    
}
