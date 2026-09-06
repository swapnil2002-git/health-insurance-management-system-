package com.healthinsurance.provider.service.impl;

import com.healthinsurance.provider.dto.ProviderNetworkRequest;
import com.healthinsurance.provider.dto.ProviderNetworkResponse;
import com.healthinsurance.provider.entity.ProviderNetwork;
import com.healthinsurance.provider.enums.NetworkStatus;
import com.healthinsurance.provider.exception.DuplicateResourceException;
import com.healthinsurance.provider.exception.ResourceNotFoundException;
import com.healthinsurance.provider.mapper.ProviderMapper;
import com.healthinsurance.provider.repository.ProviderNetworkRepository;
import com.healthinsurance.provider.service.ProviderNetworkService;
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
public class ProviderNetworkServiceImpl implements ProviderNetworkService {

    private final ProviderNetworkRepository networkRepository;
    private final ProviderMapper providerMapper;

    @Override
    public ProviderNetworkResponse createNetwork(ProviderNetworkRequest request) {
        log.info("Creating network with name: {}", request.getNetworkName());
        if (networkRepository.existsByNetworkNameIgnoreCase(request.getNetworkName())) {
            throw new DuplicateResourceException("Provider network already exists with name: " + request.getNetworkName());
        }

        ProviderNetwork network = providerMapper.toEntity(request);
        if (network.getStatus() == null) {
            network.setStatus(NetworkStatus.ACTIVE);
        }

        ProviderNetwork savedNetwork = networkRepository.save(network);
        log.info("Network created with ID: {}", savedNetwork.getNetworkId());
        return providerMapper.toResponse(savedNetwork);
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderNetworkResponse getNetworkById(UUID networkId) {
        log.info("Fetching network with ID: {}", networkId);
        ProviderNetwork network = networkRepository.findById(networkId)
                .orElseThrow(() -> new ResourceNotFoundException("Network not found with ID: " + networkId));
        return providerMapper.toResponse(network);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderNetworkResponse> getAllNetworks() {
        log.info("Fetching all provider networks");
        List<ProviderNetwork> networks = networkRepository.findAll();
        return providerMapper.toNetworkResponseList(networks);
    }

    @Override
    public ProviderNetworkResponse updateNetwork(UUID networkId, ProviderNetworkRequest request) {
        log.info("Updating network with ID: {}", networkId);
        ProviderNetwork network = networkRepository.findById(networkId)
                .orElseThrow(() -> new ResourceNotFoundException("Network not found with ID: " + networkId));

        if (!network.getNetworkName().equalsIgnoreCase(request.getNetworkName())
                && networkRepository.existsByNetworkNameIgnoreCase(request.getNetworkName())) {
            throw new DuplicateResourceException("Provider network already exists with name: " + request.getNetworkName());
        }

        network.setNetworkName(request.getNetworkName());
        network.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            network.setStatus(request.getStatus());
        }
        network.setUpdatedAt(Instant.now());

        ProviderNetwork updated = networkRepository.save(network);
        return providerMapper.toResponse(updated);
    }
}
