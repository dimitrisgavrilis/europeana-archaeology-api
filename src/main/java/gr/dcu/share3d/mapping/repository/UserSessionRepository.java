package gr.dcu.share3d.mapping.repository;

import gr.dcu.share3d.mapping.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, Long>{
    
    UserSession findBySessionIdAndIpAddress(String sessionId, String ipAdress);

    UserSession findBySessionId(String sessionId);
    
}
