package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.model.UserSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSessionEntity, Long>{
    
    UserSessionEntity findBySessionIdAndIpAddress(String sessionId, String ipAdress);

    UserSessionEntity findBySessionId(String sessionId);
    
}
