package gr.dcu.share3d.mapping.api.controller;

import gr.dcu.share3d.config.FileStorageProperties;
import gr.dcu.share3d.mapping.model.MappingTerm;
import gr.dcu.share3d.exception.ResourceNotFoundException;
import gr.dcu.share3d.mapping.model.SubjectMapping;
import gr.dcu.share3d.mapping.repository.MappingTermRepository;
import gr.dcu.share3d.mapping.repository.SubjectMappingRepository;
import gr.dcu.share3d.mapping.repository.UserRepository;
import gr.dcu.share3d.mapping.service.ExcelService;
import gr.dcu.share3d.mapping.service.FileStorageService;
import gr.dcu.share3d.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
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
import org.springframework.web.bind.annotation.RequestHeader;
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
    UserRepository userRepository;
    
    @Autowired
    SubjectMappingRepository subjectMappingRepository;
    
    @Autowired
    MappingTermRepository mappingTermRepository;
    
    @Autowired
    ExcelService excelService;
    
    @Autowired
    FileStorageService fileStorageService;
    
    @Autowired
    FileStorageProperties fileStorageProperties;
    
  
    @Autowired
    private ResourceLoader resourceLoader;
    
    @GetMapping("/mappings")
    public List<SubjectMapping> getAllMappings() {
        return subjectMappingRepository.findAll();
    }
    
    @GetMapping("/mappings/{id}")
    public SubjectMapping getMapping(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) { 
        
        return subjectMappingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }
    
    @PostMapping("/mappings")
    public SubjectMapping saveMapping(@RequestBody SubjectMapping mapping) { 
        
        return subjectMappingRepository.save(mapping);
    }
    
    @PutMapping("/mappings/{id}")
    public SubjectMapping updateMapping(@PathVariable Long id, @RequestBody SubjectMapping mapping) { 
        
        SubjectMapping existingMapping = subjectMappingRepository.findById(Long.MIN_VALUE)
                .orElseThrow(() -> new ResourceNotFoundException(id));
         
        mapping.setId(id);
        
        return subjectMappingRepository.save(mapping);
    }
    
    @DeleteMapping("/mappings/{id}")
    public void deleteMapping(@PathVariable Long id) { 
        subjectMappingRepository.deleteById(id);
    }
   
    @PostMapping("/mappings/{id}/export")
    public ResponseEntity<Resource> exportMapping(@PathVariable Long id,
            HttpServletRequest request) throws IOException { 

        // Check if mapping exists
        SubjectMapping subjectMapping = subjectMappingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
        
        List<MappingTerm> termList = mappingTermRepository.findByMappingId(id);
        log.info("Load terms. Mapping: {} | #Terms: {}", id, termList.size());
        
        // Create a unique filename
        String fileName = FileUtils.createFileName("mappings.xlsx");
        
        // Export to tmp file
        excelService.exportMappingTermsToExcel(fileName, termList);
        
        // Load file
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        
        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
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
    public List<MappingTerm> getAllMappingTerms(@PathVariable Long id) {
        return mappingTermRepository.findByMappingId(id);
    }
    
    @GetMapping("/mappings/{id}/terms/{termId}")
    public MappingTerm getMappingTerm(@PathVariable Long id, @PathVariable Long termId) { 
        return mappingTermRepository.findById(termId)
                .orElseThrow(() -> new ResourceNotFoundException(termId));
    }
    
    @PutMapping("/mappings/{id}/terms/{termId}")
    public MappingTerm updateMappingTerm(@PathVariable Long id, @PathVariable Long termId, 
            @RequestBody MappingTerm mapping) { 
        
        MappingTerm existingTerm = mappingTermRepository.findById(termId)
                .orElseThrow(() -> new ResourceNotFoundException(termId));

        mapping.setId(termId);
        mapping.setMappingId(id);
        
        return mappingTermRepository.save(mapping);
    }
    
    @PostMapping("/mappings/{id}/terms")
    public MappingTerm saveMappingTerm(@PathVariable Long id, @RequestBody MappingTerm mapping) { 
        
        mapping.setMappingId(id);
        return mappingTermRepository.save(mapping);
    }
    
    /*
    @PostMapping("/mappings/{id}/terms")
    public List<MappingTerm> saveMappingTerm(@RequestBody List<MappingTerm> mappings) { 
        
        return subjectMappingRepository.saveAll(mappings);
    }*/
    
    @DeleteMapping("/mappings/{id}/terms/{termId}")
    public void deleteMappingTerm(@PathVariable Long id, @PathVariable Long termId) { 
        mappingTermRepository.deleteById(termId);
    }
    
    /**
     * Delete all terms of a mapping.
     * @param id
     * @param termId 
     */
    @DeleteMapping("/mappings/{id}/terms")
    public void deleteTerms(@PathVariable Long id) { 
        mappingTermRepository.deleteByMappingId(id);
    } 
    
    
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
    }
    
    
    
    @PostMapping("/mappings/{id}/upload")
    public List<MappingTerm> uploadMapping(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        
        // Check existemce of mapping
        SubjectMapping mapping = subjectMappingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
        
        // Store mapping file
        
        Path filePath = fileStorageService.store(file);
        
        List<MappingTerm> termList = 
                excelService.loadMappingTermsFromExcel(filePath.toString(), id, 1, -1);
        
        // Save terms
        log.info("Saving terms. #Terms:{}", termList.size());
        
        mappingTermRepository.saveAll(termList);
        
        return termList;
    }
}
