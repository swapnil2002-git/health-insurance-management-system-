package com.healthinsurance.provider.dto;

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
public class ProviderNetworkMappingResponse {

    private UUID mappingId;
    private UUID providerId;
    private String providerName;
    private UUID networkId;
    private String networkName;
    private boolean active;
    private Instant joinedDate;
    private Instant createdAt;
    private Instant updatedAt;
}
