package gr.dcu.europeana.arch.api.controller;

import gr.dcu.europeana.arch.api.resource.EnrichDetails;
import gr.dcu.europeana.arch.model.MappingTerm;
import gr.dcu.europeana.arch.exception.ResourceNotFoundException;
import gr.dcu.europeana.arch.model.SubjectMapping;
import gr.dcu.europeana.arch.repository.MappingTermRepository;
import gr.dcu.europeana.arch.repository.SubjectMappingRepository;
import gr.dcu.europeana.arch.service.AuthService;
import gr.dcu.europeana.arch.service.MappingService;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Vangelis Nomikos
 */
@Slf4j
@RestController
@CrossOrigin
public class MappingController {
    
    @Autowired
    AuthService authService;
    
    @Autowired
    MappingService mappingService;
    
    @Autowired
    SubjectMappingRepository subjectMappingRepository;
    
    @Autowired
    MappingTermRepository mappingTermRepository;
    
    @GetMapping("/mappings")
    public List<SubjectMapping> getAllMappings(HttpServletRequest requestContext) {
        
        // int userId = authService.authorize(requestContext);
         
        return mappingService.findAll();
    }
    
    @GetMapping("/mappings/{id}")
    public SubjectMapping getMapping(HttpServletRequest requestContext, @PathVariable Long id) { 
        
        // int userId = authService.authorize(requestContext);
                
        return subjectMappingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }
    
    @PostMapping("/mappings")
    public SubjectMapping saveMapping(HttpServletRequest requestContext, @RequestBody SubjectMapping mapping) { 
        
        int userId = authService.authorize(requestContext);
        
        return mappingService.save(userId, mapping);
    }
    
    @PutMapping("/mappings/{id}")
    public SubjectMapping updateMapping(HttpServletRequest requestContext, @PathVariable Long id, @RequestBody SubjectMapping mapping) { 
        
        SubjectMapping existingMapping = subjectMappingRepository.findById(Long.MIN_VALUE)
                .orElseThrow(() -> new ResourceNotFoundException(id));
         
        mapping.setId(id);
        
        return subjectMappingRepository.save(mapping);
    }
    
    @DeleteMapping("/mappings/{id}")
    public void deleteMapping(HttpServletRequest requestContext, @PathVariable Long id) { 
        
        int userId = authService.authorize(requestContext);
        
        mappingService.delete(userId, id);
    }
   
    /**
     * 
     * @param requestContext
     * @param id
     * @return
     * @throws IOException 
     */
    @PostMapping("/mappings/{id}/export")
    public ResponseEntity<Resource> exportMapping(HttpServletRequest requestContext, @PathVariable Long id) throws IOException { 

        int userId = authService.authorize(requestContext);
        
        Resource resource = mappingService.exportTerms(id, userId);
        
        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = requestContext.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                // .contentLength(file.length())
                .body(resource);

    }
   
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~ TERMS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ //
    
    @GetMapping("/mappings/{id}/terms")
    public List<MappingTerm> getAllMappingTerms(HttpServletRequest requestContext, @PathVariable Long id) {
        return mappingTermRepository.findByMappingId(id);
    }
    
    @GetMapping("/mappings/{id}/terms/{termId}")
    public MappingTerm getMappingTerm(HttpServletRequest requestContext, @PathVariable Long id, @PathVariable Long termId) { 
        return mappingTermRepository.findById(termId)
                .orElseThrow(() -> new ResourceNotFoundException(termId));
    }
    
    @PutMapping("/mappings/{id}/terms/{termId}")
    public MappingTerm updateMappingTerm(HttpServletRequest requestContext, @PathVariable Long id, @PathVariable Long termId, 
            @RequestBody MappingTerm mapping) { 
        
        MappingTerm existingTerm = mappingTermRepository.findById(termId)
                .orElseThrow(() -> new ResourceNotFoundException(termId));

        mapping.setId(termId);
        mapping.setMappingId(id);
        mapping.setLanguage(mapping.getLanguage());
        
        return mappingTermRepository.save(mapping);
    }
    
    @PostMapping("/mappings/{id}/terms")
    public MappingTerm saveMappingTerm(HttpServletRequest requestContext, 
            @PathVariable Long id, @RequestBody MappingTerm mapping) { 
        
        int userId = authService.authorize(requestContext);
        
        mapping.setMappingId(id);
        return mappingTermRepository.save(mapping);
    }
    
    /*
    @PostMapping("/mappings/{id}/terms")
    public List<MappingTerm> saveMappingTerm(@RequestBody List<MappingTerm> mappings) { 
        
        return subjectMappingRepository.saveAll(mappings);
    }*/
    
    @DeleteMapping("/mappings/{id}/terms/{termId}")
    public void deleteMappingTerm(HttpServletRequest requestContext, @PathVariable Long id, @PathVariable Long termId) { 
        
        int userId = authService.authorize(requestContext);
        
        mappingTermRepository.deleteById(termId);
    }
    
    /**
     * Delete all terms of a mapping.
     * @param id
     * @param termId 
     */
    @DeleteMapping("/mappings/{id}/terms")
    public void deleteTerms(HttpServletRequest requestContext, @PathVariable Long id) { 
        
        int userId = authService.authorize(requestContext);
        
        mappingTermRepository.deleteByMappingId(id);
    } 
    
    
    /**
     * 
     * @param id
     * @param file
     * @return
     * @throws IOException 
     */
    @PostMapping("/mappings/{id}/upload")
    public List<MappingTerm> uploadTerms(HttpServletRequest requestContext, @PathVariable Long id, 
            @RequestParam("file") MultipartFile file) throws IOException {
        
        int userId = authService.authorize(requestContext);
        
        return mappingService.uploadTerms(id, file, userId);
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
                
                mappingTermRepository.saveAll(mappingTerms);
                
                log.info("Parsing finished...");
            }
            
        } catch(IOException ex) {
            log.error("File not found.");
        }
        
        return "OK";
    } */
}
