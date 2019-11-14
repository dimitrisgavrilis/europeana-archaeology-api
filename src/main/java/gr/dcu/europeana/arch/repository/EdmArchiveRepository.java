package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.model.EdmArchive;
import gr.dcu.europeana.arch.model.MappingUploadRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Vangelis Nomikos
 */
@Repository
public interface EdmArchiveRepository extends JpaRepository<EdmArchive, Long> {
    
    List<EdmArchive> findAllByCreatedBy(Integer createdBy);
    
}
