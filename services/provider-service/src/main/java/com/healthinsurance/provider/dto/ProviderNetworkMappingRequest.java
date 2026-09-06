package com.healthinsurance.provider.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderNetworkMappingRequest {

    @NotNull(message = "Provider ID is required")
    private UUID providerId;

    @NotNull(message = "Network ID is required")
    private UUID networkId;

    private boolean active = true;
}
