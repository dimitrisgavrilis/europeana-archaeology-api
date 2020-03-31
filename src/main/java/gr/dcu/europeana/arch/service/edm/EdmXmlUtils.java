package gr.dcu.europeana.arch.service.edm;

import gr.dcu.europeana.arch.model.AatSubjectEntity;
import gr.dcu.europeana.arch.model.SpatialTermEntity;
import gr.dcu.europeana.arch.model.SubjectTermEntity;
import gr.dcu.utils.MoReNamespaceContext;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Vangelis Nomikos
 */
@Slf4j
public class EdmXmlUtils {
    
    public static Document appendThematicElements(Document doc, String xPathExpr, String label,
                                                  List<SubjectTermEntity> subjectTermEntities, Map<String, AatSubjectEntity> aatSubjectMap) throws XPathExpressionException {
		
            try {
                XPath xPath = XPathFactory.newInstance().newXPath();
                xPath.setNamespaceContext(new MoReNamespaceContext());
                NodeList nodeList = (NodeList)xPath.compile(xPathExpr).evaluate(doc, XPathConstants.NODESET);

                for(int i=0; i<nodeList.getLength(); i++) {
                    Element element = (Element) nodeList.item(i);

                    int termsWithoutMappings = 0;
                    for(SubjectTermEntity subjectTermEntity : subjectTermEntities) {
                        
                        if(subjectTermEntity.getAatUid() != null && !subjectTermEntity.getAatUid().isEmpty()) {
                            
                            AatSubjectEntity aatSubjectEntity = aatSubjectMap.get(subjectTermEntity.getAatUid());
                            
                            Element childElement = doc.createElement(label);
                            childElement.setAttribute("rdf:resource", aatSubjectEntity.getUri());
                            // childElement.appendChild(doc.createTextNode(spatialTerm));
                            element.appendChild(childElement);
                        } else {
                            termsWithoutMappings ++;
                        }
                    }
                    
                    log.info("Thematic Terms wo mappings. #Size: {}", termsWithoutMappings);
                }

            } catch (XPathExpressionException ex) {
                log.error("",ex);
                throw ex;
            } 

            return doc;
	}
    
    public static Document appendSpatialElements(Document doc, String xPathExpr, String label, 
            List<SpatialTermEntity> spatialTermEntities) throws XPathExpressionException {
		
            try {
                XPath xPath = XPathFactory.newInstance().newXPath();
                xPath.setNamespaceContext(new MoReNamespaceContext());
                NodeList nodeList = (NodeList)xPath.compile(xPathExpr).evaluate(doc, XPathConstants.NODESET);

                for(int i=0; i<nodeList.getLength(); i++) {
                    Element element = (Element) nodeList.item(i);

                    int termsWithoutMappings = 0;
                    for(SpatialTermEntity spatialTermEntity : spatialTermEntities) {
                        
                        if(spatialTermEntity.getGeonameId() != null && !spatialTermEntity.getGeonameId().isEmpty()) {
                            
                            // AatSubject aatSubject = aatSubjectMap.get(spatialTerm.getAatUid());
                            
                            Element childElement = doc.createElement(label);
                            childElement.setAttribute("rdf:resource", "https://www.geonames.org/" + spatialTermEntity.getGeonameId());
                            // childElement.appendChild(doc.createTextNode(spatialTerm));
                            element.appendChild(childElement);
                        } else {
                            termsWithoutMappings ++;
                        }
                    }
                    
                    log.info("Spatial Terms wo mappings. #Size: {}", termsWithoutMappings);
                }

            } catch (XPathExpressionException ex) {
                log.error("",ex);
                throw ex;
            } 

            return doc;
	}
}
