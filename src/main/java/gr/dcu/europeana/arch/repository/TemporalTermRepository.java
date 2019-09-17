package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.model.TemporalTerm;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Vangelis Nomikos
 */
@Repository
public interface TemporalTermRepository extends JpaRepository<TemporalTerm, Long> {
    
     List<TemporalTerm> findByMappingId(Long mappingId);
    
    void deleteByMappingId(Long mappingId);
    
}
