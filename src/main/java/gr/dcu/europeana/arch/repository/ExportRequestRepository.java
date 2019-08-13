package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.model.ExportRequest;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Vangelis Nomikos
 */

@Repository
public interface ExportRequestRepository extends JpaRepository<ExportRequest, Long> {
    
}
