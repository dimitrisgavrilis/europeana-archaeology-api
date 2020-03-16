package gr.dcu.europeana.arch.service;

import gr.dcu.europeana.arch.repository.SettingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
public class SettingService {

    private final SettingRepository settingRepository;

    public SettingService(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    public List<String> getRecipientList(String key) {
        List<String> recipientsList = new LinkedList<>();

        String recipientsString = settingRepository.findByKey(key).getValue();

        if(recipientsString != null && !recipientsString.isEmpty()) {

            String[] recipients = recipientsString.split(";");

            recipientsList.addAll(Arrays.asList(recipients));
        }

        return recipientsList;
    }

}
