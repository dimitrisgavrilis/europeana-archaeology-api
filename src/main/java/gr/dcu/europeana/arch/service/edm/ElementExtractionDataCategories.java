package gr.dcu.europeana.arch.service.edm;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Vangelis Nomikos
 */
@Getter
@Setter
@NoArgsConstructor
public class ElementExtractionDataCategories {
    
    private Set<ElementExtractionData> thematicElementValues;
    private Set<ElementExtractionData> spatialElementValues;
    private Set<ElementExtractionData> temporalElementValues;
}
