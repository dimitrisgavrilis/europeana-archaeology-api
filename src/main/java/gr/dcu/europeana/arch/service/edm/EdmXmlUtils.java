package gr.dcu.europeana.arch.service.edm;

import gr.dcu.europeana.arch.model.*;
import gr.dcu.europeana.arch.service.AatService;
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
    /**
     * Append multiple elements (dc:subject) to XML doc.
     * @param doc the xml doc
     * @param subjectTermEntities subject terms to append
     * @param aatSubjectMap utility map for aat subjects
     */
    public static void appendThematicElements(
            Document doc, String xPathExpr, String label, List<SubjectTermEntity> subjectTermEntities,
            Map<String, AatSubjectEntity> aatSubjectMap) throws XPathExpressionException {
		
            try {
                XPath xPath = XPathFactory.newInstance().newXPath();
                xPath.setNamespaceContext(new MoReNamespaceContext());
                NodeList nodeList = (NodeList)xPath.compile(xPathExpr).evaluate(doc, XPathConstants.NODESET);

                for(int i=0; i<nodeList.getLength(); i++) {
                    Element element = (Element) nodeList.item(i);

                    int termsWithoutMappings = 0;
                    for(SubjectTermEntity subjectTermEntity : subjectTermEntities) {
                        
                        if(subjectTermEntity.getAatUid() != null  && !subjectTermEntity.getAatUid().isEmpty()) {
                            
                            // AatSubjectEntity aatSubjectEntity = aatSubjectMap.get(subjectTermEntity.getAatUid());
                            String aatUri = AatService.AAT_URI_PREFIX + subjectTermEntity.getAatUid();
                            Element childElement = doc.createElement(label);
                            // childElement.setAttribute("rdf:resource", aatSubjectEntity.getUri());
                            childElement.setAttribute("rdf:resource", aatUri);
                            // childElement.appendChild(doc.createTextNode(spatialTerm));
                            element.appendChild(childElement);
                            log.debug("Thematic term added. {}", childElement.toString());
                        } else {
                            termsWithoutMappings ++;
                            log.debug("Thematic native term without mapping => {}", subjectTermEntity.getNativeTerm());
                        }
                    }
                    
                    log.info("Thematic Terms wo mappings. #Size: {}", termsWithoutMappings);
                }

            } catch (XPathExpressionException ex) {
                log.error("",ex);
                throw ex;
            }
	}

    /**
     * Append multiple elements (dcterms:spatial) to XML doc.
     * @param doc the xml doc
     * @param spatialTermEntities spatial terms to append
     */
    public static void appendSpatialElements(Document doc, String xPathExpr, String label,
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
                            Element childElement = doc.createElement(label);
                            childElement.setAttribute("rdf:resource", "https://www.geonames.org/" + spatialTermEntity.getGeonameId());
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
	}

    /**
     * Append multiple elements (edm:TimeSpan, dc:date) to XML doc.
     * For each temporal term append two elements. A edm:Timespan and a dc:date
     * @param doc the xml doc
     * @param temporalTermEntities temporal terms to append
     * @param earchTemporalEntityMap utility map for earch temporal
     */
    public static void appendTemporalElements(Document doc, String xPathExpr, List<TemporalTermEntity> temporalTermEntities,
                                                  Map<String, EArchTemporalEntity> earchTemporalEntityMap) throws XPathExpressionException {

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new MoReNamespaceContext());
            NodeList nodeList = (NodeList)xPath.compile(xPathExpr).evaluate(doc, XPathConstants.NODESET);

            for(int i=0; i<nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);

                int termsWithoutMappings = 0;
                for(TemporalTermEntity temporalTermEntity : temporalTermEntities) {

                    if(temporalTermEntity.getAatUid() != null && !temporalTermEntity.getAatUid().isEmpty()) {

                        EArchTemporalEntity eArchTemporalEntity = earchTemporalEntityMap.get(temporalTermEntity.getAatUid());

                        String rdfAboutLabel = "EUROPEANAARCH_" + temporalTermEntity.getId() + "/TMP.1";

                        // ~~~ edm:TimeSpan ~~~
                        Element edmTimespanElement = doc.createElement("edm:TimeSpan");
                        edmTimespanElement.setAttribute("rdf:about", rdfAboutLabel);
                        element.appendChild(edmTimespanElement);

                        // skos:prefLabel
                        Element skosPrefLabelElement = doc.createElement("skos:prefLabel");
                        skosPrefLabelElement.appendChild(doc.createTextNode(eArchTemporalEntity.getLabel()));
                        edmTimespanElement.appendChild(skosPrefLabelElement);

                        // edm:begin
                        Element edmBeginElement = doc.createElement("edm:begin");
                        edmBeginElement.appendChild(doc.createTextNode(eArchTemporalEntity.getStartYear()));
                        edmTimespanElement.appendChild(edmBeginElement);

                        // edm:end
                        Element edmEndElement = doc.createElement("edm:end");
                        edmEndElement.appendChild(doc.createTextNode(eArchTemporalEntity.getEndYear()));
                        edmTimespanElement.appendChild(edmEndElement);

                        // owl:sameAs - aat_uri
                        Element owlSameAsAatUriElement = doc.createElement("owl:sameAs");
                        owlSameAsAatUriElement.appendChild(doc.createTextNode(eArchTemporalEntity.getAatUri()));
                        edmTimespanElement.appendChild(owlSameAsAatUriElement);

                        // owl:sameAs - wikidata_uri
                        Element owlSameAsWikidataUriElement = doc.createElement("owl:sameAs");
                        owlSameAsWikidataUriElement.appendChild(doc.createTextNode(eArchTemporalEntity.getWikidataUri()));
                        edmTimespanElement.appendChild(owlSameAsWikidataUriElement);

                        // ~~~ dc:date ~~~
                        Element dcDateElement = doc.createElement("dc:date");
                        dcDateElement.setAttribute("rdf:resource", rdfAboutLabel);
                        element.appendChild(dcDateElement);

                    } else {
                        termsWithoutMappings ++;
                    }
                }

                log.debug("Temporal Terms wo mappings. #Size: {}", termsWithoutMappings);
            }

        } catch (XPathExpressionException ex) {
            log.error("",ex);
            throw ex;
        }
    }
}
