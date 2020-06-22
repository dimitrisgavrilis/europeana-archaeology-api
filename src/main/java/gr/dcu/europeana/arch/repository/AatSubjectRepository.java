package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.domain.entity.AatSubjectEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AatSubjectRepository extends JpaRepository<AatSubjectEntity, Integer> {
    
    List<AatSubjectEntity> findAllByType(String type);
    
    List<AatSubjectEntity> findAllByLabelContainingIgnoreCase(String name);
    
    List<AatSubjectEntity> findAllByLabelContainingIgnoreCaseAndType(String name, String type);
    
}
