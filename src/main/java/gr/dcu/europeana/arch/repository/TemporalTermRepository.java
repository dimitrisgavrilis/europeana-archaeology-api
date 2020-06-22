package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.domain.entity.TemporalTermEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemporalTermRepository extends JpaRepository<TemporalTermEntity, Long> {
    
     List<TemporalTermEntity> findByMappingId(Long mappingId);
    
    void deleteByMappingId(Long mappingId);
    
}
