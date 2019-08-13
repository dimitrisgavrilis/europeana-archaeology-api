package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.model.MappingTerm;
import gr.dcu.europeana.arch.model.SubjectMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Vangelis Nomikos
 */
@Repository
public interface SubjectMappingRepository extends JpaRepository<SubjectMapping, Long>{
    
}
