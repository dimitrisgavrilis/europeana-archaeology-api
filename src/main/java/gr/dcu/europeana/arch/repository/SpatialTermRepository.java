package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.model.SpatialTerm;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Vangelis Nomikos
 */
@Repository
public interface SpatialTermRepository extends JpaRepository<SpatialTerm, Long> {
    
     List<SpatialTerm> findByMappingId(Long mappingId);
    
    void deleteByMappingId(Long mappingId);
    
}
