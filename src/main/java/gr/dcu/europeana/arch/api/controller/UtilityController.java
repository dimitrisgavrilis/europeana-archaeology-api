package gr.dcu.europeana.arch.api.controller;

import gr.dcu.europeana.arch.api.adapter.UtilityControllerMapper;
import gr.dcu.europeana.arch.api.dto.EArchTemporalDto;
import gr.dcu.europeana.arch.api.dto.GeonamesDto;
import gr.dcu.europeana.arch.api.dto.GeonamesMapper;
import gr.dcu.europeana.arch.geonames.Geonames;
import gr.dcu.europeana.arch.geonames.GeonamesSearchResult;
import gr.dcu.europeana.arch.geonames.GeonamesService;
import gr.dcu.europeana.arch.model.AatSubjectEntity;
import gr.dcu.europeana.arch.model.LanguageEntity;
import gr.dcu.europeana.arch.repository.AatSubjectRepository;
import gr.dcu.europeana.arch.repository.LanguageRepository;
import java.util.List;
import java.util.stream.Collectors;
import gr.dcu.europeana.arch.service.EArchTemporalService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
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
    
    private final AatSubjectRepository aatSubjectRepository;
    private final LanguageRepository languageRepository;
    private final GeonamesService geonamesService;
    private final GeonamesMapper geonamesMapper;
    private final EArchTemporalService eArchTemporalService;
    private final UtilityControllerMapper utilityControllerMapper;

    public UtilityController(AatSubjectRepository aatSubjectRepository, LanguageRepository languageRepository,
                             GeonamesService geonamesService, GeonamesMapper geonamesMapper, EArchTemporalService eArchTemporalService,
                             UtilityControllerMapper utilityControllerMapper) {
        this.aatSubjectRepository = aatSubjectRepository;
        this.languageRepository = languageRepository;
        this.geonamesService = geonamesService;
        this.geonamesMapper = geonamesMapper;
        this.eArchTemporalService = eArchTemporalService;
        this.utilityControllerMapper = utilityControllerMapper;
    }

    @Operation(summary = "Search subjects by name")
    @PostMapping("/subjects/search")
    public List<AatSubjectEntity> searchSubjectsByName(@RequestParam String q,
                                                       @RequestParam(value = "type", required=false) String type) {
        
        if(type == null || type.isEmpty()) {
            return aatSubjectRepository.findAllByLabelContainingIgnoreCase(q);
        } else {
            return aatSubjectRepository.findAllByLabelContainingIgnoreCaseAndType(q, type);
        }
    }
    
    @Operation(summary = "Search geonames by name")
    @PostMapping("/geonames/search")
    public List<GeonamesDto> searchGeonamesByName(@RequestParam String q, 
            @RequestParam(required = false, defaultValue = "en") String lang) {
        
        GeonamesSearchResult searchResult = geonamesService.search(q, lang, 10);
        
        List<Geonames> geonames = searchResult.getGeonames();
        
        log.info("Search geonames. Query: {} , #Results: {}", q, geonames.size());
        
        return geonamesMapper.toDtoList(geonames);
    }

    @Operation(summary = "Find all earch temporal")
    @GetMapping("/earch_temporal")
    public List<EArchTemporalDto> findAllEArchTemporal() {

        return eArchTemporalService.findAll()
                .stream()
                .map(utilityControllerMapper::fromEntity)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Search geonames by name")
    @PostMapping("/earch_temporal/search")
    public List<EArchTemporalDto> searchEarchTemporal(@RequestParam String q) {

        return eArchTemporalService.search(q)
                .stream()
                .map(utilityControllerMapper::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Operation(summary = "Get all languages")
    @GetMapping("/languages")
    public List<LanguageEntity> getAllLanguages() {
        return languageRepository.findAllByOrderByNameAsc();
    }
    
}
