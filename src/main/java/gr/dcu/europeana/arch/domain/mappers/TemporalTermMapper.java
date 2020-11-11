package gr.dcu.europeana.arch.domain.mappers;

import gr.dcu.europeana.arch.domain.entity.TemporalTermEntity;
import gr.dcu.europeana.arch.service.edm.ElementExtractionData;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class TemporalTermMapper {
    
    public TemporalTermEntity toTemporalTerm(ElementExtractionData extractionData, Integer count) {
        
        TemporalTermEntity term = new TemporalTermEntity();
        term.setNativeTerm(extractionData.getElementValue());
        term.setLanguage(extractionData.getXmlLangAttrValue());
        term.setEarchTemporalLabel("");
        term.setCount(count);

        return term;
    }
    
    public List<TemporalTermEntity> toTemporalTermList(Collection<ElementExtractionData> extractionDataList,
                                                       Map<ElementExtractionData, Integer> extractionDataCountMap) {
        List<TemporalTermEntity> terms = new LinkedList<>();
        
        for(ElementExtractionData extractionData : extractionDataList) {
            terms.add(toTemporalTerm(extractionData, extractionDataCountMap.get(extractionData)));
        }
        
        return terms;
    }
}
