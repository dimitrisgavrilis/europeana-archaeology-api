package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.domain.entity.UserEntity;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete
@Repository
public interface UserRepository extends CrudRepository<UserEntity, Integer>{
    
    List<UserEntity> findAllByEmail(String email);
    
    List<UserEntity> findAllByEmailAndPassword(String username, String password);
    
    // List<User> findByAccessToken(String accessToken);
    
}
