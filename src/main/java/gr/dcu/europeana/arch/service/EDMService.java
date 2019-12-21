package gr.dcu.europeana.arch.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.dcu.europeana.arch.api.resource.EnrichDetails;
import gr.dcu.europeana.arch.api.resource.ExtractTermResult;
import gr.dcu.europeana.arch.exception.NotFoundException;
import gr.dcu.europeana.arch.exception.ResourceNotFoundException;
import gr.dcu.europeana.arch.model.AatSubject;
import gr.dcu.europeana.arch.model.EdmArchive;
import gr.dcu.europeana.arch.model.EdmArchiveTerms;
import gr.dcu.europeana.arch.model.EnrichRequest;
import gr.dcu.europeana.arch.model.Mapping;
import gr.dcu.europeana.arch.model.MappingType;
import gr.dcu.europeana.arch.model.MappingUploadRequest;
import gr.dcu.europeana.arch.model.SpatialTerm;
import gr.dcu.europeana.arch.model.SubjectTerm;
import gr.dcu.europeana.arch.model.TemporalTerm;
import gr.dcu.europeana.arch.repository.AatSubjectRepository;
import gr.dcu.europeana.arch.repository.UploadRequestRepository;
import gr.dcu.europeana.arch.service.edm.EdmFileTermExtractionResult;
import gr.dcu.europeana.arch.service.edm.EdmExtractUtils;
import gr.dcu.europeana.arch.service.edm.ElementExtractionData;
import gr.dcu.utils.XMLUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import gr.dcu.europeana.arch.repository.EdmArchiveRepository;
import gr.dcu.europeana.arch.repository.EdmArchiveTermsRepository;
import gr.dcu.europeana.arch.repository.MappingRepository;
import gr.dcu.europeana.arch.repository.SpatialTermRepository;
import gr.dcu.europeana.arch.repository.SubjectTermRepository;
import gr.dcu.europeana.arch.repository.TemporalTermRepository;
import gr.dcu.europeana.arch.service.edm.EdmXmlUtils;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.TransformerException;
import net.bytebuddy.dynamic.scaffold.MethodGraph;
import org.apache.commons.io.FilenameUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Vangelis Nomikos
 */
@Slf4j
@Service
public class EDMService {
    
    @Autowired
    UploadRequestRepository uploadRequestRepository;
    
    @Autowired
    EdmArchiveRepository edmArchiveRepository;
    
    @Autowired
    EdmArchiveTermsRepository edmArchiveTermsRepository;
    
    @Autowired
    MappingRepository mappingRepository;
    
    @Autowired
    SubjectTermRepository subjectTermRepository;
    
    @Autowired
    SpatialTermRepository spatialTermRepository;
    
    @Autowired
    TemporalTermRepository temporalTermRepository;
    
    @Autowired
    AatSubjectRepository aatSubjectRepository;
    
    @Autowired
    FileStorageService fileStorageService;
    
    
    public List<EdmArchive> getEdmArchives(Integer userId) {
        return edmArchiveRepository.findAllByCreatedBy(userId);
    }
    
    
    public EdmArchive getEdmArchive(Long id) {
        
        EdmArchive edmUpload = edmArchiveRepository.findById(id).orElseThrow(() 
                -> new NotFoundException("EDM Upload", id));
        
        return edmUpload;
    }
    
    @Transactional
    public EdmArchive uploadEdmArchive(MultipartFile file, int userId) throws IOException {
        
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
        EdmArchive edmArchive = new EdmArchive();
        edmArchive.setName(file.getOriginalFilename());
        edmArchive.setItemCount(0);
        edmArchive.setFilename(file.getOriginalFilename());
        edmArchive.setFilepath("");
        edmArchive.setThematicMapping(0L);
        edmArchive.setSpatialMapping(0L);
        edmArchive.setTemporalMapping(0L);
        edmArchive.setCreatedBy(userId);
        long edmUploadId = edmArchiveRepository.save(edmArchive).getId();
        
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
        log.info("EDM archive extracted. Path: {} #Files: {}", edmExtractDirPath, edmFiles.length);
        
        // Update upload request
        edmArchive.setId(edmUploadId);
        edmArchive.setFilepath(edmArchiveFilePath.toString());
        edmArchive.setEnrichedFilepath(null);
        edmArchive.setItemCount(edmFiles.length);
        edmArchive = edmArchiveRepository.save(edmArchive);
        
        return edmArchive;
    }
    
    /**
     * Load original EDM or eEDM (if exists)
     * @param archiveId
     * @param type
     * @return
     * @throws IOException 
     */
    public File loadEdmArchive(long archiveId, String type) throws IOException {
        
        EdmArchive edmArchive = edmArchiveRepository.findById(archiveId)
                .orElseThrow(() -> new ResourceNotFoundException(archiveId));
        
        String filepath;
        String filename =  edmArchive.getFilename();
        if(type != null && type.equalsIgnoreCase("eEDM")) {
            filepath = edmArchive.getEnrichedFilepath();
        } else {
            filepath = edmArchive.getFilepath();
        }
        
        File file = null;
        if(filepath != null && !filepath.isEmpty()) {
            file = fileStorageService.loadFile(filepath);
        } else {
            log.error("File not found.");
            throw new NotFoundException("File");
        }
        
        return file;
    }
    
    public List<EdmFileTermExtractionResult> extractTermsFromEdmArcive(Long edmArchiveId) {
        
        EdmArchive edmArchive = edmArchiveRepository.findById(edmArchiveId).orElseThrow(() 
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
    
    public EdmArchiveTerms saveTerms(Long archiveId, ExtractTermResult extractTermResult, Integer userId) {
        
        EdmArchive edmArchive = edmArchiveRepository.findById(archiveId)
                .orElseThrow(() -> new ResourceNotFoundException(archiveId));
        
        String subjectTermsInJson = null;
        String spatialTermsInJson = null;
        String temporalTermsInJson = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            subjectTermsInJson = mapper.writeValueAsString(extractTermResult.getSubjectTerms());
            spatialTermsInJson = mapper.writeValueAsString(extractTermResult.getSpatialTerms());
            temporalTermsInJson = mapper.writeValueAsString(extractTermResult.getTemporalTerms());
        } catch (JsonProcessingException ex) {
            log.error("");
        }
                
        EdmArchiveTerms edmArchiveTerms = new EdmArchiveTerms();
        edmArchiveTerms.setArchiveId(archiveId);
        edmArchiveTerms.setSubjectTerms(subjectTermsInJson);
        edmArchiveTerms.setSpatialTerms(spatialTermsInJson);
        edmArchiveTerms.setTemporalTerms(temporalTermsInJson);
        edmArchiveTerms.setCreatedBy(userId);
        edmArchiveTerms = edmArchiveTermsRepository.save(edmArchiveTerms);
        
        return edmArchiveTerms;
    }
    
    public ExtractTermResult loadTerms(Long archiveId, Integer userId) {
        
        EdmArchive edmArchive = edmArchiveRepository.findById(archiveId)
                .orElseThrow(() -> new ResourceNotFoundException(archiveId));
        
        boolean hasThematicMapping = edmArchive.getThematicMapping() > 0 ? true : false;
        boolean hasSpatialMapping  = edmArchive.getSpatialMapping() > 0 ? true : false;
        boolean hasTemporalMapping = edmArchive.getTemporalMapping() > 0 ? true : false;
        
        EdmArchiveTerms edmArchiveTerms = edmArchiveTermsRepository.findByArchiveId(archiveId);
        
        List<SubjectTerm> subjectTerms = new LinkedList<>();
        List<SpatialTerm> spatialTerms = new LinkedList<>();
        List<TemporalTerm> temporalTerms = new LinkedList<>();
        if(edmArchiveTerms != null) {
            try {
                
                ObjectMapper mapper = new ObjectMapper();
                if(hasThematicMapping) {
                    subjectTerms = subjectTermRepository.findByMappingId(edmArchive.getThematicMapping());
                } else {
                    subjectTerms = mapper.readValue(edmArchiveTerms.getSubjectTerms(), 
                                    new TypeReference<List<SubjectTerm>>(){});
                }
                
                if(hasSpatialMapping) {
                    spatialTerms = spatialTermRepository.findByMappingId(edmArchive.getSpatialMapping());
                } else {
                    spatialTerms = mapper.readValue(edmArchiveTerms.getSpatialTerms(), 
                                    new TypeReference<List<SpatialTerm>>(){});
                }
                
                if(hasTemporalMapping) {
                    temporalTerms = temporalTermRepository.findByMappingId(edmArchive.getTemporalMapping());
                } else {
                    temporalTerms = mapper.readValue(edmArchiveTerms.getTemporalTerms(), 
                                    new TypeReference<List<TemporalTerm>>(){});
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
                log.warn("Cannot parse Edm Archive Terms. ArchiveID: {} ArchiveIdTerms: {}", archiveId, edmArchiveTerms.getId());
            }
        } log.warn("Cannot get Edm Archive Terms. ArchiveID: {}", archiveId);
        
        ExtractTermResult extractTermResult = new ExtractTermResult();
        extractTermResult.setSubjectTerms(subjectTerms);
        extractTermResult.setSpatialTerms(spatialTerms);
        extractTermResult.setTemporalTerms(temporalTerms);

        return extractTermResult;
    }
    
    /**
     * Create a mapping based on extracted subjectTerms from EDM archive.
     * @param archiveId
     * @param type
     * @param userId
     * @return 
     */
    @Transactional
    public Mapping createMapping(Long archiveId, String type, Integer userId) {
        
        EdmArchive edmArchive = edmArchiveRepository.findById(archiveId)
                .orElseThrow(() -> new ResourceNotFoundException(archiveId));
        
        EdmArchiveTerms edmArchiveTerms = edmArchiveTermsRepository.findByArchiveId(archiveId);
        
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
         
        
        // Add subjectTerms to mapping
        if(type.equalsIgnoreCase(MappingType.MAPPING_TYPE_SUBJECT) && !subjectTerms.isEmpty()) {
            
            for(SubjectTerm term : subjectTerms) {
                term.setMappingId(mappingId);
            }
            subjectTermRepository.saveAll(subjectTerms);
            edmArchive.setThematicMapping(mappingId);
        } else if(type.equalsIgnoreCase(MappingType.MAPPING_TYPE_SPATIAL) && !spatialTerms.isEmpty()) {
            
            for(SpatialTerm term : spatialTerms) {
                term.setMappingId(mappingId);
            }
            spatialTermRepository.saveAll(spatialTerms);
            edmArchive.setSpatialMapping(mappingId);
        } else if(type.equalsIgnoreCase(MappingType.MAPPING_TYPE_TEMPORAL) && !temporalTerms.isEmpty()) {
            
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
    public void deleteEdmArchive(Long archiveId) {
        edmArchiveTermsRepository.deleteByArchiveId(archiveId);
        edmArchiveRepository.deleteById(archiveId);
    }
    
    /**
     * 
     * @param archiveId
     * @param userId
     * @return 
     */
    public EnrichDetails enrich2(Long archiveId, int userId) {
        
        EdmArchive edmArchive = edmArchiveRepository.findById(archiveId)
                .orElseThrow(() -> new ResourceNotFoundException(archiveId));
        
        long thematicMappingId = edmArchive.getThematicMapping();
        long spatialMappingId = edmArchive.getSpatialMapping();
        long temporalMappingId = edmArchive.getTemporalMapping();
        
        // Check existemce of mapping
        // Mapping mapping = mappingRepository.findById(thematicMappingId)
        //        .orElseThrow(() -> new ResourceNotFoundException(thematicMappingId));
        
        boolean thematicEnrichment = thematicMappingId > 0 ? true : false;
        boolean spatialEnrichment  = spatialMappingId > 0 ? true : false;
        boolean temporalEnrichment = temporalMappingId > 0 ? true : false;
        
        log.info("Enirch archive. ArchiveId:{} ThematicMappingId: {} SpatialMappingId: {} TemporalMappingId: {}", 
                archiveId, thematicMappingId, spatialMappingId, temporalMappingId);
        
        // Load thematic mapping terms
        Map<String, AatSubject> aatSubjectMap = new HashMap<>();
        List<SubjectTerm> subjectTerms = new LinkedList<>();
        Map<String,SubjectTerm> subjectTermsMap = new HashMap<>();
        if(thematicEnrichment) {
            // Get mapping subjectTerms. Convert them to map
            subjectTerms = subjectTermRepository.findByMappingId(thematicMappingId);
            for(SubjectTerm mp : subjectTerms) {
                subjectTermsMap.put(mp.getNativeTerm(), mp);
            }
            log.info("Thematic mapping loaded. MappingId: {} #Terms: {}", thematicMappingId, subjectTerms.size());
        
            // Create aat subject map
            List<AatSubject> aatSubjects = aatSubjectRepository.findAll();
            for(AatSubject tmpAatSubject : aatSubjects) {
                aatSubjectMap.put(tmpAatSubject.getAatUid(), tmpAatSubject);
            }
        }
        
        // Load spatial mapping terms
        List<SpatialTerm> spatialTerms = new LinkedList<>();
        Map<String,SpatialTerm> spatialTermsMap = new HashMap<>();
        if(spatialEnrichment) {
            spatialTerms = spatialTermRepository.findByMappingId(spatialMappingId);
            for(SpatialTerm mp : spatialTerms) {
                spatialTermsMap.put(mp.getNativeTerm(), mp);
            }
            log.info("Spatial mapping loaded. MappingId: {} #Terms: {}", spatialMappingId, spatialTerms.size());
        }
        
        // Load temporal mapping terms
        List<TemporalTerm> temporalTerms = new LinkedList<>();
        Map<String,TemporalTerm> temporalTermsMap = new HashMap<>();
        if(temporalEnrichment) {
            temporalTerms = temporalTermRepository.findByMappingId(temporalMappingId);
            for(TemporalTerm mp : temporalTerms) {
                temporalTermsMap.put(mp.getNativeTerm(), mp);
            }
            log.info("Temporal mapping loaded. MappingId: {} #Terms: {}", temporalMappingId, temporalTerms.size());
        }
            
        
        // DEPRECATED
        // File system hierarchy
        // storage_tmp
        // -- <mapping_id>
        //    -- enrich
        //        -- <request_id> (at this level store the archives - edm.zip , eEDM.tar.gz)
        //            -- EDM
        //            -- eEDM
        //    -- uploads
        //    -- exports
        
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
        String enrichedArchiveFilePath = "";
        String enrichedArchiveName = "";
        int edmFileCount = 0;
        try {
            // Get EDM files
            Path edmArchiveFilePath =  Paths.get(edmArchive.getFilepath());
            Path edmExtractDirPath = Paths.get(edmArchiveFilePath.getParent().toString(), "EDM");
            File[] edmFiles = edmExtractDirPath.toFile().listFiles(); 
            edmFileCount = edmFiles.length;
            log.info("List EDM files. Path: {} #Files: {}", edmExtractDirPath, edmFiles.length);

            // Update enrich enrichRequest
            // enrichRequest.setFilepath(edmArchiveFilePath.toString());
            // enrichRequest = enrichRequestRepository.save(enrichRequest);

            // Enrich archive
            // Create enriched directory (if not)
            Path enrichedDirPath = null;
            if(edmFiles.length > 0) {
                enrichedDirPath = Paths.get(edmExtractDirPath.getParent().toString(), "eEDM");
                Files.createDirectories(enrichedDirPath);
                log.info("Create eEDM directory. Path: {} ", enrichedDirPath);
            }

            
            for(File edmFile : edmFiles) {
                if(edmFile.exists() && edmFile.isFile()) {

                    try {
                        // Retrieve xml content. ATTENTION: Namespace aware is true.
                        Document doc = XMLUtils.parse(edmFile, true);
                        // String itemContent = XMLUtils.transform(doc);

                        // ~~~~~~~~~~ Thematic ~~~~~~~~~~ //
                        List<SubjectTerm> subjectMappingsTerms = new LinkedList<> ();
                        if(thematicEnrichment) {
                        
                            // TODO: Get all elements from extraction process
                            // Get subjects
                            // "//dc:subject"

                            List<String> dcSubjectValues = XMLUtils.getElementValues(doc, "//" + EdmExtractUtils.DC_SUBJECT);

                            // Find subject mapppings (if any)
                            // int subjectTermMatchCount = 0;
                            // List<String> subjectMappings = new LinkedList<>();
                            for(String dcSubjectValue : dcSubjectValues) {
                                if(subjectTermsMap.containsKey(dcSubjectValue)) {
                                    //subjectTermMatchCount++;
                                    // subjectMappings.add(dcSubjectValue);
                                    subjectMappingsTerms.add(subjectTermsMap.get(dcSubjectValue));
                                }
                            }
                            // edmFile.getAbsolutePath()
                            log.info("File: {} #dc:subject: {} #Matches: {}", edmFile.getName(), dcSubjectValues.size(), subjectMappingsTerms.size());

                        }
                        
                        // ~~~~~~~~~~ Spatial ~~~~~~~~~ //
                        List<SpatialTerm> spatialMappingsTerms = new LinkedList<> ();
                        if(spatialEnrichment) {
                            List<String> dcTermsSpatialValues = XMLUtils.getElementValues(doc, "//" + EdmExtractUtils.DC_TERMS_SPATIAL);


                            for(String dcTermsSpatialValue : dcTermsSpatialValues) {
                                if(spatialTermsMap.containsKey(dcTermsSpatialValue)) {
                                    //subjectTermMatchCount++;
                                    // subjectMappings.add(dcSubjectValue);
                                    spatialMappingsTerms.add(spatialTermsMap.get(dcTermsSpatialValue));
                                }
                            }
                            // edmFile.getAbsolutePath()
                            log.info("File: {} #dcterms:spatial: {} #Matches: {}", edmFile.getName(), dcTermsSpatialValues.size(), spatialMappingsTerms.size());
                        }
                        
                        // Enrich with thematic mappings
                        if(!subjectMappingsTerms.isEmpty()) {

                            // doc = XMLUtils.appendThematicElements(doc, "//edm:ProvidedCHO", "dc:subject", subjectMappings);
                            doc = EdmXmlUtils.appendThematicElements(doc, "//edm:ProvidedCHO", "dc:subject", subjectMappingsTerms, aatSubjectMap);
                            
                        }
                        
                        // Enrich with spatial mappings
                        if(!spatialMappingsTerms.isEmpty()) {
                            doc = EdmXmlUtils.appendSpatialElements(doc, "//edm:ProvidedCHO", "dcterms:spatial", spatialMappingsTerms);
                            
                        }
                        
                        // Save enriched file
                        Path enrichedFilePath = Paths.get(enrichedDirPath.toString(), edmFile.getName());
                        XMLUtils.transform(doc, enrichedFilePath.toFile());

                        enrichedFileCount++;

                        // log.info("File enriched. {} subjects added.Stored at: {}", 
                        //            enrichedFileCount, enrichedFilePath);
                        
                        log.info("File enriched. Stored at: {}", enrichedFilePath);
                        
                        
                    } catch(ParserConfigurationException | SAXException | 
                            TransformerException | XPathExpressionException ex) {
                        log.error("Cannot parse file. File: {}", edmFile.getAbsolutePath());
                    }

                }
            }

            // Create archive with enriched files
            if(enrichedFileCount > 0) {
                // Example eEDM_m2_r4 => mapping 2, enrichRequest 4
                //String filenamePrefix = "eEDM_m" + mappingId + "_r" + requestId; 
                // String filenamePrefix = "eEDM_m" + 1 + "_r" + 1; 
                // FilenameUtils.removeExtension(edmArchive.getFilename())
                String filenamePrefix =  archiveId + "_eEDM"; 
                Path archiveFilePath = fileStorageService.createArchiveFromDirectory(enrichedDirPath, filenamePrefix);

                enrichedArchiveName = archiveFilePath.getFileName().toString();
                enrichedArchiveFilePath = archiveFilePath.toString();

                // Save enrichement filepath
                edmArchive.setEnrichedFilepath(enrichedArchiveFilePath);
                edmArchiveRepository.save(edmArchive);
                
                log.info("Enrichement Result. #Files: {} #Enriched: {} Enriched Archive: {}", 
                        edmFiles.length, enrichedFileCount, archiveFilePath.toString());
            } 
        }catch (IOException ex) {
            log.error("Enrihment failed.");
            log.error("", ex);
        }
        
        String message = enrichedFileCount + " files enriched successfully.";
        
        // Set enrich details
        EnrichDetails enrichDetails = new EnrichDetails();
        enrichDetails.setSuccess(true);
        enrichDetails.setMessage(message);
        enrichDetails.setEdmFileCount(edmFileCount);
        enrichDetails.setEdmArchiveName(edmArchive.getFilename());
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
    
    
    public List<MappingUploadRequest> getEdmPackages(Integer userId) {
        return uploadRequestRepository.findAllByCreatedBy(userId);
    }
    
    /**
     * 
     * @param edmExtractDirPath
     * @param thematic
     * @param temporal
     * @param spatial
     * @param skipEmptyValues
     * @return 
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
        log.info("Extract terms started. RequestId: {} Path: {} #Files: {}", requestId, edmExtractDirPath, edmFiles.length);
        
        long itemsProcessed = 0;
        long itemsTotal = edmFiles.length;
        
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
                        NodeList dcDateNodes = XMLUtils.getNodeList(doc, "//" + EdmExtractUtils.DC_DATE);
                        extractedData.addAll(EdmExtractUtils.extractNodeData(dcDateNodes, skipEmptyValues));

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
                    
                    /*
                    List<EdmExtractResult> result = new LinkedList<>();
                    // Get thematic subjectTerms
                    List<String> dcSubjectValues = XMLUtils.getElementValues(doc, "//dc:subject");
                    List<String> dcTypeValues = XMLUtils.getElementValues(doc, "//dc:type");
                    
                    // Get temporal subjectTerms
                    List<String> dcDateValues = XMLUtils.getElementValues(doc, "//dc:date");
                    List<String> dcTermsTemporalValues = XMLUtils.getElementValues(doc, "//dcterms:temporal");
                    List<String> dcTermsCreatedValues = XMLUtils.getElementValues(doc, "//dcterms:created");
                    
                    // Get spatial subjectTerms
                    List<String> dcTermSpatialValues = XMLUtils.getElementValues(doc, "//dcterms:spatial");
                    
                    // Extract result
                    EdmFileTermExtractionResult extractResult = new EdmFileTermExtractionResult();
                    extractResult.setFilename(edmFile.getName());
                    extractResult.setDcSubjectValues(dcSubjectValues);
                    extractResult.setDcTypeValues(dcTypeValues);
                    extractResult.setDcDateValues(dcDateValues);
                    extractResult.setDcTermsTemporalValues(dcTermsTemporalValues);
                    extractResult.setDcTermsCreatedValues(dcTermsCreatedValues);
                    extractResult.setDcTermsSpatialValues(dcTermSpatialValues);
                    result.add(extractResult);
                    */
                } catch(IOException | ParserConfigurationException | SAXException | XPathExpressionException ex) {
                    log.error("Cannot parse file. File: {}", edmFile.getAbsolutePath());
                }
            }
            
            itemsProcessed++;
            
            if(itemsProcessed % 50 == 0 || itemsProcessed == itemsTotal) {        
                log.info("Extraction progress... {} / {} => {}%",  
                        itemsProcessed, itemsTotal, new DecimalFormat("#0.0000").format(((double) itemsProcessed / itemsTotal) * 100));
            }
        }
        
        return extractionResult;
    }
}
