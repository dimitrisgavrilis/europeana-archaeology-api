package gr.dcu.europeana.arch.api.controller.mapper;

import gr.dcu.europeana.arch.api.dto.AatSubjectCreateDto;
import gr.dcu.europeana.arch.api.dto.EArchTemporalCreateDto;
import gr.dcu.europeana.arch.api.dto.EArchTemporalViewDto;
import gr.dcu.europeana.arch.domain.entity.AatSubjectEntity;
import gr.dcu.europeana.arch.domain.entity.EArchTemporalEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface AdminControllerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    AatSubjectEntity toEntity(AatSubjectCreateDto dto);

    EArchTemporalEntity toEntity(EArchTemporalCreateDto dto);
    EArchTemporalViewDto fromEntity(EArchTemporalEntity entity);
}
