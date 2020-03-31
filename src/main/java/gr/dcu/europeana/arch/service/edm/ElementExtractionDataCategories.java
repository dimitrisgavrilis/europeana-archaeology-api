package gr.dcu.europeana.arch.service.edm;

import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ElementExtractionDataCategories {
    
    private Set<ElementExtractionData> thematicElementValues;
    private Set<ElementExtractionData> spatialElementValues;
    private Set<ElementExtractionData> temporalElementValues;

    private Map<ElementExtractionData, Integer> thematicElementValuesCountMap;
    private Map<ElementExtractionData, Integer> spatialElementValuesCountMap;
    private Map<ElementExtractionData, Integer> temporalElementValuesCountMap;
}
