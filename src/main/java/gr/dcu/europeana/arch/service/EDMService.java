package gr.dcu.europeana.arch.service;

import gr.dcu.europeana.arch.exception.MyFileNotFoundException;
import gr.dcu.europeana.arch.model.SubjectTerm;
import gr.dcu.europeana.arch.service.edm.EdmFileTermExtractionResult;
import gr.dcu.europeana.arch.service.edm.EdmExtractUtils;
import gr.dcu.europeana.arch.service.edm.ElementExtractionData;
import gr.dcu.utils.XMLUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
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
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Vangelis Nomikos
 */
@Slf4j
@Service
public class EDMService {

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
     * @return 
     */
    public static List<EdmFileTermExtractionResult> extractTerms(
            Path edmExtractDirPath, boolean thematic, boolean temporal, boolean spatial) throws IOException {
        
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
                        NodeList dcSubjectNodes = XMLUtils.getNodeList(doc, "//dc:subject");
                        extractedData.addAll(EdmExtractUtils.extractNodeData(dcSubjectNodes));

                        NodeList dcTypeNodes = XMLUtils.getNodeList(doc, "//dc:type");
                        extractedData.addAll(EdmExtractUtils.extractNodeData(dcTypeNodes));
                    }
                    
                    // Extract temporal terms
                    if(temporal) {
                        NodeList dcDateNodes = XMLUtils.getNodeList(doc, "//dc:date");
                        extractedData.addAll(EdmExtractUtils.extractNodeData(dcDateNodes));

                        NodeList dcTermsTemporalNodes = XMLUtils.getNodeList(doc, "//dcterms:temporal");
                        extractedData.addAll(EdmExtractUtils.extractNodeData(dcTermsTemporalNodes));

                        NodeList dcTermsCreatedNodes = XMLUtils.getNodeList(doc, "//dcterms:created");
                        extractedData.addAll(EdmExtractUtils.extractNodeData(dcTermsCreatedNodes));
                    }
                    
                    // Extract spatial terms
                    if(spatial) {
                        NodeList dcTermsSpatialNodes = XMLUtils.getNodeList(doc, "//dcterms:spatial");
                        extractedData.addAll(EdmExtractUtils.extractNodeData(dcTermsSpatialNodes));
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
