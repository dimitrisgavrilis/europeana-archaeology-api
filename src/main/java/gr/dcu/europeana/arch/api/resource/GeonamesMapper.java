package gr.dcu.europeana.arch.api.resource;

import gr.dcu.europeana.arch.geonames.Geonames;
import java.util.LinkedList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vangelis Nomikos
 */
@Component
public class GeonamesMapper {
    
    public GeonamesDto toDto(Geonames geonames) {
        
        GeonamesDto geonamesDto = new GeonamesDto();
        geonamesDto.setGeonameId(geonames.getGeonameId());
        geonamesDto.setName(geonames.getName());
        geonamesDto.setCountryCode(geonames.getCountryCode());
        geonamesDto.setCountryName(geonames.getCountryName());
        geonamesDto.setLatitude(geonames.getLatitude());
        geonamesDto.setLongitude(geonames.getLongitude());
        geonamesDto.setLabel(geonames.getName() + ", " + geonames.getCountryName());
        
        return geonamesDto;
    }
    
    public List<GeonamesDto> toDtoList(List<Geonames> geonamesList) {
         
        List<GeonamesDto> geonamesDtoList = new LinkedList<>();
        for(Geonames geonames : geonamesList) {
            geonamesDtoList.add(toDto(geonames));
        }

        return geonamesDtoList;
    }
    
}
