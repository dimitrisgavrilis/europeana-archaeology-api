package gr.dcu.europeana.arch.api.dto;

import gr.dcu.europeana.arch.domain.entity.SpatialTermEntity;
import gr.dcu.europeana.arch.domain.entity.SubjectTermEntity;
import gr.dcu.europeana.arch.domain.entity.TemporalTermEntity;
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
