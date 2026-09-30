package com.healthinsurance.claims.repository;

import com.healthinsurance.claims.entity.ClaimPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClaimPaymentRepository extends JpaRepository<ClaimPayment, UUID> {

    List<ClaimPayment> findByClaim_ClaimId(UUID claimId);
}
