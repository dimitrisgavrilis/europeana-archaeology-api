package gr.dcu.europeana.arch.service.edm;

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
}
