package com.healthinsurance.provider.service;

import com.healthinsurance.provider.dto.ProviderRequest;
import com.healthinsurance.provider.dto.ProviderResponse;
import com.healthinsurance.provider.entity.Provider;
import com.healthinsurance.provider.enums.ProviderStatus;
import com.healthinsurance.provider.enums.ProviderType;
import com.healthinsurance.provider.exception.ResourceNotFoundException;
import com.healthinsurance.provider.mapper.ProviderMapper;
import com.healthinsurance.provider.repository.ProviderRepository;
import com.healthinsurance.provider.service.impl.ProviderServiceImpl;
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
class ProviderServiceTest {

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private ProviderMapper providerMapper;

    @InjectMocks
    private ProviderServiceImpl providerService;

    private Provider provider;
    private ProviderRequest providerRequest;
    private ProviderResponse providerResponse;
    private UUID providerId;

    @BeforeEach
    void setUp() {
        providerId = UUID.randomUUID();

        provider = new Provider();
        provider.setProviderId(providerId);
        provider.setProviderName("Apollo Hospital");
        provider.setProviderType(ProviderType.HOSPITAL);
        provider.setStatus(ProviderStatus.ACTIVE);

        providerRequest = ProviderRequest.builder()
                .providerName("Apollo Hospital")
                .providerType(ProviderType.HOSPITAL)
                .contactEmail("contact@apollo.com")
                .contactPhone("9876543210")
                .status(ProviderStatus.ACTIVE)
                .build();

        providerResponse = ProviderResponse.builder()
                .providerId(providerId)
                .providerName("Apollo Hospital")
                .providerType(ProviderType.HOSPITAL)
                .status(ProviderStatus.ACTIVE)
                .build();
    }

    @Test
    void testCreateProvider_Success() {
        when(providerMapper.toEntity(any(ProviderRequest.class))).thenReturn(provider);
        when(providerRepository.save(any(Provider.class))).thenReturn(provider);
        when(providerMapper.toResponse(any(Provider.class))).thenReturn(providerResponse);

        ProviderResponse result = providerService.createProvider(providerRequest);

        assertNotNull(result);
        assertEquals("Apollo Hospital", result.getProviderName());
        assertEquals(ProviderStatus.ACTIVE, result.getStatus());
        verify(providerRepository, times(1)).save(any(Provider.class));
    }

    @Test
    void testGetProviderById_Success() {
        when(providerRepository.findById(providerId)).thenReturn(Optional.of(provider));
        when(providerMapper.toResponse(provider)).thenReturn(providerResponse);

        ProviderResponse result = providerService.getProviderById(providerId);

        assertNotNull(result);
        assertEquals(providerId, result.getProviderId());
    }

    @Test
    void testGetProviderById_NotFound_ThrowsException() {
        when(providerRepository.findById(providerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> providerService.getProviderById(providerId));
    }

    @Test
    void testUpdateProviderStatus_Success() {
        when(providerRepository.findById(providerId)).thenReturn(Optional.of(provider));
        when(providerRepository.save(any(Provider.class))).thenReturn(provider);

        ProviderResponse inactiveResponse = ProviderResponse.builder()
                .providerId(providerId)
                .providerName("Apollo Hospital")
                .status(ProviderStatus.INACTIVE)
                .build();
        when(providerMapper.toResponse(provider)).thenReturn(inactiveResponse);

        ProviderResponse result = providerService.updateProviderStatus(providerId, ProviderStatus.INACTIVE);

        assertNotNull(result);
        assertEquals(ProviderStatus.INACTIVE, result.getStatus());
    }
}
