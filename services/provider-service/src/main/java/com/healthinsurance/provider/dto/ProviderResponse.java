package com.healthinsurance.provider.dto;

import com.healthinsurance.provider.enums.ProviderStatus;
import com.healthinsurance.provider.enums.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderResponse {

    private UUID providerId;
    private String providerName;
    private ProviderType providerType;
    private String contactEmail;
    private String contactPhone;
    private ProviderStatus status;
    private List<ProviderAddressResponse> addresses;
    private Instant createdAt;
    private Instant updatedAt;
}
