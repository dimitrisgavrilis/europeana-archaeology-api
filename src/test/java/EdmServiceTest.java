
import gr.dcu.europeana.arch.service.EDMService;
import gr.dcu.europeana.arch.service.ExcelService;
import gr.dcu.europeana.arch.service.edm.EdmExtractUtils;
import gr.dcu.europeana.arch.service.edm.EdmFileTermExtractionResult;
import gr.dcu.europeana.arch.service.edm.ElementExtractionData;
import gr.dcu.europeana.arch.service.edm.ElementExtractionDataCategories;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Vangelis Nomikos
 */
@Slf4j
public class EdmServiceTest {
    
    public static void main(String[] args) {
        
        String STEFAN_SAMPLE_DIR  = "/home/vangelis/tests/edm-data/stefan/sample";
        String STEFAN_ARCHIVE_DIR = "/home/vangelis/tests/edm-data/stefan/19-10-15_17:04:17";
        
        String CARARE_PACKAGE_5083 = "/home/vangelis/tests/edm-data/more/carare_a/5083_EDM";
        String CARARE_PACKAGE_5149 = "/home/vangelis/tests/edm-data/more/carare_a/5149_EDM";
        
        String EXTRACT_DIR = "/home/vangelis/tests/edm-data/";
        
        String ARCHIVE_DIR = CARARE_PACKAGE_5149;
        // String ARCHIVE_DIR = CARARE_PACKAGE_5149;
        boolean skipEmptyValues = true;
        
        try {
            // Extract terms
            Path stefanSamplePath = Paths.get(ARCHIVE_DIR);
            List<EdmFileTermExtractionResult> extractionResult = 
                    EDMService.extractTerms(stefanSamplePath, true, true, true, skipEmptyValues);

            LocalDateTime now = LocalDateTime.now();
            
            // Save all terms on excel
            Path extarctPath = Paths.get(EXTRACT_DIR, now + "_extract_all.xlsx");
            ExcelService.exportExtractedAllTerms(extarctPath, extractionResult);
            
            // Create seperate excels(thematic, spatial, temporal)
            ElementExtractionDataCategories extractionCategories = 
                    EdmExtractUtils.splitExtractionDataInCategories(extractionResult);
            
            // Save extracted thematic terms on excel
            if(!extractionCategories.getThematicElementValues().isEmpty()) {
                Path extractThematicPath = Paths.get(EXTRACT_DIR, now + "_extract_thematic.xlsx");
                ExcelService.exportExtractedThematicTerms(extractThematicPath, extractionCategories.getThematicElementValues());
            } else {
                log.warn("No thematic terms to extract.");
            }
            
            if(!extractionCategories.getSpatialElementValues().isEmpty()) {
                Path extractSpatialPath = Paths.get(EXTRACT_DIR, now + "_extract_spatial.xlsx");
                ExcelService.exportExtractedSpatialTerms(extractSpatialPath, extractionCategories.getSpatialElementValues());
            } else {
                log.warn("No spatial terms to extract.");
            }
            
            if(!extractionCategories.getTemporalElementValues().isEmpty()) {
                Path extractTemporalPath = Paths.get(EXTRACT_DIR, now + "_extract_temporal.xlsx");
                ExcelService.exportExtractedTemporalTerms(extractTemporalPath, extractionCategories.getTemporalElementValues());
            } else {
                log.warn("No temporal terms to extract.");
            }
            
        } catch (IOException ex) {
            log.error("{}", ex);
        }
        
    }
    
    
}
