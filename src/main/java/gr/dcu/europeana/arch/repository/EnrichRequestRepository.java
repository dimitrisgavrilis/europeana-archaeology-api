package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.model.EnrichRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Vangelis Nomikos
 */
@Repository
public interface EnrichRequestRepository extends JpaRepository<EnrichRequest, Long> {
    
}