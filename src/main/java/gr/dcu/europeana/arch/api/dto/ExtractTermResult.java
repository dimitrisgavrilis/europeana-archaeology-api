package gr.dcu.europeana.arch.api.dto;

import gr.dcu.europeana.arch.model.SpatialTermEntity;
import gr.dcu.europeana.arch.model.SubjectTermEntity;
import gr.dcu.europeana.arch.model.TemporalTermEntity;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Vangelis Nomikos
 */
@Getter
@Setter
@NoArgsConstructor
public class ExtractTermResult {
    
    private List<SubjectTermEntity> subjectTermEntities;
    private List<SpatialTermEntity> spatialTermEntities;
    private List<TemporalTermEntity> temporalTermEntities;
    
}
