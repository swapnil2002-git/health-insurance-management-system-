package com.healthinsurance.notification.service.impl;

import com.healthinsurance.notification.domain.NotificationChannel;
import com.healthinsurance.notification.domain.TemplateStatus;
import com.healthinsurance.notification.dto.NotificationTemplateRequest;
import com.healthinsurance.notification.dto.NotificationTemplateResponse;
import com.healthinsurance.notification.entity.NotificationTemplate;
import com.healthinsurance.notification.exception.ResourceNotFoundException;
import com.healthinsurance.notification.mapper.NotificationMapper;
import com.healthinsurance.notification.repository.NotificationTemplateRepository;
import com.healthinsurance.notification.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private static final Logger log = LoggerFactory.getLogger(NotificationTemplateServiceImpl.class);

    private final NotificationTemplateRepository templateRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public NotificationTemplateResponse createTemplate(NotificationTemplateRequest request) {
        log.info("Creating notification template for eventType: {}, channel: {}", request.getEventType(), request.getChannel());
        
        templateRepository.findByEventTypeAndChannel(request.getEventType(), request.getChannel())
                .ifPresent(t -> {
                    throw new IllegalArgumentException(
                            "Template already exists for eventType: " + request.getEventType() + " and channel: " + request.getChannel());
                });

        NotificationTemplate template = notificationMapper.toEntity(request);
        if (template.getStatus() == null) {
            template.setStatus(TemplateStatus.ACTIVE);
        }
        NotificationTemplate saved = templateRepository.save(template);
        return notificationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public NotificationTemplateResponse updateTemplate(UUID templateId, NotificationTemplateRequest request) {
        log.info("Updating notification template: {}", templateId);
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + templateId));

        if (request.getSubjectTemplate() != null) {
            template.setSubjectTemplate(request.getSubjectTemplate());
        }
        if (request.getBodyTemplate() != null) {
            template.setBodyTemplate(request.getBodyTemplate());
        }
        if (request.getStatus() != null) {
            template.setStatus(request.getStatus());
        }

        NotificationTemplate updated = templateRepository.save(template);
        return notificationMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationTemplateResponse getTemplateById(UUID templateId) {
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + templateId));
        return notificationMapper.toResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationTemplateResponse getTemplateByEventTypeAndChannel(String eventType, NotificationChannel channel) {
        NotificationTemplate template = templateRepository.findByEventTypeAndChannel(eventType, channel)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Template not found for event: " + eventType + " and channel: " + channel));
        return notificationMapper.toResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplateResponse> getTemplatesByEventType(String eventType) {
        return notificationMapper.toTemplateResponseList(templateRepository.findByEventType(eventType));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplateResponse> getAllTemplates() {
        return notificationMapper.toTemplateResponseList(templateRepository.findAll());
    }

    @Override
    @Transactional
    public void deleteTemplate(UUID templateId) {
        log.info("Deleting notification template: {}", templateId);
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + templateId));
        templateRepository.delete(template);
    }
}
