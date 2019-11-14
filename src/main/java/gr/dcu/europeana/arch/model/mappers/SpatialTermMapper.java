package gr.dcu.europeana.arch.model.mappers;

import gr.dcu.europeana.arch.model.SpatialTerm;
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
public class SpatialTermMapper {
    
    public SpatialTerm toSpatialTerm(ElementExtractionData extractionData) {
        
        SpatialTerm term = new SpatialTerm();
        term.setNativeTerm(extractionData.getElementValue());
        term.setLanguage(extractionData.getXmlLangAttrValue());
        term.setGeonameName("");
        term.setGeonameId("");
        return term;
    }
    
    public List<SpatialTerm> toSpatialTermList(Collection<ElementExtractionData> extractionDataList) {
        List<SpatialTerm> terms = new LinkedList<>();
        
        for(ElementExtractionData extractionData : extractionDataList) {
            terms.add(toSpatialTerm(extractionData));
        }
        
        return terms;
    }
    
}
