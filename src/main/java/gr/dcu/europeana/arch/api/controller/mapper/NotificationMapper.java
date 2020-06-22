package gr.dcu.europeana.arch.api.controller.mapper;

import gr.dcu.europeana.arch.api.dto.NotificationViewDto;
import gr.dcu.europeana.arch.domain.entity.NotificationEntity;
import org.mapstruct.Mapper;

@Mapper
public interface NotificationMapper {

    // @Mapping(target = "userId", ignore = true)
    NotificationViewDto fromEntity(NotificationEntity entity);
}
