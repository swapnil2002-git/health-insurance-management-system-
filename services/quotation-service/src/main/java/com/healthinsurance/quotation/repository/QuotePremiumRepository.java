package com.healthinsurance.quotation.repository;

import com.healthinsurance.quotation.entity.QuotePremium;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface QuotePremiumRepository extends JpaRepository<QuotePremium, UUID> {}