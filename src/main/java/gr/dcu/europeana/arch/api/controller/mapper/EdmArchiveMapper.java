package gr.dcu.europeana.arch.api.controller.mapper;

import gr.dcu.europeana.arch.api.dto.EdmArchiveWithJobs;
import gr.dcu.europeana.arch.api.dto.JobDto;
import gr.dcu.europeana.arch.domain.entity.EdmArchiveEntity;
import gr.dcu.europeana.arch.domain.entity.EdmArchiveJobEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface EdmArchiveMapper {

//    EdmArchiveStatus toDto(EdmArchiveEntity entiy);

    // @Mapping(target = "id", ignore = true)
    JobDto toDto(EdmArchiveJobEntity entity);

    EdmArchiveWithJobs toDto(EdmArchiveEntity entity, List<JobDto> jobs);
}
