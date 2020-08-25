package gr.dcu.europeana.arch.api.controller.mapper;

import gr.dcu.europeana.arch.api.dto.user.UserViewDto;
import gr.dcu.europeana.arch.domain.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {
    UserViewDto toUserViewDto(UserEntity entity);
}
