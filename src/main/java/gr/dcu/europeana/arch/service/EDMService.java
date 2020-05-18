package gr.dcu.europeana.arch.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.dcu.europeana.arch.api.dto.EnrichDetails;
import gr.dcu.europeana.arch.api.dto.ExtractTermResult;
import gr.dcu.europeana.arch.domain.EnrichmentResult;
import gr.dcu.europeana.arch.exception.NotFoundException;
import gr.dcu.europeana.arch.exception.ResourceNotFoundException;
import gr.dcu.europeana.arch.model.*;
import gr.dcu.europeana.arch.model.EdmArchiveTermsEntity;
import gr.dcu.europeana.arch.model.mappers.SpatialTermMapper;
import gr.dcu.europeana.arch.model.mappers.SubjectTermMapper;
import gr.dcu.europeana.arch.model.mappers.TemporalTermMapper;
import gr.dcu.europeana.arch.repository.*;
import gr.dcu.europeana.arch.service.edm.*;
import gr.dcu.utils.XMLUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.nio.file.Files;
import java.util.stream.Collectors;
import javax.xml.transform.TransformerException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class EDMService {
    
    private final UploadRequestRepository uploadRequestRepository;
    private final EdmArchiveRepository edmArchiveRepository;
    private final EdmArchiveTermsRepository edmArchiveTermsRepository;
    private final SubjectTermRepository subjectTermRepository;
    private final SpatialTermRepository spatialTermRepository;
    private final TemporalTermRepository temporalTermRepository;
    private final AatSubjectRepository aatSubjectRepository;
    private final FileStorageService fileStorageService;
    private final SubjectTermMapper subjectTermMapper;
    private final SpatialTermMapper spatialTermMapper;
    private final TemporalTermMapper temporalTermMapper;
    private final EArchTemporalEntityRepository eArchTemporalEntityRepo;

    public EDMService(UploadRequestRepository uploadRequestRepository, EdmArchiveRepository edmArchiveRepository,
                      EdmArchiveTermsRepository edmArchiveTermsRepository,
                      SubjectTermRepository subjectTermRepository, SpatialTermRepository spatialTermRepository,
                      TemporalTermRepository temporalTermRepository, AatSubjectRepository aatSubjectRepository,
                      FileStorageService fileStorageService,
                      SubjectTermMapper subjectTermMapper, SpatialTermMapper spatialTermMapper, TemporalTermMapper temporalTermMapper,
                      EArchTemporalEntityRepository eArchTemporalEntityRepo) {
        this.uploadRequestRepository = uploadRequestRepository;
        this.edmArchiveRepository = edmArchiveRepository;
        this.edmArchiveTermsRepository = edmArchiveTermsRepository;
        this.subjectTermRepository = subjectTermRepository;
        this.spatialTermRepository = spatialTermRepository;
        this.temporalTermRepository = temporalTermRepository;
        this.aatSubjectRepository = aatSubjectRepository;
        this.fileStorageService = fileStorageService;
        this.subjectTermMapper = subjectTermMapper;
        this.spatialTermMapper = spatialTermMapper;
        this.temporalTermMapper = temporalTermMapper;
        this.eArchTemporalEntityRepo = eArchTemporalEntityRepo;
    }

    public List<EdmArchiveEntity> getEdmArchives(Integer userId) {
        return edmArchiveRepository.findAllByCreatedBy(userId);
    }
    
    
    public EdmArchiveEntity getEdmArchive(Long id) {
        return edmArchiveRepository.findById(id).orElseThrow(()
                -> new NotFoundException("EDM Upload", id));
    }
    
    @Transactional
    public EdmArchiveEntity uploadEdmArchiveAndExtractFiles(MultipartFile file, int userId) throws IOException {
        
        // File system hierarchy
        // storage_tmp/europeana_Arch
        // -- packages
        //    -- p_i
        
        
        // -- DEPRECATED -- // 
        //    -- enrich 
        //        -- <request_id> (at this level store the archives - edm.zip , eEDM.tar.gz)
        //            -- EDM
        //            -- eEDM
        
        // Create an upload request
        EdmArchiveEntity edmArchiveEntity = new EdmArchiveEntity();
        edmArchiveEntity.setName(file.getOriginalFilename());
        edmArchiveEntity.setItemCount(0);
        edmArchiveEntity.setFilename(file.getOriginalFilename());
        edmArchiveEntity.setFilepath("");
        edmArchiveEntity.setThematicMapping(0L);
        edmArchiveEntity.setSpatialMapping(0L);
        edmArchiveEntity.setTemporalMapping(0L);
        edmArchiveEntity.setCreatedBy(userId);
        long edmUploadId = edmArchiveRepository.save(edmArchiveEntity).getId();
        
        // Upload EDM archive
        log.info("Upload EDM archive. RequestId: {}", edmUploadId);
        Path edmArchiveFilePath = fileStorageService.buildEdmArchiveUploadFilePathNew(edmUploadId, file.getOriginalFilename());
        fileStorageService.upload(edmArchiveFilePath, file);
        log.info("EDM archive uploaded. Path: {}", edmArchiveFilePath);
        
        // Extract EDM archive
        log.info("Extract EDM archive. RequestId: {}", edmUploadId);
        Path edmExtractDirPath = Paths.get(edmArchiveFilePath.getParent().toString(), "EDM");
        fileStorageService.extractArchive(edmArchiveFilePath, edmExtractDirPath);
        File[] edmFiles = edmExtractDirPath.toFile().listFiles();
        int edmFilesCount = edmFiles != null ? edmFiles.length : 0;
        log.info("EDM archive extracted. Path: {} #Files: {}", edmExtractDirPath, edmFilesCount);
        
        // Update upload request
        edmArchiveEntity.setId(edmUploadId);
        edmArchiveEntity.setFilepath(edmArchiveFilePath.toString());
        edmArchiveEntity.setEnrichedFilepath(null);
        edmArchiveEntity.setItemCount(edmFilesCount);
        edmArchiveEntity = edmArchiveRepository.save(edmArchiveEntity);
        
        return edmArchiveEntity;
    }
    
    /**
     * Load original EDM or eEDM (if exists)
     * @param archiveId the archive id
     * @param type eEDM
     * @return file archive
     */
    public File loadEdmArchive(long archiveId, String type) throws IOException {
        
        EdmArchiveEntity edmArchiveEntity = edmArchiveRepository.findById(archiveId)
                .orElseThrow(() -> new ResourceNotFoundException(archiveId));
        
        String filepath;
        // String filename =  edmArchive.getFilename();
        if(type != null && type.equalsIgnoreCase("eEDM")) {
            filepath = edmArchiveEntity.getEnrichedFilepath();
        } else {
            filepath = edmArchiveEntity.getFilepath();
        }
        
        File file;
        if(filepath != null && !filepath.isEmpty()) {
            file = fileStorageService.loadFile(filepath);
            log.info("Load file. ArchiveId: {} Type: {} Name: {} Path: {}", archiveId, type, file.getName(), filepath);
        } else {
            log.error("File not found. ArchiveId: {} Type: {}", archiveId, type);
            throw new NotFoundException("File");
        }
        
        return file;
    }

    public ExtractTermResult extractAndSaveTermsFromEdmArchive(Long edmArchiveId, int userId) {

        ExtractTermResult extractTermResult = new ExtractTermResult();

        // Extract terms
        List<EdmFileTermExtractionResult> extractionResult = extractTermsFromEdmArchive(edmArchiveId);

        // Create separate categories(thematic, spatial, temporal)
        ElementExtractionDataCategories extractionCategories =
                EdmExtractUtils.splitExtractionDataInCategories(extractionResult);

        // Get thematic terms
        Set<ElementExtractionData> thematicElementValues = extractionCategories.getThematicElementValues();
        if(!thematicElementValues.isEmpty()) {
            extractTermResult.setSubjectTermEntities(
                    subjectTermMapper.toSubjectTermList(thematicElementValues, extractionCategories.getThematicElementValuesCountMap())
                    .stream()
                    .sorted(Comparator.comparingInt(SubjectTermEntity::getCount).reversed())
                    .collect(Collectors.toList())
            );
        } else {
            extractTermResult.setSubjectTermEntities(new LinkedList<>());
            log.warn("No thematic terms to extract.");
        }

        Set<ElementExtractionData> spatialElementValues = extractionCategories.getSpatialElementValues();
        if(!spatialElementValues.isEmpty()) {
            extractTermResult.setSpatialTermEntities(spatialTermMapper.toSpatialTermList(spatialElementValues));
        } else {
            extractTermResult.setSpatialTermEntities(new LinkedList<>());
            log.warn("No spatial terms to extract.");
        }

        Set<ElementExtractionData> temporalElementValues = extractionCategories.getTemporalElementValues();
        if(!temporalElementValues.isEmpty()) {
            extractTermResult.setTemporalTermEntities(temporalTermMapper.toTemporalTermList(temporalElementValues));
        } else {
            extractTermResult.setTemporalTermEntities(new LinkedList<>());
            log.warn("No temporal terms to extract.");
        }

        saveTerms(edmArchiveId, extractTermResult, userId);

        return extractTermResult;

    }
    
    public List<EdmFileTermExtractionResult> extractTermsFromEdmArchive(Long edmArchiveId) {
        
        EdmArchiveEntity edmArchiveEntity = edmArchiveRepository.findById(edmArchiveId).orElseThrow(()
                -> new NotFoundException("EDM Archive", edmArchiveId));

        List<EdmFileTermExtractionResult> extractionResult = new LinkedList<>();
        try {
            Path edmExtractDirPath = fileStorageService.buildEdmArchiveExtractionPath(edmArchiveId);
            
            extractionResult = extractTerms(edmExtractDirPath, true, true, true, true);
        } catch(IOException ex) {
            log.error("Cannot extract terms from edm archive.");
            log.error("", ex);
        }
        
        return extractionResult;
    }

    @Transactional
    public EdmArchiveTermsEntity saveTerms(Long archiveId, ExtractTermResult extractTermResult, Integer userId) {
        
        EdmArchiveEntity edmArchiveEntity = edmArchiveRepository.findById(archiveId)
                .orElseThrow(() -> new ResourceNotFoundException(archiveId));
        
        String subjectTermsInJson = null;
        String spatialTermsInJson = null;
        String temporalTermsInJson = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            subjectTermsInJson = mapper.writeValueAsString(extractTermResult.getSubjectTermEntities());
            spatialTermsInJson = mapper.writeValueAsString(extractTermResult.getSpatialTermEntities());
            temporalTermsInJson = mapper.writeValueAsString(extractTermResult.getTemporalTermEntities());
        } catch (JsonProcessingException ex) {
            log.error("");
        }
                
        EdmArchiveTermsEntity edmArchiveTermsEntity = new EdmArchiveTermsEntity();
        edmArchiveTermsEntity.setArchiveId(archiveId);
        edmArchiveTermsEntity.setSubjectTerms(subjectTermsInJson);
        edmArchiveTermsEntity.setSpatialTerms(spatialTermsInJson);
        edmArchiveTermsEntity.setTemporalTerms(temporalTermsInJson);
        edmArchiveTermsEntity.setCreatedBy(userId);
        edmArchiveTermsEntity = edmArchiveTermsRepository.save(edmArchiveTermsEntity);
        
        return edmArchiveTermsEntity;
    }
    
    public ExtractTermResult loadTerms(Long archiveId, Integer userId) {
        
        EdmArchiveEntity edmArchiveEntity = edmArchiveRepository.findById(archiveId)
                .orElseThrow(() -> new ResourceNotFoundException(archiveId));
        
        boolean hasThematicMapping = edmArchiveEntity.getThematicMapping() > 0;
        boolean hasSpatialMapping  = edmArchiveEntity.getSpatialMapping() > 0;
        boolean hasTemporalMapping = edmArchiveEntity.getTemporalMapping() > 0;
        
        EdmArchiveTermsEntity edmArchiveTermsEntity = edmArchiveTermsRepository.findByArchiveId(archiveId);
        
        List<SubjectTermEntity> subjectTermEntities = new LinkedList<>();
        List<SpatialTermEntity> spatialTermEntities = new LinkedList<>();
        List<TemporalTermEntity> temporalTermEntities = new LinkedList<>();
        if(edmArchiveTermsEntity != null) {
            try {
                
                ObjectMapper mapper = new ObjectMapper();
                if(hasThematicMapping) {
                    subjectTermEntities = subjectTermRepository.findByMappingId(edmArchiveEntity.getThematicMapping());
                } else {
                    subjectTermEntities = mapper.readValue(edmArchiveTermsEntity.getSubjectTerms(),
                                    new TypeReference<List<SubjectTermEntity>>(){});
                }
                
                if(hasSpatialMapping) {
                    spatialTermEntities = spatialTermRepository.findByMappingId(edmArchiveEntity.getSpatialMapping());
                } else {
                    spatialTermEntities = mapper.readValue(edmArchiveTermsEntity.getSpatialTerms(),
                                    new TypeReference<List<SpatialTermEntity>>(){});
                }
                
                if(hasTemporalMapping) {
                    temporalTermEntities = temporalTermRepository.findByMappingId(edmArchiveEntity.getTemporalMapping());
                } else {
                    temporalTermEntities = mapper.readValue(edmArchiveTermsEntity.getTemporalTerms(),
                                    new TypeReference<List<TemporalTermEntity>>(){});
                }
                
                /*
                subjectTerms = mapper.readValue(edmArchiveTerms.getSubjectTerms(), 
                                    new TypeReference<List<SubjectTerm>>(){});
                
                spatialTerms = mapper.readValue(edmArchiveTerms.getSpatialTerms(), 
                                    new TypeReference<List<SpatialTerm>>(){});
                temporalTerms = mapper.readValue(edmArchiveTerms.getTemporalTerms(), 
                                    new TypeReference<List<TemporalTerm>>(){});
                */
            } catch (IOException ex) {
                log.warn("Cannot parse Edm Archive Terms. ArchiveID: {} ArchiveIdTerms: {}", archiveId, edmArchiveTermsEntity.getId());
            }
        } else {
            log.warn("Cannot get Edm Archive Terms. ArchiveID: {}", archiveId);
        }
        
        ExtractTermResult extractTermResult = new ExtractTermResult();
        extractTermResult.setSubjectTermEntities(subjectTermEntities);
        extractTermResult.setSpatialTermEntities(spatialTermEntities);
        extractTermResult.setTemporalTermEntities(temporalTermEntities);

        return extractTermResult;
    }

    @Transactional
    public void deleteEdmArchive(Long archiveId) {
        edmArchiveTermsRepository.deleteByArchiveId(archiveId);
        edmArchiveRepository.deleteById(archiveId);
    }

    public void logXmlToString(Document doc) {
        try {
            log.info(XMLUtils.transform(doc));
        } catch (TransformerException | IOException ex) {
            log.error("Error logging Xml to string", ex);
        }
    }

    // Enrich the content of a single EDM file
    public Document enrich(String xmlContent, long thematicMappingId, long spatialMappingId, long temporalMappingId)
            throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {

        String enrichedContent;
        boolean thematicEnrichment = thematicMappingId > 0;

        // Load thematic mapping terms
        Map<String, AatSubjectEntity> aatSubjectMap = new HashMap<>();
        Map<String, SubjectTermEntity> subjectTermsMap = new HashMap<>();
        if(thematicEnrichment) {
            // Get mapping subjectTerms. Convert them to map
            List<SubjectTermEntity> subjectTermEntities = subjectTermRepository.findByMappingId(thematicMappingId);
            for(SubjectTermEntity mp : subjectTermEntities) {
                subjectTermsMap.put(mp.getNativeTerm(), mp);
            }
            log.info("Thematic mapping loaded. MappingId: {} #Terms: {}", thematicMappingId, subjectTermEntities.size());

            // Create aat subject map
            List<AatSubjectEntity> aatSubjectEntities = aatSubjectRepository.findAll();
            for(AatSubjectEntity tmpAatSubjectEntity : aatSubjectEntities) {
                aatSubjectMap.put(tmpAatSubjectEntity.getAatUid(), tmpAatSubjectEntity);
            }
        }

        Document doc = XMLUtils.parse(xmlContent, true);

        // EnrichmentResult thematicEnrichemtnResult =
                thematicEnrichment("on_demand_enrichment", doc, aatSubjectMap, subjectTermsMap);

        // logXmlToString(doc);

        return doc;
    }

    public EnrichmentResult thematicEnrichment(
            String filename, Document doc,
            Map<String, AatSubjectEntity> aatSubjectMap, Map<String, SubjectTermEntity> subjectTermsMap)
            throws XPathExpressionException, NullPointerException{

        EnrichmentResult result = new EnrichmentResult(0, 0);
        try {

            // Get thematic values in EDM file
            List<String> thematicValues =
                    XMLUtils.getElementValues(doc, "//" + EdmExtractUtils.DC_SUBJECT);
            thematicValues.addAll(XMLUtils.getElementValues(doc, "//" + EdmExtractUtils.DC_TYPE));

            // Find matches with subject terms (if any)
            List<SubjectTermEntity> subjectTermMatches = new LinkedList<>(); // List with matches
            for (String tmpThematicValue : thematicValues) {
                if (subjectTermsMap.containsKey(tmpThematicValue)) {
                    subjectTermMatches.add(subjectTermsMap.get(tmpThematicValue));
                }
            }

            log.info("File: {} #Thematic Values: {} #Matches: {}",
                    filename, thematicValues.size(), subjectTermMatches.size());

            // Enrich with subject terms
            if (!subjectTermMatches.isEmpty()) {
                // log.info("Trying to append thematic elements...");
                EdmXmlUtils.appendThematicElements(doc,
                        "//edm:ProvidedCHO", "dc:subject", subjectTermMatches, aatSubjectMap);
            }

            result.setValueCount(thematicValues.size());
            result.setMatchCount(subjectTermMatches.size());

        } catch (XPathExpressionException | NullPointerException ex) {
            log.error("Cannot parse file. File: {}", filename);
            throw ex;
        }

        return result;
    }

    public void spatialEnrichment(String filename, Document doc,
                                  Map<String, SpatialTermEntity> spatialTermsMap)
            throws XPathExpressionException, NullPointerException {
        try {
            List<String> spatialValues =
                    XMLUtils.getElementValues(doc, "//" + EdmExtractUtils.DC_TERMS_SPATIAL);

            List<SpatialTermEntity> spatialTermsMatches = new LinkedList<> ();
            for(String tmpSpatialValue : spatialValues) {
                if(spatialTermsMap.containsKey(tmpSpatialValue)) {
                    //subjectTermMatchCount++;
                    // subjectMappings.add(dcSubjectValue);
                    spatialTermsMatches.add(spatialTermsMap.get(tmpSpatialValue));
                }
            }
            // edmFile.getAbsolutePath()
            log.info("File: {} #Spatial Values: {} #Matches: {}",
                    filename, spatialValues.size(), spatialTermsMatches.size());

            // Enrich with spatial mappings
            if(!spatialTermsMatches.isEmpty()) {
                EdmXmlUtils.appendSpatialElements(doc,
                        "//edm:ProvidedCHO", "dcterms:spatial", spatialTermsMatches);
            }
        } catch (XPathExpressionException | NullPointerException ex) {
            log.error("Cannot parse file. File: {}", filename);
            throw ex;
        }

    }

    public void temporalEnrichment(String filename, Document doc,
                                   Map<String, EArchTemporalEntity> earchTemporalEntityMap,
                                   Map<String, TemporalTermEntity> temporalTermsMap)
            throws XPathExpressionException, NullPointerException{

        try {
            List<String> temporalValues =
                    XMLUtils.getElementValues(doc, "//" + EdmExtractUtils.DC_DATE);

            temporalValues.addAll(XMLUtils.getElementValues(doc, "//" + EdmExtractUtils.DC_TERMS_TEMPORAL));
            temporalValues.addAll(XMLUtils.getElementValues(doc, "//" + EdmExtractUtils.DC_TERMS_CREATED));

            List<TemporalTermEntity> temporalTermMatches = new LinkedList<>();
            for (String tmpTemporalValue : temporalValues) {
                if (temporalTermsMap.containsKey(tmpTemporalValue)) {
                    temporalTermMatches.add(temporalTermsMap.get(tmpTemporalValue));
                }
            }
            log.info("File: {} #Temporal Values: {} #Matches: {}",
                    filename, temporalValues.size(), temporalTermMatches.size());

            // Enrich with temporal mappings
            if (!temporalTermMatches.isEmpty()) {
                EdmXmlUtils.appendTemporalElements(doc,
                        "//edm:ProvidedCHO", temporalTermMatches, earchTemporalEntityMap);
            }
        } catch (XPathExpressionException | NullPointerException ex) {
            log.error("Cannot parse file. File: {}", filename);
        }
    }
    
    /**
     * Enrich an archive. An archive cah have a thematicMapping, a spatialMapping, a temporalMapping
     * @param archiveId the id of archive to enrich
     * @param userId user who requested the enrichment
     * @return enrich details
     */
    public EnrichDetails enrich(Long archiveId, int userId) {

        // Get archive details
        EdmArchiveEntity edmArchiveEntity = edmArchiveRepository.findById(archiveId)
                .orElseThrow(() -> new ResourceNotFoundException(archiveId));

        // Get mappings.
        long thematicMappingId = edmArchiveEntity.getThematicMapping();
        long spatialMappingId = edmArchiveEntity.getSpatialMapping();
        long temporalMappingId = edmArchiveEntity.getTemporalMapping();
        
        // Check existence of mapping
        // Mapping mapping = mappingRepository.findById(thematicMappingId)
        //        .orElseThrow(() -> new ResourceNotFoundException(thematicMappingId));
        
        boolean thematicEnrichment = thematicMappingId > 0;
        boolean spatialEnrichment  = spatialMappingId > 0;
        boolean temporalEnrichment = temporalMappingId > 0;
        
        log.info("Enrich archive. ArchiveId:{} ThematicMappingId: {} SpatialMappingId: {} TemporalMappingId: {}",
                archiveId, thematicMappingId, spatialMappingId, temporalMappingId);
        
        // Load thematic mapping terms
        Map<String, AatSubjectEntity> aatSubjectMap = new HashMap<>();
        Map<String, SubjectTermEntity> subjectTermsMap = new HashMap<>();
        if(thematicEnrichment) {
            // Get mapping subjectTerms. Convert them to map
            List<SubjectTermEntity> subjectTermEntities = subjectTermRepository.findByMappingId(thematicMappingId);
            for(SubjectTermEntity mp : subjectTermEntities) {
                subjectTermsMap.put(mp.getNativeTerm(), mp);
            }
            log.info("Thematic mapping loaded. MappingId: {} #Terms: {}", thematicMappingId, subjectTermEntities.size());

            // TODO: This is not mandatory because a mapping can contain more aat terms from what we have in our database
            // Create aat subject map
            List<AatSubjectEntity> aatSubjectEntities = aatSubjectRepository.findAll();
            for(AatSubjectEntity tmpAatSubjectEntity : aatSubjectEntities) {
                aatSubjectMap.put(tmpAatSubjectEntity.getAatUid(), tmpAatSubjectEntity);
            }
        }
        
        // Load spatial mapping terms
        Map<String, SpatialTermEntity> spatialTermsMap = new HashMap<>();
        if(spatialEnrichment) {
            List<SpatialTermEntity> spatialTermEntities = spatialTermRepository.findByMappingId(spatialMappingId);
            for(SpatialTermEntity mp : spatialTermEntities) {
                spatialTermsMap.put(mp.getNativeTerm(), mp);
            }
            log.info("Spatial mapping loaded. MappingId: {} #Terms: {}", spatialMappingId, spatialTermEntities.size());
        }
        
        // Load temporal mapping terms
        Map<String, EArchTemporalEntity> earchTemporalEntityMap = new HashMap<>();
        Map<String, TemporalTermEntity> temporalTermsMap = new HashMap<>();
        if(temporalEnrichment) {
            List<TemporalTermEntity> temporalTermEntities = temporalTermRepository.findByMappingId(temporalMappingId);
            for(TemporalTermEntity mp : temporalTermEntities) {
                temporalTermsMap.put(mp.getNativeTerm(), mp);
            }
            log.info("Temporal mapping loaded. MappingId: {} #Terms: {}", temporalMappingId, temporalTermEntities.size());

            // Create earch temporal map
            List<EArchTemporalEntity> eArchTemporalEntities = eArchTemporalEntityRepo.findAll();
            for(EArchTemporalEntity earchTemporalEntity : eArchTemporalEntities) {
                earchTemporalEntityMap.put(earchTemporalEntity.getAatUid(), earchTemporalEntity);
            }
        }

        // File system hierarchy
        // storage_tmp
        // -- <europeana_arch>
        //    -- packages
        //        -- p_<package_id> (at this level store the archives - edm.zip , eEDM.tar.gz)
        //            -- EDM
        //            -- eEDM
        
        /*
        // Create enrich enrichRequest
        EnrichRequest enrichRequest = new EnrichRequest();
        enrichRequest.setMappingId(thematicMappingId);
        enrichRequest.setFilename(file.getOriginalFilename());
        enrichRequest.setCreatedBy(userId);
        enrichRequest = enrichRequestRepository.save(enrichRequest);
        long requestId = enrichRequest.getId();
        */
        
        int enrichedFileCount = 0;
        String enrichedArchiveFilePath;
        String enrichedArchiveName = "";
        int edmFileCount = 0;
        try {
            // Get EDM files
            Path edmArchiveFilePath =  Paths.get(edmArchiveEntity.getFilepath());
            Path edmExtractDirPath = Paths.get(edmArchiveFilePath.getParent().toString(), "EDM");
            File[] edmFiles = edmExtractDirPath.toFile().listFiles(); 
            edmFileCount = edmFiles != null ? edmFiles.length : 0;
            log.info("List EDM files. Path: {} #Files: {}", edmExtractDirPath, edmFileCount);

            // Update enrich enrichRequest
            // enrichRequest.setFilepath(edmArchiveFilePath.toString());
            // enrichRequest = enrichRequestRepository.save(enrichRequest);

            // Enrich archive
            // Create enriched directory (if not)
            Path enrichedDirPath = null;
            if(edmFileCount > 0) {
                enrichedDirPath = Paths.get(edmExtractDirPath.getParent().toString(), "eEDM");
                Files.createDirectories(enrichedDirPath);
                log.info("Create eEDM directory. Path: {} ", enrichedDirPath);
            }

            long fileTotalCount = edmFiles != null ? edmFiles.length : 0;
            long fileProcessedCount = 0;
            long fileErrorCount = 0;
            List<String> filesWithErrors = new LinkedList<>();

            // Process each EDM file
            for(File edmFile : edmFiles) {
                if(edmFile.exists() && edmFile.isFile()) {

                    String filename = edmFile.getName();

                    try {
                        // Retrieve xml content. ATTENTION: Namespace aware is true.
                        Document doc = XMLUtils.parse(edmFile, true);
                        // String itemContent = XMLUtils.transform(doc);

                        // ~~~~~~~~~~ Thematic ~~~~~~~~~~ //
                        if(thematicEnrichment) {
                            thematicEnrichment(filename, doc, aatSubjectMap, subjectTermsMap);
                        }
                        
                        // ~~~~~~~~~~ Spatial ~~~~~~~~~ //
                        if(spatialEnrichment) {
                            spatialEnrichment(filename, doc, spatialTermsMap);
                        }

                        // ~~~~~~~~~~ Temporal ~~~~~~~~~ //
                        if(temporalEnrichment) {
                            temporalEnrichment(filename, doc, earchTemporalEntityMap, temporalTermsMap);
                        }

                        // Save enriched file
                        Path enrichedFilePath = Paths.get(enrichedDirPath.toString(), edmFile.getName());
                        XMLUtils.transform(doc, enrichedFilePath.toFile());

                        enrichedFileCount++;

                        // log.info("File enriched. {} subjects added.Stored at: {}", 
                        //            enrichedFileCount, enrichedFilePath);
                        
                        log.debug("File enriched. Stored at: {}", enrichedFilePath);

                    } catch(ParserConfigurationException | SAXException | 
                            TransformerException | XPathExpressionException | NullPointerException ex) {
                        // log.error("Cannot parse file. File: {}", edmFile.getAbsolutePath());
                        filesWithErrors.add(edmFile.getAbsolutePath());
                    }
                }
                fileProcessedCount++;

                if(fileProcessedCount % 50 == 0 || fileProcessedCount == fileTotalCount) {
                    log.info("Extraction progress... {} / {} => {}%",
                            fileProcessedCount, fileTotalCount, new DecimalFormat("#0.0000").format(((double) fileProcessedCount / fileTotalCount) * 100));
                }
            }

            // TODO: Save enrich result in database
            // log enrich results
            log.info("#Files (total): {}", fileTotalCount);
            log.info("#Files (with errors): {}", filesWithErrors.size());
            for(String tmpFileName : filesWithErrors) {
                log.error("Enrichment error at: {}", tmpFileName);
            }

            // Create archive with enriched files
            if(enrichedFileCount > 0) {
                // Example eEDM_m2_r4 => mapping 2, enrichRequest 4
                // String filenamePrefix = "eEDM_m" + mappingId + "_r" + requestId;
                String filenamePrefix =  archiveId + "_eEDM"; 
                Path archiveFilePath = fileStorageService.createArchiveFromDirectory(enrichedDirPath, filenamePrefix);

                enrichedArchiveName = archiveFilePath.getFileName().toString();
                enrichedArchiveFilePath = archiveFilePath.toString();

                // Save enrichment filepath
                edmArchiveEntity.setEnrichedFilename(enrichedArchiveName);
                edmArchiveEntity.setEnrichedFilepath(enrichedArchiveFilePath);
                edmArchiveRepository.save(edmArchiveEntity);
                
                log.info("Enrichment Result. #Files: {} #Enriched: {} Enriched Archive: {}",
                        edmFiles.length, enrichedFileCount, archiveFilePath.toString());
            } 
        }catch (IOException ex) {
            log.error("Enrichment failed.");
            log.error("", ex);
        }
        
        String message = enrichedFileCount + " files enriched successfully.";
        
        // Set enrich details
        EnrichDetails enrichDetails = new EnrichDetails();
        enrichDetails.setSuccess(true);
        enrichDetails.setMessage(message);
        enrichDetails.setEdmFileCount(edmFileCount);
        enrichDetails.setEdmArchiveName(edmArchiveEntity.getFilename());
        enrichDetails.setEnrichedFileCount(enrichedFileCount);
        enrichDetails.setEnrichedArchiveName(enrichedArchiveName);
        // enrichDetails.setEnrichedArchiveUrl("/enrich/requests/" + requestId + "/download");
        
        // ObjectMapper objectMaper = new ObjectMapper();
        // String details = objectMaper.writeValueAsString(enrichDetails);
        
        // Update enrich enrichRequest
        //enrichRequest.setEnrichedFilename(enrichedArchiveName);
        //enrichRequest.setEnrichedFilepath(enrichedArchiveFilePath);
        //enrichRequest.setDetails(details);
        //enrichRequestRepository.save(enrichRequest);
        
        return enrichDetails;
    }
    
    
    public List<MappingUploadRequestEntity> getEdmPackages(Integer userId) {
        return uploadRequestRepository.findAllByCreatedBy(userId);
    }
    
    /**
     * Extract terms from a destination path with EDM files
     * @param edmExtractDirPath destination path for extraction
     * @param thematic flag to extract thematic values
     * @param temporal flag to extract temporal values
     * @param spatial  flag to extract subject values
     * @param skipEmptyValues flag to skip empty values
     */
    public static List<EdmFileTermExtractionResult> extractTerms(
            Path edmExtractDirPath, boolean thematic, boolean temporal, boolean spatial, boolean skipEmptyValues) throws IOException {
        
        List<EdmFileTermExtractionResult> extractionResult = new LinkedList<>();
        
        long requestId = 0;
        
        if(!edmExtractDirPath.toFile().exists() || !edmExtractDirPath.toFile().isDirectory()) {
            log.warn("Path does not exist or it is not directory");
            throw new IOException("Path does not exist or it is not directory");
        }
                    
        // List edm files
        File[] edmFiles = edmExtractDirPath.toFile().listFiles();
        int edmFilesCount = edmFiles != null ? edmFiles.length : 0;
        log.info("Extract terms started. RequestId: {} Path: {} #Files: {}", requestId, edmExtractDirPath, edmFilesCount);
        
        long itemsProcessed = 0;
        // long itemsTotal = edmFilesCount;
        
        // Process edm file
        for(File edmFile : edmFiles) {
            if(edmFile.exists() && edmFile.isFile()) {
                
                try {
                    
                    String filename = edmFile.getName();
                    
                    // Retrieve xml content. ATTENTION: Namespace aware is true.
                    Document doc = XMLUtils.parse(edmFile, true);
                    
                    // Extract data
                    List<ElementExtractionData> extractedData = new LinkedList<>();
                    
                    // Extract thematic subjectTerms
                    if(thematic) {
                        NodeList dcSubjectNodes = XMLUtils.getNodeList(doc, "//" + EdmExtractUtils.DC_SUBJECT);
                        extractedData.addAll(EdmExtractUtils.extractNodeData(dcSubjectNodes, skipEmptyValues));

                        NodeList dcTypeNodes = XMLUtils.getNodeList(doc, "//" + EdmExtractUtils.DC_TYPE);
                        extractedData.addAll(EdmExtractUtils.extractNodeData(dcTypeNodes, skipEmptyValues));
                    }
                    
                    // Extract temporal subjectTerms
                    if(temporal) {
                        // NodeList dcDateNodes = XMLUtils.getNodeList(doc, "//" + EdmExtractUtils.DC_DATE);
                        // extractedData.addAll(EdmExtractUtils.extractNodeData(dcDateNodes, skipEmptyValues));

                        NodeList dcTermsTemporalNodes = XMLUtils.getNodeList(doc, "//" + EdmExtractUtils.DC_TERMS_TEMPORAL);
                        extractedData.addAll(EdmExtractUtils.extractNodeData(dcTermsTemporalNodes, skipEmptyValues));

                        NodeList dcTermsCreatedNodes = XMLUtils.getNodeList(doc, "//" + EdmExtractUtils.DC_TERMS_CREATED);
                        extractedData.addAll(EdmExtractUtils.extractNodeData(dcTermsCreatedNodes, skipEmptyValues));
                    }
                    
                    // Extract spatial subjectTerms
                    if(spatial) {
                        NodeList dcTermsSpatialNodes = XMLUtils.getNodeList(doc, "//" + EdmExtractUtils.DC_TERMS_SPATIAL);
                        extractedData.addAll(EdmExtractUtils.extractNodeData(dcTermsSpatialNodes, skipEmptyValues));
                    }
                     
                    extractionResult.add(new EdmFileTermExtractionResult(filename, extractedData));
                } catch(IOException | ParserConfigurationException | SAXException | XPathExpressionException ex) {
                    log.error("Cannot parse file. File: {}", edmFile.getAbsolutePath());
                }
            }
            
            itemsProcessed++;
            
            if(itemsProcessed % 50 == 0 || itemsProcessed == edmFilesCount) {
                log.info("Extraction progress... {} / {} => {}%",  
                        itemsProcessed, edmFilesCount, new DecimalFormat("#0.0000").format(((double) itemsProcessed / edmFilesCount) * 100));
            }
        }
        
        return extractionResult;
    }
}
