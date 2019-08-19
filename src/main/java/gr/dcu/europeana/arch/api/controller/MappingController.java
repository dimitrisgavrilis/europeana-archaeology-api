package gr.dcu.europeana.arch.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.dcu.europeana.arch.api.resource.EnrichDetails;
import gr.dcu.europeana.arch.config.FileStorageProperties;
import gr.dcu.europeana.arch.model.MappingTerm;
import gr.dcu.europeana.arch.exception.ResourceNotFoundException;
import gr.dcu.europeana.arch.model.EnrichRequest;
import gr.dcu.europeana.arch.model.ExportRequest;
import gr.dcu.europeana.arch.model.SubjectMapping;
import gr.dcu.europeana.arch.model.UploadRequest;
import gr.dcu.europeana.arch.repository.EnrichRequestRepository;
import gr.dcu.europeana.arch.repository.ExportRequestRepository;
import gr.dcu.europeana.arch.repository.MappingTermRepository;
import gr.dcu.europeana.arch.repository.SubjectMappingRepository;
import gr.dcu.europeana.arch.repository.UploadRequestRepository;
import gr.dcu.europeana.arch.repository.UserRepository;
import gr.dcu.europeana.arch.service.ExcelService;
import gr.dcu.europeana.arch.service.FileStorageService;
import gr.dcu.utils.CompressUtils;
import gr.dcu.utils.XMLUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
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
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
    ExportRequestRepository exportRequestRepository;
    
    @Autowired
    UploadRequestRepository uploadRequestRepository;
    
    @Autowired
    EnrichRequestRepository enrichRequestRepository;
    
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
        
        // Get terms
        List<MappingTerm> termList = mappingTermRepository.findByMappingId(id);
        log.info("Load terms. Mapping: {} | #Terms: {}", id, termList.size());
        
        // Export to tmp file
        Path filePath = fileStorageService.buildExportFilePath(id);
        excelService.exportMappingTermsToExcel(filePath, termList);
        log.info("Export saved at {}", filePath);
        
        // Load export file
        Resource resource = fileStorageService.loadFileAsResource(filePath);
        
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
        
        // Create export enrichRequest
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setMappingId(id);
        exportRequest.setFilepath(filePath.toString());
        exportRequestRepository.save(exportRequest);
        
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
    
    
    /**
     * 
     * @param id
     * @param file
     * @return
     * @throws IOException 
     */
    @PostMapping("/mappings/{id}/upload")
    public List<MappingTerm> uploadMapping(@PathVariable Long id, 
            @RequestParam("file") MultipartFile file) throws IOException {
        
        // Check existemce of mapping
        SubjectMapping mapping = subjectMappingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
        
        // Upload mapping file
        Path filePath = fileStorageService.buildUploadFilePath(id, file.getOriginalFilename());
        fileStorageService.upload(filePath, file);
        
        // Load terms
        List<MappingTerm> termList = 
                excelService.loadMappingTermsFromExcel(filePath.toString(), id, 1, -1);
        
        // Save terms
        log.info("Saving terms. #Terms:{}", termList.size());
        
        // TODO: Do not save automatically. Preview first.
        mappingTermRepository.saveAll(termList);
        
        // Create upload enrichRequest
        UploadRequest uploadRequest = new UploadRequest();
        uploadRequest.setMappingId(id);
        uploadRequest.setFilename(file.getOriginalFilename());
        uploadRequest.setFilepath(filePath.toString());
        uploadRequestRepository.save(uploadRequest);
        
        return termList;
    }
    
    /**
     * Upload an EDM package and enrich the EDM files based on subject mapping.
     * @param id
     * @param file
     * @return
     * @throws IOException 
     */
    @PostMapping("/mappings/{id}/enrich")
    public EnrichDetails enrichEdmArchive(@PathVariable Long id, 
            @RequestParam("file") MultipartFile file) throws IOException {
        
        // Check existemce of mapping
        SubjectMapping mapping = subjectMappingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
        
        // Get mapping terms. Convert them to map
        List<MappingTerm> terms = mappingTermRepository.findByMappingId(id);
        Map<String,MappingTerm> termsMap = new HashMap<>();
        for(MappingTerm mp : terms) {
            termsMap.put(mp.getNativeTerm(), mp);
        }
        log.info("Mapping: {} #Terms: {}", id, terms.size());
        
        // File system hierarchy
        // storage_tmp
        // -- <mapping_id>
        //    -- enrich
        //        -- <request_id> (at this level store the archives - edm.zip , eEDM.tar.gz)
        //            -- EDM
        //            -- eEDM
        //    -- uploads
        //    -- exports
        
        // Create enrich enrichRequest
        EnrichRequest enrichRequest = new EnrichRequest();
        enrichRequest.setMappingId(id);
        enrichRequest.setFilename(file.getOriginalFilename());
        enrichRequest = enrichRequestRepository.save(enrichRequest);
        long requestId = enrichRequest.getId();
        
        // Upload EDM archive
        log.info("Upload EDM archive. RequestId: {}", requestId);
        Path edmArchiveFilePath = fileStorageService.buildUploadEdmArchiveFilePath(id, file.getOriginalFilename(), requestId);
        fileStorageService.upload(edmArchiveFilePath, file);
        log.info("EDM archive uploaded. Path: {}", edmArchiveFilePath);
        
        // Update enrich enrichRequest
        enrichRequest.setFilepath(edmArchiveFilePath.toString());
        enrichRequest = enrichRequestRepository.save(enrichRequest);
        
        // Extract EDM archive
        log.info("Extract EDM archive. RequestId: {}", requestId);
        Path edmExtractDirPath = Paths.get(edmArchiveFilePath.getParent().toString(), "EDM");
        fileStorageService.extractArchive(edmArchiveFilePath, edmExtractDirPath);
        File[] edmFiles = edmExtractDirPath.toFile().listFiles(); 
        log.info("EDM archive extracted. Path: {} #Files: {}", edmExtractDirPath, edmFiles.length);
        
        // Enrich archive
        boolean enrichedDirCreated = false;
        Path enrichedDirPath = null;
        int enrichedFileCount = 0;
        for(File edmFile : edmFiles) {
            if(edmFile.exists() && edmFile.isFile()) {
                
                try {
                    // Retrieve xml content. ATTENTION: Namespace aware is true.
                    Document doc = XMLUtils.parse(edmFile, true);
                    // String itemContent = XMLUtils.transform(doc);

                    // Get subjects
                    List<String> subjectValues = XMLUtils.getElementValues(doc, "//dc:subject");
                    
                    // Find subject mapppings (if any)
                    int termMatchCount = 0;
                    List<String> subjectMappings = new LinkedList<>();
                    for(String value : subjectValues) {
                        if(termsMap.containsKey(value)) {
                            termMatchCount++;
                            subjectMappings.add(value);
                        }
                    }
                    
                    // edmFile.getAbsolutePath()
                    log.info("File: {} #Subjects: {} #Matches: {}", edmFile.getName(), subjectValues.size(), termMatchCount);
                    
                    if(!subjectMappings.isEmpty()) {
                        
                        // Add subject mappings
                        doc = XMLUtils.appendElements(doc, "//edm:ProvidedCHO", "dc:subject", subjectMappings);
                        
                        // Create enriched directory (if not)
                        if(!enrichedDirCreated) {
                            enrichedDirPath = Paths.get(edmExtractDirPath.getParent().toString(), "eEDM");
                            Files.createDirectories(enrichedDirPath);
                        }
                        
                        // Save enriched file
                        Path enrichedFilePath = Paths.get(enrichedDirPath.toString(), edmFile.getName());
                        XMLUtils.transform(doc, enrichedFilePath.toFile());
                        
                        enrichedFileCount++;
                        
                        log.info("File enriched. {} subjects added.Stored at: {}", 
                                termMatchCount, enrichedFilePath);
                    }
                } catch(ParserConfigurationException | SAXException | 
                        TransformerException | XPathExpressionException ex) {
                    log.error("Cannot parse file. File: {}", edmFile.getAbsolutePath());
                }
                
            }
        }
        
        // Create archive with enriched files
        String enrichedArchiveFilePath = "";
        String enrichedArchiveName = "";
        if(enrichedFileCount > 0) {
            log.info("#Files: {} #Enriched: {}", edmFiles.length, enrichedFileCount);
            
            // Example eEDM_m2_r4 => mapping 2, enrichRequest 4
            String filenamePrefix = "eEDM_m" + id + "_r" + requestId; 
            Path archiveFilePath = fileStorageService.createArchiveFromDirectory(enrichedDirPath, filenamePrefix);
            
            enrichedArchiveName = archiveFilePath.getFileName().toString();
            enrichedArchiveFilePath = archiveFilePath.toString();
            log.info("Enriched archive created at {}", archiveFilePath.toString());
            
        } 
        
        String message = enrichedFileCount + " files enriched successfully.";
                
        // Set enrich details
        EnrichDetails enrichDetails = new EnrichDetails();
        enrichDetails.setSuccess(true);
        enrichDetails.setMessage(message);
        enrichDetails.setEdmFileCount(edmFiles.length);
        enrichDetails.setEdmArchiveName(file.getOriginalFilename());
        enrichDetails.setEnrichedFileCount(enrichedFileCount);
        enrichDetails.setEnrichedArchiveName(enrichedArchiveName);
        enrichDetails.setEnrichedArchiveUrl("/enrich/requests/" + requestId + "/download");
        
        ObjectMapper objectMaper = new ObjectMapper();
        String details = objectMaper.writeValueAsString(enrichDetails);
        
        // Update enrich enrichRequest
        enrichRequest.setEnrichedFilename(enrichedArchiveName);
        enrichRequest.setEnrichedFilepath(enrichedArchiveFilePath);
        enrichRequest.setDetails(details);
        enrichRequestRepository.save(enrichRequest);
                
        return enrichDetails;
    }
    
    /**
     * Download enriched archive
     * @param id
     * @param requestId
     * @return
     * @throws IOException 
     */
    @GetMapping("/mappings/{id}/enrich/{requestId}/download")
    public ResponseEntity<?> downloadEnrichedEdmArchive(@PathVariable Long id, 
            @PathVariable Long requestId) throws IOException {
        
        EnrichRequest enrichRequest = enrichRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(requestId));
        
        String filename = enrichRequest.getFilename();
        String filepath = enrichRequest.getEnrichedFilepath();
        
        File file = fileStorageService.loadFile(filepath);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/force-download")
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + filename + "\"")
                .body(new FileSystemResource(file));
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
}
