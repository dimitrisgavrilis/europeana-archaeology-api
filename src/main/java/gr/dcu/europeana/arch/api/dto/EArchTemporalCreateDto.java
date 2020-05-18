package gr.dcu.europeana.arch.api.dto;

import lombok.Data;

@Data
public class EArchTemporalCreateDto {
    private String label;
    private String aatUid;
    private String aatUri;
    private String startYear;
    private String endYear;
    private String wikidataUri;
}
