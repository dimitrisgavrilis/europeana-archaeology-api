package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.model.Mapping;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Vangelis Nomikos
 */
@Repository
public interface MappingRepository extends JpaRepository<Mapping, Long>{
 
    public List<Mapping> findAllByCreatedBy(int createdBy);
    
}
