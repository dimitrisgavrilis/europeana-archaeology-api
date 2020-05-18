package gr.dcu.europeana.arch.api.controller;

import gr.dcu.europeana.arch.api.controller.mapper.AdminControllerMapper;
import gr.dcu.europeana.arch.api.dto.EArchTemporalCreateDto;
import gr.dcu.europeana.arch.api.dto.EArchTemporalViewDto;
import gr.dcu.europeana.arch.api.dto.AatSubjectCreateDto;
import gr.dcu.europeana.arch.exception.NotFoundException;
import gr.dcu.europeana.arch.model.AatSubjectEntity;
import gr.dcu.europeana.arch.repository.AatSubjectRepository;
import gr.dcu.europeana.arch.repository.EArchTemporalEntityRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RestController
public class AdminController {

    @Autowired
    private AatSubjectRepository aatSubjectRepo;

    @Autowired
    private EArchTemporalEntityRepository eArchTemporalEntityRepo;

    @Autowired
    private AdminControllerMapper adminControllerMapper;

    // TODO: Check if user is admin

    @Operation(summary = "Add a new aat term")
    @PostMapping("/subjects")
    public AatSubjectEntity addAatSubject(@RequestBody AatSubjectCreateDto newAatSubject) {
        return aatSubjectRepo.save(adminControllerMapper.toEntity(newAatSubject));
    }

    // TODO: Check primary key. Use aat_uid and not the id. In db there ara duplicates on aat_uid
    @Operation(summary = "Update an aat term ")
    @PutMapping("/subjects/{id}")
    public AatSubjectEntity updateAatSubject(@PathVariable Integer id,
                                             @RequestBody AatSubjectCreateDto newAatSubject) {

        aatSubjectRepo.findById(id).orElseThrow(()
                -> new NotFoundException("Aat Subject", id));

        return aatSubjectRepo.save(adminControllerMapper.toEntity(newAatSubject));
    }

    @Operation(summary = "Add a new earch temporal term")
    @PostMapping("/temporal")
    public EArchTemporalViewDto addEarchTemporal(@RequestBody EArchTemporalCreateDto newEArchTemporal) {

        return adminControllerMapper.fromEntity(eArchTemporalEntityRepo.save(
                        adminControllerMapper.toEntity(newEArchTemporal)));
    }
}