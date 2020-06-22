package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.domain.entity.EdmArchiveTermsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Vangelis Nomikos
 */
@Repository
public interface EdmArchiveTermsRepository extends JpaRepository<EdmArchiveTermsEntity, Long> {
    
    EdmArchiveTermsEntity findByArchiveId(Long archiveId);
    
    Long deleteByArchiveId(Long archiveId);
    
}
