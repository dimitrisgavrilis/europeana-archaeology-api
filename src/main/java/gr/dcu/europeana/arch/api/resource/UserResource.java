package gr.dcu.europeana.arch.api.resource;

import gr.dcu.europeana.arch.model.User;
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
    
    public static UserResource toResource(User user) {
        
        if(user == null) {
            return null;
        }
        
        UserResource resource = new UserResource();
        resource.setId(user.getId());
        resource.setName(user.getName());
        resource.setEmail(user.getEmail());
        
        return resource;
        
    }
    
}
