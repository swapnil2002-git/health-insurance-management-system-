package com.healthinsurance.notification.controller;

import com.healthinsurance.notification.dto.NotificationRequest;
import com.healthinsurance.notification.dto.NotificationResponse;
import com.healthinsurance.notification.service.NotificationService;
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
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "Endpoints for dispatching and querying notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'ADMIN', 'POLICY_ADMINISTRATOR', 'CLAIMS_OFFICER')")
    @Operation(summary = "Send a notification", description = "Dispatches a notification synchronously or asynchronously via template")
    public ResponseEntity<NotificationResponse> sendNotification(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.sendNotification(request);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @GetMapping("/recipient/{recipient}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Get notifications by recipient")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByRecipient(@PathVariable("recipient") String recipient) {
        return ResponseEntity.ok(notificationService.getNotificationsByRecipient(recipient));
    }

    @GetMapping("/reference/{referenceId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Get notifications by reference entity ID")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByReferenceId(@PathVariable("referenceId") UUID referenceId) {
        return ResponseEntity.ok(notificationService.getNotificationsByReferenceId(referenceId));
    }
}
