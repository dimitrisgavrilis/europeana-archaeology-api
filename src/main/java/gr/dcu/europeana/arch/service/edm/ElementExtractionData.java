package gr.dcu.europeana.arch.service.edm;

import java.util.Objects;
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
    private String elementValue;
    private String xmlLangAttrValue;
    private String rdfResourceAttrValue;

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.elementValue);
        hash = 79 * hash + Objects.hashCode(this.xmlLangAttrValue);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ElementExtractionData other = (ElementExtractionData) obj;
        if (!Objects.equals(this.elementValue, other.elementValue)) {
            return false;
        }
        if (!Objects.equals(this.xmlLangAttrValue, other.xmlLangAttrValue)) {
            return false;
        }
        return true;
    }

    
    
    
    
}
