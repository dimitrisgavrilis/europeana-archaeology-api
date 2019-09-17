package gr.dcu.europeana.arch.geonames;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *More: http://www.geonames.org/export/geonames-search.html
 * @author Vangelis Nomikos
 */
@Slf4j
@Service
public class GeonamesService {
    
    private static final String GEONAMES_BASE_URL = "http://api.geonames.org";
    private static final String GEONAMES_USERNAME = "vnomikos";
    
    /**
     * 
     * @param q
     * @param lang
     * @param maxRows
     * @return 
     */
    public GeonamesSearchResult search(String q, String lang, int maxRows) {
        
        // log.info("Search geonames. Query: {}", q);
        
        try {
            String url = GEONAMES_BASE_URL + "/searchJSON";

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

            // Set query params
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("q", q)
                    .queryParam("lang", lang)
                    .queryParam("maxRows", maxRows)
                    .queryParam("username", GEONAMES_USERNAME);

            HttpEntity<?> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<GeonamesSearchResult> response = restTemplate.exchange(
                    builder.toUriString(), HttpMethod.GET, entity, GeonamesSearchResult.class);
            
            // if(response.getStatusCodeValue() == HttpStatus.OK) { 
            // }
            
            return response.getBody();
            
        } catch(HttpClientErrorException | HttpServerErrorException ex) {
            log.error("Geonames API: Ssearch failed", ex);
            throw ex;
        }
    }
    
}
