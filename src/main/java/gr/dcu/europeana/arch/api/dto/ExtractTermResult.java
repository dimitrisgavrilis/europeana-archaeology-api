package gr.dcu.europeana.arch.api.dto;

import gr.dcu.europeana.arch.domain.entity.SpatialTermEntity;
import gr.dcu.europeana.arch.domain.entity.SubjectTermEntity;
import gr.dcu.europeana.arch.domain.entity.TemporalTermEntity;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
public class ExtractTermResult {
    
    private List<SubjectTermEntity> subjectTermEntities;
    private List<SpatialTermEntity> spatialTermEntities;
    private List<TemporalTermEntity> temporalTermEntities;
    
}
