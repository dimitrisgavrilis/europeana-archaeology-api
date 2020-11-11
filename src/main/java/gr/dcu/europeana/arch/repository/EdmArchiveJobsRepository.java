package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.domain.entity.EdmArchiveJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EdmArchiveJobsRepository extends JpaRepository<EdmArchiveJobEntity, Long> {

    List<EdmArchiveJobEntity> findAllByArchiveId(long archiveId);
}
