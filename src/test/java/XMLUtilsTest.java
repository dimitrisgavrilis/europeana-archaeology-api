
import gr.dcu.europeana.arch.service.edm.EdmExtractUtils;
import gr.dcu.europeana.arch.service.edm.ElementExtractionData;
import gr.dcu.utils.XMLUtils;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 *
 * @author Vangelis Nomikos
 */
@Slf4j
public class XMLUtilsTest {
    
    public static void main(String[] args) {
        
        
        XMLUtilsTest xmlUtilsTest = new XMLUtilsTest();
        File f = xmlUtilsTest.getFileFromResource();
        
        if(f.exists() && f.isFile()) {
            System.out.println("Exists");
            
            try {
                Document doc = XMLUtils.parse(f, true);
                // String itemContent = XMLUtils.transform(doc);

                // Get subjects
                log.info("------------------------------");
                NodeList nodeList = XMLUtils.getNodeList(doc, "//dc:subject");
                log.info("File: {} #Subjects: {}", f.getName(), nodeList.getLength());
                
                List<ElementExtractionData> extractDataList = EdmExtractUtils.extractNodeData(nodeList, true);
                for(ElementExtractionData extractData : extractDataList) {
                    log.info("{}",extractData);
                }
                
                                
                log.info("------------------------------");
                
                List<String> values = XMLUtils.getElementValues(doc, "//dc:subject");
                log.info("File: {} Subjects: {}", f.getName(), values.toString());
                
                List<String> attributes = XMLUtils.getAttributeValues(doc, "//dc:subject/@rdf:resource");
                log.info("==> File: {} #AttributeValues: {}", f.getName(), attributes.toString());
                
                List<String> valuesToAdd = new LinkedList<>();
                valuesToAdd.add("Test Subject 1");
                valuesToAdd.add("Test Subject 2");
                doc = XMLUtils.appendElements(doc, "//edm:ProvidedCHO", "dc:subject", valuesToAdd);
                String itemContent = XMLUtils.transform(doc);
                log.info(itemContent);
            } catch(IOException | ParserConfigurationException | SAXException | 
                        TransformerException | XPathExpressionException ex) {
                    log.error("Cannot parse file. File: {}", f.getAbsolutePath());
                
            }
        } else {
            System.out.println("Not Found");
        }
    }
    
    private File getFileFromResource() {
        File f = new File(getClass().getClassLoader().getResource("edm1.xml").getFile());
        
        return f;
    }
}
