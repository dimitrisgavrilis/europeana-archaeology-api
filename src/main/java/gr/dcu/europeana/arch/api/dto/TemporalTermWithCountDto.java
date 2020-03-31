package gr.dcu.europeana.arch.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class TemporalTermWithCountDto {
    private Long id;
    private String nativeTerm;
    private String language;
    private String aatConceptLabel;
    private String aatUid;
    private LocalDateTime createdAt;
    private Integer count;
}
