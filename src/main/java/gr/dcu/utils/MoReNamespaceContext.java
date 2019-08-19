package gr.dcu.utils;

/**
 *
 * @author Δημήτρης Γαβρίλης
 */
import java.util.Iterator;
import javax.xml.*;
import javax.xml.namespace.NamespaceContext;

public class MoReNamespaceContext implements NamespaceContext {

    @Override
    public String getNamespaceURI(String prefix) {
        //ystem.out.println(prefix);
        
        if (prefix == null) throw new NullPointerException("Null prefix");
        else if ("edm".equals(prefix)) return "http://www.europeana.eu/schemas/edm/";
        else if ("car".equals(prefix)) return "http://www.dcu.gr/carareSchema";
        else if ("carare".equals(prefix)) return "http://www.carare.eu/carareSchema";
        else if ("dc".equals(prefix)) return "http://purl.org/dc/elements/1.1/";
        else if ("dcterms".equals(prefix)) return "http://purl.org/dc/terms/";
        else if ("oai_dc".equals(prefix)) return "http://www.openarchives.org/OAI/2.0/oai_dc/";
        else if ("ore".equals(prefix)) return "http://www.openarchives.org/ore/terms/";
        else if ("xsi".equals(prefix)) return "http://www.w3.org/2001/XMLSchema-instance";
        else if ("rdf".equals(prefix)) return "http://www.w3.org/1999/02/22-rdf-syntax-ns#";    
        else if ("wgs84_pos".equals(prefix)) return "http://www.w3.org/2003/01/geo/wgs84_pos#";    
        else if ("europeana".equals(prefix)) return "http://www.europeana.eu/schemas/ese/";    
        else if ("skos".equals(prefix)) return "http://www.w3.org/2004/02/skos/core#";    
        else if ("owl".equals(prefix)) return "http://www.w3.org/2002/07/owl#";
        else if ("foaf".equals(prefix)) return "http://xmlns.com/foaf/";
        else if ("rdau".equals(prefix)) return "http://www.rdaregistry.info/Elements/u/";
        else if ("crm".equals(prefix)) return "http://www.cidoc-crm.org/rdfs/cidoc-crm#";
        else if ("emd".equals(prefix)) return "http://easy.dans.knaw.nl/easy/easymetadata/";
        else if ("cc".equals(prefix)) return "https://creativecommons.org/ns#";
        // else if ("acdm".equals(prefix)) return "http://schemas.cloud.dcu.gr/ariadne-registry/";
        else if ("acdm".equals(prefix)) return "http://registry.ariadne-infrastructure.eu/";
        else if ("oai".equals(prefix)) return "http://www.openarchives.org/OAI/2.0/";
        else if ("marc".equals(prefix)) return "http://www.loc.gov/MARC21/slim";
        else if ("dli".equals(prefix)) return "http://dliservice.research-infrastructures.eu";
        else if ("mets".equals(prefix)) return "http://www.loc.gov/METS/";
        else if ("mods".equals(prefix)) return "http://www.loc.gov/mods/v3";
        else if ("agls".equals(prefix)) return "http://www.naa.gov.au/recordkeeping/gov_online/agls/1.2";
        else if ("ags".equals(prefix)) return "http://purl.org/agmes/1.1/";
        else if ("xalan".equals(prefix)) return "http://xml.apache.org/xalan";
        else if ("dcat".equals(prefix)) return "http://www.w3.org/acdm/dcat#";
        else if ("geo".equals(prefix)) return "http://www.w3.org/2003/01/geo/wgs84_pos";
        else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;                 
        
        return XMLConstants.NULL_NS_URI;
    }

    // This method isn't necessary for XPath processing.
    @Override
    public String getPrefix(String uri) {
        throw new UnsupportedOperationException();
    }

    // This method isn't necessary for XPath processing either.
    @Override
    public Iterator getPrefixes(String uri) {
        throw new UnsupportedOperationException();
    }

}
