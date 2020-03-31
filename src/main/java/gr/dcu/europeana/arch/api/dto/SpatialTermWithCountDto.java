package gr.dcu.europeana.arch.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class SpatialTermWithCountDto {
    private Long id;
    private String nativeTerm;
    private String geonameName;
    private String geonameId;
    private String language;
    private LocalDateTime createdAt;
    private Integer count;
}
