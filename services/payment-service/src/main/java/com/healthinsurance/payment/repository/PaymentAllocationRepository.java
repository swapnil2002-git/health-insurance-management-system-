package com.healthinsurance.payment.repository;

import com.healthinsurance.payment.entity.PaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, UUID> {
    List<PaymentAllocation> findByPayment_PaymentId(UUID paymentId);
    List<PaymentAllocation> findByInstallmentId(UUID installmentId);
}