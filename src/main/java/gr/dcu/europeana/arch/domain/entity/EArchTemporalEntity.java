package gr.dcu.europeana.arch.domain.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "earch_temporal")
public class EArchTemporalEntity {

    @Id
    @Column(name = "label")
    private String label;

    @Column(name = "aat_uid")
    private String aatUid;

    @Column(name = "aat_uri")
    private String aatUri;

    @Column(name = "start_year")
    private String startYear;

    @Column(name = "end_year")
    private String endYear;

    @Column(name = "wikidata_uri")
    private String wikidataUri;
}
