package gr.dcu.europeana.arch.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EnrichmentResult {
    private int valueCount;
    private int matchCount;
}
