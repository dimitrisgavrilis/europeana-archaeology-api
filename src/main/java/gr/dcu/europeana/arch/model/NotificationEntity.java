package gr.dcu.europeana.arch.model;

import gr.dcu.europeana.arch.model.enums.NotificationType;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data
public class NotificationEntity implements Serializable  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (name="user_id")
    private Long userId;

    @Column (name="edm_archive_id")
    private Long edmArchiveId;

    @Column (name="label")
    private String label;

    @Column (name="description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column (name="n_type")
    private NotificationType notificationType;

    @Column (name="read")
    private Boolean read;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
