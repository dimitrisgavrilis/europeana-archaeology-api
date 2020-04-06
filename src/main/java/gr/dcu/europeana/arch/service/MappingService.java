package gr.dcu.europeana.arch.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.dcu.europeana.arch.api.dto.AppendTermsResult;
import gr.dcu.europeana.arch.api.dto.EnrichDetails;
import gr.dcu.europeana.arch.exception.ResourceNotFoundException;
import gr.dcu.europeana.arch.model.*;
import gr.dcu.europeana.arch.model.EdmArchiveTermsEntity;
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
    public List<MappingEntity> findAll() {
        
        return mappingRepository.findAll();
    }
    
    /**
     * 
     * @param userId
     * @return 
     */
    public List<MappingEntity> findAllByUserId(int userId) {
        
        return mappingRepository.findAllByCreatedBy(userId);
    }

    public MappingEntity findById(Long id) {
        return mappingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    }
    
    /**
     * 
     * @param userId
     * @param mappingEntity
     * @return 
     */
    public MappingEntity save(int userId, MappingEntity mappingEntity) {
        
        mappingEntity.setCreatedBy(userId);
        
        return mappingRepository.save(mappingEntity);
    }

    public MappingEntity updateMapping(Long id, MappingEntity mappingEntity) {

        MappingEntity existingMappingEntity = mappingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        existingMappingEntity.setLabel(mappingEntity.getLabel());
        existingMappingEntity.setDescription(mappingEntity.getDescription());
        existingMappingEntity.setLanguage(mappingEntity.getLanguage());
        existingMappingEntity.setProviderName(mappingEntity.getProviderName());
        existingMappingEntity.setVocabularyName(mappingEntity.getVocabularyName());

        return mappingRepository.save(existingMappingEntity);
    }
    
    /**
     * 
     * @param userId
     * @param mappingId
     */
    @Transactional
    public void delete(int userId, long mappingId) {
        
        // Check if mapping exists
        MappingEntity mappingEntity = mappingRepository.findById(mappingId)
                    .orElseThrow(() -> new ResourceNotFoundException(mappingId));
        
        // Delete terms
        switch(mappingEntity.getType()) {
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
            MappingEntity mappingEntity = mappingRepository.findById(mappingId)
                    .orElseThrow(() -> new ResourceNotFoundException(mappingId));

            // Export to tmp file
            Path filePath = fileStorageService.buildMappingExportFilePath(mappingId);
            
            switch(mappingEntity.getType()) {
                case MappingType.MAPPING_TYPE_SUBJECT:
                    // Get terms
                    List<SubjectTermEntity> termList = subjectTermRepository.findByMappingId(mappingId);
                    log.info("Load terms. Mapping: {} | #Terms: {}", mappingId, termList.size());
            
                    // Export
                    excelService.exportSubjectTermsToExcel(filePath, termList);
                    break;
                case MappingType.MAPPING_TYPE_SPATIAL:
                    List<SpatialTermEntity> spatialTermEntityList = spatialTermRepository.findByMappingId(mappingId);
                    log.info("Load terms. Mapping: {} | #Terms: {}", mappingId, spatialTermEntityList.size());
                    excelService.exportSpatialTermsToExcel(filePath, spatialTermEntityList);
                    break;
                case MappingType.MAPPING_TYPE_TEMPORAL:
                    List<TemporalTermEntity> temporalTermEntityList = temporalTermRepository.findByMappingId(mappingId);
                    log.info("Load terms. Mapping: {} | #Terms: {}", mappingId, temporalTermEntityList.size());

                    // Export
                    excelService.exportTemporalTermsToExcel(filePath, temporalTermEntityList);
                    break;
                default:
                    log.warn("Unknown mapping type.");
            }
            
            log.info("Export saved at {}", filePath);

            // Load exportExtractedAllTerms file
            resource = fileStorageService.loadFileAsResource(filePath);

            // Create exportExtractedAllTerms enrichRequest
            MappingExportRequestEntity exportRequest = new MappingExportRequestEntity();
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
    public List<SubjectTermEntity> uploadSubjectTerms(long mappingId, MultipartFile file, int userId) throws IOException {
        
        // Check existemce of mapping
        MappingEntity mappingEntity = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException(mappingId));
        
        // Upload mapping file
        Path filePath = fileStorageService.buildMappingUploadFilePath(mappingId, file.getOriginalFilename());
        fileStorageService.upload(filePath, file);
        
        // Load terms
        List<SubjectTermEntity> termList =
                excelService.loadSubjectTermsFromExcel(filePath.toString(), mappingId, 1, -1);
        
        // Save terms
        log.info("Saving terms. #Terms:{}", termList.size());
        
        // TODO: Do not save automatically. Preview first.
        subjectTermRepository.saveAll(termList);
        
        // Create upload enrichRequest
        MappingUploadRequestEntity uploadRequest = new MappingUploadRequestEntity();
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
    public List<SpatialTermEntity> uploadSpatialTerms(long mappingId, MultipartFile file, int userId) throws IOException {
        
        // Check existemce of mapping
        MappingEntity mappingEntity = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException(mappingId));
        
        // Upload mapping file
        Path filePath = fileStorageService.buildMappingUploadFilePath(mappingId, file.getOriginalFilename());
        fileStorageService.upload(filePath, file);
        
        // Load terms
        List<SpatialTermEntity> termList =
                excelService.loadSpatialTermsFromExcel(filePath.toString(), mappingId, 1, -1);
        
        // Save terms
        log.info("Saving terms. #Terms:{}", termList.size());
        
        // TODO: Do not save automatically. Preview first.
        spatialTermRepository.saveAll(termList);
        
        // Create upload enrichRequest
        MappingUploadRequestEntity uploadRequest = new MappingUploadRequestEntity();
        uploadRequest.setMappingId(mappingId);
        uploadRequest.setFilename(file.getOriginalFilename());
        uploadRequest.setFilepath(filePath.toString());
        uploadRequest.setCreatedBy(userId);
        uploadRequestRepository.save(uploadRequest);
        
        return termList;
    }
    
    public List<TemporalTermEntity> uploadTemporalTerms(long mappingId, MultipartFile file, int userId) throws IOException {
        
        // Check existemce of mapping
        MappingEntity mappingEntity = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException(mappingId));
        
        // Upload mapping file
        Path filePath = fileStorageService.buildMappingUploadFilePath(mappingId, file.getOriginalFilename());
        fileStorageService.upload(filePath, file);
        
        // Load terms
        List<TemporalTermEntity> termList =
                excelService.loadTemporalTermsFromExcel(filePath.toString(), mappingId, 1, -1);
        
        // Save terms
        log.info("Saving terms. #Terms:{}", termList.size());
        
        // TODO: Do not save automatically. Preview first.
        temporalTermRepository.saveAll(termList);
        
        // Create upload enrichRequest
        MappingUploadRequestEntity uploadRequest = new MappingUploadRequestEntity();
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
        MappingEntity mappingEntity = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException(mappingId));
        
        // Get mapping terms. Convert them to map
        List<SubjectTermEntity> terms = subjectTermRepository.findByMappingId(mappingId);
        Map<String, SubjectTermEntity> termsMap = new HashMap<>();
        for(SubjectTermEntity mp : terms) {
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
        EnrichRequestEntity enrichRequestEntity = new EnrichRequestEntity();
        enrichRequestEntity.setMappingId(mappingId);
        enrichRequestEntity.setFilename(file.getOriginalFilename());
        enrichRequestEntity.setCreatedBy(userId);
        enrichRequestEntity = enrichRequestRepository.save(enrichRequestEntity);
        long requestId = enrichRequestEntity.getId();
        
        // Upload EDM archive
        log.info("Upload EDM archive. RequestId: {}", requestId);
        Path edmArchiveFilePath = fileStorageService.buildUploadEdmArchiveFilePath(mappingId, file.getOriginalFilename(), requestId);
        fileStorageService.upload(edmArchiveFilePath, file);
        log.info("EDM archive uploaded. Path: {}", edmArchiveFilePath);
        
        // Update enrich enrichRequest
        enrichRequestEntity.setFilepath(edmArchiveFilePath.toString());
        enrichRequestEntity = enrichRequestRepository.save(enrichRequestEntity);
        
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
        enrichRequestEntity.setEnrichedFilename(enrichedArchiveName);
        enrichRequestEntity.setEnrichedFilepath(enrichedArchiveFilePath);
        enrichRequestEntity.setDetails(details);
        enrichRequestRepository.save(enrichRequestEntity);
        
        return enrichDetails;
    }
    
    
    
    /**
     * 
     * @param requestId
     * @return
     * @throws IOException 
     */
    public File loadEnrichedArchive(long requestId) throws IOException {
        
        EnrichRequestEntity enrichRequestEntity = enrichRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(requestId));
        
        String filename = enrichRequestEntity.getFilename();
        String filepath = enrichRequestEntity.getEnrichedFilepath();
        
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
    public MappingEntity createMappingByArchiveId(Long archiveId, String type, Integer userId) {
        
        EdmArchiveEntity edmArchiveEntity = edmArchiveRepository.findById(archiveId)
                .orElseThrow(() -> new ResourceNotFoundException(archiveId));
        
        EdmArchiveTermsEntity edmArchiveTermsEntity = edmArchiveTermsRepository.findByArchiveId(archiveId);
        
        log.info("Create mapping for archive. Archive: {} Type: {}", archiveId, type);
        List<SubjectTermEntity> subjectTermEntities = new LinkedList<>();
        List<SpatialTermEntity> spatialTermEntities = new LinkedList<>();
        List<TemporalTermEntity> temporalTermEntities = new LinkedList<>();
        if(edmArchiveTermsEntity != null) {
        
            try { 
                ObjectMapper mapper = new ObjectMapper();
                switch(type) {
                    case MappingType.MAPPING_TYPE_SUBJECT:
                        subjectTermEntities = mapper.readValue(edmArchiveTermsEntity.getSubjectTerms(),
                                new TypeReference<List<SubjectTermEntity>>(){});
                        break;
                    case MappingType.MAPPING_TYPE_SPATIAL:
                        spatialTermEntities = mapper.readValue(edmArchiveTermsEntity.getSpatialTerms(),
                                new TypeReference<List<SpatialTermEntity>>(){});
                        break;
                    case MappingType.MAPPING_TYPE_TEMPORAL:
                        temporalTermEntities = mapper.readValue(edmArchiveTermsEntity.getTemporalTerms(),
                                new TypeReference<List<TemporalTermEntity>>(){});
                        break;
                }
            } catch (IOException ex){
                log.warn("Cannot parse Edm Archive Terms. ArchiveID: {} ArchiveIdTerms: {}", archiveId, edmArchiveTermsEntity.getId());
            }
        } else {
            log.warn("Cannot get Edm Archive Terms. ArchiveID: {}", archiveId);
        }
        
        // Create mapping
        MappingEntity mappingEntity = new MappingEntity();
        mappingEntity.setLabel(edmArchiveEntity.getFilename());
        mappingEntity.setDescription(edmArchiveEntity.getFilename());
        mappingEntity.setType(type);
        mappingEntity.setLanguage("");
        mappingEntity.setProviderName(edmArchiveEntity.getFilename());
        mappingEntity.setVocabularyName(edmArchiveEntity.getFilename());
        mappingEntity.setCreatedBy(userId);
        long mappingId = mappingRepository.save(mappingEntity).getId();
        log.info("Mapping created. MappingId:{}", mappingId);
        
        // Add terms to mapping
        if(type.equalsIgnoreCase(MappingType.MAPPING_TYPE_SUBJECT) && !subjectTermEntities.isEmpty()) {
            
            log.info("Add terms to mappings. Size: {}", subjectTermEntities.size());
            
            for(SubjectTermEntity term : subjectTermEntities) {
                term.setMappingId(mappingId);
            }
            subjectTermRepository.saveAll(subjectTermEntities);
            edmArchiveEntity.setThematicMapping(mappingId);
        } else if(type.equalsIgnoreCase(MappingType.MAPPING_TYPE_SPATIAL) && !spatialTermEntities.isEmpty()) {
            
            log.info("Add terms to mappings. Size: {}", spatialTermEntities.size());
            
            for(SpatialTermEntity term : spatialTermEntities) {
                term.setMappingId(mappingId);
            }
            spatialTermRepository.saveAll(spatialTermEntities);
            edmArchiveEntity.setSpatialMapping(mappingId);
        } else if(type.equalsIgnoreCase(MappingType.MAPPING_TYPE_TEMPORAL) && !temporalTermEntities.isEmpty()) {
            
            log.info("Add terms to mappings. Size: {}", temporalTermEntities.size());
            
            for(TemporalTermEntity term : temporalTermEntities) {
                term.setMappingId(mappingId);
            }
            temporalTermRepository.saveAll(temporalTermEntities);
            edmArchiveEntity.setTemporalMapping(mappingId);
        }
        
        edmArchiveRepository.save(edmArchiveEntity);
        
        return mappingEntity;
    }
    
    @Transactional
    public AppendTermsResult appendTermsToMappingByArchiveId(Long mappingId, Long archiveId, Integer userId) {
        
        AppendTermsResult appendTermResult = new AppendTermsResult();
        long existingTermCount = 0;
        long appendTermCount = 0;
        
        MappingEntity mappingEntity = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException(mappingId));
        String mappingType = mappingEntity.getType();
        
        log.info("Append archive terms to mapping. Archive: {} Mapping: {} Type: {}", 
                archiveId, mappingId, mappingType);
        
        EdmArchiveEntity edmArchiveEntity = edmArchiveRepository.findById(archiveId)
                .orElseThrow(() -> new ResourceNotFoundException(archiveId));
        
        EdmArchiveTermsEntity edmArchiveTermsEntity = edmArchiveTermsRepository.findByArchiveId(archiveId);
        
        List<SubjectTermEntity> mappingSubjectTermEntities = new LinkedList<>();
        List<SpatialTermEntity> mappingSpatialTermEntities = new LinkedList<>();
        List<TemporalTermEntity> mappingTemporalTermEntities = new LinkedList<>();
        
        List<SubjectTermEntity> archiveSubjectTermEntities = new LinkedList<>();
        List<SpatialTermEntity> archiveSpatialTermEntities = new LinkedList<>();
        List<TemporalTermEntity> archiveTemporalTermEntities = new LinkedList<>();
        if(edmArchiveTermsEntity != null) {
        
            try { 
                ObjectMapper mapper = new ObjectMapper();
                switch(mappingType) {
                    case MappingType.MAPPING_TYPE_SUBJECT:
                        archiveSubjectTermEntities = mapper.readValue(edmArchiveTermsEntity.getSubjectTerms(),
                                new TypeReference<List<SubjectTermEntity>>(){});
                        
                        mappingSubjectTermEntities = subjectTermRepository.findByMappingId(mappingId);
                        
                        log.info("ArchiveSubjectTerms: {}, MappingSubjectTerms: {}", 
                                archiveSubjectTermEntities.size(), mappingSubjectTermEntities.size());
                        break;
                    case MappingType.MAPPING_TYPE_SPATIAL:
                        archiveSpatialTermEntities = mapper.readValue(edmArchiveTermsEntity.getSpatialTerms(),
                                new TypeReference<List<SpatialTermEntity>>(){});
                        
                        mappingSpatialTermEntities = spatialTermRepository.findByMappingId(mappingId);
                        
                        log.info("ArchiveSpatialTerms: {}, MappingSpatialTerms: {}", 
                                archiveSpatialTermEntities.size(), mappingSpatialTermEntities.size());
                        break;
                    case MappingType.MAPPING_TYPE_TEMPORAL:
                        archiveTemporalTermEntities = mapper.readValue(edmArchiveTermsEntity.getTemporalTerms(),
                                new TypeReference<List<TemporalTermEntity>>(){});
                        
                        mappingTemporalTermEntities = temporalTermRepository.findByMappingId(mappingId);
                        
                        log.info("ArchiveTemporalTerms: {}, MappingTemporalTerms: {}", 
                                archiveTemporalTermEntities.size(), mappingTemporalTermEntities.size());
                        break;
                }
            } catch (IOException ex){
                log.warn("Cannot parse Edm Archive Terms. ArchiveID: {} ArchiveIdTerms: {}", archiveId, edmArchiveTermsEntity.getId());
            }
        } else {
            log.warn("Cannot get Edm Archive Terms. ArchiveID: {}", archiveId);
        }
        
        // Add terms to mapping
        if(mappingType.equalsIgnoreCase(MappingType.MAPPING_TYPE_SUBJECT) && !archiveSubjectTermEntities.isEmpty()) {
            
            existingTermCount = mappingSubjectTermEntities.size();
            appendTermCount = archiveSubjectTermEntities.size();
            
            for(SubjectTermEntity term : archiveSubjectTermEntities) {
                term.setMappingId(mappingId);
            }
            
            mappingSubjectTermEntities.addAll(archiveSubjectTermEntities);
            subjectTermRepository.saveAll(mappingSubjectTermEntities);
            
            
        } else if(mappingType.equalsIgnoreCase(MappingType.MAPPING_TYPE_SPATIAL) && !archiveSpatialTermEntities.isEmpty()) {
            
            existingTermCount = mappingSpatialTermEntities.size();
            appendTermCount = archiveSpatialTermEntities.size();
            
            for(SpatialTermEntity term : archiveSpatialTermEntities) {
                term.setMappingId(mappingId);
            }
            
            mappingSpatialTermEntities.addAll(archiveSpatialTermEntities);
            spatialTermRepository.saveAll(mappingSpatialTermEntities);
        } else if(mappingType.equalsIgnoreCase(MappingType.MAPPING_TYPE_TEMPORAL) && !archiveTemporalTermEntities.isEmpty()) {
            
            existingTermCount = mappingTemporalTermEntities.size();
            appendTermCount = archiveTemporalTermEntities.size();
            
            for(TemporalTermEntity term : archiveTemporalTermEntities) {
                term.setMappingId(mappingId);
            }
            mappingTemporalTermEntities.addAll(archiveTemporalTermEntities);
            temporalTermRepository.saveAll(mappingTemporalTermEntities);
        }
        
        appendTermResult.setExistingTermCount(existingTermCount);
        appendTermResult.setAppendTermCount(appendTermCount);
        
        return appendTermResult;
    }
}
