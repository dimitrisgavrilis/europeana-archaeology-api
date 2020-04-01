package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.model.EArchTemporalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EArchTemporalEntityRepository extends JpaRepository<EArchTemporalEntity, Integer> {
}
