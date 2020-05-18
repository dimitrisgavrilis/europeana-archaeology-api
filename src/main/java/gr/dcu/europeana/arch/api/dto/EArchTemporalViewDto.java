package gr.dcu.europeana.arch.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EArchTemporalViewDto {
    private String label;
    private String aatUid;
    private String aatUri;
    private String startYear;
    private String endYear;
    private String wikidataUri;
}