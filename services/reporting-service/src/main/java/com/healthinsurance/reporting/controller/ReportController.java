package com.healthinsurance.reporting.controller;

import com.healthinsurance.reporting.dto.ClaimReportResponse;
import com.healthinsurance.reporting.dto.DashboardSummaryResponse;
import com.healthinsurance.reporting.dto.PolicyReportResponse;
import com.healthinsurance.reporting.dto.PremiumReportResponse;
import com.healthinsurance.reporting.service.ReportingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reporting Management", description = "Endpoints for generating insurance policy, claims, premium, and dashboard analytics")
public class ReportController {

    private final ReportingService reportingService;

    @GetMapping("/dashboard-summary")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'ADMIN', 'POLICY_ADMINISTRATOR', 'FINANCE_OFFICER', 'CLAIMS_OFFICER', 'UNDERWRITER')")
    @Operation(summary = "Get executive dashboard summary", description = "Fetches global KPIs across policies, claims, and premiums")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
        return ResponseEntity.ok(reportingService.getDashboardSummary());
    }

    @GetMapping("/policies")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'ADMIN', 'POLICY_ADMINISTRATOR', 'UNDERWRITER')")
    @Operation(summary = "Get policy daily report", description = "Fetches aggregated policy metrics across a date range")
    public ResponseEntity<PolicyReportResponse> getPolicyReport(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportingService.getPolicyReport(startDate, endDate));
    }

    @GetMapping("/claims")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'ADMIN', 'POLICY_ADMINISTRATOR', 'CLAIMS_OFFICER', 'FINANCE_OFFICER')")
    @Operation(summary = "Get claims daily report", description = "Fetches aggregated claim metrics, approval counts, and settlement financial sums")
    public ResponseEntity<ClaimReportResponse> getClaimReport(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportingService.getClaimReport(startDate, endDate));
    }

    @GetMapping("/premiums")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'ADMIN', 'POLICY_ADMINISTRATOR', 'FINANCE_OFFICER')")
    @Operation(summary = "Get premium revenue daily report", description = "Fetches aggregated payment transactions and collected revenue volume")
    public ResponseEntity<PremiumReportResponse> getPremiumReport(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportingService.getPremiumReport(startDate, endDate));
    }
}
