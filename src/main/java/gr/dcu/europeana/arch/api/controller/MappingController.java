package gr.dcu.europeana.arch.api.controller;

import gr.dcu.europeana.arch.model.SubjectTerm;
import gr.dcu.europeana.arch.exception.ResourceNotFoundException;
import gr.dcu.europeana.arch.model.SpatialTerm;
import gr.dcu.europeana.arch.model.Mapping;
import gr.dcu.europeana.arch.repository.SpatialTermRepository;
import gr.dcu.europeana.arch.service.AuthService;
import gr.dcu.europeana.arch.service.MappingService;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import gr.dcu.europeana.arch.repository.SubjectTermRepository;
import gr.dcu.europeana.arch.repository.TemporalTermRepository;
import gr.dcu.europeana.arch.repository.MappingRepository;

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
    MappingRepository mappingRepository;
    
    @Autowired
    SubjectTermRepository subjectTermRepository;
    
    @Autowired
    SpatialTermRepository spatialTermRepository;
    
    @Autowired
    TemporalTermRepository temporalTermRepository;
    
    
    @GetMapping("/mappings/all")
    public List<Mapping> getAllMappings(HttpServletRequest requestContext) {
        
        // int userId = authService.authorize(requestContext);
         
        return mappingService.findAll();
    }
    
    @GetMapping("/mappings")
    public List<Mapping> getUserMappings(HttpServletRequest requestContext) {
        
        int userId = authService.authorize(requestContext);
         
        return mappingService.findAllByUserId(userId);
    }
    
    @GetMapping("/mappings/{id}")
    public Mapping getMapping(HttpServletRequest requestContext, @PathVariable Long id) { 
        
        // int userId = authService.authorize(requestContext);
                
        return mappingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }
    
    @PostMapping("/mappings")
    public Mapping saveMapping(HttpServletRequest requestContext, @RequestBody Mapping mapping) { 
        
        int userId = authService.authorize(requestContext);
        
        return mappingService.save(userId, mapping);
    }
    
    @PutMapping("/mappings/{id}")
    public Mapping updateMapping(HttpServletRequest requestContext, 
            @PathVariable Long id, @RequestBody Mapping mapping) { 
        
        Mapping existingMapping = mappingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
        
        existingMapping.setLabel(mapping.getLabel());
        existingMapping.setDescription(mapping.getDescription());
        existingMapping.setLanguage(mapping.getLanguage());
        existingMapping.setProviderName(mapping.getProviderName());
        existingMapping.setVocabularyName(mapping.getVocabularyName());
        
        return mappingRepository.save(mapping);
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
   
    
    /**
     * 
     * @param id
     * @param file
     * @return
     * @throws IOException 
     */
    @PostMapping("/mappings/{id}/upload")
    public List<SubjectTerm> uploadTerms(HttpServletRequest requestContext, @PathVariable Long id, 
            @RequestParam("file") MultipartFile file) throws IOException {
        
        int userId = authService.authorize(requestContext);
        
        return mappingService.uploadSubjectTerms(id, file, userId);
    }
    
    @PostMapping("/mappings/{id}/upload_spatial")
    public List<SpatialTerm> uploadSpatialTerms(HttpServletRequest requestContext, @PathVariable Long id, 
            @RequestParam("file") MultipartFile file) throws IOException {
        
        int userId = authService.authorize(requestContext);
        
        return mappingService.uploadSpatialTerms(id, file, userId);
    }
    
    @PostMapping("/mappings/{id}/upload_temporal")
    public List<SpatialTerm> uploadTemporalTerms(HttpServletRequest requestContext, @PathVariable Long id, 
            @RequestParam("file") MultipartFile file) throws IOException {
        
        int userId = authService.authorize(requestContext);
        
        return mappingService.uploadSpatialTerms(id, file, userId);
    }
}
