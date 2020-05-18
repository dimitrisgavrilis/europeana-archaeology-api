import gr.dcu.utils.XMLUtils;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;

@Slf4j
public class XmlSplitter {

    private static final String FILE_DIR          = "/home/vangelis/tests/xml-split/";
    private static final String LARGE_FILE_SAMPLE = "ariadne_greylit_test.xml";
    private static final String LARGE_FILE        = "ariadne_1093.xml";

    public static void main(String[] args) {

        File largeFile = new File(FILE_DIR + LARGE_FILE);
        if(!largeFile.exists() || !largeFile.isFile()) {
            log.error("File does not exist.");
            return;
        }

        try {
            Document doc = XMLUtils.parse(largeFile, true);

            NodeList recordNodes = XMLUtils.getNodeList(doc, "//record");
            log.info("#Record Nodes: {}", recordNodes.getLength());

            for(int i=0; i<recordNodes.getLength(); i++) {
                /*
                Document recordDoc = XMLUtils.createDocument();
                Element root = recordDoc.createElement("records");
                recordDoc.appendChild(root);

                Node recordNode = recordNodes.item(i);
                Node clonedNode = recordNode.cloneNode(true);
                recordDoc.adoptNode(clonedNode);
                root.appendChild(clonedNode);

                // recordNode.appendChild(recordNode);

                XMLUtils.transform(recordDoc, new File(FILE_DIR + "exports/" + i + ".xml"));
                 */

                Node recordNode = recordNodes.item(i);
                recordNode.normalize();
                XMLUtils.transform(recordNode, new File(FILE_DIR + "exports/" + (i+1) + ".xml"));
            }

        } catch (ParserConfigurationException | SAXException | IOException |
                XPathExpressionException | TransformerException ex) {
            log.error("Splitting error.", ex);
        }
    }
}
