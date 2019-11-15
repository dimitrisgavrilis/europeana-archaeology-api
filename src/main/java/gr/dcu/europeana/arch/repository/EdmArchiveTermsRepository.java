package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.model.EdmArchiveTerms;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Vangelis Nomikos
 */
@Repository
public interface EdmArchiveTermsRepository extends JpaRepository<EdmArchiveTerms, Long> {
    
    EdmArchiveTerms findByArchiveId(Long archiveId);
    
}
