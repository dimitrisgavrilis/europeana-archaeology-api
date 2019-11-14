package gr.dcu.europeana.arch.model.mappers;

import gr.dcu.europeana.arch.model.SubjectTerm;
import gr.dcu.europeana.arch.model.TemporalTerm;
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
    
    public TemporalTerm toTemporalTerm(ElementExtractionData extractionData) {
        
        TemporalTerm term = new TemporalTerm();
        term.setNativeTerm(extractionData.getElementValue());
        term.setLanguage(extractionData.getXmlLangAttrValue());
        term.setAatConceptLabel("");
        term.setAatUid("");
        return term;
    }
    
    public List<TemporalTerm> toTemporalTermList(Collection<ElementExtractionData> extractionDataList) {
        List<TemporalTerm> terms = new LinkedList<>();
        
        for(ElementExtractionData extractionData : extractionDataList) {
            terms.add(toTemporalTerm(extractionData));
        }
        
        return terms;
    }

    
    
}
