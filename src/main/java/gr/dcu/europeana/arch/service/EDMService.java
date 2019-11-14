package gr.dcu.europeana.arch.service;

import gr.dcu.europeana.arch.exception.MyFileNotFoundException;
import gr.dcu.europeana.arch.exception.NotFoundException;
import gr.dcu.europeana.arch.exception.ResourceNotFoundException;
import gr.dcu.europeana.arch.model.EdmArchive;
import gr.dcu.europeana.arch.model.Mapping;
import gr.dcu.europeana.arch.model.MappingUploadRequest;
import gr.dcu.europeana.arch.model.SubjectTerm;
import gr.dcu.europeana.arch.repository.UploadRequestRepository;
import gr.dcu.europeana.arch.service.edm.EdmFileTermExtractionResult;
import gr.dcu.europeana.arch.service.edm.EdmExtractUtils;
import gr.dcu.europeana.arch.service.edm.ElementExtractionData;
import gr.dcu.utils.XMLUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import gr.dcu.europeana.arch.repository.EdmArchiveRepository;

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
    FileStorageService fileStorageService;
    
    
    public List<EdmArchive> getEdmArchives(Integer userId) {
        return edmArchiveRepository.findAllByCreatedBy(userId);
    }
    
    
    public EdmArchive getEdmArchive(Long id) {
        
        EdmArchive edmUpload = edmArchiveRepository.findById(id).orElseThrow(() 
                -> new NotFoundException("EDM Upload", id));
        
        return edmUpload;
    }
    
    public void uploadEdmArchive(MultipartFile file, int userId) throws IOException {
        
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
        enrichRequest.setMappingId(mappingId);
        enrichRequest.setFilename(file.getOriginalFilename());
        enrichRequest.setCreatedBy(userId);
        enrichRequest = enrichRequestRepository.save(enrichRequest);
        long requestId = enrichRequest.getId();
        */
        
        // Create an upload request
        EdmArchive edmUpload = new EdmArchive();
        edmUpload.setName("test");
        edmUpload.setItemCount(0);
        edmUpload.setFilename("");
        edmUpload.setFilepath("");
        edmUpload.setCreatedBy(userId);
        long edmUploadId = edmArchiveRepository.save(edmUpload).getId();
        
        // Upload EDM archive
        log.info("Upload EDM archive. RequestId: {}", edmUploadId);
        Path edmArchiveFilePath = fileStorageService.buildUploadEdmArchiveFilePathNew(edmUploadId, file.getOriginalFilename());
        fileStorageService.upload(edmArchiveFilePath, file);
        log.info("EDM archive uploaded. Path: {}", edmArchiveFilePath);
        
        // Update enrich enrichRequest
        edmUpload.setFilepath(edmArchiveFilePath.toString());
        edmUpload = edmArchiveRepository.save(edmUpload);
        
        // Extract EDM archive
        log.info("Extract EDM archive. RequestId: {}", edmUploadId);
        Path edmExtractDirPath = Paths.get(edmArchiveFilePath.getParent().toString(), "EDM");
        fileStorageService.extractArchive(edmArchiveFilePath, edmExtractDirPath);
        File[] edmFiles = edmExtractDirPath.toFile().listFiles(); 
        log.info("EDM archive extracted. Path: {} #Files: {}", edmExtractDirPath, edmFiles.length);
        
        /*
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
        } */
        
        /*
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
        */
        
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
    
    
    /*
    public List<SubjectTerm> uploadSubjectTerms(long mappingId, MultipartFile file, int userId) throws IOException {
        
        // Check existemce of mapping
        Mapping mapping = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException(mappingId));
        
        // Upload mapping file
        Path filePath = fileStorageService.buildUploadFilePath(mappingId, file.getOriginalFilename());
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
    } */
    
    
    
    
    
    
    
    
    
    public List<MappingUploadRequest> getEdmPackages(Integer userId) {
        return uploadRequestRepository.findAllByCreatedBy(userId);
    }

//    public EDMService() {
//    }
    
    
    
    /**
     * 
     * @param filename
     * @param mappingId
     * @param skipLineCount
     * @param limitCount
     * @return 
     */
    public List<SubjectTerm> loadMappingTermsFromExcel(String filename, long mappingId, 
            int skipLineCount, int limitCount) {

        List<SubjectTerm> mappings = new LinkedList<>();
        
        /*
        try {
            
            // Resource resource = resourceLoader.getResource("classpath:" + filename);
            // File file = resource.getFile();
            
            File file = new File(filename);
            if(file.exists() && file.isFile()) {
                log.info("URI: {}", file.getAbsolutePath());
                
                // Process archive
                
                
            } else {
                log.error("File not found " + filename);
                throw new MyFileNotFoundException("File not found " + filename);
            }
           
        } catch(IOException ex) {
            log.error("File not found " + filename);
                throw new MyFileNotFoundException("File not found " + filename);
        } */
        
        return mappings;
        
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
                    
                    // Extract thematic terms
                    if(thematic) {
                        NodeList dcSubjectNodes = XMLUtils.getNodeList(doc, "//" + EdmExtractUtils.DC_SUBJECT);
                        extractedData.addAll(EdmExtractUtils.extractNodeData(dcSubjectNodes, skipEmptyValues));

                        NodeList dcTypeNodes = XMLUtils.getNodeList(doc, "//" + EdmExtractUtils.DC_TYPE);
                        extractedData.addAll(EdmExtractUtils.extractNodeData(dcTypeNodes, skipEmptyValues));
                    }
                    
                    // Extract temporal terms
                    if(temporal) {
                        NodeList dcDateNodes = XMLUtils.getNodeList(doc, "//" + EdmExtractUtils.DC_DATE);
                        extractedData.addAll(EdmExtractUtils.extractNodeData(dcDateNodes, skipEmptyValues));

                        NodeList dcTermsTemporalNodes = XMLUtils.getNodeList(doc, "//" + EdmExtractUtils.DC_TERMS_TEMPORAL);
                        extractedData.addAll(EdmExtractUtils.extractNodeData(dcTermsTemporalNodes, skipEmptyValues));

                        NodeList dcTermsCreatedNodes = XMLUtils.getNodeList(doc, "//" + EdmExtractUtils.DC_TERMS_CREATED);
                        extractedData.addAll(EdmExtractUtils.extractNodeData(dcTermsCreatedNodes, skipEmptyValues));
                    }
                    
                    // Extract spatial terms
                    if(spatial) {
                        NodeList dcTermsSpatialNodes = XMLUtils.getNodeList(doc, "//" + EdmExtractUtils.DC_TERMS_SPATIAL);
                        extractedData.addAll(EdmExtractUtils.extractNodeData(dcTermsSpatialNodes, skipEmptyValues));
                    }
                     
                    extractionResult.add(new EdmFileTermExtractionResult(filename, extractedData));
                    
                    /*
                    List<EdmExtractResult> result = new LinkedList<>();
                    // Get thematic terms
                    List<String> dcSubjectValues = XMLUtils.getElementValues(doc, "//dc:subject");
                    List<String> dcTypeValues = XMLUtils.getElementValues(doc, "//dc:type");
                    
                    // Get temporal terms
                    List<String> dcDateValues = XMLUtils.getElementValues(doc, "//dc:date");
                    List<String> dcTermsTemporalValues = XMLUtils.getElementValues(doc, "//dcterms:temporal");
                    List<String> dcTermsCreatedValues = XMLUtils.getElementValues(doc, "//dcterms:created");
                    
                    // Get spatial terms
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
