package com.healthinsurance.provider.dto;

import com.healthinsurance.provider.enums.ProviderStatus;
import com.healthinsurance.provider.enums.ProviderType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderRequest {

    @NotBlank(message = "Provider name is required")
    private String providerName;

    @NotNull(message = "Provider type is required")
    private ProviderType providerType;

    private String contactEmail;

    private String contactPhone;

    private ProviderStatus status;

    private List<ProviderAddressRequest> addresses;
}
