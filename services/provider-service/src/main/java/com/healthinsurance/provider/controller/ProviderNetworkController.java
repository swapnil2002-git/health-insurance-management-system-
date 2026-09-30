package com.healthinsurance.provider.controller;

import com.healthinsurance.provider.dto.*;
import com.healthinsurance.provider.service.ProviderNetworkMappingService;
import com.healthinsurance.provider.service.ProviderNetworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/provider-networks")
@RequiredArgsConstructor
@Tag(name = "Provider Network Management", description = "Endpoints for managing provider networks, network mappings, and eligibility checks")
public class ProviderNetworkController {

    private final ProviderNetworkService networkService;
    private final ProviderNetworkMappingService networkMappingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('HEALTHCARE_PROVIDER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Create a provider network")
    public ResponseEntity<ProviderNetworkResponse> createNetwork(
            @Valid @RequestBody ProviderNetworkRequest request) {
        log.info("REST: Create provider network request received");
        ProviderNetworkResponse response = networkService.createNetwork(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{networkId}")
    @PreAuthorize("hasAnyRole('HEALTHCARE_PROVIDER', 'POLICY_ADMINISTRATOR', 'CLAIMS_OFFICER', 'AGENT', 'CUSTOMER', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Get provider network by ID")
    public ResponseEntity<ProviderNetworkResponse> getNetworkById(@PathVariable("networkId") UUID networkId) {
        log.info("REST: Get network by ID: {}", networkId);
        ProviderNetworkResponse response = networkService.getNetworkById(networkId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HEALTHCARE_PROVIDER', 'POLICY_ADMINISTRATOR', 'CLAIMS_OFFICER', 'AGENT', 'CUSTOMER', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Get all provider networks")
    public ResponseEntity<List<ProviderNetworkResponse>> getAllNetworks() {
        log.info("REST: Get all provider networks");
        List<ProviderNetworkResponse> responses = networkService.getAllNetworks();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{networkId}")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Update provider network details")
    public ResponseEntity<ProviderNetworkResponse> updateNetwork(
            @PathVariable("networkId") UUID networkId,
            @Valid @RequestBody ProviderNetworkRequest request) {
        log.info("REST: Update network ID: {}", networkId);
        ProviderNetworkResponse response = networkService.updateNetwork(networkId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mappings")
    @PreAuthorize("hasAnyRole('HEALTHCARE_PROVIDER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Map provider to network")
    public ResponseEntity<ProviderNetworkMappingResponse> mapProviderToNetwork(
            @Valid @RequestBody ProviderNetworkMappingRequest request) {
        log.info("REST: Map provider {} to network {}", request.getProviderId(), request.getNetworkId());
        ProviderNetworkMappingResponse response = networkMappingService.mapProviderToNetwork(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{networkId}/providers")
    @PreAuthorize("hasAnyRole('HEALTHCARE_PROVIDER', 'POLICY_ADMINISTRATOR', 'CLAIMS_OFFICER', 'AGENT', 'CUSTOMER', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Get all providers in a network")
    public ResponseEntity<List<ProviderNetworkMappingResponse>> getProvidersInNetwork(@PathVariable("networkId") UUID networkId) {
        log.info("REST: Get providers for network ID: {}", networkId);
        List<ProviderNetworkMappingResponse> responses = networkMappingService.getProvidersInNetwork(networkId);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{networkId}/providers/{providerId}")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Remove provider from a network")
    public ResponseEntity<Void> removeProviderFromNetwork(
            @PathVariable("networkId") UUID networkId,
            @PathVariable("providerId") UUID providerId) {
        log.info("REST: Remove provider {} from network {}", providerId, networkId);
        networkMappingService.removeProviderFromNetwork(providerId, networkId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{networkId}/eligibility/{providerId}")
    @PreAuthorize("hasAnyRole('HEALTHCARE_PROVIDER', 'CLAIMS_OFFICER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Verify provider network eligibility (In-Network check for Claims Service)")
    public ResponseEntity<ProviderNetworkEligibilityResponse> verifyEligibility(
            @PathVariable("networkId") UUID networkId,
            @PathVariable("providerId") UUID providerId) {
        log.info("REST: Verify eligibility for provider {} and network {}", providerId, networkId);
        ProviderNetworkEligibilityResponse response = networkMappingService.verifyEligibility(providerId, networkId);
        return ResponseEntity.ok(response);
    }
}
