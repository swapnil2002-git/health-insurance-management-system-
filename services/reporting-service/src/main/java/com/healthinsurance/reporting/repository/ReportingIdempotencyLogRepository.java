package com.healthinsurance.reporting.repository;

import com.healthinsurance.reporting.entity.ReportingIdempotencyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportingIdempotencyLogRepository extends JpaRepository<ReportingIdempotencyLog, Long> {

    boolean existsByEventId(String eventId);

    Optional<ReportingIdempotencyLog> findByEventId(String eventId);
}
