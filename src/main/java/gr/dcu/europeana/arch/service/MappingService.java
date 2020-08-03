package gr.dcu.europeana.arch.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.dcu.europeana.arch.api.dto.AppendTermsResult;
import gr.dcu.europeana.arch.api.dto.EnrichDetails;
import gr.dcu.europeana.arch.domain.MappingType;
import gr.dcu.europeana.arch.domain.entity.*;
import gr.dcu.europeana.arch.exception.ResourceNotFoundException;
import gr.dcu.europeana.arch.repository.*;
import gr.dcu.utils.XMLUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MappingService {

    @Autowired
    private MappingRepository mappingRepo;

    @Autowired
    private SubjectTermRepository subjectTermRepo;

    @Autowired
    private SpatialTermRepository spatialTermRepo;

    @Autowired
    private TemporalTermRepository temporalTermRepo;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private ExportRequestRepository exportRequestRepo;

    @Autowired
    private UploadRequestRepository uploadRequestRepo;

    @Autowired
    private EnrichRequestRepository enrichRequestRepo;

    @Autowired
    private EdmArchiveRepository edmArchiveRepo;

    @Autowired
    private EdmArchiveTermsRepository edmArchiveTermsRepo;

    @Autowired
    private VocabularyService vocabularyService;

    /**
     * @return
     */
    public List<MappingEntity> findAll() {

        return mappingRepo.findAll();
    }

    /**
     * Find all mappings by user id
     * @param userId the user id
     * @return list of mappings
     */
    public List<MappingEntity> findAllByUserId(int userId) {

        return mappingRepo.findAllByCreatedBy(userId);
    }

    public MappingEntity findById(Long id) {
        return mappingRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    }

    /**
     * Save a new mapping
     */
    public MappingEntity save(int userId, MappingEntity mappingEntity) {

        mappingEntity.setCreatedBy(userId);

        return mappingRepo.save(mappingEntity);
    }

    /**
     * Update an existing mapping
     * @param id the mapping id
     * @param mappingEntity the updated mapping
     * @return updated mapping
     */
    public MappingEntity updateMapping(Long id, MappingEntity mappingEntity) {

        MappingEntity existingMappingEntity = mappingRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        existingMappingEntity.setLabel(mappingEntity.getLabel());
        existingMappingEntity.setDescription(mappingEntity.getDescription());
        existingMappingEntity.setLanguage(mappingEntity.getLanguage());
        existingMappingEntity.setProviderName(mappingEntity.getProviderName());
        existingMappingEntity.setVocabularyName(mappingEntity.getVocabularyName());

        return mappingRepo.save(existingMappingEntity);
    }

    /**
     * @param userId
     * @param mappingId
     */
    @Transactional
    public void delete(int userId, long mappingId) {

        // Check if mapping exists
        MappingEntity mappingEntity = mappingRepo.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException(mappingId));

        // Delete terms
        switch (mappingEntity.getType()) {
            case MappingType.MAPPING_TYPE_SUBJECT:
                subjectTermRepo.deleteByMappingId(mappingId);
                break;
            case MappingType.MAPPING_TYPE_SPATIAL:
                spatialTermRepo.deleteByMappingId(mappingId);
                break;
            case MappingType.MAPPING_TYPE_TEMPORAL:
                temporalTermRepo.deleteByMappingId(mappingId);
                break;
            default:
                log.warn("Unknown mapping type.");
        }

        // Delete mapping
        mappingRepo.deleteById(mappingId);
    }

    /**
     * @param userId
     * @param mappingId
     * @return
     * @throws IOException
     */
    public Resource exportTerms(long mappingId, int userId) throws IOException {

        Resource resource;

        try {
            // Check if mapping exists
            MappingEntity mappingEntity = mappingRepo.findById(mappingId)
                    .orElseThrow(() -> new ResourceNotFoundException(mappingId));

            // Export to tmp file
            Path filePath = fileStorageService.buildMappingExportFilePath(mappingId);

            switch (mappingEntity.getType()) {
                case MappingType.MAPPING_TYPE_SUBJECT:
                    // Get terms
                    List<SubjectTermEntity> termList = subjectTermRepo.findByMappingId(mappingId);
                    log.info("Load terms. Mapping: {} | #Terms: {}", mappingId, termList.size());

                    // Export
                    ExcelService.exportSubjectTermsToExcel(filePath, termList);
                    break;
                case MappingType.MAPPING_TYPE_SPATIAL:
                    List<SpatialTermEntity> spatialTermEntityList = spatialTermRepo.findByMappingId(mappingId);
                    log.info("Load terms. Mapping: {} | #Terms: {}", mappingId, spatialTermEntityList.size());
                    ExcelService.exportSpatialTermsToExcel(filePath, spatialTermEntityList);
                    break;
                case MappingType.MAPPING_TYPE_TEMPORAL:
                    List<TemporalTermEntity> temporalTermEntityList = temporalTermRepo.findByMappingId(mappingId);
                    log.info("Load terms. Mapping: {} | #Terms: {}", mappingId, temporalTermEntityList.size());

                    // Export
                    ExcelService.exportTemporalTermsToExcel(filePath, temporalTermEntityList);
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
            exportRequestRepo.save(exportRequest);

        } catch (IOException ex) {
            throw ex;
        }

        return resource;

    }


    /**
     * @param mappingId
     * @param file
     * @param userId
     * @return
     * @throws IOException
     */
    public List<SubjectTermEntity> uploadSubjectTerms(long mappingId, MultipartFile file, int userId) throws IOException {

        // Check existemce of mapping
        MappingEntity mappingEntity = mappingRepo.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException(mappingId));

        // Upload mapping file
        Path filePath = fileStorageService.buildMappingUploadFilePath(mappingId, file.getOriginalFilename());
        fileStorageService.upload(filePath, file);

        log.info("Analyzing terms...");
        // Load aat subject stored in database
        Map<String, AatSubjectEntity> aatSubjectEntityMap = vocabularyService.loadAatTerms();

        // Load terms from excel
        List<SubjectTermEntity> subjectTermEntityList =
                excelService.loadSubjectTermsFromExcel(filePath.toString(), mappingId, 1, -1);

        long termsWoMappingCount = 0;
        long termsUnknownCount = 0;
        List<String> aatUidUnknownList = new LinkedList<>();
        for (SubjectTermEntity tmpSubjectTermEntity : subjectTermEntityList) {
            String tmpAatUuid = tmpSubjectTermEntity.getAatUid();
            if (StringUtils.isBlank(tmpAatUuid)) {
                termsWoMappingCount++;
            } else {
                if (!aatSubjectEntityMap.containsKey(tmpAatUuid)) {
                    termsUnknownCount++;
                    aatUidUnknownList.add(tmpAatUuid);
                }
            }
        }
        log.info("#Total: {}, #WO Mapping: {}, #Unkmown: {}",
                subjectTermEntityList.size(), termsWoMappingCount, termsUnknownCount);

        // Print unknown aatUid
        aatUidUnknownList.forEach(log::info);

        // Save terms
        log.info("Saving terms. #Total Terms:{}", subjectTermEntityList.size());

        // TODO: Do not save automatically. Preview first.
        subjectTermRepo.saveAll(subjectTermEntityList);

        // Create upload enrichRequest
        MappingUploadRequestEntity uploadRequest = new MappingUploadRequestEntity();
        uploadRequest.setMappingId(mappingId);
        uploadRequest.setFilename(file.getOriginalFilename());
        uploadRequest.setFilepath(filePath.toString());
        uploadRequest.setCreatedBy(userId);
        uploadRequestRepo.save(uploadRequest);

        return subjectTermEntityList;
    }

    /**
     * @param mappingId
     * @param file
     * @param userId
     * @return
     * @throws IOException
     */
    public List<SpatialTermEntity> uploadSpatialTerms(long mappingId, MultipartFile file, int userId) throws IOException {

        // Check existemce of mapping
        MappingEntity mappingEntity = mappingRepo.findById(mappingId)
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
        spatialTermRepo.saveAll(termList);

        // Create upload enrichRequest
        MappingUploadRequestEntity uploadRequest = new MappingUploadRequestEntity();
        uploadRequest.setMappingId(mappingId);
        uploadRequest.setFilename(file.getOriginalFilename());
        uploadRequest.setFilepath(filePath.toString());
        uploadRequest.setCreatedBy(userId);
        uploadRequestRepo.save(uploadRequest);

        return termList;
    }

    public List<TemporalTermEntity> uploadTemporalTerms(long mappingId, MultipartFile file, int userId) throws IOException {

        // Check existemce of mapping
        MappingEntity mappingEntity = mappingRepo.findById(mappingId)
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
        temporalTermRepo.saveAll(termList);

        // Create upload enrichRequest
        MappingUploadRequestEntity uploadRequest = new MappingUploadRequestEntity();
        uploadRequest.setMappingId(mappingId);
        uploadRequest.setFilename(file.getOriginalFilename());
        uploadRequest.setFilepath(filePath.toString());
        uploadRequest.setCreatedBy(userId);
        uploadRequestRepo.save(uploadRequest);

        return termList;
    }


    /**
     * @param requestId
     * @return
     * @throws IOException
     */
    public File loadEnrichedArchive(long requestId) throws IOException {

        EnrichRequestEntity enrichRequestEntity = enrichRequestRepo.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(requestId));

        String filename = enrichRequestEntity.getFilename();
        String filepath = enrichRequestEntity.getEnrichedFilepath();

        File file = fileStorageService.loadFile(filepath);

        return file;
    }

    /**
     * Create a mapping based on extracted terms from EDM archive.
     *
     * @param archiveId
     * @param type
     * @param userId
     * @return
     */
    @Transactional
    public MappingEntity createMappingByArchiveId(Long archiveId, String type, Integer userId) {

        EdmArchiveEntity edmArchiveEntity = edmArchiveRepo.findById(archiveId)
                .orElseThrow(() -> new ResourceNotFoundException(archiveId));

        EdmArchiveTermsEntity edmArchiveTermsEntity = edmArchiveTermsRepo.findByArchiveId(archiveId);

        log.info("Create mapping for archive. Archive: {} Type: {}", archiveId, type);
        List<SubjectTermEntity> subjectTermEntities = new LinkedList<>();
        List<SpatialTermEntity> spatialTermEntities = new LinkedList<>();
        List<TemporalTermEntity> temporalTermEntities = new LinkedList<>();
        if (edmArchiveTermsEntity != null) {

            try {
                ObjectMapper mapper = new ObjectMapper();
                switch (type) {
                    case MappingType.MAPPING_TYPE_SUBJECT:
                        subjectTermEntities = mapper.readValue(edmArchiveTermsEntity.getSubjectTerms(),
                                new TypeReference<List<SubjectTermEntity>>() {
                                });
                        break;
                    case MappingType.MAPPING_TYPE_SPATIAL:
                        spatialTermEntities = mapper.readValue(edmArchiveTermsEntity.getSpatialTerms(),
                                new TypeReference<List<SpatialTermEntity>>() {
                                });
                        break;
                    case MappingType.MAPPING_TYPE_TEMPORAL:
                        temporalTermEntities = mapper.readValue(edmArchiveTermsEntity.getTemporalTerms(),
                                new TypeReference<List<TemporalTermEntity>>() {
                                });
                        break;
                }
            } catch (IOException ex) {
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
        long mappingId = mappingRepo.save(mappingEntity).getId();
        log.info("Mapping created. MappingId:{}", mappingId);

        // Add terms to mapping
        if (type.equalsIgnoreCase(MappingType.MAPPING_TYPE_SUBJECT) && !subjectTermEntities.isEmpty()) {

            log.info("Add terms to mappings. Size: {}", subjectTermEntities.size());

            for (SubjectTermEntity term : subjectTermEntities) {
                term.setMappingId(mappingId);
            }
            subjectTermRepo.saveAll(subjectTermEntities);
            edmArchiveEntity.setThematicMapping(mappingId);
        } else if (type.equalsIgnoreCase(MappingType.MAPPING_TYPE_SPATIAL) && !spatialTermEntities.isEmpty()) {

            log.info("Add terms to mappings. Size: {}", spatialTermEntities.size());

            for (SpatialTermEntity term : spatialTermEntities) {
                term.setMappingId(mappingId);
            }
            spatialTermRepo.saveAll(spatialTermEntities);
            edmArchiveEntity.setSpatialMapping(mappingId);
        } else if (type.equalsIgnoreCase(MappingType.MAPPING_TYPE_TEMPORAL) && !temporalTermEntities.isEmpty()) {

            log.info("Add terms to mappings. Size: {}", temporalTermEntities.size());

            for (TemporalTermEntity term : temporalTermEntities) {
                term.setMappingId(mappingId);
            }
            temporalTermRepo.saveAll(temporalTermEntities);
            edmArchiveEntity.setTemporalMapping(mappingId);
        }

        edmArchiveRepo.save(edmArchiveEntity);

        return mappingEntity;
    }

    @Transactional
    public AppendTermsResult appendTermsToMappingByArchiveId(Long mappingId, Long archiveId, Integer userId) {

        AppendTermsResult appendTermResult = new AppendTermsResult();
        long mappingTermCount = 0;
        long archiveTermCount = 0;
        long appendTermCount = 0;

        MappingEntity mappingEntity = mappingRepo.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException(mappingId));
        String mappingType = mappingEntity.getType();

        log.info("Append archive terms to mapping. Archive: {} Mapping: {} Type: {}",
                archiveId, mappingId, mappingType);

        EdmArchiveEntity edmArchiveEntity = edmArchiveRepo.findById(archiveId)
                .orElseThrow(() -> new ResourceNotFoundException(archiveId));

        EdmArchiveTermsEntity edmArchiveTermsEntity = edmArchiveTermsRepo.findByArchiveId(archiveId);

        List<SubjectTermEntity> mappingSubjectTermEntities = new LinkedList<>();
        List<SpatialTermEntity> mappingSpatialTermEntities = new LinkedList<>();
        List<TemporalTermEntity> mappingTemporalTermEntities = new LinkedList<>();

        Set<String> nativeTermsInSubjectMapping = new HashSet<>();
        Set<String> nativeTermsInSpatialMapping = new HashSet<>();
        Set<String> nativeTermsInTemporalMapping = new HashSet<>();

        List<SubjectTermEntity> archiveSubjectTermEntities = new LinkedList<>();
        List<SpatialTermEntity> archiveSpatialTermEntities = new LinkedList<>();
        List<TemporalTermEntity> archiveTemporalTermEntities = new LinkedList<>();
        if (edmArchiveTermsEntity != null) {

            try {
                ObjectMapper mapper = new ObjectMapper();
                switch (mappingType) {
                    case MappingType.MAPPING_TYPE_SUBJECT:
                        archiveSubjectTermEntities = mapper.readValue(edmArchiveTermsEntity.getSubjectTerms(),
                                new TypeReference<List<SubjectTermEntity>>() {
                                });

                        mappingSubjectTermEntities = subjectTermRepo.findByMappingId(mappingId);

                        nativeTermsInSubjectMapping = mappingSubjectTermEntities.stream()
                                .map(SubjectTermEntity::getNativeTerm)
                                .collect(Collectors.toSet());

                        log.info("ArchiveSubjectTerms: {}, MappingSubjectTerms: {}",
                                archiveSubjectTermEntities.size(), mappingSubjectTermEntities.size());
                        break;
                    case MappingType.MAPPING_TYPE_SPATIAL:
                        archiveSpatialTermEntities = mapper.readValue(edmArchiveTermsEntity.getSpatialTerms(),
                                new TypeReference<List<SpatialTermEntity>>() {
                                });

                        mappingSpatialTermEntities = spatialTermRepo.findByMappingId(mappingId);

                        nativeTermsInSpatialMapping = mappingSpatialTermEntities.stream()
                                .map(SpatialTermEntity::getNativeTerm)
                                .collect(Collectors.toSet());

                        log.info("ArchiveSpatialTerms: {}, MappingSpatialTerms: {}",
                                archiveSpatialTermEntities.size(), mappingSpatialTermEntities.size());
                        break;
                    case MappingType.MAPPING_TYPE_TEMPORAL:
                        archiveTemporalTermEntities = mapper.readValue(edmArchiveTermsEntity.getTemporalTerms(),
                                new TypeReference<List<TemporalTermEntity>>() {
                                });

                        mappingTemporalTermEntities = temporalTermRepo.findByMappingId(mappingId);

                        nativeTermsInTemporalMapping = mappingTemporalTermEntities.stream()
                                .map(TemporalTermEntity::getNativeTerm)
                                .collect(Collectors.toSet());

                        log.info("ArchiveTemporalTerms: {}, MappingTemporalTerms: {}",
                                archiveTemporalTermEntities.size(), mappingTemporalTermEntities.size());
                        break;
                }
            } catch (IOException ex) {
                log.warn("Cannot parse Edm Archive Terms. ArchiveID: {} ArchiveIdTerms: {}", archiveId, edmArchiveTermsEntity.getId());
            }
        } else {
            log.warn("Cannot get Edm Archive Terms. ArchiveID: {}", archiveId);
        }

        // Add terms to mapping
        if (mappingType.equalsIgnoreCase(MappingType.MAPPING_TYPE_SUBJECT) && !archiveSubjectTermEntities.isEmpty()) {

            mappingTermCount = mappingSubjectTermEntities.size();
            archiveTermCount = archiveSubjectTermEntities.size();

            // Append to mapping only new native terms
            // log.info("{}", nativeTermsInSubjectMapping);
            List<SubjectTermEntity> archiveSubjectTermEntitiesFiltered = new LinkedList<>();
            for (SubjectTermEntity term : archiveSubjectTermEntities) {
                // log.info("--> {}", term.getNativeTerm());
                if (nativeTermsInSubjectMapping.contains(term.getNativeTerm())) {
                    continue;
                }
                term.setMappingId(mappingId);
                archiveSubjectTermEntitiesFiltered.add(term);
            }

            // mappingSubjectTermEntities.addAll(archiveSubjectTermEntitiesFiltered);
            subjectTermRepo.saveAll(archiveSubjectTermEntitiesFiltered);

            appendTermCount = archiveSubjectTermEntitiesFiltered.size();

            edmArchiveEntity.setThematicMapping(mappingId);

        } else if (mappingType.equalsIgnoreCase(MappingType.MAPPING_TYPE_SPATIAL) && !archiveSpatialTermEntities.isEmpty()) {

            mappingTermCount = mappingSpatialTermEntities.size();
            archiveTermCount = archiveSpatialTermEntities.size();

            // Append to mapping only new native terms
            List<SpatialTermEntity> archiveSpatialTermEntitiesFiltered = new LinkedList<>();
            for (SpatialTermEntity term : archiveSpatialTermEntities) {
                if (nativeTermsInSpatialMapping.contains(term.getNativeTerm())) {
                    continue;
                }
                term.setMappingId(mappingId);
                archiveSpatialTermEntitiesFiltered.add(term);
            }

            // mappingSpatialTermEntities.addAll(archiveSpatialTermEntities);
            spatialTermRepo.saveAll(archiveSpatialTermEntitiesFiltered);

            appendTermCount = archiveSpatialTermEntitiesFiltered.size();

            edmArchiveEntity.setSpatialMapping(mappingId);

        } else if (mappingType.equalsIgnoreCase(MappingType.MAPPING_TYPE_TEMPORAL) && !archiveTemporalTermEntities.isEmpty()) {

            mappingTermCount = mappingTemporalTermEntities.size();
            archiveTermCount = archiveTemporalTermEntities.size();

            // Append to mapping only new native terms
            List<TemporalTermEntity> archiveTemporalTermEntitiesFiltered = new LinkedList<>();
            for (TemporalTermEntity term : archiveTemporalTermEntities) {
                if (nativeTermsInTemporalMapping.contains(term.getNativeTerm())) {
                    continue;
                }
                term.setMappingId(mappingId);
                archiveTemporalTermEntitiesFiltered.add(term);
            }
            // mappingTemporalTermEntities.addAll(archiveTemporalTermEntities);
            temporalTermRepo.saveAll(archiveTemporalTermEntitiesFiltered);

            appendTermCount = archiveTemporalTermEntitiesFiltered.size();

            edmArchiveEntity.setTemporalMapping(mappingId);
        }

        // edmArchiveRepository.save(edmArchiveEntity);

        appendTermResult.setMappingTermCount(mappingTermCount);
        appendTermResult.setArchiveTermCount(archiveTermCount);
        appendTermResult.setAppendTermCount(appendTermCount);

        log.info("#MappingTerms: {}, #ArchiveTerms: {}, #AppendTerms: {}", mappingTermCount, archiveTermCount, appendTermCount);

        return appendTermResult;
    }

    /**
     * Load thematic terms
     *
     * @param mappingId the mapping id
     * @return a map with terms
     */
    public Map<String, SubjectTermEntity> loadThematicTerms(long mappingId) {
        Map<String, SubjectTermEntity> subjectTermsMap = new HashMap<>();

        // Get mapping subjectTerms. Convert them to map
        List<SubjectTermEntity> subjectTermEntities = subjectTermRepo.findByMappingId(mappingId);
        for (SubjectTermEntity mp : subjectTermEntities) {
            subjectTermsMap.put(mp.getNativeTerm(), mp);
        }
        log.info("Thematic mapping loaded. MappingId: {} #Terms: {}", mappingId, subjectTermEntities.size());

        return subjectTermsMap;
    }

    /**
     * Load spatial terms
     *
     * @param mappingId the mapping id
     * @return a map with terms
     */
    public Map<String, SpatialTermEntity> loadSpatialTerms(long mappingId) {

        Map<String, SpatialTermEntity> spatialTermsMap = new HashMap<>();

        List<SpatialTermEntity> spatialTermEntities = spatialTermRepo.findByMappingId(mappingId);
        for (SpatialTermEntity mp : spatialTermEntities) {
            spatialTermsMap.put(mp.getNativeTerm(), mp);
        }
        log.info("Spatial mapping loaded. MappingId: {} #Terms: {}", mappingId, spatialTermEntities.size());

        return spatialTermsMap;
    }

    /**
     * Load temporal terms
     *
     * @param mappingId the mapping id
     * @return a map with terms
     */
    public Map<String, TemporalTermEntity> loadTemporalTerms(long mappingId) {
        Map<String, TemporalTermEntity> temporalTermsMap = new HashMap<>();

        List<TemporalTermEntity> temporalTermEntities = temporalTermRepo.findByMappingId(mappingId);
        for (TemporalTermEntity mp : temporalTermEntities) {
            temporalTermsMap.put(mp.getNativeTerm(), mp);
        }
        log.info("Temporal mapping loaded. MappingId: {} #Terms: {}", mappingId, temporalTermEntities.size());

        return temporalTermsMap;
    }


    /**
     * @param mappingId
     * @param file
     * @return
     * @throws IOException
     */
    @Deprecated
    public EnrichDetails enrich(long mappingId, MultipartFile file, int userId) throws IOException {

        // Check existemce of mapping
        MappingEntity mappingEntity = mappingRepo.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException(mappingId));

        // Get mapping terms. Convert them to map
        List<SubjectTermEntity> terms = subjectTermRepo.findByMappingId(mappingId);
        Map<String, SubjectTermEntity> termsMap = new HashMap<>();
        for (SubjectTermEntity mp : terms) {
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
        enrichRequestEntity = enrichRequestRepo.save(enrichRequestEntity);
        long requestId = enrichRequestEntity.getId();

        // Upload EDM archive
        log.info("Upload EDM archive. RequestId: {}", requestId);
        Path edmArchiveFilePath = fileStorageService.buildUploadEdmArchiveFilePath(mappingId, file.getOriginalFilename(), requestId);
        fileStorageService.upload(edmArchiveFilePath, file);
        log.info("EDM archive uploaded. Path: {}", edmArchiveFilePath);

        // Update enrich enrichRequest
        enrichRequestEntity.setFilepath(edmArchiveFilePath.toString());
        enrichRequestEntity = enrichRequestRepo.save(enrichRequestEntity);

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
        for (File edmFile : edmFiles) {
            if (edmFile.exists() && edmFile.isFile()) {

                try {
                    // Retrieve xml content. ATTENTION: Namespace aware is true.
                    Document doc = XMLUtils.parse(edmFile, true);
                    // String itemContent = XMLUtils.transform(doc);

                    // Get subjects
                    List<String> subjectValues = XMLUtils.getElementValues(doc, "//dc:subject");

                    // Find subject mapppings (if any)
                    int termMatchCount = 0;
                    List<String> subjectMappings = new LinkedList<>();
                    for (String value : subjectValues) {
                        if (termsMap.containsKey(value)) {
                            termMatchCount++;
                            subjectMappings.add(value);
                        }
                    }

                    // edmFile.getAbsolutePath()
                    log.info("File: {} #Subjects: {} #Matches: {}", edmFile.getName(), subjectValues.size(), termMatchCount);

                    if (!subjectMappings.isEmpty()) {

                        // Add subject mappings
                        doc = XMLUtils.appendElements(doc, "//edm:ProvidedCHO", "dc:subject", subjectMappings);

                        // Create enriched directory (if not)
                        if (!enrichedDirCreated) {
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
                } catch (ParserConfigurationException | SAXException |
                        TransformerException | XPathExpressionException ex) {
                    log.error("Cannot parse file. File: {}", edmFile.getAbsolutePath());
                }

            }
        }

        // Create archive with enriched files
        String enrichedArchiveFilePath = "";
        String enrichedArchiveName = "";
        if (enrichedFileCount > 0) {
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
        enrichRequestRepo.save(enrichRequestEntity);

        return enrichDetails;
    }

    // public void

}
