package gr.dcu.europeana.arch.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.dcu.europeana.arch.api.resource.AppendTermsResult;
import gr.dcu.europeana.arch.api.resource.EnrichDetails;
import gr.dcu.europeana.arch.exception.ResourceNotFoundException;
import gr.dcu.europeana.arch.model.EdmArchive;
import gr.dcu.europeana.arch.model.EdmArchiveTerms;
import gr.dcu.europeana.arch.model.EnrichRequest;
import gr.dcu.europeana.arch.model.MappingExportRequest;
import gr.dcu.europeana.arch.model.MappingType;
import gr.dcu.europeana.arch.model.SpatialTerm;
import gr.dcu.europeana.arch.model.SubjectTerm;
import gr.dcu.europeana.arch.model.Mapping;
import gr.dcu.europeana.arch.model.MappingUploadRequest;
import gr.dcu.europeana.arch.model.TemporalTerm;
import gr.dcu.europeana.arch.repository.EdmArchiveRepository;
import gr.dcu.europeana.arch.repository.EdmArchiveTermsRepository;
import gr.dcu.europeana.arch.repository.EnrichRequestRepository;
import gr.dcu.europeana.arch.repository.ExportRequestRepository;
import gr.dcu.europeana.arch.repository.SpatialTermRepository;
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
import gr.dcu.europeana.arch.repository.MappingRepository;
import javax.transaction.TransactionScoped;
import javax.transaction.Transactional;

/**
 *
 * @author Vangelis Nomikos
 */
@Slf4j
@Service
public class MappingService {
    
    @Autowired
    MappingRepository mappingRepository;
    
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
    
    @Autowired
    EdmArchiveRepository edmArchiveRepository;
    
    @Autowired
    EdmArchiveTermsRepository edmArchiveTermsRepository;
    
    /**
     * 
     * @return 
     */
    public List<Mapping> findAll() {
        
        return mappingRepository.findAll();
    }
    
    /**
     * 
     * @param userId
     * @return 
     */
    public List<Mapping> findAllByUserId(int userId) {
        
        return mappingRepository.findAllByCreatedBy(userId);
    }
    
    /**
     * 
     * @param userId
     * @param mapping
     * @return 
     */
    public Mapping save(int userId, Mapping mapping) {
        
        mapping.setCreatedBy(userId);
        
        return mappingRepository.save(mapping);
    }
    
    /**
     * 
     * @param userId
     * @param mappingId
     */
    @Transactional
    public void delete(int userId, long mappingId) {
        
        // Check if mapping exists
        Mapping mapping = mappingRepository.findById(mappingId)
                    .orElseThrow(() -> new ResourceNotFoundException(mappingId));
        
        // Delete terms
        switch(mapping.getType()) {
            case MappingType.MAPPING_TYPE_SUBJECT:
                subjectTermRepository.deleteByMappingId(mappingId);
                break;
            case MappingType.MAPPING_TYPE_SPATIAL:
                spatialTermRepository.deleteByMappingId(mappingId);
                break;
            case MappingType.MAPPING_TYPE_TEMPORAL:
                temporalTermRepository.deleteByMappingId(mappingId);
                break;
            default:
                log.warn("Unknown mapping type.");
         }
        
        // Delete mapping
        mappingRepository.deleteById(mappingId);
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
            Mapping mapping = mappingRepository.findById(mappingId)
                    .orElseThrow(() -> new ResourceNotFoundException(mappingId));

            // Export to tmp file
            Path filePath = fileStorageService.buildMappingExportFilePath(mappingId);
            
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

            // Load exportExtractedAllTerms file
            resource = fileStorageService.loadFileAsResource(filePath);

            // Create exportExtractedAllTerms enrichRequest
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
     * @param userId
     * @return 
     * @throws IOException 
     */
    public List<SubjectTerm> uploadSubjectTerms(long mappingId, MultipartFile file, int userId) throws IOException {
        
        // Check existemce of mapping
        Mapping mapping = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException(mappingId));
        
        // Upload mapping file
        Path filePath = fileStorageService.buildMappingUploadFilePath(mappingId, file.getOriginalFilename());
        fileStorageService.upload(filePath, file);
        
        // Load terms
        List<SubjectTerm> termList = 
                excelService.loadSubjectTermsFromExcel(filePath.toString(), mappingId, 1, -1);
        
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
     * @param userId
     * @return
     * @throws IOException 
     */
    public List<SpatialTerm> uploadSpatialTerms(long mappingId, MultipartFile file, int userId) throws IOException {
        
        // Check existemce of mapping
        Mapping mapping = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException(mappingId));
        
        // Upload mapping file
        Path filePath = fileStorageService.buildMappingUploadFilePath(mappingId, file.getOriginalFilename());
        fileStorageService.upload(filePath, file);
        
        // Load terms
        List<SpatialTerm> termList = 
                excelService.loadSpatialTermsFromExcel(filePath.toString(), mappingId, 1, -1);
        
        // Save terms
        log.info("Saving terms. #Terms:{}", termList.size());
        
        // TODO: Do not save automatically. Preview first.
        spatialTermRepository.saveAll(termList);
        
        // Create upload enrichRequest
        MappingUploadRequest uploadRequest = new MappingUploadRequest();
        uploadRequest.setMappingId(mappingId);
        uploadRequest.setFilename(file.getOriginalFilename());
        uploadRequest.setFilepath(filePath.toString());
        uploadRequest.setCreatedBy(userId);
        uploadRequestRepository.save(uploadRequest);
        
        return termList;
    }
    
    public List<TemporalTerm> uploadTemporalTerms(long mappingId, MultipartFile file, int userId) throws IOException {
        
        // Check existemce of mapping
        Mapping mapping = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException(mappingId));
        
        // Upload mapping file
        Path filePath = fileStorageService.buildMappingUploadFilePath(mappingId, file.getOriginalFilename());
        fileStorageService.upload(filePath, file);
        
        // Load terms
        List<TemporalTerm> termList = 
                excelService.loadTemporalTermsFromExcel(filePath.toString(), mappingId, 1, -1);
        
        // Save terms
        log.info("Saving terms. #Terms:{}", termList.size());
        
        // TODO: Do not save automatically. Preview first.
        temporalTermRepository.saveAll(termList);
        
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
        Mapping mapping = mappingRepository.findById(mappingId)
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
       // enrichDetails.setEnrichedArchiveUrl("/enrich/requests/" + requestId + "/download");
        
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
    
     /**
     * Create a mapping based on extracted terms from EDM archive.
     * @param archiveId
     * @param type
     * @param userId
     * @return 
     */
    @Transactional
    public Mapping createMappingByArchiveId(Long archiveId, String type, Integer userId) {
        
        EdmArchive edmArchive = edmArchiveRepository.findById(archiveId)
                .orElseThrow(() -> new ResourceNotFoundException(archiveId));
        
        EdmArchiveTerms edmArchiveTerms = edmArchiveTermsRepository.findByArchiveId(archiveId);
        
        log.info("Create mapping for archive. Archive: {} Type: {}", archiveId, type);
        List<SubjectTerm> subjectTerms = new LinkedList<>();
        List<SpatialTerm> spatialTerms = new LinkedList<>();
        List<TemporalTerm> temporalTerms = new LinkedList<>();
        if(edmArchiveTerms != null) {
        
            try { 
                ObjectMapper mapper = new ObjectMapper();
                switch(type) {
                    case MappingType.MAPPING_TYPE_SUBJECT:
                        subjectTerms = mapper.readValue(edmArchiveTerms.getSubjectTerms(), 
                                new TypeReference<List<SubjectTerm>>(){});
                        break;
                    case MappingType.MAPPING_TYPE_SPATIAL:
                        spatialTerms = mapper.readValue(edmArchiveTerms.getSpatialTerms(), 
                                new TypeReference<List<SpatialTerm>>(){});
                        break;
                    case MappingType.MAPPING_TYPE_TEMPORAL:
                        temporalTerms = mapper.readValue(edmArchiveTerms.getTemporalTerms(), 
                                new TypeReference<List<TemporalTerm>>(){});
                        break;
                }
            } catch (IOException ex){
                log.warn("Cannot parse Edm Archive Terms. ArchiveID: {} ArchiveIdTerms: {}", archiveId, edmArchiveTerms.getId());
            }
        } else {
            log.warn("Cannot get Edm Archive Terms. ArchiveID: {}", archiveId);
        }
        
        // Create mapping
        Mapping mapping = new Mapping();
        mapping.setLabel(edmArchive.getFilename());
        mapping.setDescription(edmArchive.getFilename());
        mapping.setType(type);
        mapping.setLanguage("");
        mapping.setProviderName(edmArchive.getFilename());
        mapping.setVocabularyName(edmArchive.getFilename());
        mapping.setCreatedBy(userId);
        long mappingId = mappingRepository.save(mapping).getId();
        log.info("Mapping created. MappingId:{}", mappingId);
        
        // Add terms to mapping
        if(type.equalsIgnoreCase(MappingType.MAPPING_TYPE_SUBJECT) && !subjectTerms.isEmpty()) {
            
            log.info("Add terms to mappings. Size: {}", subjectTerms.size());
            
            for(SubjectTerm term : subjectTerms) {
                term.setMappingId(mappingId);
            }
            subjectTermRepository.saveAll(subjectTerms);
            edmArchive.setThematicMapping(mappingId);
        } else if(type.equalsIgnoreCase(MappingType.MAPPING_TYPE_SPATIAL) && !spatialTerms.isEmpty()) {
            
            log.info("Add terms to mappings. Size: {}", spatialTerms.size());
            
            for(SpatialTerm term : spatialTerms) {
                term.setMappingId(mappingId);
            }
            spatialTermRepository.saveAll(spatialTerms);
            edmArchive.setSpatialMapping(mappingId);
        } else if(type.equalsIgnoreCase(MappingType.MAPPING_TYPE_TEMPORAL) && !temporalTerms.isEmpty()) {
            
            log.info("Add terms to mappings. Size: {}", temporalTerms.size());
            
            for(TemporalTerm term : temporalTerms) {
                term.setMappingId(mappingId);
            }
            temporalTermRepository.saveAll(temporalTerms);
            edmArchive.setTemporalMapping(mappingId);
        }
        
        edmArchiveRepository.save(edmArchive);
        
        return mapping;
    }
    
    @Transactional
    public AppendTermsResult appendTermsToMappingByArchiveId(Long mappingId, Long archiveId, Integer userId) {
        
        AppendTermsResult appendTermResult = new AppendTermsResult();
        long existingTermCount = 0;
        long appendTermCount = 0;
        
        Mapping mapping = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException(mappingId));
        String mappingType = mapping.getType();
        
        log.info("Append archive terms to mapping. Archive: {} Mapping: {} Type: {}", 
                archiveId, mappingId, mappingType);
        
        EdmArchive edmArchive = edmArchiveRepository.findById(archiveId)
                .orElseThrow(() -> new ResourceNotFoundException(archiveId));
        
        EdmArchiveTerms edmArchiveTerms = edmArchiveTermsRepository.findByArchiveId(archiveId);
        
        List<SubjectTerm> mappingSubjectTerms = new LinkedList<>();
        List<SpatialTerm> mappingSpatialTerms = new LinkedList<>();
        List<TemporalTerm> mappingTemporalTerms = new LinkedList<>();
        
        List<SubjectTerm> archiveSubjectTerms = new LinkedList<>();
        List<SpatialTerm> archiveSpatialTerms = new LinkedList<>();
        List<TemporalTerm> archiveTemporalTerms = new LinkedList<>();
        if(edmArchiveTerms != null) {
        
            try { 
                ObjectMapper mapper = new ObjectMapper();
                switch(mappingType) {
                    case MappingType.MAPPING_TYPE_SUBJECT:
                        archiveSubjectTerms = mapper.readValue(edmArchiveTerms.getSubjectTerms(), 
                                new TypeReference<List<SubjectTerm>>(){});
                        
                        mappingSubjectTerms = subjectTermRepository.findByMappingId(mappingId);
                        
                        log.info("ArchiveSubjectTerms: {}, MappingSubjectTerms: {}", 
                                archiveSubjectTerms.size(), mappingSubjectTerms.size());
                        break;
                    case MappingType.MAPPING_TYPE_SPATIAL:
                        archiveSpatialTerms = mapper.readValue(edmArchiveTerms.getSpatialTerms(), 
                                new TypeReference<List<SpatialTerm>>(){});
                        
                        mappingSpatialTerms = spatialTermRepository.findByMappingId(mappingId);
                        
                        log.info("ArchiveSpatialTerms: {}, MappingSpatialTerms: {}", 
                                archiveSpatialTerms.size(), mappingSpatialTerms.size());
                        break;
                    case MappingType.MAPPING_TYPE_TEMPORAL:
                        archiveTemporalTerms = mapper.readValue(edmArchiveTerms.getTemporalTerms(), 
                                new TypeReference<List<TemporalTerm>>(){});
                        
                        mappingTemporalTerms = temporalTermRepository.findByMappingId(mappingId);
                        
                        log.info("ArchiveTemporalTerms: {}, MappingTemporalTerms: {}", 
                                archiveTemporalTerms.size(), mappingTemporalTerms.size());
                        break;
                }
            } catch (IOException ex){
                log.warn("Cannot parse Edm Archive Terms. ArchiveID: {} ArchiveIdTerms: {}", archiveId, edmArchiveTerms.getId());
            }
        } else {
            log.warn("Cannot get Edm Archive Terms. ArchiveID: {}", archiveId);
        }
        
        // Add terms to mapping
        if(mappingType.equalsIgnoreCase(MappingType.MAPPING_TYPE_SUBJECT) && !archiveSubjectTerms.isEmpty()) {
            
            existingTermCount = mappingSubjectTerms.size();
            appendTermCount = archiveSubjectTerms.size();
            
            for(SubjectTerm term : archiveSubjectTerms) {
                term.setMappingId(mappingId);
            }
            
            mappingSubjectTerms.addAll(archiveSubjectTerms);
            subjectTermRepository.saveAll(mappingSubjectTerms);
            
            
        } else if(mappingType.equalsIgnoreCase(MappingType.MAPPING_TYPE_SPATIAL) && !archiveSpatialTerms.isEmpty()) {
            
            existingTermCount = mappingSpatialTerms.size();
            appendTermCount = archiveSpatialTerms.size();
            
            for(SpatialTerm term : archiveSpatialTerms) {
                term.setMappingId(mappingId);
            }
            
            mappingSpatialTerms.addAll(archiveSpatialTerms);
            spatialTermRepository.saveAll(mappingSpatialTerms);
        } else if(mappingType.equalsIgnoreCase(MappingType.MAPPING_TYPE_TEMPORAL) && !archiveTemporalTerms.isEmpty()) {
            
            existingTermCount = mappingTemporalTerms.size();
            appendTermCount = archiveTemporalTerms.size();
            
            for(TemporalTerm term : archiveTemporalTerms) {
                term.setMappingId(mappingId);
            }
            mappingTemporalTerms.addAll(archiveTemporalTerms);
            temporalTermRepository.saveAll(mappingTemporalTerms);
        }
        
        appendTermResult.setExistingTermCount(existingTermCount);
        appendTermResult.setAppendTermCount(appendTermCount);
        
        return appendTermResult;
    }
}
