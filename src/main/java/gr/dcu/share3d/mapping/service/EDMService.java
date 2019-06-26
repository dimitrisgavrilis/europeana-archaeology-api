package gr.dcu.share3d.mapping.service;

import gr.dcu.share3d.exception.MyFileNotFoundException;
import gr.dcu.share3d.mapping.model.MappingTerm;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vangelis Nomikos
 */
@Slf4j
@Service
public class EDMService {
    
    
    public List<MappingTerm> loadMappingTermsFromExcel(String filename, long mappingId, 
            int skipLineCount, int limitCount) {

        List<MappingTerm> mappings = new LinkedList<>();
        
        /*
        try {
            
            // Resource resource = resourceLoader.getResource("classpath:" + filename);
            // File file = resource.getFile();
            
            File file = new File(filename);
            if(file.exists() && file.isFile()) {
                log.info("URI: {}", file.getAbsolutePath());
                
                // Process archive
                
                
            } else {
                log.error("File not found " + filename);
                throw new MyFileNotFoundException("File not found " + filename);
            }
           
        } catch(IOException ex) {
            log.error("File not found " + filename);
                throw new MyFileNotFoundException("File not found " + filename);
        } */
        
        return mappings;
        
    }
    
    
}
