package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.domain.entity.UserEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    
    List<UserEntity> findAllByEmail(String email);
    
    List<UserEntity> findAllByEmailAndPassword(String username, String password);
    
    // List<User> findByAccessToken(String accessToken);
    
}
