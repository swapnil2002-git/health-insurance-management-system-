package com.healthinsurance.customer.repository;

import com.healthinsurance.customer.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, UUID> {
    java.util.List<Beneficiary> findByCustomer_CustomerId(UUID customerId);
}