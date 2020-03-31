package gr.dcu.europeana.arch.service.edm;

import java.util.*;
import javax.xml.xpath.XPathExpressionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.nio.client.HttpAsyncClient;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Vangelis Nomikos
 */
@Slf4j
public class EdmExtractUtils {
    
    public static final String DC_SUBJECT        = "dc:subject";
    public static final String DC_TYPE           = "dc:type";
    public static final String DC_DATE           = "dc:date";
    public static final String DC_TERMS_TEMPORAL = "dcterms:temporal";
    public static final String DC_TERMS_CREATED  = "dcterms:created";
    public static final String DC_TERMS_SPATIAL  = "dcterms:spatial";
    
    /**
     * Extract data from an XML node
     * @throws XPathExpressionException 
     */
    public static ElementExtractionData extractNodeData(Node node) throws XPathExpressionException {
        
        Element element = (Element) node;
        String elementName = element.getNodeName();
        String elementValue = element.getTextContent().trim();

        String xmlLangValue = "";
        Node xmlLangAttrNode = element.getAttributes().getNamedItem("xml:lang");
        if(xmlLangAttrNode != null) {
            xmlLangValue = xmlLangAttrNode.getNodeValue();
        }

        String rdfResourceValue = "";
        Node rdfResourceAttrNode = element.getAttributes().getNamedItem("rdf:resource");
        if(rdfResourceAttrNode != null) {
            rdfResourceValue = rdfResourceAttrNode.getNodeValue();
        }

        ElementExtractionData elementExtractData = new ElementExtractionData();
        elementExtractData.setElementName(elementName);
        elementExtractData.setElementValue(elementValue);
        elementExtractData.setXmlLangAttrValue(xmlLangValue);
        elementExtractData.setRdfResourceAttrValue(rdfResourceValue);
        
        return elementExtractData;
    }
    
    /**
     * Extract data from a list of XML nodes
     * @param nodeList
     * @param skipEmptyValues
     * @throws XPathExpressionException 
     */
    public static List<ElementExtractionData> extractNodeData(NodeList nodeList, boolean skipEmptyValues) throws XPathExpressionException {
        
        List<ElementExtractionData> elementExtractDataList = new LinkedList<>();
        for(int i=0; i<nodeList.getLength(); i++) {
            
            // if element value is empty
            if(skipEmptyValues && nodeList.item(i).getTextContent().trim().isEmpty()) {
                 continue;
            }
            elementExtractDataList.add(extractNodeData(nodeList.item(i)));
        }  

        return elementExtractDataList;
    }
    
    /**
     * Split extraction data in 3 categories. a) thematic, b) temporal, c) spatial
     * @param extractionResult list of extraction result from a set of files
     * @return
     */
    public static ElementExtractionDataCategories splitExtractionDataInCategories(
            List<EdmFileTermExtractionResult> extractionResult)  {

        Map<ElementExtractionData, Integer> thematicElementValuesCountMap = new HashMap<>();
        Map<ElementExtractionData, Integer> spatialElementValuesCountMap = new HashMap<>();
        Map<ElementExtractionData, Integer> temporalElementValuesCountMap = new HashMap<>();

        Set<ElementExtractionData> thematicElementValues = new HashSet<>();
        Set<ElementExtractionData> spatialElementValues = new HashSet<>();
        Set<ElementExtractionData> temporalElementValues = new HashSet<>();
        for(EdmFileTermExtractionResult edmFileExtractionResult : extractionResult) {
            for(ElementExtractionData elementExtractionData : edmFileExtractionResult.getExtractionData()) {
            
                switch(elementExtractionData.getElementName()) {
                    case DC_SUBJECT:
                    case DC_TYPE:
                        thematicElementValuesCountMap.merge(elementExtractionData, 1, Integer::sum);
                        thematicElementValues.add(elementExtractionData);
                        break;
                    case DC_DATE:
                    case DC_TERMS_TEMPORAL:
                    case DC_TERMS_CREATED:
                        temporalElementValuesCountMap.merge(elementExtractionData, 1, Integer::sum);
                        temporalElementValues.add(elementExtractionData);
                        break;
                    case DC_TERMS_SPATIAL:
                        spatialElementValuesCountMap.merge(elementExtractionData, 1, Integer::sum);
                        spatialElementValues.add(elementExtractionData);
                        break;
                    default:
                        log.warn("Unknown element: {}", elementExtractionData.getElementName());

                }
            }
        }
        
        // Build categories
        ElementExtractionDataCategories categories = new ElementExtractionDataCategories();
        categories.setThematicElementValues(thematicElementValues);
        categories.setTemporalElementValues(temporalElementValues);
        categories.setSpatialElementValues(spatialElementValues);

        categories.setThematicElementValuesCountMap(thematicElementValuesCountMap);
        categories.setTemporalElementValuesCountMap(temporalElementValuesCountMap);
        categories.setSpatialElementValuesCountMap(spatialElementValuesCountMap);

        // Debug count
        log.info("Total: {}", extractionResult.size());
        log.info("Thematic: {} - {}", thematicElementValuesCountMap.size(), thematicElementValues.size());
        log.info("Spatial:  {} - {}", spatialElementValuesCountMap.size(), spatialElementValues.size());
        log.info("Temporal: {} - {}", temporalElementValuesCountMap.size(), temporalElementValues.size());

        /*
        for(ElementExtractionData elementExtractionData : temporalElementValuesCountMap.keySet()) {
            log.info("{} - {}", elementExtractionData.getElementValue(), temporalElementValuesCountMap.get(elementExtractionData));
        }*/
        
        return  categories;
    }
}
