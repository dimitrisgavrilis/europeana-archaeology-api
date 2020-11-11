package gr.dcu.europeana.arch.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;
import java.util.List;
import gr.dcu.europeana.arch.domain.enums.EdmArchiveStatus;

@Data
@NoArgsConstructor
public class EdmArchiveWithJobs {

    private Long id;
    private String name;
    private Integer itemCount;
    private String filename;
    private String filepath;
    private String enrichedFilename;
    private String enrichedFilepath;
    private Long thematicMapping;
    private Long spatialMapping;
    private Long temporalMapping;
    @Enumerated(EnumType.STRING)
    private EdmArchiveStatus status;
    private LocalDateTime createdAt;
    private int createdBy;
    private List<JobDto> jobs;
}
