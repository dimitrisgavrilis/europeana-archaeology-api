package gr.dcu.europeana.arch.repository;

import gr.dcu.europeana.arch.model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingRepository extends JpaRepository<Setting, String> {

    Setting findByKey(String key);
}