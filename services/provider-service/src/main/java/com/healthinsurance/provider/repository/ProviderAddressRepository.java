package com.healthinsurance.provider.repository;

import com.healthinsurance.provider.entity.ProviderAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProviderAddressRepository extends JpaRepository<ProviderAddress, UUID> {

    List<ProviderAddress> findByProvider_ProviderId(UUID providerId);
}
