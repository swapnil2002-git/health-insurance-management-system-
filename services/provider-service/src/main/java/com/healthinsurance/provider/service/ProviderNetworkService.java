package com.healthinsurance.provider.service;

import com.healthinsurance.provider.dto.ProviderNetworkRequest;
import com.healthinsurance.provider.dto.ProviderNetworkResponse;

import java.util.List;
import java.util.UUID;

public interface ProviderNetworkService {

    ProviderNetworkResponse createNetwork(ProviderNetworkRequest request);

    ProviderNetworkResponse getNetworkById(UUID networkId);

    List<ProviderNetworkResponse> getAllNetworks();

    ProviderNetworkResponse updateNetwork(UUID networkId, ProviderNetworkRequest request);
}
