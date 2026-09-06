package com.healthinsurance.provider.dto;

import com.healthinsurance.provider.enums.NetworkStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderNetworkResponse {

    private UUID networkId;
    private String networkName;
    private String description;
    private NetworkStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
