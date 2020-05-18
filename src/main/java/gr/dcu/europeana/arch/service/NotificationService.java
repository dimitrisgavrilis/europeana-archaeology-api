package gr.dcu.europeana.arch.service;

import gr.dcu.europeana.arch.api.controller.mapper.NotificationMapper;
import gr.dcu.europeana.arch.api.dto.NotificationViewDto;
import gr.dcu.europeana.arch.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private NotificationRepository notificationRepo;


    public List<NotificationViewDto> findAllByUserId() {

        return new LinkedList<>();
    }
}
