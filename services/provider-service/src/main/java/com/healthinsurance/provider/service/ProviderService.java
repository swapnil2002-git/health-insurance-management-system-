package com.healthinsurance.provider.service;

import com.healthinsurance.provider.dto.ProviderRequest;
import com.healthinsurance.provider.dto.ProviderResponse;
import com.healthinsurance.provider.enums.ProviderStatus;

import java.util.List;
import java.util.UUID;

public interface ProviderService {

    ProviderResponse createProvider(ProviderRequest request);

    ProviderResponse getProviderById(UUID providerId);

    List<ProviderResponse> getAllProviders(ProviderStatus status);

    ProviderResponse updateProvider(UUID providerId, ProviderRequest request);

    ProviderResponse updateProviderStatus(UUID providerId, ProviderStatus status);
}
