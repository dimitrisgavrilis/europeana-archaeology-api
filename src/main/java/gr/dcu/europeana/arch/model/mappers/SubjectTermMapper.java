package gr.dcu.europeana.arch.model.mappers;

import gr.dcu.europeana.arch.model.SubjectTermEntity;
import gr.dcu.europeana.arch.service.edm.ElementExtractionData;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 *
 * @author Vangelis Nomikos
 */
@Component
public class SubjectTermMapper {
    
    public SubjectTermEntity toSubjectTerm(ElementExtractionData extractionData, Integer count) {
        
        SubjectTermEntity term = new SubjectTermEntity();
        term.setNativeTerm(extractionData.getElementValue());
        term.setLanguage(extractionData.getXmlLangAttrValue());
        term.setAatConceptLabel("");
        term.setAatUid("");
        term.setCount(count);
        return term;
    }
    
    public List<SubjectTermEntity> toSubjectTermList(Collection<ElementExtractionData> extractionDataList,
                                                     Map<ElementExtractionData, Integer> extractionDataCountMap) {
        List<SubjectTermEntity> terms = new LinkedList<>();
        
        for(ElementExtractionData extractionData : extractionDataList) {
            terms.add(toSubjectTerm(extractionData, extractionDataCountMap.get(extractionData)));
        }
        
        return terms;
    }

    
    
}
