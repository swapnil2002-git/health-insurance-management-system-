package com.healthinsurance.customer.controller;

import com.healthinsurance.customer.dto.request.ContactRequest;
import com.healthinsurance.customer.dto.response.ContactResponse;
import com.healthinsurance.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers/{customerId}/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<ContactResponse> addContact(
            @PathVariable("customerId") UUID customerId,
            @Valid @RequestBody ContactRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.addContact(customerId, request));
    }

    @GetMapping
    public ResponseEntity<List<ContactResponse>> getContacts(@PathVariable("customerId") UUID customerId) {
        return ResponseEntity.ok(customerService.getContacts(customerId));
    }

    @PutMapping("/{contactId}")
    public ResponseEntity<ContactResponse> updateContact(
            @PathVariable("customerId") UUID customerId,
            @PathVariable("contactId") UUID contactId,
            @Valid @RequestBody ContactRequest request) {
        return ResponseEntity.ok(customerService.updateContact(customerId, contactId, request));
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<Void> removeContact(
            @PathVariable("customerId") UUID customerId,
            @PathVariable("contactId") UUID contactId) {
        customerService.removeContact(customerId, contactId);
        return ResponseEntity.noContent().build();
    }
}