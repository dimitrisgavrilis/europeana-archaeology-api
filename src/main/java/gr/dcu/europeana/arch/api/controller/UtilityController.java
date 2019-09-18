package gr.dcu.europeana.arch.api.controller;

import gr.dcu.europeana.arch.api.resource.GeonamesDto;
import gr.dcu.europeana.arch.api.resource.GeonamesMapper;
import gr.dcu.europeana.arch.geonames.Geonames;
import gr.dcu.europeana.arch.geonames.GeonamesSearchResult;
import gr.dcu.europeana.arch.geonames.GeonamesService;
import gr.dcu.europeana.arch.model.AatSubject;
import gr.dcu.europeana.arch.model.Language;
import gr.dcu.europeana.arch.repository.AatSubjectRepository;
import gr.dcu.europeana.arch.repository.LanguageRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Vangelis Nomikos
 */
@Slf4j
@CrossOrigin
@RestController
public class UtilityController {
    
    @Autowired
    AatSubjectRepository aatSubjectRepository;
    
    @Autowired
    LanguageRepository languageRepository;
    
    @Autowired
    GeonamesService geonamesService;
    
    @Autowired
    GeonamesMapper geonamesMapper;
    
    @ApiOperation("Search Subjects by name")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = AatSubject.class)})
    @PostMapping("/subjects/search")
    public List<AatSubject> searchSubjectsByName(@RequestParam String q) {
        
        return aatSubjectRepository.findAllByLabelContainingIgnoreCase(q);
    }
    
    @ApiOperation("Search geonames by name")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Geonames.class)})
    @PostMapping("/geonames/search")
    public List<GeonamesDto> searchGeonamesByName(@RequestParam String q, 
            @RequestParam(required = false, defaultValue = "en") String lang) {
        
        GeonamesSearchResult searchResult = geonamesService.search(q, lang, 10);
        
        List<Geonames> geonames = searchResult.getGeonames();
        
        log.info("Search geonames. Query: {} , #Results: {}", q, geonames.size());
        
        return geonamesMapper.toDtoList(geonames);
    }
    
    @ApiOperation("Get all languages")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Collection.class)})
    @GetMapping("/languages")
    public List<Language> getAllLanguages() {
        return languageRepository.findAllByOrderByNameAsc();
    }
    
}
