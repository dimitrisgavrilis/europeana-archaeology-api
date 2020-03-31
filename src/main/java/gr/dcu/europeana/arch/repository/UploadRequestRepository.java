package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.model.MappingUploadRequestEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Vangelis Nomikos
 */
@Repository
public interface UploadRequestRepository extends JpaRepository<MappingUploadRequestEntity, Long> {
    
    List<MappingUploadRequestEntity> findAllByCreatedBy(Integer createdBy);
    
}
