package com.healthinsurance.provider.dto;

import com.healthinsurance.provider.enums.NetworkStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderNetworkRequest {

    @NotBlank(message = "Network name is required")
    private String networkName;

    private String description;

    private NetworkStatus status;
}
