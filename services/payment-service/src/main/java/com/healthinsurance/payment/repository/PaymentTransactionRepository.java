package com.healthinsurance.payment.repository;

import com.healthinsurance.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    List<PaymentTransaction> findByPolicyIdOrderByCreatedAtDesc(UUID policyId);
    Optional<PaymentTransaction> findByGatewayReference(String gatewayReference);
}