package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.model.SettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingRepository extends JpaRepository<SettingEntity, String> {

    SettingEntity findByKey(String key);
}