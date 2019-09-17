package gr.dcu.europeana.arch.geonames;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Vangelis Nomikos
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeonamesSearchResult {
    
    private long totalResultsCount;
    private List<Geonames> geonames;
    
    
}
