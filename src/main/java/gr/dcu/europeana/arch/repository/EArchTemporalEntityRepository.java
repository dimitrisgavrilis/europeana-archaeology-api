package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.domain.entity.EArchTemporalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EArchTemporalEntityRepository extends JpaRepository<EArchTemporalEntity, Integer> {

    List<EArchTemporalEntity> findAllByLabelContainingIgnoreCase(String label);
}
