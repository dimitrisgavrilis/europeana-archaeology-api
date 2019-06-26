package gr.dcu.share3d.mapping.repository;

import gr.dcu.share3d.mapping.model.MappingTerm;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Vangelis Nomikos
 */
@Repository
public interface MappingTermRepository extends JpaRepository<MappingTerm, Long>{
    
    List<MappingTerm> findByMappingId(Long mappingId);
    
    void deleteByMappingId(Long mappingId);
}
