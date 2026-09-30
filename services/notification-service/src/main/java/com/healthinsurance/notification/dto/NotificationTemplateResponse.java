package com.healthinsurance.notification.dto;

import com.healthinsurance.notification.domain.NotificationChannel;
import com.healthinsurance.notification.domain.TemplateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateResponse {

    private UUID templateId;
    private String eventType;
    private NotificationChannel channel;
    private String subjectTemplate;
    private String bodyTemplate;
    private TemplateStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
