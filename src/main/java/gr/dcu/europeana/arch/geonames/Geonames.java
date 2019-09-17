package gr.dcu.europeana.arch.geonames;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class Geonames {
    
    private long geonameId;
    private String name;
    
    private String countryCode;
    private String countryName;
    
    @JsonProperty("lat")
    private String latitude;
    
    @JsonProperty("lng")
    private String longitude;
    
    
    
}
