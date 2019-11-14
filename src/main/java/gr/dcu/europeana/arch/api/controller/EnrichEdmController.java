package gr.dcu.europeana.arch.api.controller;

import gr.dcu.europeana.arch.api.resource.EnrichDetails;
import gr.dcu.europeana.arch.model.EdmArchive;
import gr.dcu.europeana.arch.model.MappingUploadRequest;
import gr.dcu.europeana.arch.service.AuthService;
import gr.dcu.europeana.arch.service.EDMService;
import gr.dcu.europeana.arch.service.MappingService;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Vangelis Nomikos
 */
@CrossOrigin
@RestController
public class EnrichEdmController {
    
    @Autowired
    AuthService authService;
    
    @Autowired
    EDMService edmService;
    
    @Autowired
    MappingService mappingService;
    
    @GetMapping("/edm_packages")
    public List<MappingUploadRequest> getEdmPackages(HttpServletRequest requestContext) {
        
        int userId = authService.authorize(requestContext);
         
        return edmService.getEdmPackages(userId);
    }
    
    @PostMapping("/edm_packages/{id}/enrich")
    public EnrichDetails enrichEdmPackage(HttpServletRequest requestContext, @PathVariable Long id, 
            @RequestParam("file") MultipartFile file) throws IOException {
        
        int userId = authService.authorize(requestContext);
        
        return mappingService.enrich(id, file, userId);

    }
    
    @GetMapping("/edm_packages/{id}/enrich/{requestId}/download")
    public ResponseEntity<?> downloadEnrichedEdmUpload(HttpServletRequest requestContext, @PathVariable Long id, 
            @PathVariable Long requestId) throws IOException {
        
        File file = mappingService.loadEnrichedArchive(requestId);
        
        String filename = file.getName();
        // String filepath = file.getAbsolutePath();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/force-download")
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + filename + "\"")
                .body(new FileSystemResource(file));
    }
    
    
    
    
    
    /**
     * Upload an EDM package and enrich the EDM files based on subject mapping.
     * @param id
     * @param file
     * @return
     * @throws IOException 
     */
    @PostMapping("/mappings/{id}/enrich")
    public EnrichDetails enrichEdmArchive(HttpServletRequest requestContext, @PathVariable Long id, 
            @RequestParam("file") MultipartFile file) throws IOException {
        
        int userId = authService.authorize(requestContext);
        
        return mappingService.enrich(id, file, userId);

    }
    
    
    /**
     * Download enriched archive
     * @param requestContext
     * @param id
     * @param requestId
     * @return
     * @throws IOException 
     */
    @GetMapping("/mappings/{id}/enrich/{requestId}/download")
    public ResponseEntity<?> downloadEnrichedEdmArchive(HttpServletRequest requestContext, @PathVariable Long id, 
            @PathVariable Long requestId) throws IOException {
        
        File file = mappingService.loadEnrichedArchive(requestId);
        
        String filename = file.getName();
        // String filepath = file.getAbsolutePath();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/force-download")
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + filename + "\"")
                .body(new FileSystemResource(file));
    }
    
    /*
    @GetMapping("/upload/test")
    public String testUpload() {
        
        log.info("trying to upload...");
        try {
            String filename = "Subject_Mapping_Template.xlsx";
            Resource resource = resourceLoader.getResource("classpath:" + filename);
            File templateFile = resource.getFile();
            if(templateFile.exists()) {
                log.info("File exists!!!");
                
                List<MappingTerm> mappingTerms = 
                        excelService.loadMappingTermsFromExcel(filename, 2, 1, -1);
                
                subjectTermRepository.saveAll(mappingTerms);
                
                log.info("Parsing finished...");
            }
            
        } catch(IOException ex) {
            log.error("File not found.");
        }
        
        return "OK";
    } */
    
}
