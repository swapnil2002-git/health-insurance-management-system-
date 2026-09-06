package com.healthinsurance.provider.controller;

import com.healthinsurance.provider.dto.*;
import com.healthinsurance.provider.enums.ProviderStatus;
import com.healthinsurance.provider.service.ProviderAddressService;
import com.healthinsurance.provider.service.ProviderNetworkMappingService;
import com.healthinsurance.provider.service.ProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
@Tag(name = "Provider Management", description = "Endpoints for managing healthcare providers and their addresses")
public class ProviderController {

    private final ProviderService providerService;
    private final ProviderAddressService providerAddressService;
    private final ProviderNetworkMappingService networkMappingService;

    @PostMapping
    @Operation(summary = "Create a new provider")
    public ResponseEntity<ProviderResponse> createProvider(@Valid @RequestBody ProviderRequest request) {
        log.info("REST: Create provider request received");
        ProviderResponse response = providerService.createProvider(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{providerId}")
    @Operation(summary = "Get provider by ID")
    public ResponseEntity<ProviderResponse> getProviderById(@PathVariable UUID providerId) {
        log.info("REST: Get provider by ID: {}", providerId);
        ProviderResponse response = providerService.getProviderById(providerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all providers, optionally filtered by status")
    public ResponseEntity<List<ProviderResponse>> getAllProviders(
            @RequestParam(required = false) ProviderStatus status) {
        log.info("REST: Get all providers with status filter: {}", status);
        List<ProviderResponse> responses = providerService.getAllProviders(status);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{providerId}")
    @Operation(summary = "Update provider details")
    public ResponseEntity<ProviderResponse> updateProvider(
            @PathVariable UUID providerId,
            @Valid @RequestBody ProviderRequest request) {
        log.info("REST: Update provider ID: {}", providerId);
        ProviderResponse response = providerService.updateProvider(providerId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{providerId}/status")
    @Operation(summary = "Update provider status")
    public ResponseEntity<ProviderResponse> updateProviderStatus(
            @PathVariable UUID providerId,
            @RequestParam ProviderStatus status) {
        log.info("REST: Update provider status for ID: {} to {}", providerId, status);
        ProviderResponse response = providerService.updateProviderStatus(providerId, status);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{providerId}/addresses")
    @Operation(summary = "Add address to provider")
    public ResponseEntity<ProviderAddressResponse> addAddress(
            @PathVariable UUID providerId,
            @Valid @RequestBody ProviderAddressRequest request) {
        log.info("REST: Add address to provider ID: {}", providerId);
        ProviderAddressResponse response = providerAddressService.addAddress(providerId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{providerId}/addresses")
    @Operation(summary = "Get addresses of a provider")
    public ResponseEntity<List<ProviderAddressResponse>> getAddresses(@PathVariable UUID providerId) {
        log.info("REST: Get addresses for provider ID: {}", providerId);
        List<ProviderAddressResponse> responses = providerAddressService.getAddressesByProviderId(providerId);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/addresses/{addressId}")
    @Operation(summary = "Delete an address")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID addressId) {
        log.info("REST: Delete address ID: {}", addressId);
        providerAddressService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{providerId}/networks")
    @Operation(summary = "Get networks mapped to a provider")
    public ResponseEntity<List<ProviderNetworkMappingResponse>> getNetworksForProvider(@PathVariable UUID providerId) {
        log.info("REST: Get networks for provider ID: {}", providerId);
        List<ProviderNetworkMappingResponse> responses = networkMappingService.getNetworksForProvider(providerId);
        return ResponseEntity.ok(responses);
    }
}
