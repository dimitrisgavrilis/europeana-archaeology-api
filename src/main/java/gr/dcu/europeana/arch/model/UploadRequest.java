package gr.dcu.europeana.arch.model;

import java.io.Serializable;
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

/**
 *
 * @author Vangelis Nomikos
 */
@Entity 
@Table(name = "subject_mapping_upload")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class UploadRequest implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column (name="mapping_id")
    private Long mappingId;
    
    @Column (name="filename")
    private String filename;
    
    @Column (name="filepath")
    private String filepath;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "created_by")
    private int createdBy;

}
