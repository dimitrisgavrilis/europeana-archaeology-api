package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.model.User;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete
@Repository
public interface UserRepository extends CrudRepository<User, Integer>{
    
    List<User> findAllByEmail(String email);
    
    List<User> findAllByEmailAndPassword(String username, String password);
    
    // List<User> findByAccessToken(String accessToken);
    
}
