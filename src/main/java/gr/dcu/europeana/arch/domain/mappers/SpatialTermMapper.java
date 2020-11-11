package gr.dcu.europeana.arch.domain.mappers;

import gr.dcu.europeana.arch.domain.entity.SpatialTermEntity;
import gr.dcu.europeana.arch.service.edm.ElementExtractionData;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class SpatialTermMapper {
    
    public SpatialTermEntity toSpatialTerm(ElementExtractionData extractionData, Integer count) {
        
        SpatialTermEntity term = new SpatialTermEntity();
        term.setNativeTerm(extractionData.getElementValue());
        term.setLanguage(extractionData.getXmlLangAttrValue());
        term.setGeonameName("");
        term.setGeonameId("");
        term.setCount(count);
        return term;
    }
    
    public List<SpatialTermEntity> toSpatialTermList(Collection<ElementExtractionData> extractionDataList,
                                                     Map<ElementExtractionData, Integer> extractionDataCountMap) {
        List<SpatialTermEntity> terms = new LinkedList<>();
        
        for(ElementExtractionData extractionData : extractionDataList) {
            terms.add(toSpatialTerm(extractionData, extractionDataCountMap.get(extractionData)));
        }
        
        return terms;
    }
    
}
