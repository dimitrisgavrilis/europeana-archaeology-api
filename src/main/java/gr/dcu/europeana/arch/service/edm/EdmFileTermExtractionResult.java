package gr.dcu.europeana.arch.service.edm;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Vangelis Nomikos
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EdmFileTermExtractionResult {
    
    private String filename;
    private List<ElementExtractionData> extractionData;
    /*
    private List<String> dcSubjectValues;
    private List<String> dcTypeValues;
    private List<String> dcDateValues;
    private List<String> dcTermsTemporalValues;
    private List<String> dcTermsCreatedValues;
    private List<String> dcTermsSpatialValues;
    
    public void printResultSize() {
        
        log.info("File: {} => Thematic: {} {} - Temporal: {} {} {} - Spatial: {}", filename,
                dcSubjectValues.size(),dcTypeValues.size(), 
                dcDateValues.size(), dcTermsTemporalValues.size(), dcTermsCreatedValues.size(),
                dcTermsSpatialValues.size());
    }*/
}
