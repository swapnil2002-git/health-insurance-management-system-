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
    @Operation(summary = "Create a provider network")
    public ResponseEntity<ProviderNetworkResponse> createNetwork(
            @Valid @RequestBody ProviderNetworkRequest request) {
        log.info("REST: Create provider network request received");
        ProviderNetworkResponse response = networkService.createNetwork(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{networkId}")
    @Operation(summary = "Get provider network by ID")
    public ResponseEntity<ProviderNetworkResponse> getNetworkById(@PathVariable UUID networkId) {
        log.info("REST: Get network by ID: {}", networkId);
        ProviderNetworkResponse response = networkService.getNetworkById(networkId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all provider networks")
    public ResponseEntity<List<ProviderNetworkResponse>> getAllNetworks() {
        log.info("REST: Get all provider networks");
        List<ProviderNetworkResponse> responses = networkService.getAllNetworks();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{networkId}")
    @Operation(summary = "Update provider network details")
    public ResponseEntity<ProviderNetworkResponse> updateNetwork(
            @PathVariable UUID networkId,
            @Valid @RequestBody ProviderNetworkRequest request) {
        log.info("REST: Update network ID: {}", networkId);
        ProviderNetworkResponse response = networkService.updateNetwork(networkId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mappings")
    @Operation(summary = "Map provider to network")
    public ResponseEntity<ProviderNetworkMappingResponse> mapProviderToNetwork(
            @Valid @RequestBody ProviderNetworkMappingRequest request) {
        log.info("REST: Map provider {} to network {}", request.getProviderId(), request.getNetworkId());
        ProviderNetworkMappingResponse response = networkMappingService.mapProviderToNetwork(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{networkId}/providers")
    @Operation(summary = "Get all providers in a network")
    public ResponseEntity<List<ProviderNetworkMappingResponse>> getProvidersInNetwork(@PathVariable UUID networkId) {
        log.info("REST: Get providers for network ID: {}", networkId);
        List<ProviderNetworkMappingResponse> responses = networkMappingService.getProvidersInNetwork(networkId);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{networkId}/providers/{providerId}")
    @Operation(summary = "Remove provider from a network")
    public ResponseEntity<Void> removeProviderFromNetwork(
            @PathVariable UUID networkId,
            @PathVariable UUID providerId) {
        log.info("REST: Remove provider {} from network {}", providerId, networkId);
        networkMappingService.removeProviderFromNetwork(providerId, networkId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{networkId}/eligibility/{providerId}")
    @Operation(summary = "Verify provider network eligibility (In-Network check for Claims Service)")
    public ResponseEntity<ProviderNetworkEligibilityResponse> verifyEligibility(
            @PathVariable UUID networkId,
            @PathVariable UUID providerId) {
        log.info("REST: Verify eligibility for provider {} and network {}", providerId, networkId);
        ProviderNetworkEligibilityResponse response = networkMappingService.verifyEligibility(providerId, networkId);
        return ResponseEntity.ok(response);
    }
}
