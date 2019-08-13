package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, Long>{
    
    UserSession findBySessionIdAndIpAddress(String sessionId, String ipAdress);

    UserSession findBySessionId(String sessionId);
    
}
