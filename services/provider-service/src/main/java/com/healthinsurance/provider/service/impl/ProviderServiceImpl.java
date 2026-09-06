package com.healthinsurance.provider.service.impl;

import com.healthinsurance.provider.dto.ProviderRequest;
import com.healthinsurance.provider.dto.ProviderResponse;
import com.healthinsurance.provider.entity.Provider;
import com.healthinsurance.provider.entity.ProviderAddress;
import com.healthinsurance.provider.enums.ProviderStatus;
import com.healthinsurance.provider.exception.ResourceNotFoundException;
import com.healthinsurance.provider.mapper.ProviderMapper;
import com.healthinsurance.provider.repository.ProviderRepository;
import com.healthinsurance.provider.service.ProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProviderServiceImpl implements ProviderService {

    private final ProviderRepository providerRepository;
    private final ProviderMapper providerMapper;

    @Override
    public ProviderResponse createProvider(ProviderRequest request) {
        log.info("Creating provider with name: {}", request.getProviderName());
        Provider provider = providerMapper.toEntity(request);
        if (provider.getStatus() == null) {
            provider.setStatus(ProviderStatus.ACTIVE);
        }

        if (request.getAddresses() != null && !request.getAddresses().isEmpty()) {
            request.getAddresses().forEach(addrReq -> {
                ProviderAddress address = providerMapper.toEntity(addrReq);
                provider.addAddress(address);
            });
        }

        Provider savedProvider = providerRepository.save(provider);
        log.info("Provider created successfully with ID: {}", savedProvider.getProviderId());
        return providerMapper.toResponse(savedProvider);
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderResponse getProviderById(UUID providerId) {
        log.info("Fetching provider with ID: {}", providerId);
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + providerId));
        return providerMapper.toResponse(provider);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderResponse> getAllProviders(ProviderStatus status) {
        log.info("Fetching all providers with status filter: {}", status);
        List<Provider> providers;
        if (status != null) {
            providers = providerRepository.findByStatus(status);
        } else {
            providers = providerRepository.findAll();
        }
        return providerMapper.toProviderResponseList(providers);
    }

    @Override
    public ProviderResponse updateProvider(UUID providerId, ProviderRequest request) {
        log.info("Updating provider with ID: {}", providerId);
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + providerId));

        provider.setProviderName(request.getProviderName());
        provider.setProviderType(request.getProviderType());
        provider.setContactEmail(request.getContactEmail());
        provider.setContactPhone(request.getContactPhone());
        if (request.getStatus() != null) {
            provider.setStatus(request.getStatus());
        }
        provider.setUpdatedAt(Instant.now());

        Provider updatedProvider = providerRepository.save(provider);
        log.info("Provider updated successfully with ID: {}", updatedProvider.getProviderId());
        return providerMapper.toResponse(updatedProvider);
    }

    @Override
    public ProviderResponse updateProviderStatus(UUID providerId, ProviderStatus status) {
        log.info("Updating status of provider ID {} to {}", providerId, status);
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + providerId));

        provider.setStatus(status);
        provider.setUpdatedAt(Instant.now());

        Provider updatedProvider = providerRepository.save(provider);
        return providerMapper.toResponse(updatedProvider);
    }
}
