package com.healthinsurance.policy.repository;

import com.healthinsurance.policy.entity.PolicyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PolicyMemberRepository extends JpaRepository<PolicyMember, UUID> {
}