package gr.dcu.europeana.arch.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EnrichmentResult {
    private int valueCount; // Number of all possible values for enrichment
    private int matchCount; // Number of matches
}
