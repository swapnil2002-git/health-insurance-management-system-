package com.healthinsurance.provider.service;

import com.healthinsurance.provider.dto.ProviderAddressRequest;
import com.healthinsurance.provider.dto.ProviderAddressResponse;

import java.util.List;
import java.util.UUID;

public interface ProviderAddressService {

    ProviderAddressResponse addAddress(UUID providerId, ProviderAddressRequest request);

    List<ProviderAddressResponse> getAddressesByProviderId(UUID providerId);

    void deleteAddress(UUID addressId);
}
