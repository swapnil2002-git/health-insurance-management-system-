package com.healthinsurance.claims.repository;

import com.healthinsurance.claims.domain.ClaimStatus;
import com.healthinsurance.claims.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    Optional<Claim> findByClaimNumber(String claimNumber);

    List<Claim> findByPolicyId(UUID policyId);

    List<Claim> findByMemberId(UUID memberId);

    List<Claim> findByStatus(ClaimStatus status);

    boolean existsByPolicyIdAndMemberIdAndServiceDateAndStatusNot(
            UUID policyId, UUID memberId, LocalDate serviceDate, ClaimStatus status);
}
