package gr.dcu.europeana.arch.api.controller.mapper;

import gr.dcu.europeana.arch.api.dto.AatSubjectCreateDto;
import gr.dcu.europeana.arch.api.dto.EArchTemporalCreateDto;
import gr.dcu.europeana.arch.api.dto.EArchTemporalViewDto;
import gr.dcu.europeana.arch.api.dto.NotificationViewDto;
import gr.dcu.europeana.arch.model.AatSubjectEntity;
import gr.dcu.europeana.arch.model.EArchTemporalEntity;
import gr.dcu.europeana.arch.model.NotificationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface NotificationMapper {

    // @Mapping(target = "userId", ignore = true)
    NotificationViewDto fromEntity(NotificationEntity entity);
}
