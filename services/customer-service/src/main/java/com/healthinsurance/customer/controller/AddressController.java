package com.healthinsurance.customer.controller;

import com.healthinsurance.customer.dto.request.AddressRequest;
import com.healthinsurance.customer.dto.response.AddressResponse;
import com.healthinsurance.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers/{customerId}/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(
            @PathVariable("customerId") UUID customerId,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.addAddress(customerId, request));
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAddresses(@PathVariable("customerId") UUID customerId) {
        return ResponseEntity.ok(customerService.getAddresses(customerId));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable("customerId") UUID customerId,
            @PathVariable("addressId") UUID addressId,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(customerService.updateAddress(customerId, addressId, request));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> removeAddress(
            @PathVariable("customerId") UUID customerId,
            @PathVariable("addressId") UUID addressId) {
        customerService.removeAddress(customerId, addressId);
        return ResponseEntity.noContent().build();
    }
}