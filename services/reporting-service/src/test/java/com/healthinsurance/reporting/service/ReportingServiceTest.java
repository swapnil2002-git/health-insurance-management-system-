package com.healthinsurance.reporting.service;

import com.healthinsurance.reporting.dto.*;
import com.healthinsurance.reporting.entity.*;
import com.healthinsurance.reporting.mapper.ReportingMapper;
import com.healthinsurance.reporting.repository.*;
import com.healthinsurance.reporting.service.impl.ReportingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportingServiceTest {

    @Mock
    private PolicyDailySummaryRepository policySummaryRepo;

    @Mock
    private ClaimDailySummaryRepository claimSummaryRepo;

    @Mock
    private PremiumDailySummaryRepository premiumSummaryRepo;

    @Mock
    private ReportingIdempotencyLogRepository idempotencyRepo;

    @Mock
    private ReportingMapper reportingMapper;

    @InjectMocks
    private ReportingServiceImpl reportingService;

    @Test
    void testRecordPolicyIssued_NewDay() {
        LocalDate date = LocalDate.now();
        String eventId = "EVENT_POL_1";

        when(idempotencyRepo.existsByEventId(eventId)).thenReturn(false);
        when(policySummaryRepo.findBySummaryDate(date)).thenReturn(Optional.empty());

        reportingService.recordPolicyIssued(eventId, date, "ACTIVE");

        verify(policySummaryRepo).save(any(PolicyDailySummary.class));
        verify(idempotencyRepo).save(any(ReportingIdempotencyLog.class));
    }

    @Test
    void testRecordPolicyIssued_Idempotent() {
        String eventId = "EVENT_POL_1";
        when(idempotencyRepo.existsByEventId(eventId)).thenReturn(true);

        reportingService.recordPolicyIssued(eventId, LocalDate.now(), "ACTIVE");

        verify(policySummaryRepo, never()).save(any());
    }

    @Test
    void testRecordPremiumPayment_Success() {
        LocalDate date = LocalDate.now();
        String eventId = "EVENT_PREM_1";
        BigDecimal amount = new BigDecimal("150.00");

        when(idempotencyRepo.existsByEventId(eventId)).thenReturn(false);
        when(premiumSummaryRepo.findBySummaryDate(date)).thenReturn(Optional.empty());

        reportingService.recordPremiumPayment(eventId, date, amount);

        verify(premiumSummaryRepo).save(any(PremiumDailySummary.class));
        verify(idempotencyRepo).save(any(ReportingIdempotencyLog.class));
    }

    @Test
    void testRecordClaimEvent_SubmittedAndApproved() {
        LocalDate date = LocalDate.now();
        String eventId = "EVENT_CLAIM_1";

        when(idempotencyRepo.existsByEventId(eventId)).thenReturn(false);
        when(claimSummaryRepo.findBySummaryDate(date)).thenReturn(Optional.empty());

        reportingService.recordClaimEvent(eventId, date, "CLAIM_SUBMITTED", "SUBMITTED", new BigDecimal("500.00"), null);

        verify(claimSummaryRepo).save(any(ClaimDailySummary.class));
        verify(idempotencyRepo).save(any(ReportingIdempotencyLog.class));
    }

    @Test
    void testGetDashboardSummary() {
        when(policySummaryRepo.sumTotalPoliciesIssued()).thenReturn(100L);
        when(policySummaryRepo.sumTotalPoliciesActive()).thenReturn(90L);
        when(policySummaryRepo.sumTotalPoliciesExpired()).thenReturn(5L);
        when(policySummaryRepo.sumTotalPoliciesCancelled()).thenReturn(5L);

        when(claimSummaryRepo.sumTotalClaimsSubmitted()).thenReturn(20L);
        when(claimSummaryRepo.sumTotalClaimsApproved()).thenReturn(15L);
        when(claimSummaryRepo.sumTotalClaimsRejected()).thenReturn(3L);
        when(claimSummaryRepo.sumTotalClaimsSettled()).thenReturn(12L);
        when(claimSummaryRepo.sumTotalClaimedAmount()).thenReturn(new BigDecimal("10000.00"));
        when(claimSummaryRepo.sumTotalApprovedAmount()).thenReturn(new BigDecimal("7500.00"));

        when(premiumSummaryRepo.sumTotalPaymentsCollected()).thenReturn(80L);
        when(premiumSummaryRepo.sumTotalPremiumAmount()).thenReturn(new BigDecimal("25000.00"));

        DashboardSummaryResponse response = reportingService.getDashboardSummary();

        assertNotNull(response);
        assertEquals(100L, response.getTotalPoliciesIssued());
        assertEquals(90L, response.getTotalPoliciesActive());
        assertEquals(20L, response.getTotalClaimsSubmitted());
        assertEquals(15L, response.getTotalClaimsApproved());
        assertEquals(75.0, response.getClaimApprovalRate());
        assertEquals(new BigDecimal("25000.00"), response.getTotalPremiumAmount());
    }

    @Test
    void testGetPolicyReport() {
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now();

        PolicyDailySummary summary = PolicyDailySummary.builder()
                .summaryDate(start)
                .totalPoliciesIssued(10L)
                .totalPoliciesActive(8L)
                .totalPoliciesExpired(1L)
                .totalPoliciesCancelled(1L)
                .build();

        when(policySummaryRepo.findBySummaryDateBetweenOrderBySummaryDateAsc(start, end))
                .thenReturn(Collections.singletonList(summary));
        when(reportingMapper.toPolicyDtoList(any())).thenReturn(Collections.emptyList());

        PolicyReportResponse response = reportingService.getPolicyReport(start, end);

        assertNotNull(response);
        assertEquals(10L, response.getOverallPoliciesIssued());
        assertEquals(8L, response.getOverallPoliciesActive());
    }
}
