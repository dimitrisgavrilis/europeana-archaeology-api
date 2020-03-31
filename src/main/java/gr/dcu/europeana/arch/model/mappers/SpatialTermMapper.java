package gr.dcu.europeana.arch.model.mappers;

import gr.dcu.europeana.arch.model.SpatialTermEntity;
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
public class SpatialTermMapper {
    
    public SpatialTermEntity toSpatialTerm(ElementExtractionData extractionData) {
        
        SpatialTermEntity term = new SpatialTermEntity();
        term.setNativeTerm(extractionData.getElementValue());
        term.setLanguage(extractionData.getXmlLangAttrValue());
        term.setGeonameName("");
        term.setGeonameId("");
        return term;
    }
    
    public List<SpatialTermEntity> toSpatialTermList(Collection<ElementExtractionData> extractionDataList) {
        List<SpatialTermEntity> terms = new LinkedList<>();
        
        for(ElementExtractionData extractionData : extractionDataList) {
            terms.add(toSpatialTerm(extractionData));
        }
        
        return terms;
    }
    
}
