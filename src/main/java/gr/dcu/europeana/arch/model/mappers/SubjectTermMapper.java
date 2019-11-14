package gr.dcu.europeana.arch.model.mappers;

import gr.dcu.europeana.arch.model.SubjectTerm;
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
public class SubjectTermMapper {
    
    public SubjectTerm toSubjectTerm(ElementExtractionData extractionData) {
        
        SubjectTerm term = new SubjectTerm();
        term.setNativeTerm(extractionData.getElementValue());
        term.setLanguage(extractionData.getXmlLangAttrValue());
        term.setAatConceptLabel("");
        term.setAatUid("");
        return term;
    }
    
    public List<SubjectTerm> toSubjectTermList(Collection<ElementExtractionData> extractionDataList) {
        List<SubjectTerm> terms = new LinkedList<>();
        
        for(ElementExtractionData extractionData : extractionDataList) {
            terms.add(toSubjectTerm(extractionData));
        }
        
        return terms;
    }

    
    
}
