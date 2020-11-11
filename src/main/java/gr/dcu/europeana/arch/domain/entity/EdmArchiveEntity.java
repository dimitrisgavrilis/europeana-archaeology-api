package gr.dcu.europeana.arch.domain.entity;

import java.time.LocalDateTime;
import javax.persistence.*;

import gr.dcu.europeana.arch.domain.enums.EdmArchiveStatus;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity 
@Table(name = "edm_archive")
@Data
@ToString
@NoArgsConstructor
public class EdmArchiveEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column (name="name")
    private String name;
    
    @Column (name="item_count")
    private Integer itemCount;
    
    @Column (name="filename")
    private String filename;
    
    @Column (name="filepath")
    private String filepath;

    @Column (name="enriched_filename")
    private String enrichedFilename;

    @Column (name="enriched_filepath")
    private String enrichedFilepath;
    
    @Column (name="thematic_mapping")
    private Long thematicMapping;
    
    @Column (name="spatial_mapping")
    private Long spatialMapping;
    
    @Column (name="temporal_mapping")
    private Long temporalMapping;

    @Enumerated(EnumType.STRING)
    @Column (name="status")
    private EdmArchiveStatus status;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "created_by")
    private int createdBy;
    
}
