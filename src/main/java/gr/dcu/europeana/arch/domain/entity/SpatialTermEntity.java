package gr.dcu.europeana.arch.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "spatial_terms")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class SpatialTermEntity implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonIgnore
    @Column (name="mapping_id")
    private Long mappingId;
    
    @Column (name="native_term")
    private String nativeTerm;
    
    @Column (name="geoname_name")
    private String geonameName;
    
    @Column (name="geoname_id")
    private String geonameId;
    
    @Column (name="language")
    private String language;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
}
