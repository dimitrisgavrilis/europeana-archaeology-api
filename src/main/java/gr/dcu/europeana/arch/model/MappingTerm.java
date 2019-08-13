package gr.dcu.europeana.arch.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
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

/**
 *
 * @author Vangelis Nomikos
 */
@Entity 
@Table(name = "subject_mapping_terms")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class MappingTerm implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonIgnore
    @Column (name="mapping_id")
    private Long mappingId;
    
    @Column (name="native_term")
    private String nativeTerm;
    
    @Column (name="aat_concept_label")
    private String aatConceptLabel;
    
    @Column (name="aat_uid")
    private String aatUid;
    
}
