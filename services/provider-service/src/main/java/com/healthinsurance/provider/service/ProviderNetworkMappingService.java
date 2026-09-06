package com.healthinsurance.provider.service;

import com.healthinsurance.provider.dto.ProviderNetworkEligibilityResponse;
import com.healthinsurance.provider.dto.ProviderNetworkMappingRequest;
import com.healthinsurance.provider.dto.ProviderNetworkMappingResponse;

import java.util.List;
import java.util.UUID;

public interface ProviderNetworkMappingService {

    ProviderNetworkMappingResponse mapProviderToNetwork(ProviderNetworkMappingRequest request);

    List<ProviderNetworkMappingResponse> getNetworksForProvider(UUID providerId);

    List<ProviderNetworkMappingResponse> getProvidersInNetwork(UUID networkId);

    void removeProviderFromNetwork(UUID providerId, UUID networkId);

    ProviderNetworkEligibilityResponse verifyEligibility(UUID providerId, UUID networkId);
}
