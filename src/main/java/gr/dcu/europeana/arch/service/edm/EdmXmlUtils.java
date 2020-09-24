package gr.dcu.europeana.arch.service.edm;

import gr.dcu.europeana.arch.domain.entity.*;
import gr.dcu.europeana.arch.service.VocabularyService;
import gr.dcu.utils.MoReNamespaceContext;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import io.micrometer.core.instrument.util.StringUtils;
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
    public static int appendThematicElements(
            Document doc, String xPathExpr, String label, List<SubjectTermEntity> subjectTermEntities,
            Map<String, AatSubjectEntity> aatSubjectMap) throws XPathExpressionException {

            int elementsAddedCount = 0;

            try {
                XPath xPath = XPathFactory.newInstance().newXPath();
                xPath.setNamespaceContext(new MoReNamespaceContext());
                NodeList nodeList = (NodeList)xPath.compile(xPathExpr).evaluate(doc, XPathConstants.NODESET);

                for(int i=0; i<nodeList.getLength(); i++) {
                    Element element = (Element) nodeList.item(i);

                    int termsWithoutMappings = 0;
                    for(SubjectTermEntity subjectTermEntity : subjectTermEntities) {
                        
                        if(StringUtils.isNotBlank(subjectTermEntity.getAatUid())) {
                            Element childElement = doc.createElement(label);
                            childElement.setAttribute("rdf:resource",
                                    VocabularyService.toAatLodUriFromAatUid(subjectTermEntity.getAatUid()));
                            element.appendChild(childElement);
                            elementsAddedCount++;
                            log.debug("Thematic term added. {}", childElement.toString());
                        } else {
                            termsWithoutMappings ++;
                            log.debug("Thematic native term without mapping => {}", subjectTermEntity.getNativeTerm());
                        }
                    }
                    
                    log.debug("Thematic Terms wo mappings. #Size: {}", termsWithoutMappings);
                }

            } catch (XPathExpressionException ex) {
                log.error("",ex);
                throw ex;
            }

            return elementsAddedCount;
	}

    /**
     * Append multiple elements (dcterms:spatial) to XML doc.
     * @param doc the xml doc
     * @param spatialTermEntities spatial terms to append
     */
    public static int appendSpatialElements(Document doc, String xPathExpr, String label,
            List<SpatialTermEntity> spatialTermEntities) throws XPathExpressionException {

            int elementsAddedCount = 0;

            try {
                XPath xPath = XPathFactory.newInstance().newXPath();
                xPath.setNamespaceContext(new MoReNamespaceContext());
                NodeList nodeList = (NodeList)xPath.compile(xPathExpr).evaluate(doc, XPathConstants.NODESET);

                for(int i=0; i<nodeList.getLength(); i++) {
                    Element element = (Element) nodeList.item(i);

                    int termsWithoutMappings = 0;
                    for(SpatialTermEntity spatialTermEntity : spatialTermEntities) {
                        
                        if(StringUtils.isNotBlank(spatialTermEntity.getGeonameId())) {
                            Element childElement = doc.createElement(label);
                            childElement.setAttribute("rdf:resource", "https://www.geonames.org/" + spatialTermEntity.getGeonameId());
                            element.appendChild(childElement);
                            elementsAddedCount++;
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

            return elementsAddedCount;
	}

    /**
     * Append multiple elements (edm:TimeSpan, dc:date) to XML doc.
     * For each temporal term append two elements. A edm:Timespan and a dc:date
     * @param doc the xml doc
     * @param temporalTermEntities temporal terms to append
     * @param earchTemporalEntityMap utility map for earch temporal
     */
    public static int appendTemporalElementEdmTimeSpan(Document doc, String xPathExpr, List<TemporalTermEntity> temporalTermEntities,
                                                        Map<String, EArchTemporalEntity> earchTemporalEntityMap) throws XPathExpressionException {

        int elementsAddedCount = 0;

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new MoReNamespaceContext());
            NodeList nodeList = (NodeList)xPath.compile(xPathExpr).evaluate(doc, XPathConstants.NODESET);

            int currentIndex = 1;
            for(int i=0; i<nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);

                int termsWithoutMappingCount = 0;
                List<String> missingAatUidInEarchTemporalList = new LinkedList<>();
                // int termsWithMissingAatUidInEarchTemporalCount = 0;
                for(TemporalTermEntity temporalTermEntity : temporalTermEntities) {

                    if(!StringUtils.isBlank(temporalTermEntity.getAatUid())) {
                    // temporalTermEntity.getAatUid() != null && !temporalTermEntity.getAatUid().isEmpty()) {

                        // ~~~ edm:TimeSpan ~~~
                        String rdfAboutLabel = "EUROPEANAARCH_" + temporalTermEntity.getId() + "/TMP." + currentIndex;
                        currentIndex++;
                        Element edmTimespanElement = doc.createElement("edm:TimeSpan");
                        edmTimespanElement.setAttribute("rdf:about", rdfAboutLabel);
                        element.appendChild(edmTimespanElement);

                        // skos:prefLabel
                        if (!StringUtils.isBlank(temporalTermEntity.getEarchTemporalLabel())) {
                            Element skosPrefLabelElement = doc.createElement("skos:prefLabel");
                            skosPrefLabelElement.appendChild(doc.createTextNode(temporalTermEntity.getEarchTemporalLabel()));
                            edmTimespanElement.appendChild(skosPrefLabelElement);
                        }

                        // Get start_year and end_year at instance level

                        // edm:begin
                        if (!StringUtils.isBlank(temporalTermEntity.getStartYear())) {
                            Element edmBeginElement = doc.createElement("edm:begin");
                            edmBeginElement.appendChild(doc.createTextNode(temporalTermEntity.getStartYear()));
                            edmTimespanElement.appendChild(edmBeginElement);
                        }

                        // edm:end
                        if (!StringUtils.isBlank(temporalTermEntity.getEndYear())) {
                            Element edmEndElement = doc.createElement("edm:end");
                            edmEndElement.appendChild(doc.createTextNode(temporalTermEntity.getEndYear()));
                            edmTimespanElement.appendChild(edmEndElement);
                        }

                        // owl:sameAs - aat_uri
                        if (!StringUtils.isBlank(temporalTermEntity.getAatUid())) {
                            Element owlSameAsAatUriElement = doc.createElement("owl:sameAs");
                            owlSameAsAatUriElement.setAttribute("rdf:resource",
                                    VocabularyService.toAatLodUriFromAatUid(temporalTermEntity.getAatUid()));
                            edmTimespanElement.appendChild(owlSameAsAatUriElement);
                        }

                        // Get wikidata_uri from EArchTemporal
                        if(earchTemporalEntityMap.containsKey(temporalTermEntity.getAatUid())) {

                            EArchTemporalEntity eArchTemporalEntity = earchTemporalEntityMap.get(temporalTermEntity.getAatUid());

                            // owl:sameAs - wikidata_uri
                            if (!StringUtils.isBlank(eArchTemporalEntity.getWikidataUri())) {
                                Element owlSameAsWikidataUriElement = doc.createElement("owl:sameAs");
                                owlSameAsWikidataUriElement.setAttribute("rdf:resource", eArchTemporalEntity.getWikidataUri());
                                edmTimespanElement.appendChild(owlSameAsWikidataUriElement);
                            }

                            // ~~~ dc:date ~~~
                            // Element dcDateElement = doc.createElement("dc:date");
                            // dcDateElement.setAttribute("rdf:resource", rdfAboutLabel);
                            // element.appendChild(dcDateElement);
                        } else {
                            missingAatUidInEarchTemporalList.add(temporalTermEntity.getAatUid());
                        }

                        elementsAddedCount++;
                    } else {
                        termsWithoutMappingCount ++;
                    }
                }

                log.debug("Temporal Terms wo mappings. #Size: {}", termsWithoutMappingCount);
                log.debug("Temporal Terms with mappings but no earch temporal. #Size: {} Missing earch aat terms:{}",
                        missingAatUidInEarchTemporalList.size(), missingAatUidInEarchTemporalList);
            }

        } catch (XPathExpressionException ex) {
            log.error("",ex);
            throw ex;
        }

        return elementsAddedCount;
    }

    public static int appendTemporalElementsDcDate(Document doc, String xPathExpr, List<TemporalTermEntity> temporalTermEntities,
                                                    Map<String, EArchTemporalEntity> earchTemporalEntityMap) throws XPathExpressionException {

        int elementsAddedCount = 0;

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new MoReNamespaceContext());
            NodeList nodeList = (NodeList)xPath.compile(xPathExpr).evaluate(doc, XPathConstants.NODESET);

            int currentIndex = 1;
            for(int i=0; i<nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);

                int termsWithoutMappings = 0;
                for(TemporalTermEntity temporalTermEntity : temporalTermEntities) {

                    if(temporalTermEntity.getAatUid() != null && !temporalTermEntity.getAatUid().isEmpty()) {

                        EArchTemporalEntity eArchTemporalEntity = earchTemporalEntityMap.get(temporalTermEntity.getAatUid());

                        String rdfAboutLabel = "EUROPEANAARCH_" + temporalTermEntity.getId() + "/TMP." + currentIndex;
                        currentIndex++;

                        // ~~~ dc:date ~~~
                        Element dcDateElement = doc.createElement("dc:date");
                        dcDateElement.setAttribute("rdf:resource", rdfAboutLabel);
                        element.appendChild(dcDateElement);
                        elementsAddedCount++;

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

        return  elementsAddedCount;
    }
}
