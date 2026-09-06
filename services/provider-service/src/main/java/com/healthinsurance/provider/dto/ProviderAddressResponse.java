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
public class ProviderAddressResponse {

    private UUID providerAddressId;
    private UUID providerId;
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private boolean primary;
    private Instant createdAt;
    private Instant updatedAt;
}
