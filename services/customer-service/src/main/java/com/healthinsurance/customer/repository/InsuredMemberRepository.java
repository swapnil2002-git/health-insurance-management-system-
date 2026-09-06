package com.healthinsurance.customer.repository;

import com.healthinsurance.customer.entity.InsuredMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface InsuredMemberRepository extends JpaRepository<InsuredMember, UUID> {
    java.util.List<InsuredMember> findByCustomer_CustomerId(UUID customerId);
}