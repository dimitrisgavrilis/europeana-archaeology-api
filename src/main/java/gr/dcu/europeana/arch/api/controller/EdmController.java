package gr.dcu.europeana.arch.api.controller;

import gr.dcu.europeana.arch.api.resource.AppendTermsResult;
import gr.dcu.europeana.arch.api.resource.ExtractTermResult;
import gr.dcu.europeana.arch.exception.BadRequestException;
import gr.dcu.europeana.arch.model.EdmArchive;
import gr.dcu.europeana.arch.model.EdmArchiveTerms;
import gr.dcu.europeana.arch.model.Mapping;
import gr.dcu.europeana.arch.model.MappingType;
import gr.dcu.europeana.arch.model.SubjectTerm;
import gr.dcu.europeana.arch.model.mappers.SpatialTermMapper;
import gr.dcu.europeana.arch.model.mappers.SubjectTermMapper;
import gr.dcu.europeana.arch.model.mappers.TemporalTermMapper;
import gr.dcu.europeana.arch.service.AuthService;
import gr.dcu.europeana.arch.service.EDMService;
import gr.dcu.europeana.arch.service.MappingService;
import gr.dcu.europeana.arch.service.edm.EdmExtractUtils;
import gr.dcu.europeana.arch.service.edm.EdmFileTermExtractionResult;
import gr.dcu.europeana.arch.service.edm.ElementExtractionData;
import gr.dcu.europeana.arch.service.edm.ElementExtractionDataCategories;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Vangelis Nomikos
 */
@Slf4j
@CrossOrigin
@RestController
public class EdmController {
    
    @Autowired
    AuthService authService;
    
    @Autowired
    EDMService edmService;
    
    @Autowired
    MappingService mappingService;
    
    @Autowired
    SubjectTermMapper subjectTermMapper;
    
    @Autowired
    SpatialTermMapper spatialTermMapper;
    
    @Autowired
    TemporalTermMapper temporalTermMapper;
    
    
    @GetMapping("/edm_archives")
    public List<EdmArchive> getEdmArchives(HttpServletRequest requestContext) {
        
        int userId = authService.authorize(requestContext);
         
        return edmService.getEdmArchives(userId);
    }
    
    @GetMapping("/edm_archives/{id}")
    public EdmArchive getEdmArchive(HttpServletRequest requestContext,  @PathVariable Long id) {
        
        int userId = authService.authorize(requestContext);
         
        return edmService.getEdmArchive(id);
    }
    
    
    @PostMapping("/edm_archives/upload")
    public EdmArchive uploadEdmArchive(HttpServletRequest requestContext,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        int userId = authService.authorize(requestContext);
        
        return edmService.uploadEdmArchive(file, userId);
        
    }
    
    @PostMapping("/edm_archives/{id}/download")
    public ResponseEntity<?> downloadEdmArchive(HttpServletRequest requestContext, 
            @PathVariable Long id) throws IOException {
        
        File file = edmService.loadEdmArchive(id);
        
        String filename = file.getName();
        // String filepath = file.getAbsolutePath();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/force-download")
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + filename + "\"")
                .body(new FileSystemResource(file));
    }
    
    @PostMapping("/edm_archives/{id}/extract_terms")
    public ExtractTermResult extractTermsFromEdmArchive(
            HttpServletRequest requestContext, @PathVariable Long id) {
        
        int userId = authService.authorize(requestContext);
        
        ExtractTermResult extractTermResult = new ExtractTermResult();
        
        List<EdmFileTermExtractionResult> extractionResult = edmService.extractTermsFromEdmArcive(id);
        
        // Create seperate categories(thematic, spatial, temporal)
        ElementExtractionDataCategories extractionCategories = 
                EdmExtractUtils.splitExtractionDataInCategories(extractionResult);
        
        // Get thematic terms
        Set<ElementExtractionData> thematicElementValues = extractionCategories.getThematicElementValues();
        if(!thematicElementValues.isEmpty()) {
            extractTermResult.setSubjectTerms(subjectTermMapper.toSubjectTermList(thematicElementValues));
        } else {
            log.warn("No thematic terms to extract.");
        }
        
        Set<ElementExtractionData> spatialElementValues = extractionCategories.getSpatialElementValues();
        if(!spatialElementValues.isEmpty()) {
            extractTermResult.setSpatialTerms(spatialTermMapper.toSpatialTermList(spatialElementValues));
        } else {
            log.warn("No spatial terms to extract.");
        }

        Set<ElementExtractionData> temporalElementValues = extractionCategories.getTemporalElementValues();
        if(!temporalElementValues.isEmpty()) {
            extractTermResult.setTemporalTerms(temporalTermMapper.toTemporalTermList(temporalElementValues));
        } else {
            log.warn("No temporal terms to extract.");
        }
        
        edmService.saveTerms(id, extractTermResult, userId);
        
        return extractTermResult;
        // return edmService.extractTermsFromEdmArcive(id);
        
        // return new ResponseEntity<>("", HttpStatus.OK);
    }
   
    @DeleteMapping("/edm_archives/{id}")
    public void deleteArchive(HttpServletRequest requestContext, @PathVariable Long id) {
        
        int userId = authService.authorize(requestContext);
        
        edmService.deleteEdmArchive(id);
        
    }
    
    /*
    @PostMapping("/edm_archives/{id}/terms")
    public EdmArchiveTerms saveTerms(HttpServletRequest requestContext, 
            @PathVariable Long id, @RequestBody ExtractTermResult extractTermResult) {
        
        int userId = authService.authorize(requestContext);
        
        return edmService.saveTerms(id, extractTermResult, userId);
        
    }*/
   
    @PostMapping("/edm_archives/{id}/mappings")
    public Mapping createMapping(HttpServletRequest requestContext, 
            @PathVariable Long id, @RequestParam (required = true) String type) {
        
        int userId = authService.authorize(requestContext);    
        
        // Validate mapping type
        if(!type.equalsIgnoreCase(MappingType.MAPPING_TYPE_SUBJECT) 
                && !type.equalsIgnoreCase(MappingType.MAPPING_TYPE_SPATIAL) 
                && !type.equalsIgnoreCase(MappingType.MAPPING_TYPE_TEMPORAL)) {
            throw new BadRequestException("Invalid mapping type.");
        }
        
        return mappingService.createMappingByArchiveId(id, type, userId);
        
    }
    
    @PostMapping("/edm_archives/{id}/mappings/{mappingId}")
    public AppendTermsResult appendToMapping(HttpServletRequest requestContext, 
            @PathVariable Long id, @PathVariable Long mappingId) {
        
        int userId = authService.authorize(requestContext);    
        
        return mappingService.appendTermsToMappingByArchiveId(mappingId, id, userId);
    }
    
    
    @PostMapping("/edm_archives/{id}/enrich")
    public void enrichEdmArchive(
            HttpServletRequest requestContext, @PathVariable Long id) {
        
        int userId = authService.authorize(requestContext);
        
        log.info("Unimplemented ");
    }
    
}
