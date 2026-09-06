package com.healthinsurance.premium.repository;

import com.healthinsurance.premium.entity.PremiumInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PremiumInstallmentRepository extends JpaRepository<PremiumInstallment, UUID> {
    List<PremiumInstallment> findByPremiumSchedule_ScheduleIdOrderByInstallmentNumberAsc(UUID scheduleId);
}