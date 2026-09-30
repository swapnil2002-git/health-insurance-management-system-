package com.healthinsurance.claims.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderClientDto {
    private UUID providerId;
    private String providerName;
    private String providerType;
    private String contactEmail;
    private String contactPhone;
    private String status;
    private List<ProviderNetworkMappingClientDto> networks;

    @Data
    public static class ProviderNetworkMappingClientDto {
        private UUID mappingId;
        private UUID networkId;
        private String networkName;
        private String networkTier;
        private String effectiveFrom;
        private String effectiveTo;
    }
}
