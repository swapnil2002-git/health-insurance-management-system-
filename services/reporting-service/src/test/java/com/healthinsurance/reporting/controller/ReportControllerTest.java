package com.healthinsurance.reporting.controller;

import com.healthinsurance.reporting.dto.DashboardSummaryResponse;
import com.healthinsurance.reporting.dto.PolicyReportResponse;
import com.healthinsurance.reporting.service.ReportingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportingService reportingService;

    @InjectMocks
    private ReportController reportController;

    @Test
    void testGetDashboardSummary() {
        DashboardSummaryResponse mockResponse = DashboardSummaryResponse.builder()
                .totalPoliciesIssued(50L)
                .totalPoliciesActive(45L)
                .claimApprovalRate(80.0)
                .generatedAt(LocalDateTime.now())
                .build();

        when(reportingService.getDashboardSummary()).thenReturn(mockResponse);

        ResponseEntity<DashboardSummaryResponse> response = reportController.getDashboardSummary();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(50L, response.getBody().getTotalPoliciesIssued());
    }

    @Test
    void testGetPolicyReport() {
        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now();

        PolicyReportResponse mockResponse = PolicyReportResponse.builder()
                .startDate(start)
                .endDate(end)
                .overallPoliciesIssued(20L)
                .build();

        when(reportingService.getPolicyReport(start, end)).thenReturn(mockResponse);

        ResponseEntity<PolicyReportResponse> response = reportController.getPolicyReport(start, end);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(20L, response.getBody().getOverallPoliciesIssued());
    }
}
