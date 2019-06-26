package gr.dcu.share3d.mapping.repository;

import gr.dcu.share3d.mapping.model.AatSubject;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AatSubjectRepository extends JpaRepository<AatSubject, Integer> {
    
    List<AatSubject> findAllByType(String type);
    
    List<AatSubject> findAllByLabelContainingIgnoreCase(String name);
    
}
