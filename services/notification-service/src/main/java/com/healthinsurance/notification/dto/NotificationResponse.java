package com.healthinsurance.notification.dto;

import com.healthinsurance.notification.domain.NotificationChannel;
import com.healthinsurance.notification.domain.NotificationStatus;
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
public class NotificationResponse {

    private UUID notificationId;
    private String recipient;
    private NotificationChannel channel;
    private String eventType;
    private String subject;
    private String content;
    private NotificationStatus status;
    private UUID referenceId;
    private String eventId;
    private String errorMessage;
    private Instant sentAt;
    private Instant createdAt;
}
