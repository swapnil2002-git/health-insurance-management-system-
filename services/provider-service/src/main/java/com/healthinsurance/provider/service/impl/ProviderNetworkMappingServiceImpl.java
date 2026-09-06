package com.healthinsurance.provider.service.impl;

import com.healthinsurance.provider.dto.ProviderNetworkEligibilityResponse;
import com.healthinsurance.provider.dto.ProviderNetworkMappingRequest;
import com.healthinsurance.provider.dto.ProviderNetworkMappingResponse;
import com.healthinsurance.provider.entity.Provider;
import com.healthinsurance.provider.entity.ProviderNetwork;
import com.healthinsurance.provider.entity.ProviderNetworkMapping;
import com.healthinsurance.provider.enums.NetworkStatus;
import com.healthinsurance.provider.enums.ProviderStatus;
import com.healthinsurance.provider.exception.DuplicateResourceException;
import com.healthinsurance.provider.exception.ResourceNotFoundException;
import com.healthinsurance.provider.mapper.ProviderMapper;
import com.healthinsurance.provider.repository.ProviderNetworkMappingRepository;
import com.healthinsurance.provider.repository.ProviderNetworkRepository;
import com.healthinsurance.provider.repository.ProviderRepository;
import com.healthinsurance.provider.service.ProviderNetworkMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProviderNetworkMappingServiceImpl implements ProviderNetworkMappingService {

    private final ProviderNetworkMappingRepository mappingRepository;
    private final ProviderRepository providerRepository;
    private final ProviderNetworkRepository networkRepository;
    private final ProviderMapper providerMapper;

    @Override
    public ProviderNetworkMappingResponse mapProviderToNetwork(ProviderNetworkMappingRequest request) {
        log.info("Mapping provider {} to network {}", request.getProviderId(), request.getNetworkId());

        Provider provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + request.getProviderId()));

        ProviderNetwork network = networkRepository.findById(request.getNetworkId())
                .orElseThrow(() -> new ResourceNotFoundException("Network not found with ID: " + request.getNetworkId()));

        if (mappingRepository.existsByProvider_ProviderIdAndNetwork_NetworkId(provider.getProviderId(), network.getNetworkId())) {
            throw new DuplicateResourceException("Provider is already mapped to this network");
        }

        ProviderNetworkMapping mapping = new ProviderNetworkMapping();
        mapping.setProvider(provider);
        mapping.setNetwork(network);
        mapping.setActive(request.isActive());
        mapping.setJoinedDate(Instant.now());

        provider.addNetworkMapping(mapping);
        ProviderNetworkMapping savedMapping = mappingRepository.save(mapping);
        log.info("Provider mapped to network with mapping ID: {}", savedMapping.getMappingId());
        return providerMapper.toResponse(savedMapping);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderNetworkMappingResponse> getNetworksForProvider(UUID providerId) {
        log.info("Fetching networks for provider ID: {}", providerId);
        if (!providerRepository.existsById(providerId)) {
            throw new ResourceNotFoundException("Provider not found with ID: " + providerId);
        }
        List<ProviderNetworkMapping> mappings = mappingRepository.findByProvider_ProviderId(providerId);
        return providerMapper.toMappingResponseList(mappings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderNetworkMappingResponse> getProvidersInNetwork(UUID networkId) {
        log.info("Fetching providers for network ID: {}", networkId);
        if (!networkRepository.existsById(networkId)) {
            throw new ResourceNotFoundException("Network not found with ID: " + networkId);
        }
        List<ProviderNetworkMapping> mappings = mappingRepository.findByNetwork_NetworkId(networkId);
        return providerMapper.toMappingResponseList(mappings);
    }

    @Override
    public void removeProviderFromNetwork(UUID providerId, UUID networkId) {
        log.info("Removing provider {} from network {}", providerId, networkId);
        ProviderNetworkMapping mapping = mappingRepository.findByProvider_ProviderIdAndNetwork_NetworkId(providerId, networkId)
                .orElseThrow(() -> new ResourceNotFoundException("Mapping not found for provider " + providerId + " and network " + networkId));
        mappingRepository.delete(mapping);
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderNetworkEligibilityResponse verifyEligibility(UUID providerId, UUID networkId) {
        log.info("Verifying network eligibility for provider {} and network {}", providerId, networkId);

        Optional<Provider> providerOpt = providerRepository.findById(providerId);
        if (!providerOpt.isPresent()) {
            return ProviderNetworkEligibilityResponse.builder()
                    .networkProvider(false)
                    .providerId(providerId)
                    .networkId(networkId)
                    .message("Provider does not exist")
                    .build();
        }

        Optional<ProviderNetwork> networkOpt = networkRepository.findById(networkId);
        if (!networkOpt.isPresent()) {
            return ProviderNetworkEligibilityResponse.builder()
                    .networkProvider(false)
                    .providerId(providerId)
                    .providerName(providerOpt.get().getProviderName())
                    .networkId(networkId)
                    .message("Network does not exist")
                    .build();
        }

        Provider provider = providerOpt.get();
        ProviderNetwork network = networkOpt.get();

        Optional<ProviderNetworkMapping> mappingOpt =
                mappingRepository.findByProvider_ProviderIdAndNetwork_NetworkId(providerId, networkId);

        if (!mappingOpt.isPresent()) {
            return ProviderNetworkEligibilityResponse.builder()
                    .networkProvider(false)
                    .providerId(providerId)
                    .providerName(provider.getProviderName())
                    .networkId(networkId)
                    .networkName(network.getNetworkName())
                    .providerStatus(provider.getStatus())
                    .mappingActive(false)
                    .message("Provider is not associated with this network (Out-of-Network)")
                    .build();
        }

        ProviderNetworkMapping mapping = mappingOpt.get();
        boolean isEligible = provider.getStatus() == ProviderStatus.ACTIVE
                && network.getStatus() == NetworkStatus.ACTIVE
                && mapping.isActive();

        String message = isEligible
                ? "Provider is an eligible active In-Network provider"
                : "Provider is mapped but not active or network/provider status is inactive";

        return ProviderNetworkEligibilityResponse.builder()
                .networkProvider(isEligible)
                .providerId(providerId)
                .providerName(provider.getProviderName())
                .networkId(networkId)
                .networkName(network.getNetworkName())
                .providerStatus(provider.getStatus())
                .mappingActive(mapping.isActive())
                .message(message)
                .build();
    }
}
