package gr.dcu.europeana.arch.service.edm;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.xml.xpath.XPathExpressionException;
import lombok.extern.slf4j.Slf4j;
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
     * 
     * @param node
     * @return
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
     * 
     * @param nodeList
     * @param skipEmptyValues
     * @return
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
     * 
     * @param extractionResult
     * @return
     */
    public static ElementExtractionDataCategories splitExtractionDataInCategories(
            List<EdmFileTermExtractionResult> extractionResult)  {
        
//         List<ElementExtractionData> extractionDataList = new 
                
        Set<ElementExtractionData> thematicElementValues = new HashSet<>();
        Set<ElementExtractionData> spatialElementValues = new HashSet<>();
        Set<ElementExtractionData> temporalElementValues = new HashSet<>();
        for(EdmFileTermExtractionResult edmFileExtractionresult : extractionResult) {
            for(ElementExtractionData elementExtractionData : edmFileExtractionresult.getExtractionData()) {
            
                switch(elementExtractionData.getElementName()) {
                    case DC_SUBJECT:
                        thematicElementValues.add(elementExtractionData);
                        break;
                    case DC_TYPE:
                        thematicElementValues.add(elementExtractionData);
                        break;
                    case DC_DATE:
                        temporalElementValues.add(elementExtractionData);
                        break;
                    case DC_TERMS_TEMPORAL:
                        temporalElementValues.add(elementExtractionData);
                        break;
                    case DC_TERMS_CREATED:
                        temporalElementValues.add(elementExtractionData);
                        break;
                    case DC_TERMS_SPATIAL:
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
        
        return  categories;
        
    }
    

    
}
