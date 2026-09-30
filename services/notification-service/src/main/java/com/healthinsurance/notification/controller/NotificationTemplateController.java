package com.healthinsurance.notification.controller;

import com.healthinsurance.notification.domain.NotificationChannel;
import com.healthinsurance.notification.dto.NotificationTemplateRequest;
import com.healthinsurance.notification.dto.NotificationTemplateResponse;
import com.healthinsurance.notification.service.NotificationTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notification-templates")
@RequiredArgsConstructor
@Tag(name = "Notification Template Management", description = "Endpoints for managing notification templates")
public class NotificationTemplateController {

    private final NotificationTemplateService templateService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'ADMIN', 'POLICY_ADMINISTRATOR')")
    @Operation(summary = "Create notification template")
    public ResponseEntity<NotificationTemplateResponse> createTemplate(@Valid @RequestBody NotificationTemplateRequest request) {
        NotificationTemplateResponse response = templateService.createTemplate(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'ADMIN', 'POLICY_ADMINISTRATOR')")
    @Operation(summary = "Update notification template")
    public ResponseEntity<NotificationTemplateResponse> updateTemplate(
            @PathVariable("id") UUID id,
            @Valid @RequestBody NotificationTemplateRequest request) {
        return ResponseEntity.ok(templateService.updateTemplate(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'ADMIN', 'POLICY_ADMINISTRATOR', 'CLAIMS_OFFICER')")
    @Operation(summary = "Get notification template by ID")
    public ResponseEntity<NotificationTemplateResponse> getTemplateById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(templateService.getTemplateById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'ADMIN', 'POLICY_ADMINISTRATOR', 'CLAIMS_OFFICER')")
    @Operation(summary = "Get all notification templates or by event type")
    public ResponseEntity<List<NotificationTemplateResponse>> getTemplates(
            @RequestParam(value = "eventType", required = false) String eventType) {
        if (eventType != null && !eventType.isBlank()) {
            return ResponseEntity.ok(templateService.getTemplatesByEventType(eventType));
        }
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @GetMapping("/event/{eventType}/channel/{channel}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'ADMIN', 'POLICY_ADMINISTRATOR', 'CLAIMS_OFFICER')")
    @Operation(summary = "Get notification template by event type and channel")
    public ResponseEntity<NotificationTemplateResponse> getTemplateByEventAndChannel(
            @PathVariable("eventType") String eventType,
            @PathVariable("channel") NotificationChannel channel) {
        return ResponseEntity.ok(templateService.getTemplateByEventTypeAndChannel(eventType, channel));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Delete notification template")
    public ResponseEntity<Void> deleteTemplate(@PathVariable("id") UUID id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
