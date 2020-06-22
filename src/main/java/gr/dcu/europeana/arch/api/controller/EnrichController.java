package gr.dcu.europeana.arch.api.controller;

import gr.dcu.europeana.arch.service.EDMService;
import gr.dcu.utils.XMLUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

@Slf4j
@CrossOrigin
@RestController
public class EnrichController {

    private final EDMService edmService;

    public EnrichController(EDMService edmService) {
        this.edmService = edmService;
    }

    @Operation(summary = "Enrich a specific file")
    @PostMapping(value = "/enrich", consumes = MediaType.APPLICATION_XML, produces = MediaType.APPLICATION_XML)
    public String enrichEdmArchive(
            HttpServletRequest requestContext,
            @RequestBody String xmlContent,
            @RequestParam long thematicMappingId,
            @RequestParam (required = false, defaultValue = "-1") Long spatialMappingId,
            @RequestParam (required = false, defaultValue = "-1") Long temporalMappingId) {

        log.info("On demand enrichment. ThematicMapping: {}, SpatialMapping: {}, TemporalMapping: {}",
                thematicMappingId, spatialMappingId, temporalMappingId);

        // log.info(xmlContent);

        // int userId = authService.authorize(requestContext);
        String enrichedXmlContent = null;
        try {
            Document doc = edmService.enrichXml(xmlContent, thematicMappingId, spatialMappingId, temporalMappingId);
            enrichedXmlContent = XMLUtils.transform(doc);
        } catch (IOException | SAXException | ParserConfigurationException
                | XPathExpressionException | TransformerException ex) {
            log.error("Error at enrich controller.");
        }

        return enrichedXmlContent;
    }
}
