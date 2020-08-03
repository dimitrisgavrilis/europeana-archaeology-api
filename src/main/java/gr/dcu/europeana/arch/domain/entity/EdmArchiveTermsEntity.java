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
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Entity 
@Table(name = "edm_archive_terms")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class EdmArchiveTermsEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column (name="archive_id")
    private Long archiveId;
    
    @Column (name="subject_terms")
    private String subjectTerms;
    
    @Column (name="spatial_terms")
    private String spatialTerms;
    
    @Column (name="temporal_terms")
    private String temporalTerms;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "created_by")
    private Integer createdBy;
    
    
    
}
