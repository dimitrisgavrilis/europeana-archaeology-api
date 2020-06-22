package gr.dcu.europeana.arch.api.adapter;

import gr.dcu.europeana.arch.api.dto.EArchTemporalViewDto;
import gr.dcu.europeana.arch.domain.entity.EArchTemporalEntity;
import org.mapstruct.Mapper;

@Mapper
public interface UtilityControllerMapper {

    EArchTemporalViewDto fromEntity(EArchTemporalEntity entity);
}
