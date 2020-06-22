package gr.dcu.europeana.arch.service;

import gr.dcu.europeana.arch.domain.entity.AatSubjectEntity;
import gr.dcu.europeana.arch.domain.entity.EArchTemporalEntity;
import gr.dcu.europeana.arch.repository.AatSubjectRepository;
import gr.dcu.europeana.arch.repository.EArchTemporalEntityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class VocabularyService {

    private final AatSubjectRepository aatSubjectRepo;
    private final EArchTemporalEntityRepository eArchTemporalEntityRepo;

    // public static final String AAT_URI_PREFIX = "http://vocab.getty.edu/page/aat/";
    public static final String AAT_LOD_URI_PREFIX = "http://vocab.getty.edu/aat/";

    public VocabularyService(AatSubjectRepository aatSubjectRepo, EArchTemporalEntityRepository eArchTemporalEntityRepo) {
        this.aatSubjectRepo = aatSubjectRepo;
        this.eArchTemporalEntityRepo = eArchTemporalEntityRepo;
    }

    public List<AatSubjectEntity> findAllAat() {
        return aatSubjectRepo.findAll();
    }

    public List<EArchTemporalEntity> findAllEArchTemporal() {
        return eArchTemporalEntityRepo.findAll();
    }

    /**
     * Load all AAT terms from db
     * @return a map with terms
     */
    public Map<String, AatSubjectEntity> loadAatTerms() {

        Map<String, AatSubjectEntity> aatSubjectMap = new HashMap<>();

        // Create aat subject map
        List<AatSubjectEntity> aatSubjectEntities = aatSubjectRepo.findAll();
        for(AatSubjectEntity tmpAatSubjectEntity : aatSubjectEntities) {
            aatSubjectMap.put(tmpAatSubjectEntity.getAatUid(), tmpAatSubjectEntity);
        }

        log.info("AAT terms loaded. #Terms: {}", aatSubjectEntities.size());

        // This works if we element aat_uid is unique
        // return aatSubjectEntities.stream().collect(
        //        Collectors.toMap(AatSubjectEntity::getAatUid, Function.identity()));


        return aatSubjectMap;
    }

    /**
     * Load all EArch temporal terms
     * @return a map with terms
     */
    public Map<String, EArchTemporalEntity> loadEArchTemporal() {

        Map<String, EArchTemporalEntity> earchTemporalEntityMap = new HashMap<>();

        // Create earch temporal map
        List<EArchTemporalEntity> eArchTemporalEntities = eArchTemporalEntityRepo.findAll();
        for(EArchTemporalEntity earchTemporalEntity : eArchTemporalEntities) {
            earchTemporalEntityMap.put(earchTemporalEntity.getAatUid(), earchTemporalEntity);
        }

        log.info("EArch Temporal terms loaded. #Terms: {}", eArchTemporalEntities.size());

        return earchTemporalEntityMap;
    }
}
