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
import org.hibernate.annotations.CreationTimestamp;

/**
 *
 * @author Vangelis Nomikos
 */
@Entity
@Table (name = "subject_mapping")
@Getter
@Setter
@NoArgsConstructor
public class SubjectMapping implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column (name="label")
    private String label;
    
    @Column (name="description")
    private String description;
    
    @Column (name="type")
    private String type;
    
    @Column (name="language")
    private String language;
    
    @Column (name="provider_name")
    private String providerName;
    
    @Column (name="vocabulary_name")
    private String vocabularyName;
    
    @JsonIgnore
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @JsonIgnore
    @Column (name="created_by")
    private int createdBy;
}
