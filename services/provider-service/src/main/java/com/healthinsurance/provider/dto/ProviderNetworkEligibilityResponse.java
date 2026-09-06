package com.healthinsurance.provider.dto;

import com.healthinsurance.provider.enums.ProviderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderNetworkEligibilityResponse {

    private boolean networkProvider;
    private UUID providerId;
    private String providerName;
    private UUID networkId;
    private String networkName;
    private ProviderStatus providerStatus;
    private boolean mappingActive;
    private String message;
}
