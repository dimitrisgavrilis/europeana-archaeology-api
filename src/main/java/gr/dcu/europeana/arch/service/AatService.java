package gr.dcu.europeana.arch.service;

import gr.dcu.europeana.arch.model.AatSubjectEntity;
import gr.dcu.europeana.arch.repository.AatSubjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AatService {

    private final AatSubjectRepository aatSubjectRepo;

    public static final String AAT_URI_PREFIX = "http://vocab.getty.edu/page/aat/";

    public AatService(AatSubjectRepository aatSubjectRepo) {
        this.aatSubjectRepo = aatSubjectRepo;
    }

    public List<AatSubjectEntity> findAll() {
        return aatSubjectRepo.findAll();
    }

    public Map<String, AatSubjectEntity> findAllInMap() {

        Map<String, AatSubjectEntity> aatSubjectMap = new HashMap<>();

        // Create aat subject map
        List<AatSubjectEntity> aatSubjectEntities = aatSubjectRepo.findAll();
        for(AatSubjectEntity tmpAatSubjectEntity : aatSubjectEntities) {
            aatSubjectMap.put(tmpAatSubjectEntity.getAatUid(), tmpAatSubjectEntity);
        }

        return aatSubjectMap;
    }
}
