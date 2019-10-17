package gr.dcu.europeana.arch.service.edm;

import java.util.LinkedList;
import java.util.List;
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
     * @return
     * @throws XPathExpressionException 
     */
    public static List<ElementExtractionData> extractNodeData(NodeList nodeList) throws XPathExpressionException {
        
        List<ElementExtractionData> elementExtractDataList = new LinkedList<>();
        for(int i=0; i<nodeList.getLength(); i++) {
            elementExtractDataList.add(extractNodeData(nodeList.item(i)));
        }  

        return elementExtractDataList;
    }
    

    
}
