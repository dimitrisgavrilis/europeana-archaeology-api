package gr.dcu.europeana.arch.api.controller;

import gr.dcu.europeana.arch.exception.ResourceNotFoundException;
import gr.dcu.europeana.arch.model.SpatialTerm;
import gr.dcu.europeana.arch.model.SubjectTerm;
import gr.dcu.europeana.arch.model.TemporalTerm;
import gr.dcu.europeana.arch.repository.SpatialTermRepository;
import gr.dcu.europeana.arch.repository.SubjectMappingRepository;
import gr.dcu.europeana.arch.repository.SubjectTermRepository;
import gr.dcu.europeana.arch.repository.TemporalTermRepository;
import gr.dcu.europeana.arch.service.AuthService;
import gr.dcu.europeana.arch.service.MappingService;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Vangelis Nomikos
 */
@CrossOrigin
@RestController
public class TermController {
    
    @Autowired
    AuthService authService;
    
    // @Autowired
    // MappingService mappingService;
    
    //@Autowired
    //SubjectMappingRepository subjectMappingRepository;
    
    @Autowired
    SubjectTermRepository subjectTermRepository;
    
    @Autowired
    SpatialTermRepository spatialTermRepository;
    
    @Autowired
    TemporalTermRepository temporalTermRepository;
    
     // ~~~~~~~~~~~~~~~~~~~~~~~~~~ Subject Terms ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ //
    
    @GetMapping("/mappings/{id}/terms")
    public List<SubjectTerm> getAllMappingTerms(HttpServletRequest requestContext, @PathVariable Long id) {
        return subjectTermRepository.findByMappingId(id);
    }
    
    @GetMapping("/mappings/{id}/terms/{termId}")
    public SubjectTerm getMappingTerm(HttpServletRequest requestContext, 
            @PathVariable Long id, @PathVariable Long termId) { 
        return subjectTermRepository.findById(termId)
                .orElseThrow(() -> new ResourceNotFoundException(termId));
    }
    
    @PutMapping("/mappings/{id}/terms/{termId}")
    public SubjectTerm updateMappingTerm(HttpServletRequest requestContext, 
            @PathVariable Long id, @PathVariable Long termId, @RequestBody SubjectTerm term) { 
        
        SubjectTerm existingTerm = subjectTermRepository.findById(termId)
                .orElseThrow(() -> new ResourceNotFoundException(termId));

        term.setId(termId);
        term.setMappingId(id);
        term.setLanguage(term.getLanguage());
        
        return subjectTermRepository.save(term);
    }
    
    @PostMapping("/mappings/{id}/terms")
    public SubjectTerm saveMappingTerm(HttpServletRequest requestContext, 
            @PathVariable Long id, @RequestBody SubjectTerm term) { 
        
        int userId = authService.authorize(requestContext);
        
        term.setMappingId(id);
        return subjectTermRepository.save(term);
    }
    
    /*
    @PostMapping("/mappings/{id}/terms")
    public List<MappingTerm> saveMappingTerm(@RequestBody List<MappingTerm> mappings) { 
        
        return subjectMappingRepository.saveAll(mappings);
    }*/
    
    @DeleteMapping("/mappings/{id}/terms/{termId}")
    public void deleteMappingTerm(HttpServletRequest requestContext, 
            @PathVariable Long id, @PathVariable Long termId) { 
        
        int userId = authService.authorize(requestContext);
        
        subjectTermRepository.deleteById(termId);
    }
    
    /**
     * Delete all subject terms of a mapping.
     * @param requestContext
     * @param id 
     */
    @DeleteMapping("/mappings/{id}/terms")
    public void deleteAllTerms(HttpServletRequest requestContext, @PathVariable Long id) { 
        
        int userId = authService.authorize(requestContext);
        
        subjectTermRepository.deleteByMappingId(id);
    }
    
    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~ Spatial Terms ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ //
    @GetMapping("/mappings/{id}/spatial_terms")
    public List<SpatialTerm> getAllSpatialTerms(HttpServletRequest requestContext, @PathVariable Long id) {
        return spatialTermRepository.findByMappingId(id);
    }
    
    @PutMapping("/mappings/{id}/spatial_terms/{termId}")
    public SpatialTerm updateSpatialTerm(HttpServletRequest requestContext, 
            @PathVariable Long id, @PathVariable Long termId, 
            @RequestBody SpatialTerm term) { 
        
        SpatialTerm existingTerm = spatialTermRepository.findById(termId)
                .orElseThrow(() -> new ResourceNotFoundException(termId));

        term.setId(termId);
        term.setMappingId(id);
        term.setLanguage(term.getLanguage());
        
        return spatialTermRepository.save(term);
    }
    
    @PostMapping("/mappings/{id}/spatial_terms")
    public SpatialTerm saveSpatialTerm(HttpServletRequest requestContext, 
            @PathVariable Long id, @RequestBody SpatialTerm term) { 
        
        int userId = authService.authorize(requestContext);
        
        term.setMappingId(id);
        return spatialTermRepository.save(term);
    }
    
    @DeleteMapping("/mappings/{id}/spatial_terms/{termId}")
    public void deleteSpatialTerm(HttpServletRequest requestContext, 
            @PathVariable Long id, @PathVariable Long termId) { 
        
        int userId = authService.authorize(requestContext);
        
        spatialTermRepository.deleteById(termId);
    }
    
    /**
     * Delete all spatial terms of a mapping.
     * @param requestContext
     * @param id 
     */
    @DeleteMapping("/mappings/{id}/spatial_terms")
    public void deleteAllSpatialTerms(HttpServletRequest requestContext, @PathVariable Long id) { 
        
        int userId = authService.authorize(requestContext);
        
        spatialTermRepository.deleteByMappingId(id);
    }
   
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~ Temporal Terms ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ //
    @GetMapping("/mappings/{id}/temporal_terms")
    public List<TemporalTerm> getAllTemporalTerms(HttpServletRequest requestContext, @PathVariable Long id) {
        return temporalTermRepository.findByMappingId(id);
    }
    
    @PutMapping("/mappings/{id}/temporal_terms/{termId}")
    public TemporalTerm updateTemporalTerm(HttpServletRequest requestContext, 
            @PathVariable Long id, @PathVariable Long termId, 
            @RequestBody TemporalTerm term) { 
        
        TemporalTerm existingTerm = temporalTermRepository.findById(termId)
                .orElseThrow(() -> new ResourceNotFoundException(termId));

        term.setId(termId);
        term.setMappingId(id);
        term.setLanguage(term.getLanguage());
        
        return temporalTermRepository.save(term);
    }
    
    @PostMapping("/mappings/{id}/temporal_terms")
    public TemporalTerm saveTemporalTerm(HttpServletRequest requestContext, 
            @PathVariable Long id, @RequestBody TemporalTerm term) { 
        
        int userId = authService.authorize(requestContext);
        
        term.setMappingId(id);
        return temporalTermRepository.save(term);
    }
    
    @DeleteMapping("/mappings/{id}/temporal_terms/{termId}")
    public void deleteTemporalTerm(HttpServletRequest requestContext, 
            @PathVariable Long id, @PathVariable Long termId) { 
        
        int userId = authService.authorize(requestContext);
        
        temporalTermRepository.deleteById(termId);
    }
    
    /**
     * Delete all temporal terms of a mapping.
     * @param requestContext
     * @param id 
     */
    @DeleteMapping("/mappings/{id}/temporal_terms")
    public void deleteAllTemporalTerms(HttpServletRequest requestContext, @PathVariable Long id) { 
        
        int userId = authService.authorize(requestContext);
        
        temporalTermRepository.deleteByMappingId(id);
    }
    
    
}
