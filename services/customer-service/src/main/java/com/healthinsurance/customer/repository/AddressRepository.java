package com.healthinsurance.customer.repository;

import com.healthinsurance.customer.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {}