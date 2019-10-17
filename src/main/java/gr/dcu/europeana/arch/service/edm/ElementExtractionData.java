package gr.dcu.europeana.arch.service.edm;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Vangelis Nomikos
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ElementExtractionData {
    
    private String elementName;
    private String xmlLangAttrValue;
    private String rdfResourceAttrValue;
    private String elementValue;
}
