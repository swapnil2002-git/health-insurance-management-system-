package com.healthinsurance.provider.service;

import com.healthinsurance.provider.dto.ProviderNetworkEligibilityResponse;
import com.healthinsurance.provider.dto.ProviderNetworkMappingRequest;
import com.healthinsurance.provider.dto.ProviderNetworkMappingResponse;
import com.healthinsurance.provider.entity.Provider;
import com.healthinsurance.provider.entity.ProviderNetwork;
import com.healthinsurance.provider.entity.ProviderNetworkMapping;
import com.healthinsurance.provider.enums.NetworkStatus;
import com.healthinsurance.provider.enums.ProviderStatus;
import com.healthinsurance.provider.enums.ProviderType;
import com.healthinsurance.provider.exception.DuplicateResourceException;
import com.healthinsurance.provider.mapper.ProviderMapper;
import com.healthinsurance.provider.repository.ProviderNetworkMappingRepository;
import com.healthinsurance.provider.repository.ProviderNetworkRepository;
import com.healthinsurance.provider.repository.ProviderRepository;
import com.healthinsurance.provider.service.impl.ProviderNetworkMappingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderNetworkMappingServiceTest {

    @Mock
    private ProviderNetworkMappingRepository mappingRepository;

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private ProviderNetworkRepository networkRepository;

    @Mock
    private ProviderMapper providerMapper;

    @InjectMocks
    private ProviderNetworkMappingServiceImpl mappingService;

    private UUID providerId;
    private UUID networkId;
    private Provider provider;
    private ProviderNetwork network;
    private ProviderNetworkMapping mapping;

    @BeforeEach
    void setUp() {
        providerId = UUID.randomUUID();
        networkId = UUID.randomUUID();

        provider = new Provider();
        provider.setProviderId(providerId);
        provider.setProviderName("Max Super Speciality");
        provider.setProviderType(ProviderType.HOSPITAL);
        provider.setStatus(ProviderStatus.ACTIVE);

        network = new ProviderNetwork();
        network.setNetworkId(networkId);
        network.setNetworkName("Tier-1 Hospitals Network");
        network.setStatus(NetworkStatus.ACTIVE);

        mapping = new ProviderNetworkMapping();
        mapping.setMappingId(UUID.randomUUID());
        mapping.setProvider(provider);
        mapping.setNetwork(network);
        mapping.setActive(true);
    }

    @Test
    void testMapProviderToNetwork_Success() {
        ProviderNetworkMappingRequest request = ProviderNetworkMappingRequest.builder()
                .providerId(providerId)
                .networkId(networkId)
                .active(true)
                .build();

        when(providerRepository.findById(providerId)).thenReturn(Optional.of(provider));
        when(networkRepository.findById(networkId)).thenReturn(Optional.of(network));
        when(mappingRepository.existsByProvider_ProviderIdAndNetwork_NetworkId(providerId, networkId)).thenReturn(false);
        when(mappingRepository.save(any(ProviderNetworkMapping.class))).thenReturn(mapping);

        ProviderNetworkMappingResponse response = ProviderNetworkMappingResponse.builder()
                .mappingId(mapping.getMappingId())
                .providerId(providerId)
                .networkId(networkId)
                .active(true)
                .build();
        when(providerMapper.toResponse(any(ProviderNetworkMapping.class))).thenReturn(response);

        ProviderNetworkMappingResponse result = mappingService.mapProviderToNetwork(request);

        assertNotNull(result);
        assertEquals(providerId, result.getProviderId());
        assertEquals(networkId, result.getNetworkId());
        assertTrue(result.isActive());
        verify(mappingRepository, times(1)).save(any(ProviderNetworkMapping.class));
    }

    @Test
    void testMapProviderToNetwork_DuplicateThrowsException() {
        ProviderNetworkMappingRequest request = ProviderNetworkMappingRequest.builder()
                .providerId(providerId)
                .networkId(networkId)
                .active(true)
                .build();

        when(providerRepository.findById(providerId)).thenReturn(Optional.of(provider));
        when(networkRepository.findById(networkId)).thenReturn(Optional.of(network));
        when(mappingRepository.existsByProvider_ProviderIdAndNetwork_NetworkId(providerId, networkId)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> mappingService.mapProviderToNetwork(request));
        verify(mappingRepository, never()).save(any(ProviderNetworkMapping.class));
    }

    @Test
    void testVerifyEligibility_InNetworkActive() {
        when(providerRepository.findById(providerId)).thenReturn(Optional.of(provider));
        when(networkRepository.findById(networkId)).thenReturn(Optional.of(network));
        when(mappingRepository.findByProvider_ProviderIdAndNetwork_NetworkId(providerId, networkId))
                .thenReturn(Optional.of(mapping));

        ProviderNetworkEligibilityResponse eligibility = mappingService.verifyEligibility(providerId, networkId);

        assertNotNull(eligibility);
        assertTrue(eligibility.isNetworkProvider());
        assertEquals("Max Super Speciality", eligibility.getProviderName());
        assertEquals("Tier-1 Hospitals Network", eligibility.getNetworkName());
        assertTrue(eligibility.getMessage().contains("eligible active In-Network provider"));
    }

    @Test
    void testVerifyEligibility_OutOfNetwork() {
        when(providerRepository.findById(providerId)).thenReturn(Optional.of(provider));
        when(networkRepository.findById(networkId)).thenReturn(Optional.of(network));
        when(mappingRepository.findByProvider_ProviderIdAndNetwork_NetworkId(providerId, networkId))
                .thenReturn(Optional.empty());

        ProviderNetworkEligibilityResponse eligibility = mappingService.verifyEligibility(providerId, networkId);

        assertNotNull(eligibility);
        assertFalse(eligibility.isNetworkProvider());
        assertTrue(eligibility.getMessage().contains("Out-of-Network"));
    }
}
