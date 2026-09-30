package com.healthinsurance.notification.dto;

import com.healthinsurance.notification.domain.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotBlank(message = "Recipient is required")
    private String recipient;

    @NotNull(message = "Notification channel is required")
    private NotificationChannel channel;

    private String eventType;

    private String subject;

    private String content;

    private UUID referenceId;

    private String eventId;

    private Map<String, Object> templateData;
}
