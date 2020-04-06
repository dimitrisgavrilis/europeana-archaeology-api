package gr.dcu.europeana.arch.service;

import gr.dcu.europeana.arch.model.EArchTemporalEntity;
import gr.dcu.europeana.arch.repository.EArchTemporalEntityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class EArchTemporalService {

    private final EArchTemporalEntityRepository eArchTemporalEntityRepo;

    public EArchTemporalService(EArchTemporalEntityRepository eArchTemporalEntityRepo) {
        this.eArchTemporalEntityRepo = eArchTemporalEntityRepo;
    }

    public List<EArchTemporalEntity> findAll() {
        return eArchTemporalEntityRepo.findAll();
    }

    public List<EArchTemporalEntity> search(String q) {
        return eArchTemporalEntityRepo.findAllByLabelContainingIgnoreCase(q);
    }
}
