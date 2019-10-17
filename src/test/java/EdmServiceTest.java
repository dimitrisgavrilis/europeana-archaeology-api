
import gr.dcu.europeana.arch.service.EDMService;
import gr.dcu.europeana.arch.service.ExcelService;
import gr.dcu.europeana.arch.service.edm.EdmFileTermExtractionResult;
import gr.dcu.europeana.arch.service.edm.ElementExtractionData;
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
        
        String EXTRACT_DIR = "/home/vangelis/tests/edm-data/";
        
        try {
            // Extract terms
            Path stefanSamplePath = Paths.get(STEFAN_ARCHIVE_DIR);
            List<EdmFileTermExtractionResult> extractionResult = 
                    EDMService.extractTerms(stefanSamplePath, true, true, true);

            // Save terms on excel
            Path extarctPath = Paths.get(EXTRACT_DIR, "extract_" + LocalDateTime.now() + ".xlsx");
            ExcelService.exportTerms(extarctPath, extractionResult);
        } catch (IOException ex) {
            log.error("{}", ex);
        }
        
    }
    
    
}
