package com.healthinsurance.customer.repository;

import com.healthinsurance.customer.entity.Nominee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface NomineeRepository extends JpaRepository<Nominee, UUID> {
    java.util.List<Nominee> findByCustomer_CustomerId(UUID customerId);
}