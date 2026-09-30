package com.healthinsurance.notification.service;

import com.healthinsurance.notification.domain.NotificationChannel;
import com.healthinsurance.notification.domain.NotificationStatus;
import com.healthinsurance.notification.domain.TemplateStatus;
import com.healthinsurance.notification.dto.NotificationRequest;
import com.healthinsurance.notification.dto.NotificationResponse;
import com.healthinsurance.notification.entity.Notification;
import com.healthinsurance.notification.entity.NotificationTemplate;
import com.healthinsurance.notification.mapper.NotificationMapper;
import com.healthinsurance.notification.provider.NotificationChannelProvider;
import com.healthinsurance.notification.repository.NotificationRepository;
import com.healthinsurance.notification.repository.NotificationTemplateRepository;
import com.healthinsurance.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private NotificationChannelProvider emailProvider;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        when(emailProvider.supports(NotificationChannel.EMAIL)).thenReturn(true);
        notificationService = new NotificationServiceImpl(
                notificationRepository,
                templateRepository,
                notificationMapper,
                Collections.singletonList(emailProvider)
        );
    }

    @Test
    void testSendNotification_WithTemplateResolution() {
        UUID notifId = UUID.randomUUID();
        NotificationRequest request = NotificationRequest.builder()
                .recipient("test@example.com")
                .channel(NotificationChannel.EMAIL)
                .eventType("POLICY_ISSUED")
                .templateData(Map.of("policyNumber", "POL-1001"))
                .build();

        Notification notification = new Notification();
        notification.setRecipient("test@example.com");
        notification.setChannel(NotificationChannel.EMAIL);
        notification.setEventType("POLICY_ISSUED");

        NotificationTemplate template = new NotificationTemplate();
        template.setSubjectTemplate("Policy {policyNumber}");
        template.setBodyTemplate("Welcome! Policy number is {policyNumber}");
        template.setStatus(TemplateStatus.ACTIVE);

        Notification saved = new Notification();
        saved.setNotificationId(notifId);
        saved.setRecipient("test@example.com");
        saved.setChannel(NotificationChannel.EMAIL);
        saved.setSubject("Policy POL-1001");
        saved.setContent("Welcome! Policy number is POL-1001");
        saved.setStatus(NotificationStatus.PENDING);

        NotificationResponse response = NotificationResponse.builder()
                .notificationId(notifId)
                .recipient("test@example.com")
                .channel(NotificationChannel.EMAIL)
                .subject("Policy POL-1001")
                .content("Welcome! Policy number is POL-1001")
                .status(NotificationStatus.PENDING)
                .build();

        when(notificationMapper.toEntity(request)).thenReturn(notification);
        when(templateRepository.findByEventTypeAndChannelAndStatus("POLICY_ISSUED", NotificationChannel.EMAIL, TemplateStatus.ACTIVE))
                .thenReturn(Optional.of(template));
        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);
        when(notificationMapper.toResponse(saved)).thenReturn(response);
        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(saved));

        NotificationResponse actual = notificationService.sendNotification(request);

        assertNotNull(actual);
        assertEquals("test@example.com", actual.getRecipient());
        assertEquals("Policy POL-1001", actual.getSubject());
        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
    }

    @Test
    void testSendNotification_Idempotency() {
        NotificationRequest request = NotificationRequest.builder()
                .recipient("test@example.com")
                .channel(NotificationChannel.EMAIL)
                .eventId("EVT-12345")
                .build();

        Notification existing = new Notification();
        existing.setNotificationId(UUID.randomUUID());
        existing.setEventId("EVT-12345");
        existing.setRecipient("test@example.com");
        existing.setChannel(NotificationChannel.EMAIL);

        NotificationResponse response = NotificationResponse.builder()
                .notificationId(existing.getNotificationId())
                .eventId("EVT-12345")
                .recipient("test@example.com")
                .build();

        when(notificationRepository.existsByEventIdAndChannel("EVT-12345", NotificationChannel.EMAIL)).thenReturn(true);
        when(notificationRepository.findByEventIdAndChannel("EVT-12345", NotificationChannel.EMAIL)).thenReturn(Optional.of(existing));
        when(notificationMapper.toResponse(existing)).thenReturn(response);

        NotificationResponse actual = notificationService.sendNotification(request);

        assertNotNull(actual);
        assertEquals("EVT-12345", actual.getEventId());
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testProcessNotificationAsync_Success() {
        UUID notifId = UUID.randomUUID();
        Notification notification = new Notification();
        notification.setNotificationId(notifId);
        notification.setChannel(NotificationChannel.EMAIL);
        notification.setRecipient("user@example.com");
        notification.setStatus(NotificationStatus.PENDING);

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notification));
        doNothing().when(emailProvider).send(notification);

        notificationService.processNotificationAsync(notifId);

        assertEquals(NotificationStatus.SENT, notification.getStatus());
        assertNotNull(notification.getSentAt());
        verify(notificationRepository).save(notification);
    }
}
