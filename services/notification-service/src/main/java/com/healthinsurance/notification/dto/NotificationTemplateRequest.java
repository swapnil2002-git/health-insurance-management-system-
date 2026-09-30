package com.healthinsurance.notification.dto;

import com.healthinsurance.notification.domain.NotificationChannel;
import com.healthinsurance.notification.domain.TemplateStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateRequest {

    @NotBlank(message = "Event type is required")
    private String eventType;

    @NotNull(message = "Notification channel is required")
    private NotificationChannel channel;

    private String subjectTemplate;

    @NotBlank(message = "Body template is required")
    private String bodyTemplate;

    private TemplateStatus status;
}
