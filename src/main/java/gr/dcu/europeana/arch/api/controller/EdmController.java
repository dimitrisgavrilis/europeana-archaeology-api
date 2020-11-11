package gr.dcu.europeana.arch.api.controller;

import gr.dcu.europeana.arch.api.dto.AppendTermsResult;
import gr.dcu.europeana.arch.api.dto.EdmArchiveWithJobs;
import gr.dcu.europeana.arch.api.dto.EnrichDetails;
import gr.dcu.europeana.arch.api.dto.ExtractTermResult;
import gr.dcu.europeana.arch.exception.BadRequestException;
import gr.dcu.europeana.arch.domain.entity.EdmArchiveEntity;
import gr.dcu.europeana.arch.domain.entity.MappingEntity;
import gr.dcu.europeana.arch.domain.MappingType;
import gr.dcu.europeana.arch.service.AuthService;
import gr.dcu.europeana.arch.service.EDMService;
import gr.dcu.europeana.arch.service.MappingService;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@CrossOrigin
@RestController
public class EdmController {
    
    private final AuthService authService;
    private final EDMService edmService;
    private final MappingService mappingService;

    public EdmController(AuthService authService, EDMService edmService, MappingService mappingService) {
        this.authService = authService;
        this.edmService = edmService;
        this.mappingService = mappingService;
    }

    @Operation(summary = "Get all edm archives")
    @GetMapping("/edm_archives")
    public List<EdmArchiveEntity> getEdmArchives(HttpServletRequest requestContext) {
        
        int userId = authService.authorize(requestContext);
         
        return edmService.getEdmArchives(userId);
    }

    @Operation(summary = "Get a specific edm archive")
    @GetMapping("/edm_archives/{id}")
    public EdmArchiveEntity getEdmArchive(HttpServletRequest requestContext, @PathVariable Long id) {
        
        int userId = authService.authorize(requestContext);
         
        return edmService.getEdmArchive(id);
    }

    @Operation(summary = "Upload an edm archive")
    @PostMapping("/edm_archives/upload")
    public EdmArchiveEntity uploadEdmArchive(HttpServletRequest requestContext,
                                             @RequestParam("file") MultipartFile file) throws IOException {
        
        int userId = authService.authorize(requestContext);
        
        return edmService.uploadEdmArchiveAndExtractFiles(file, userId);
        
    }

    @Operation(summary = "Download an EDM / eEDM archive (original or enriched")
    @PostMapping("/edm_archives/{id}/download")
    public ResponseEntity<?> downloadEdmArchive(HttpServletRequest requestContext, 
            @PathVariable Long id, @RequestParam (required = false) String type) throws IOException {
        
        File file = edmService.loadEdmArchive(id, type);
        
        String filename = file.getName();
        // String filepath = file.getAbsolutePath();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/force-download")
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + filename + "\"")
                .body(new FileSystemResource(file));
    }

    @Operation(summary = "Extract and save terms from an EDM archive")
    @PostMapping("/edm_archives/{id}/extract_terms")
    public ExtractTermResult extractTermsFromEdmArchive(
            HttpServletRequest requestContext, @PathVariable Long id) {
        
        int userId = authService.authorize(requestContext);
        return edmService.extractAndSaveTermsFromEdmArchive(id, userId);
    }

    @Operation(summary = "Get status of EDM archive")
    @GetMapping("/edm_archives/{id}/status")
    public EdmArchiveWithJobs getEdmArchiveStatus(
            HttpServletRequest requestContext, @PathVariable Long id) {

        int userId = authService.authorize(requestContext);
        return edmService.getEdmArciveStatus(id, userId);
    }

    @Operation(summary = "Load terms from an EDM archive (saved or extracted)")
    @GetMapping("/edm_archives/{id}/terms")
    public ExtractTermResult loadTermsFromEdmArchive(
            HttpServletRequest requestContext, @PathVariable Long id) {
        
        int userId = authService.authorize(requestContext);
        
        return edmService.loadTerms(id, userId);
    }

    @Operation(summary = "Delete an EDM archive and it's data")
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

    @Operation(summary = "Create a mapping from extracted terms")
    @PostMapping("/edm_archives/{id}/mappings")
    public MappingEntity createMapping(HttpServletRequest requestContext,
                                       @PathVariable Long id, @RequestParam String type) {
        
        int userId = authService.authorize(requestContext);    
        
        // Validate mapping type
        if(!type.equalsIgnoreCase(MappingType.MAPPING_TYPE_SUBJECT) 
                && !type.equalsIgnoreCase(MappingType.MAPPING_TYPE_SPATIAL) 
                && !type.equalsIgnoreCase(MappingType.MAPPING_TYPE_TEMPORAL)) {
            throw new BadRequestException("Invalid mapping type.");
        }
        
        return mappingService.createMappingByArchiveId(id, type, userId);
        
    }

    @Operation(summary = "Append terms to an existing mapping")
    @PostMapping("/edm_archives/{id}/mappings/{mappingId}")
    public AppendTermsResult appendToMapping(HttpServletRequest requestContext, 
            @PathVariable Long id, @PathVariable Long mappingId) {
        
        int userId = authService.authorize(requestContext);    
        
        return mappingService.appendTermsToMappingByArchiveId(mappingId, id, userId);
    }

    @Operation(summary = "Enrich an EDM archive")
    @PostMapping("/edm_archives/{id}/enrich")
    public EnrichDetails enrichEdmArchive(
            HttpServletRequest requestContext, @PathVariable Long id) {
        
        int userId = authService.authorize(requestContext);
        return edmService.enrichArchive(id, userId);
    }
}
