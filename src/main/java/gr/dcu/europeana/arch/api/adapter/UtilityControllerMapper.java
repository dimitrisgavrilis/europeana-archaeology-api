package gr.dcu.europeana.arch.api.adapter;

import gr.dcu.europeana.arch.api.dto.EArchTemporalDto;
import gr.dcu.europeana.arch.model.EArchTemporalEntity;
import org.mapstruct.Mapper;

@Mapper
public interface UtilityControllerMapper {

    EArchTemporalDto fromEntity(EArchTemporalEntity entity);
}
