package com.healthinsurance.notification.service.impl;

import com.healthinsurance.notification.domain.NotificationStatus;
import com.healthinsurance.notification.domain.TemplateStatus;
import com.healthinsurance.notification.dto.NotificationRequest;
import com.healthinsurance.notification.dto.NotificationResponse;
import com.healthinsurance.notification.entity.Notification;
import com.healthinsurance.notification.entity.NotificationTemplate;
import com.healthinsurance.notification.exception.ResourceNotFoundException;
import com.healthinsurance.notification.mapper.NotificationMapper;
import com.healthinsurance.notification.provider.NotificationChannelProvider;
import com.healthinsurance.notification.repository.NotificationRepository;
import com.healthinsurance.notification.repository.NotificationTemplateRepository;
import com.healthinsurance.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationMapper notificationMapper;
    private final List<NotificationChannelProvider> channelProviders;

    @Override
    @Transactional
    public NotificationResponse sendNotification(NotificationRequest request) {
        log.info("Receiving notification request for recipient: {}, channel: {}, eventType: {}",
                request.getRecipient(), request.getChannel(), request.getEventType());

        // Check idempotency if eventId is provided
        if (request.getEventId() != null && !request.getEventId().isBlank()) {
            if (notificationRepository.existsByEventIdAndChannel(request.getEventId(), request.getChannel())) {
                log.warn("Notification already exists for eventId: {} and channel: {}. Skipping duplicate.",
                        request.getEventId(), request.getChannel());
                Notification existing = notificationRepository.findByEventIdAndChannel(request.getEventId(), request.getChannel())
                        .orElseThrow(() -> new ResourceNotFoundException("Existing notification not found"));
                return notificationMapper.toResponse(existing);
            }
        }

        Notification notification = notificationMapper.toEntity(request);

        // If content is empty and eventType is present, attempt template resolution
        if ((notification.getContent() == null || notification.getContent().isBlank()) && request.getEventType() != null) {
            Optional<NotificationTemplate> templateOpt = templateRepository
                    .findByEventTypeAndChannelAndStatus(request.getEventType(), request.getChannel(), TemplateStatus.ACTIVE);
            
            if (templateOpt.isPresent()) {
                NotificationTemplate template = templateOpt.get();
                String resolvedBody = resolveTemplate(template.getBodyTemplate(), request.getTemplateData());
                String resolvedSubject = resolveTemplate(template.getSubjectTemplate(), request.getTemplateData());
                notification.setContent(resolvedBody);
                if (notification.getSubject() == null || notification.getSubject().isBlank()) {
                    notification.setSubject(resolvedSubject);
                }
            } else {
                log.warn("No active template found for eventType: {} and channel: {}. Content might remain empty.",
                        request.getEventType(), request.getChannel());
            }
        }

        if (notification.getContent() == null) {
            notification.setContent("");
        }

        Notification saved = notificationRepository.save(notification);

        // Process delivery asynchronously
        processNotificationAsync(saved.getNotificationId());

        return notificationMapper.toResponse(saved);
    }

    @Override
    @Async
    @Transactional
    public void processNotificationAsync(UUID notificationId) {
        log.info("Processing notification dispatch async for notificationId: {}", notificationId);
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            log.error("Notification not found for async dispatch: {}", notificationId);
            return;
        }

        Notification notification = notificationOpt.get();
        NotificationChannelProvider provider = channelProviders.stream()
                .filter(p -> p.supports(notification.getChannel()))
                .findFirst()
                .orElse(null);

        if (provider == null) {
            log.error("No provider registered for channel: {}", notification.getChannel());
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage("Unsupported notification channel: " + notification.getChannel());
            notificationRepository.save(notification);
            return;
        }

        try {
            provider.send(notification);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            notification.setErrorMessage(null);
            log.info("Notification successfully dispatched: {}", notificationId);
        } catch (Exception e) {
            log.error("Failed to dispatch notification: {}", notificationId, e);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
        }

        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByRecipient(String recipient) {
        return notificationMapper.toResponseList(notificationRepository.findByRecipient(recipient));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByReferenceId(UUID referenceId) {
        return notificationMapper.toResponseList(notificationRepository.findByReferenceId(referenceId));
    }

    private String resolveTemplate(String template, Map<String, Object> data) {
        if (template == null) return null;
        if (data == null || data.isEmpty()) return template;

        String result = template;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }
}
