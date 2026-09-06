package com.healthinsurance.provider.service.impl;

import com.healthinsurance.provider.dto.ProviderAddressRequest;
import com.healthinsurance.provider.dto.ProviderAddressResponse;
import com.healthinsurance.provider.entity.Provider;
import com.healthinsurance.provider.entity.ProviderAddress;
import com.healthinsurance.provider.exception.ResourceNotFoundException;
import com.healthinsurance.provider.mapper.ProviderMapper;
import com.healthinsurance.provider.repository.ProviderAddressRepository;
import com.healthinsurance.provider.repository.ProviderRepository;
import com.healthinsurance.provider.service.ProviderAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProviderAddressServiceImpl implements ProviderAddressService {

    private final ProviderAddressRepository addressRepository;
    private final ProviderRepository providerRepository;
    private final ProviderMapper providerMapper;

    @Override
    public ProviderAddressResponse addAddress(UUID providerId, ProviderAddressRequest request) {
        log.info("Adding address for provider ID: {}", providerId);
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + providerId));

        ProviderAddress address = providerMapper.toEntity(request);
        provider.addAddress(address);

        ProviderAddress savedAddress = addressRepository.save(address);
        log.info("Address added with ID: {}", savedAddress.getProviderAddressId());
        return providerMapper.toResponse(savedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderAddressResponse> getAddressesByProviderId(UUID providerId) {
        log.info("Fetching addresses for provider ID: {}", providerId);
        if (!providerRepository.existsById(providerId)) {
            throw new ResourceNotFoundException("Provider not found with ID: " + providerId);
        }
        List<ProviderAddress> addresses = addressRepository.findByProvider_ProviderId(providerId);
        return providerMapper.toAddressResponseList(addresses);
    }

    @Override
    public void deleteAddress(UUID addressId) {
        log.info("Deleting address ID: {}", addressId);
        ProviderAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));
        addressRepository.delete(address);
    }
}
