package gr.dcu.europeana.arch.model.mappers;

import gr.dcu.europeana.arch.model.TemporalTermEntity;
import gr.dcu.europeana.arch.service.edm.ElementExtractionData;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vangelis Nomikos
 */
@Component
public class TemporalTermMapper {
    
    public TemporalTermEntity toTemporalTerm(ElementExtractionData extractionData) {
        
        TemporalTermEntity term = new TemporalTermEntity();
        term.setNativeTerm(extractionData.getElementValue());
        term.setLanguage(extractionData.getXmlLangAttrValue());
        term.setEarchTemporalLabel("");

        return term;
    }
    
    public List<TemporalTermEntity> toTemporalTermList(Collection<ElementExtractionData> extractionDataList) {
        List<TemporalTermEntity> terms = new LinkedList<>();
        
        for(ElementExtractionData extractionData : extractionDataList) {
            terms.add(toTemporalTerm(extractionData));
        }
        
        return terms;
    }

    
    
}
