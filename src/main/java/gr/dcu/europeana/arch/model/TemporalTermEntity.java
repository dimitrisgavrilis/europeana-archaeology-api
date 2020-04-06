package gr.dcu.europeana.arch.model;

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

@Entity 
@Table(name = "temporal_terms")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class TemporalTermEntity implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @Column (name="mapping_id")
    private Long mappingId;
    
    @Column (name="native_term")
    private String nativeTerm;

    @Column (name="earch_temporal_label")
    private String earchTemporalLabel;

    @Column (name="aat_uid")
    private String aatUid;

    @Column (name="language")
    private String language;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
}
