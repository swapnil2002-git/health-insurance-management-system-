package com.healthinsurance.reporting.service.impl;

import com.healthinsurance.reporting.dto.*;
import com.healthinsurance.reporting.entity.*;
import com.healthinsurance.reporting.mapper.ReportingMapper;
import com.healthinsurance.reporting.repository.*;
import com.healthinsurance.reporting.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportingServiceImpl implements ReportingService {

    private static final Logger log = LoggerFactory.getLogger(ReportingServiceImpl.class);

    private final PolicyDailySummaryRepository policySummaryRepo;
    private final ClaimDailySummaryRepository claimSummaryRepo;
    private final PremiumDailySummaryRepository premiumSummaryRepo;
    private final ReportingIdempotencyLogRepository idempotencyRepo;
    private final ReportingMapper reportingMapper;

    @Override
    @Transactional
    public void recordPolicyIssued(String eventId, LocalDate eventDate, String status) {
        if (isAlreadyProcessed(eventId)) {
            log.info("Policy event {} already processed for reporting. Skipping.", eventId);
            return;
        }

        LocalDate date = (eventDate != null) ? eventDate : LocalDate.now();
        PolicyDailySummary summary = policySummaryRepo.findBySummaryDate(date)
                .orElseGet(() -> PolicyDailySummary.builder()
                        .summaryDate(date)
                        .totalPoliciesIssued(0L)
                        .totalPoliciesActive(0L)
                        .totalPoliciesExpired(0L)
                        .totalPoliciesCancelled(0L)
                        .build());

        summary.setTotalPoliciesIssued(summary.getTotalPoliciesIssued() + 1);
        summary.setTotalPoliciesActive(summary.getTotalPoliciesActive() + 1);

        policySummaryRepo.save(summary);
        logIdempotency(eventId, "POLICY_ISSUED");
        log.info("Successfully recorded policy event: {} for date: {}", eventId, date);
    }

    @Override
    @Transactional
    public void recordPremiumPayment(String eventId, LocalDate eventDate, BigDecimal amount) {
        if (isAlreadyProcessed(eventId)) {
            log.info("Premium payment event {} already processed for reporting. Skipping.", eventId);
            return;
        }

        LocalDate date = (eventDate != null) ? eventDate : LocalDate.now();
        BigDecimal paymentAmount = (amount != null) ? amount : BigDecimal.ZERO;

        PremiumDailySummary summary = premiumSummaryRepo.findBySummaryDate(date)
                .orElseGet(() -> PremiumDailySummary.builder()
                        .summaryDate(date)
                        .totalPaymentsCollected(0L)
                        .totalPremiumAmount(BigDecimal.ZERO)
                        .build());

        summary.setTotalPaymentsCollected(summary.getTotalPaymentsCollected() + 1);
        summary.setTotalPremiumAmount(summary.getTotalPremiumAmount().add(paymentAmount));

        premiumSummaryRepo.save(summary);
        logIdempotency(eventId, "PREMIUM_PAID");
        log.info("Successfully recorded premium payment event: {} for date: {}, amount: {}", eventId, date, paymentAmount);
    }

    @Override
    @Transactional
    public void recordClaimEvent(String eventId, LocalDate eventDate, String eventType, String status, BigDecimal claimedAmount, BigDecimal approvedAmount) {
        if (isAlreadyProcessed(eventId)) {
            log.info("Claim event {} already processed for reporting. Skipping.", eventId);
            return;
        }

        LocalDate date = (eventDate != null) ? eventDate : LocalDate.now();
        ClaimDailySummary summary = claimSummaryRepo.findBySummaryDate(date)
                .orElseGet(() -> ClaimDailySummary.builder()
                        .summaryDate(date)
                        .totalClaimsSubmitted(0L)
                        .totalClaimsApproved(0L)
                        .totalClaimsRejected(0L)
                        .totalClaimsSettled(0L)
                        .totalClaimedAmount(BigDecimal.ZERO)
                        .totalApprovedAmount(BigDecimal.ZERO)
                        .build());

        if ("CLAIM_SUBMITTED".equalsIgnoreCase(eventType) || "SUBMITTED".equalsIgnoreCase(status)) {
            summary.setTotalClaimsSubmitted(summary.getTotalClaimsSubmitted() + 1);
            if (claimedAmount != null) {
                summary.setTotalClaimedAmount(summary.getTotalClaimedAmount().add(claimedAmount));
            }
        } else if ("CLAIM_APPROVED".equalsIgnoreCase(eventType) || "APPROVED".equalsIgnoreCase(status)) {
            summary.setTotalClaimsApproved(summary.getTotalClaimsApproved() + 1);
            if (approvedAmount != null) {
                summary.setTotalApprovedAmount(summary.getTotalApprovedAmount().add(approvedAmount));
            }
        } else if ("CLAIM_REJECTED".equalsIgnoreCase(eventType) || "REJECTED".equalsIgnoreCase(status)) {
            summary.setTotalClaimsRejected(summary.getTotalClaimsRejected() + 1);
        } else if ("CLAIM_SETTLED".equalsIgnoreCase(eventType) || "SETTLED".equalsIgnoreCase(status)) {
            summary.setTotalClaimsSettled(summary.getTotalClaimsSettled() + 1);
        }

        claimSummaryRepo.save(summary);
        logIdempotency(eventId, eventType != null ? eventType : "CLAIM_EVENT");
        log.info("Successfully recorded claim event: {} type: {} date: {}", eventId, eventType, date);
    }

    @Override
    @Transactional(readOnly = true)
    public PolicyReportResponse getPolicyReport(LocalDate startDate, LocalDate endDate) {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();

        List<PolicyDailySummary> summaries = policySummaryRepo.findBySummaryDateBetweenOrderBySummaryDateAsc(start, end);

        long totalIssued = summaries.stream().mapToLong(PolicyDailySummary::getTotalPoliciesIssued).sum();
        long totalActive = summaries.stream().mapToLong(PolicyDailySummary::getTotalPoliciesActive).sum();
        long totalExpired = summaries.stream().mapToLong(PolicyDailySummary::getTotalPoliciesExpired).sum();
        long totalCancelled = summaries.stream().mapToLong(PolicyDailySummary::getTotalPoliciesCancelled).sum();

        return PolicyReportResponse.builder()
                .startDate(start)
                .endDate(end)
                .overallPoliciesIssued(totalIssued)
                .overallPoliciesActive(totalActive)
                .overallPoliciesExpired(totalExpired)
                .overallPoliciesCancelled(totalCancelled)
                .dailySummaries(reportingMapper.toPolicyDtoList(summaries))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimReportResponse getClaimReport(LocalDate startDate, LocalDate endDate) {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();

        List<ClaimDailySummary> summaries = claimSummaryRepo.findBySummaryDateBetweenOrderBySummaryDateAsc(start, end);

        long submitted = summaries.stream().mapToLong(ClaimDailySummary::getTotalClaimsSubmitted).sum();
        long approved = summaries.stream().mapToLong(ClaimDailySummary::getTotalClaimsApproved).sum();
        long rejected = summaries.stream().mapToLong(ClaimDailySummary::getTotalClaimsRejected).sum();
        long settled = summaries.stream().mapToLong(ClaimDailySummary::getTotalClaimsSettled).sum();

        BigDecimal totalClaimed = summaries.stream()
                .map(ClaimDailySummary::getTotalClaimedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalApproved = summaries.stream()
                .map(ClaimDailySummary::getTotalApprovedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double approvalRate = (submitted > 0)
                ? BigDecimal.valueOf((double) approved / submitted * 100.0).setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        return ClaimReportResponse.builder()
                .startDate(start)
                .endDate(end)
                .overallClaimsSubmitted(submitted)
                .overallClaimsApproved(approved)
                .overallClaimsRejected(rejected)
                .overallClaimsSettled(settled)
                .overallClaimedAmount(totalClaimed)
                .overallApprovedAmount(totalApproved)
                .approvalRatePercentage(approvalRate)
                .dailySummaries(reportingMapper.toClaimDtoList(summaries))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PremiumReportResponse getPremiumReport(LocalDate startDate, LocalDate endDate) {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();

        List<PremiumDailySummary> summaries = premiumSummaryRepo.findBySummaryDateBetweenOrderBySummaryDateAsc(start, end);

        long payments = summaries.stream().mapToLong(PremiumDailySummary::getTotalPaymentsCollected).sum();
        BigDecimal totalAmount = summaries.stream()
                .map(PremiumDailySummary::getTotalPremiumAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PremiumReportResponse.builder()
                .startDate(start)
                .endDate(end)
                .overallPaymentsCollected(payments)
                .overallPremiumAmount(totalAmount)
                .dailySummaries(reportingMapper.toPremiumDtoList(summaries))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        Long totalIssued = policySummaryRepo.sumTotalPoliciesIssued();
        Long totalActive = policySummaryRepo.sumTotalPoliciesActive();
        Long totalExpired = policySummaryRepo.sumTotalPoliciesExpired();
        Long totalCancelled = policySummaryRepo.sumTotalPoliciesCancelled();

        Long totalClaimsSubmitted = claimSummaryRepo.sumTotalClaimsSubmitted();
        Long totalClaimsApproved = claimSummaryRepo.sumTotalClaimsApproved();
        Long totalClaimsRejected = claimSummaryRepo.sumTotalClaimsRejected();
        Long totalClaimsSettled = claimSummaryRepo.sumTotalClaimsSettled();
        BigDecimal totalClaimedAmount = claimSummaryRepo.sumTotalClaimedAmount();
        BigDecimal totalApprovedAmount = claimSummaryRepo.sumTotalApprovedAmount();

        Long totalPaymentsCollected = premiumSummaryRepo.sumTotalPaymentsCollected();
        BigDecimal totalPremiumAmount = premiumSummaryRepo.sumTotalPremiumAmount();

        double approvalRate = (totalClaimsSubmitted != null && totalClaimsSubmitted > 0 && totalClaimsApproved != null)
                ? BigDecimal.valueOf((double) totalClaimsApproved / totalClaimsSubmitted * 100.0).setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        return DashboardSummaryResponse.builder()
                .totalPoliciesIssued(totalIssued != null ? totalIssued : 0L)
                .totalPoliciesActive(totalActive != null ? totalActive : 0L)
                .totalPoliciesExpired(totalExpired != null ? totalExpired : 0L)
                .totalPoliciesCancelled(totalCancelled != null ? totalCancelled : 0L)
                .totalClaimsSubmitted(totalClaimsSubmitted != null ? totalClaimsSubmitted : 0L)
                .totalClaimsApproved(totalClaimsApproved != null ? totalClaimsApproved : 0L)
                .totalClaimsRejected(totalClaimsRejected != null ? totalClaimsRejected : 0L)
                .totalClaimsSettled(totalClaimsSettled != null ? totalClaimsSettled : 0L)
                .totalClaimedAmount(totalClaimedAmount != null ? totalClaimedAmount : BigDecimal.ZERO)
                .totalApprovedAmount(totalApprovedAmount != null ? totalApprovedAmount : BigDecimal.ZERO)
                .totalPaymentsCollected(totalPaymentsCollected != null ? totalPaymentsCollected : 0L)
                .totalPremiumAmount(totalPremiumAmount != null ? totalPremiumAmount : BigDecimal.ZERO)
                .claimApprovalRate(approvalRate)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private boolean isAlreadyProcessed(String eventId) {
        if (eventId == null || eventId.isBlank()) return false;
        return idempotencyRepo.existsByEventId(eventId);
    }

    private void logIdempotency(String eventId, String eventType) {
        if (eventId != null && !eventId.isBlank()) {
            idempotencyRepo.save(ReportingIdempotencyLog.builder()
                    .eventId(eventId)
                    .eventType(eventType)
                    .processedAt(LocalDateTime.now())
                    .build());
        }
    }
}
