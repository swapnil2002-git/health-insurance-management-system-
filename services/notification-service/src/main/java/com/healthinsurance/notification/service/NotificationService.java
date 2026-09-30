package com.healthinsurance.notification.service;

import com.healthinsurance.notification.dto.NotificationRequest;
import com.healthinsurance.notification.dto.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    NotificationResponse sendNotification(NotificationRequest request);

    void processNotificationAsync(UUID notificationId);

    NotificationResponse getNotificationById(UUID notificationId);

    List<NotificationResponse> getNotificationsByRecipient(String recipient);

    List<NotificationResponse> getNotificationsByReferenceId(UUID referenceId);
}
