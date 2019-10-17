package gr.dcu.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Slf4j
public class XMLUtils {
    
        /////////// This implementation is not thread-safe /////////////////////
        private static DocumentBuilderFactory docBuilderFactory;
        
        private static DocumentBuilder docBuilder;
	
        
        /**
         * 
         * @param xml
         * @param nsAware
         * @return
         * @throws ParserConfigurationException
         * @throws SAXException
         * @throws IOException 
         */
        public static Document parse(String xml, boolean nsAware) throws ParserConfigurationException, SAXException, IOException {
		Document doc = null;
		
		try {
                    InputStream xmlInputStream = IOUtils.toInputStream(xml, "UTF-8");
                    
                    if(docBuilderFactory == null){
                        docBuilderFactory = DocumentBuilderFactory.newInstance();
                        docBuilderFactory.setNamespaceAware(nsAware);
                    }
                    if(docBuilder == null)
                        docBuilder = docBuilderFactory.newDocumentBuilder();
                    doc = docBuilder.parse(xmlInputStream);
                    docBuilder.reset();
                    xmlInputStream.close();
		} catch (ParserConfigurationException | SAXException | IOException ex) {
                    log.error("", ex);
                    throw ex;
		}
		
		return doc;
	}
        
        /**
         * 
         * @param xmlFile
         * @param nsAware
         * @return
         * @throws ParserConfigurationException
         * @throws SAXException
         * @throws IOException 
         */
        public static Document parse(File xmlFile, boolean nsAware) throws ParserConfigurationException, SAXException, IOException {
            Document doc = null;

            try {
                if(docBuilderFactory == null){
                    docBuilderFactory = DocumentBuilderFactory.newInstance();
                    docBuilderFactory.setNamespaceAware(nsAware);
                }
                if(docBuilder == null)
                    docBuilder = docBuilderFactory.newDocumentBuilder();                
                doc = docBuilder.parse(xmlFile);
                docBuilder.reset();
            } catch (ParserConfigurationException | SAXException | IOException ex) {
                log.error("",ex);
                throw ex;
            }

            return doc;
        }
        
       
        
        /**
         * 
         * @return
         * @throws ParserConfigurationException 
         */
        public static Document createDocument() throws ParserConfigurationException {

            Document doc = null;

            try {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

                doc = docBuilder.newDocument();
            } catch(ParserConfigurationException ex) {
                throw ex;
            }

            return doc;
        }
        
        
        /**
         * 
         * @param doc
         * @param xPathExpr
         * @param label
         * @param value
         * @return
         * @throws XPathExpressionException 
         */
	public static Document appendElement(Document doc, String xPathExpr, String label, String value) throws XPathExpressionException {
		
            try {
                XPath xPath = XPathFactory.newInstance().newXPath();
                xPath.setNamespaceContext(new MoReNamespaceContext());
                NodeList nodeList = (NodeList)xPath.compile(xPathExpr).evaluate(doc, XPathConstants.NODESET);

                for(int i=0; i<nodeList.getLength(); i++) {
                    Element element = (Element) nodeList.item(i);

                    Element childElement = doc.createElement(label);
                    childElement.appendChild(doc.createTextNode(value));
                    element.appendChild(childElement);
                }

            } catch (XPathExpressionException ex) {
                log.error("",ex);
                throw ex;
            } 

            return doc;
	}
        
        
        public static Document appendElements(Document doc, String xPathExpr, String label, List<String> values) throws XPathExpressionException {
		
            try {
                XPath xPath = XPathFactory.newInstance().newXPath();
                xPath.setNamespaceContext(new MoReNamespaceContext());
                NodeList nodeList = (NodeList)xPath.compile(xPathExpr).evaluate(doc, XPathConstants.NODESET);

                for(int i=0; i<nodeList.getLength(); i++) {
                    Element element = (Element) nodeList.item(i);

                    for(String value : values) {
                        Element childElement = doc.createElement(label);
                        childElement.appendChild(doc.createTextNode(value));
                        element.appendChild(childElement);
                    }
                }

            } catch (XPathExpressionException ex) {
                log.error("",ex);
                throw ex;
            } 

            return doc;
	}
        
        /**
         * 
         * @param doc
         * @param xPathExpr
         * @return
         * @throws XPathExpressionException 
         */
        public static NodeList getNodeList(Document doc, String xPathExpr) throws XPathExpressionException {
            NodeList nList = null;
            
            try {
                XPath xpath = XPathFactory.newInstance().newXPath();
                xpath.setNamespaceContext(new MoReNamespaceContext());
                nList = (NodeList) xpath.compile(xPathExpr).evaluate(doc, XPathConstants.NODESET);
            } catch (XPathExpressionException ex) {
                log.error("",ex);
                throw(ex);
            }
            
            return nList;
        }
        
        /**
         * 
         * @param node
         * @param xPathExpr
         * @return
         * @throws XPathExpressionException 
         */
        public static NodeList getNodeList(Node node, String xPathExpr) throws XPathExpressionException {
            NodeList nList = null;
            
            try {
                XPath xpath = XPathFactory.newInstance().newXPath();
                xpath.setNamespaceContext(new MoReNamespaceContext());
                nList = (NodeList) xpath.compile(xPathExpr).evaluate(node, XPathConstants.NODESET);
            } catch (XPathExpressionException ex) {
                log.error("",ex);
                throw(ex);
            }
            
            return nList;
        }
        
        
        /**
         * 
         * @param xPathQuery
         * @param doc
         * @return
         * @throws XPathExpressionException 
         */
        public static NodeList getNodes(Document doc, String xPathQuery) throws XPathExpressionException {

            NodeList nList = null;

            try {
                XPath xpath = XPathFactory.newInstance().newXPath();
                nList = (NodeList)xpath.compile(xPathQuery).evaluate(doc, XPathConstants.NODESET);

            } catch (XPathExpressionException ex) {
                log.error("",ex);
                throw ex;
            } 

            return nList;
        }
    
        /**
         * 
         * @param xPathQuery
         * @param node
         * @return
         * @throws XPathExpressionException 
         */
        /*
        public static NodeList getNodes(Node node, String xPathQuery) throws XPathExpressionException {

            NodeList nList = null;

            try {
                XPath xpath = XPathFactory.newInstance().newXPath();
                nList = (NodeList)xpath.compile(xPathQuery).evaluate(node, XPathConstants.NODESET);
            } catch (XPathExpressionException ex) {
                log.error(ex);
                throw ex;
            } 

            return nList;
        }
        */
        

	/**
         * 
         * @param doc
         * @param xPathExpr
         * @return
         * @throws XPathExpressionException 
         */
	public static String getElementValue(Document doc, String xPathExpr) throws XPathExpressionException {
		
                String value = null;
		
                try {
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    xpath.setNamespaceContext(new MoReNamespaceContext());
                    NodeList nodeList = (NodeList)xpath.compile(xPathExpr).evaluate(doc, XPathConstants.NODESET);

                    // log.info( "==> " + nodeList.getLength());
                    for(int i=0; i<nodeList.getLength(); i++) {
                        Element element = (Element) nodeList.item(i);
                        value = element.getTextContent().trim();
                        // log.info(element.getNodeName() + " - " + value);
                    }
		} catch (XPathExpressionException ex) {
                    log.error("",ex);
                    throw ex;
		} 
		
		return value;
	}
        
        /**
         * 
         * @param doc
         * @param xPathExpr
         * @return
         * @throws XPathExpressionException 
         */
        public static List<String> getElementValues(Document doc, String xPathExpr) throws XPathExpressionException {
		
                List<String> values = new LinkedList<>();
		
                try {
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    xpath.setNamespaceContext(new MoReNamespaceContext());
                    NodeList nodeList = (NodeList)xpath.compile(xPathExpr).evaluate(doc, XPathConstants.NODESET);

                    // log.info( "==> " + nodeList.getLength());
                    for(int i=0; i<nodeList.getLength(); i++) {
                        Element element = (Element) nodeList.item(i);
                        values.add(element.getTextContent().trim());
                        // log.info(element.getNodeName() + " - " + value);
                    }
		} catch (XPathExpressionException ex) {
                    log.error("",ex);
                    throw ex;
		} 
		
		return values;
	}
        
        /**
         * e.g. "//dc:subject"
         * e.g. "//dc:subject/@rdf:resource"
         * @param document
         * @param xpathExpression
         * @return
         * @throws XPathExpressionException 
         */
        public static List<String> getAttributeValues(Document document, String xpathExpression) throws XPathExpressionException {
            
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new MoReNamespaceContext());

            List<String> values = new ArrayList<>();
            try {
                // Create XPathExpression object
                XPathExpression expr = xpath.compile(xpathExpression);

                // Evaluate expression result on XML document
                NodeList nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

                for (int i = 0; i < nodes.getLength(); i++) {
                    values.add(nodes.item(i).getNodeValue());
                }

            } catch (XPathExpressionException ex) {
                log.error("",ex);
                throw ex;
            }
            return values;
        }
        
        /**
         * 
         * @param xPathQuery
         * @param node
         * @return
         * @throws XPathExpressionException 
         */
        /*
        public static String getElementValue(String xPathQuery, Node node) throws XPathExpressionException {

            String value = null;

            try {
                XPath xpath = XPathFactory.newInstance().newXPath();
                NodeList nodeList = 
                                (NodeList)xpath.compile(xPathQuery).evaluate(node, XPathConstants.NODESET);

                for(int i=0; i<nodeList.getLength(); i++) {
                        Element element = (Element) nodeList.item(i);
                        value = element.getTextContent().trim();
                }
            } catch (XPathExpressionException ex) {
                    log.error(ex);
                    throw ex;
            } 

            return value;
        }
        
	*/
	/**
         * 
         * @param doc
         * @param xPathExpr
         * @param value
         * @return
         * @throws XPathExpressionException 
         */
	public static Document setElementValue(Document doc, String xPathExpr, String value) throws XPathExpressionException {
		
            try {
                XPath xPath = XPathFactory.newInstance().newXPath();
                NodeList nodeList = (NodeList)xPath.compile(xPathExpr).evaluate(doc, XPathConstants.NODESET);

                for(int i=0; i<nodeList.getLength(); i++) {
                    Element element = (Element) nodeList.item(i);
                    element.setTextContent(value);
                }
            } catch (XPathExpressionException ex) {
                log.error("",ex);
                throw ex;
            } 

            return doc;
	}
	
        /**
         * 
         * @param xmlFile
         * @return
         * @throws ParserConfigurationException
         * @throws SAXException
         * @throws IOException
         * @throws TransformerException 
         */
        public static String parseToString(File xmlFile, boolean nsAware) throws ParserConfigurationException, SAXException, IOException, TransformerException {
            String content = null;

            try {
                if(docBuilderFactory == null){
                    docBuilderFactory = DocumentBuilderFactory.newInstance();
                    docBuilderFactory.setNamespaceAware(nsAware);
                }
                if(docBuilder == null)
                    docBuilder = docBuilderFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(xmlFile);

                content = transform(doc);
                
                docBuilder.reset();
            } catch (ParserConfigurationException | SAXException | IOException | TransformerException ex) {
                log.error("",ex);
                throw ex;
            }

            return content;
        }
        
        
        
	/**
         * 
         * @param sourceDoc
         * @return
         * @throws TransformerConfigurationException
         * @throws TransformerException 
         */
        public static String transform(Document sourceDoc) throws TransformerConfigurationException, TransformerException, IOException {
            String targetXml = null;

            try {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

                Source transformedSource = new DOMSource(sourceDoc);
                StringWriter xmlWriter = new StringWriter();
                StreamResult streamResult = new StreamResult(xmlWriter);
                transformer.transform(transformedSource, streamResult);
                targetXml = streamResult.getWriter().toString();
                xmlWriter.close();
            } catch (TransformerException ex) {
                log.error("",ex);
                throw ex;
            } catch (IOException ex) { 
                log.error("",ex);
                    throw ex;
            } 

            return targetXml;
	}
        
        /**
         * 
         * @param node
         * @return
         * @throws TransformerConfigurationException
         * @throws TransformerException
         * @throws IOException 
         */
        public static String transform(Node node) throws TransformerConfigurationException, TransformerException, IOException {
            String content = null;

            try {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");

                Source transformedSource = new DOMSource(node);
                StringWriter xmlWriter = new StringWriter();
                StreamResult streamResult = new StreamResult(xmlWriter);
                transformer.transform(transformedSource, streamResult);
                content = streamResult.getWriter().toString();
                xmlWriter.close();
            } catch (TransformerConfigurationException ex) {
                    log.error("",ex);
                    throw ex;
            } catch (TransformerException ex) {
                    log.error("",ex);
                    throw ex;
            } catch (IOException ex) {
                log.error("",ex);
                    throw ex;
            }
            
            return content;
        }
        
        /**
         * 
         * @param node
         * @param file
         * @throws TransformerConfigurationException
         * @throws TransformerException
         * @throws IOException 
         */
        public static void transform(Node node, File file) throws TransformerConfigurationException, TransformerException, IOException {
                String targetXml = null;

                try {
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

                    transformer.transform(new DOMSource(node), new StreamResult(new FileWriter(file)));
                } catch (TransformerConfigurationException ex) {
                        log.error("",ex);
                        throw ex;
                } catch (TransformerException | IOException ex) {
                        log.error("",ex);
                        throw ex;
                }
        }
        
        public static void transform(Document doc, File file) throws TransformerConfigurationException, TransformerException, IOException {
                String targetXml = null;

                try {
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

                    transformer.transform(new DOMSource(doc), new StreamResult(new FileWriter(file)));
                } catch (TransformerConfigurationException ex) {
                        log.error("",ex);
                        throw ex;
                } catch (TransformerException | IOException ex) {
                        log.error("",ex);
                        throw ex;
                }
        }
    
        /**
         * 
         * @param sourceDoc
         * @param xsltInputStream
         * @return
         * @throws TransformerConfigurationException
         * @throws TransformerException 
         */
        public static String transform(Document sourceDoc, InputStream xsltInputStream) throws TransformerConfigurationException, TransformerException {
            String target = null;

            try {
                String factoryClassName = "net.sf.saxon.TransformerFactoryImpl";
                
                Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(xsltInputStream));
                
                // Create a transformer for the stylesheet
                Source source = new DOMSource(sourceDoc);
                StreamResult targetStreamResult = new StreamResult(new StringWriter());

                // Transform the source XML to string
                transformer.transform(source, targetStreamResult);

                // Get the result of transformation to string format
                target = targetStreamResult.getWriter().toString();

            } catch (TransformerConfigurationException ex) {
                log.error("",ex);
                throw ex;
            } catch (TransformerException ex) {
                log.error("",ex);
                throw ex;
            }

            return target;
        }
        
}

