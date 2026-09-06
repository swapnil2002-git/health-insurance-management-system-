package com.healthinsurance.payment.repository;

import com.healthinsurance.payment.entity.RefundTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RefundTransactionRepository extends JpaRepository<RefundTransaction, UUID> {
    List<RefundTransaction> findByPayment_PaymentId(UUID paymentId);
}