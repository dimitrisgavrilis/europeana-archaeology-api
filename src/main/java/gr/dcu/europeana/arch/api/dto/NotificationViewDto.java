package gr.dcu.europeana.arch.api.dto;

import gr.dcu.europeana.arch.domain.enums.NotificationType;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
public class NotificationViewDto {
    private Long id;

    private Long edmArchiveId;

    private String label;

    private String description;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    private Boolean read;

    private LocalDateTime createdAt;
}
