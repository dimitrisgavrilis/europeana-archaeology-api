package gr.dcu.europeana.arch.api.controller;

import gr.dcu.europeana.arch.model.AatSubject;
import gr.dcu.europeana.arch.repository.AatSubjectRepository;
import gr.dcu.europeana.arch.repository.LanguageRepository;
import gr.dcu.share3d.entity.Language;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Vangelis Nomikos
 */
@RestController
public class UtilityController {
    
    @Autowired
    AatSubjectRepository aatSubjectRepository;
    
    @Autowired
    LanguageRepository languageRepository;
    
    @ApiOperation("Search Subjects by name")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = AatSubject.class)})
    @PostMapping("/subjects/search")
    public List<AatSubject> searchSubjectsByName(@RequestParam String q) {
        
        return aatSubjectRepository.findAllByLabelContainingIgnoreCase(q);
    }
    
    @ApiOperation("Get all languages")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Collection.class)})
    @GetMapping("/languages")
    public List<Language> getAllLanguages() {
        return languageRepository.findAllByOrderByNameAsc();
    }
    
}
