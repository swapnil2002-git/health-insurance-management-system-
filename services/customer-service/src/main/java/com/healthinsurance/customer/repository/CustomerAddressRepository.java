package com.healthinsurance.customer.repository;

import com.healthinsurance.customer.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, UUID> {
    java.util.List<CustomerAddress> findByCustomer_CustomerId(UUID customerId);
}