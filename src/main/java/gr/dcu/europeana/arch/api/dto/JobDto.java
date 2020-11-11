package gr.dcu.europeana.arch.api.dto;

import gr.dcu.europeana.arch.domain.enums.JobType;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class JobDto {

    private Long id;
    private Long archiveId;
    @Enumerated(EnumType.STRING)
    private JobType jobType;
    private Double progress;
    private Boolean completed;
    private Boolean error;
    private String details;
    private LocalDateTime createdAt;
    private Integer createdBy;
}
