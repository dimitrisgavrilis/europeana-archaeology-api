package gr.dcu.europeana.arch.api.controller;

import gr.dcu.europeana.arch.api.dto.NotificationViewDto;
import gr.dcu.europeana.arch.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@CrossOrigin
@RestController
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "Get all notifications")
    @GetMapping("/notifications")
    public List<NotificationViewDto> getNotifications() {
        return notificationService.findAllByUserId();
    }

}