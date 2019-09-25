package gr.dcu.europeana.arch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.dcu.europeana.arch.api.resource.EnrichDetails;
import gr.dcu.europeana.arch.exception.ResourceNotFoundException;
import gr.dcu.europeana.arch.model.EnrichRequest;
import gr.dcu.europeana.arch.model.MappingExportRequest;
import gr.dcu.europeana.arch.model.MappingType;
import gr.dcu.europeana.arch.model.SpatialTerm;
import gr.dcu.europeana.arch.model.SubjectTerm;
import gr.dcu.europeana.arch.model.Mapping;
import gr.dcu.europeana.arch.model.MappingUploadRequest;
import gr.dcu.europeana.arch.repository.EnrichRequestRepository;
import gr.dcu.europeana.arch.repository.ExportRequestRepository;
import gr.dcu.europeana.arch.repository.SpatialTermRepository;
import gr.dcu.europeana.arch.repository.SubjectMappingRepository;
import gr.dcu.europeana.arch.repository.UploadRequestRepository;
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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import gr.dcu.europeana.arch.repository.SubjectTermRepository;
import gr.dcu.europeana.arch.repository.TemporalTermRepository;

/**
 *
 * @author Vangelis Nomikos
 */
@Slf4j
@Service
public class MappingService {
    
    @Autowired
    SubjectMappingRepository subjectMappingRepository;
    
    @Autowired
    SubjectTermRepository subjectTermRepository;
    
    @Autowired
    SpatialTermRepository spatialTermRepository;
    
    @Autowired
    TemporalTermRepository temporalTermRepository;
    
    @Autowired
    FileStorageService fileStorageService;
    
    @Autowired
    ExcelService excelService;
    
    @Autowired
    ExportRequestRepository exportRequestRepository;
    
    @Autowired
    UploadRequestRepository uploadRequestRepository;
    
    @Autowired
    EnrichRequestRepository enrichRequestRepository;
    
    /**
     * 
     * @return 
     */
    public List<Mapping> findAll() {
        
        return subjectMappingRepository.findAll();
    }
    
    /**
     * 
     * @param userId
     * @return 
     */
    public List<Mapping> findAllByUserId(int userId) {
        
        return subjectMappingRepository.findAllByCreatedBy(userId);
    }
    
    /**
     * 
     * @param userId
     * @param mapping
     * @return 
     */
    public Mapping save(int userId, Mapping mapping) {
        
        mapping.setCreatedBy(userId);
        
        return subjectMappingRepository.save(mapping);
    }
    
    /**
     * 
     * @param userId
     * @param id 
     */
    public void delete(int userId, long id) {
        
        subjectMappingRepository.deleteById(id);
    }
    
    /**
     * 
     * @param userId
     * @param mappingId
     * @return
     * @throws IOException 
     */
    public Resource exportTerms(long mappingId, int userId) throws IOException {
        
        Resource resource;
        
        try {
            // Check if mapping exists
            Mapping mapping = subjectMappingRepository.findById(mappingId)
                    .orElseThrow(() -> new ResourceNotFoundException(mappingId));

            // Export to tmp file
            Path filePath = fileStorageService.buildExportFilePath(mappingId);
            
            switch(mapping.getType()) {
                case MappingType.MAPPING_TYPE_SUBJECT:
                    // Get terms
                    List<SubjectTerm> termList = subjectTermRepository.findByMappingId(mappingId);
                    log.info("Load terms. Mapping: {} | #Terms: {}", mappingId, termList.size());
            
                    // Export
                    excelService.exportSubjectTermsToExcel(filePath, termList);
                    break;
                case MappingType.MAPPING_TYPE_SPATIAL:
                    List<SpatialTerm> spatialTermList = spatialTermRepository.findByMappingId(mappingId);
                    log.info("Load terms. Mapping: {} | #Terms: {}", mappingId, spatialTermList.size());
                    excelService.exportSpatialTermsToExcel(filePath, spatialTermList);
                    break;
                case MappingType.MAPPING_TYPE_TEMPORAL:
                    
                    // excelService.exportSubjectTermsToExcel(filePath, termList);
                    break;
                default:
                    log.warn("Unknown mapping type.");
            }
            
            log.info("Export saved at {}", filePath);

            // Load exportTerms file
            resource = fileStorageService.loadFileAsResource(filePath);

            // Create exportTerms enrichRequest
            MappingExportRequest exportRequest = new MappingExportRequest();
            exportRequest.setMappingId(mappingId);
            exportRequest.setFilepath(filePath.toString());
            exportRequest.setCreatedBy(userId);
            exportRequestRepository.save(exportRequest);
            
        } catch(IOException ex) {
            throw ex;
        }
        
        return resource;
        
    }
    
    /**
     * 
     * @param mappingId
     * @param file
     * @throws IOException 
     */
    public List<SubjectTerm> uploadTerms(long mappingId, MultipartFile file, int userId) throws IOException {
        
        // Check existemce of mapping
        Mapping mapping = subjectMappingRepository.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException(mappingId));
        
        // Upload mapping file
        Path filePath = fileStorageService.buildUploadFilePath(mappingId, file.getOriginalFilename());
        fileStorageService.upload(filePath, file);
        
        // Load terms
        List<SubjectTerm> termList = 
                excelService.loadMappingTermsFromExcel(filePath.toString(), mappingId, 1, -1);
        
        // Save terms
        log.info("Saving terms. #Terms:{}", termList.size());
        
        // TODO: Do not save automatically. Preview first.
        subjectTermRepository.saveAll(termList);
        
        // Create upload enrichRequest
        MappingUploadRequest uploadRequest = new MappingUploadRequest();
        uploadRequest.setMappingId(mappingId);
        uploadRequest.setFilename(file.getOriginalFilename());
        uploadRequest.setFilepath(filePath.toString());
        uploadRequest.setCreatedBy(userId);
        uploadRequestRepository.save(uploadRequest);
        
        return termList;
    }
    
    /**
     * 
     * @param mappingId
     * @param file
     * @return
     * @throws IOException 
     */
    public EnrichDetails enrich(long mappingId, MultipartFile file, int userId) throws IOException {
         
        // Check existemce of mapping
        Mapping mapping = subjectMappingRepository.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException(mappingId));
        
        // Get mapping terms. Convert them to map
        List<SubjectTerm> terms = subjectTermRepository.findByMappingId(mappingId);
        Map<String,SubjectTerm> termsMap = new HashMap<>();
        for(SubjectTerm mp : terms) {
            termsMap.put(mp.getNativeTerm(), mp);
        }
        log.info("Mapping: {} #Terms: {}", mappingId, terms.size());
        
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
        enrichRequest.setMappingId(mappingId);
        enrichRequest.setFilename(file.getOriginalFilename());
        enrichRequest.setCreatedBy(userId);
        enrichRequest = enrichRequestRepository.save(enrichRequest);
        long requestId = enrichRequest.getId();
        
        // Upload EDM archive
        log.info("Upload EDM archive. RequestId: {}", requestId);
        Path edmArchiveFilePath = fileStorageService.buildUploadEdmArchiveFilePath(mappingId, file.getOriginalFilename(), requestId);
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
            String filenamePrefix = "eEDM_m" + mappingId + "_r" + requestId; 
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
     * 
     * @param requestId
     * @return
     * @throws IOException 
     */
    public File loadEnrichedArchive(long requestId) throws IOException {
        
        EnrichRequest enrichRequest = enrichRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(requestId));
        
        String filename = enrichRequest.getFilename();
        String filepath = enrichRequest.getEnrichedFilepath();
        
        File file = fileStorageService.loadFile(filepath);
        
        return file;
    }
}
