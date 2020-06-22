package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.domain.entity.LanguageEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Vangelis Nomikos
 */
@Repository
public interface LanguageRepository extends JpaRepository<LanguageEntity, String> {
 
    List<LanguageEntity> findAllByOrderByNameAsc();
}
