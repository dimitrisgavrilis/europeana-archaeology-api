package gr.dcu.europeana.arch.model;

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
@Table(name = "edm_archive")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class EdmArchive {
    
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
    
    @Column (name="enriched_filepath")
    private String enrichedFilepath;
    
    @Column (name="thematic_mapping")
    private Long thematicMapping;
    
    @Column (name="spatial_mapping")
    private Long spatialMapping;
    
    @Column (name="temporal_mapping")
    private Long temporalMapping;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "created_by")
    private int createdBy;
    
}
