package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.domain.entity.EdmArchiveEntity;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Vangelis Nomikos
 */
@Repository
public interface EdmArchiveRepository extends JpaRepository<EdmArchiveEntity, Long> {
    
    List<EdmArchiveEntity> findAllByCreatedBy(Integer createdBy);
    
}
