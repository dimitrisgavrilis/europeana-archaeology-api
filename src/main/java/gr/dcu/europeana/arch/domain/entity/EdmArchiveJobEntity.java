package gr.dcu.europeana.arch.domain.entity;

import gr.dcu.europeana.arch.domain.enums.JobType;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "edm_archive_jobs")
public class EdmArchiveJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "archive_id")
    private Long archiveId;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type")
    private JobType jobType;

    @Column(name = "progress")
    private Double progress;

    @Column(name = "completed")
    private Boolean completed;

    @Column(name = "error")
    private Boolean error;

    @Column(name = "details")
    private String details;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

}
