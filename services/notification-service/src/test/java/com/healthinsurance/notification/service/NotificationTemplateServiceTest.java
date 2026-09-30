package com.healthinsurance.notification.service;

import com.healthinsurance.notification.domain.NotificationChannel;
import com.healthinsurance.notification.domain.TemplateStatus;
import com.healthinsurance.notification.dto.NotificationTemplateRequest;
import com.healthinsurance.notification.dto.NotificationTemplateResponse;
import com.healthinsurance.notification.entity.NotificationTemplate;
import com.healthinsurance.notification.mapper.NotificationMapper;
import com.healthinsurance.notification.repository.NotificationTemplateRepository;
import com.healthinsurance.notification.service.impl.NotificationTemplateServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateServiceTest {

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationTemplateServiceImpl templateService;

    @Test
    void testCreateTemplate_Success() {
        UUID templateId = UUID.randomUUID();
        NotificationTemplateRequest request = NotificationTemplateRequest.builder()
                .eventType("POLICY_ISSUED")
                .channel(NotificationChannel.EMAIL)
                .subjectTemplate("Policy {policyNumber}")
                .bodyTemplate("Body")
                .status(TemplateStatus.ACTIVE)
                .build();

        NotificationTemplate template = new NotificationTemplate();
        template.setTemplateId(templateId);

        NotificationTemplateResponse response = NotificationTemplateResponse.builder()
                .templateId(templateId)
                .eventType("POLICY_ISSUED")
                .channel(NotificationChannel.EMAIL)
                .build();

        when(templateRepository.findByEventTypeAndChannel("POLICY_ISSUED", NotificationChannel.EMAIL)).thenReturn(Optional.empty());
        when(notificationMapper.toEntity(request)).thenReturn(template);
        when(templateRepository.save(any(NotificationTemplate.class))).thenReturn(template);
        when(notificationMapper.toResponse(template)).thenReturn(response);

        NotificationTemplateResponse actual = templateService.createTemplate(request);

        assertNotNull(actual);
        assertEquals(templateId, actual.getTemplateId());
        verify(templateRepository).save(template);
    }

    @Test
    void testCreateTemplate_DuplicateThrowsException() {
        NotificationTemplateRequest request = NotificationTemplateRequest.builder()
                .eventType("POLICY_ISSUED")
                .channel(NotificationChannel.EMAIL)
                .build();

        when(templateRepository.findByEventTypeAndChannel("POLICY_ISSUED", NotificationChannel.EMAIL))
                .thenReturn(Optional.of(new NotificationTemplate()));

        assertThrows(IllegalArgumentException.class, () -> templateService.createTemplate(request));
        verify(templateRepository, never()).save(any());
    }
}
