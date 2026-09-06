package com.healthinsurance.provider.repository;

import com.healthinsurance.provider.entity.ProviderNetworkMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProviderNetworkMappingRepository extends JpaRepository<ProviderNetworkMapping, UUID> {

    List<ProviderNetworkMapping> findByProvider_ProviderId(UUID providerId);

    List<ProviderNetworkMapping> findByNetwork_NetworkId(UUID networkId);

    Optional<ProviderNetworkMapping> findByProvider_ProviderIdAndNetwork_NetworkId(UUID providerId, UUID networkId);

    boolean existsByProvider_ProviderIdAndNetwork_NetworkId(UUID providerId, UUID networkId);
}
