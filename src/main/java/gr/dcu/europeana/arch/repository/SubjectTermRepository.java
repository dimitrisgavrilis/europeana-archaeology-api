package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.model.SubjectTerm;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Vangelis Nomikos
 */
@Repository
public interface SubjectTermRepository extends JpaRepository<SubjectTerm, Long>{
    
    List<SubjectTerm> findByMappingId(Long mappingId);
    
    void deleteByMappingId(Long mappingId);
}
