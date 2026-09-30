package com.healthinsurance.notification.service;

import com.healthinsurance.notification.domain.NotificationChannel;
import com.healthinsurance.notification.dto.NotificationTemplateRequest;
import com.healthinsurance.notification.dto.NotificationTemplateResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationTemplateService {

    NotificationTemplateResponse createTemplate(NotificationTemplateRequest request);

    NotificationTemplateResponse updateTemplate(UUID templateId, NotificationTemplateRequest request);

    NotificationTemplateResponse getTemplateById(UUID templateId);

    NotificationTemplateResponse getTemplateByEventTypeAndChannel(String eventType, NotificationChannel channel);

    List<NotificationTemplateResponse> getTemplatesByEventType(String eventType);

    List<NotificationTemplateResponse> getAllTemplates();

    void deleteTemplate(UUID templateId);
}
