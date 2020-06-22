package gr.dcu.europeana.arch.domain.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_sessions")
@NoArgsConstructor
@Getter
@Setter
public class UserSessionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;
    
    @Column(name = "user_id")
    private Integer userId;
    
    @Column(name = "session_id")
    private String sessionId;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "login_at")
    private LocalDateTime loginAt;
    
    @Column(name = "logout_at")
    private LocalDateTime logoutAt;
}

