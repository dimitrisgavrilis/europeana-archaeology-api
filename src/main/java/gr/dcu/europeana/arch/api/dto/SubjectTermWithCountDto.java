package gr.dcu.europeana.arch.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Transient;

@Data
@NoArgsConstructor
public class SubjectTermWithCountDto {
    private Long id;
    private String nativeTerm;
    private String language;
    private String aatConceptLabel;
    private String aatUid;
    private Integer count;
}
