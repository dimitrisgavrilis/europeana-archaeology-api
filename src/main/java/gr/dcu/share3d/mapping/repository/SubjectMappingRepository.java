package gr.dcu.share3d.mapping.repository;

import gr.dcu.share3d.mapping.model.MappingTerm;
import gr.dcu.share3d.mapping.model.SubjectMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Vangelis Nomikos
 */
@Repository
public interface SubjectMappingRepository extends JpaRepository<SubjectMapping, Long>{
    
}
