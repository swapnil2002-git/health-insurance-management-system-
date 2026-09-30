export interface DashboardSummary {
  totalPoliciesIssued: number;
  totalPoliciesActive: number;
  totalPoliciesExpired: number;
  totalPoliciesCancelled: number;
  totalClaimsSubmitted: number;
  totalClaimsApproved: number;
  totalClaimsRejected: number;
  totalClaimsSettled: number;
  totalClaimedAmount: number;
  totalApprovedAmount: number;
  totalPaymentsCollected: number;
  totalPremiumAmount: number;
  claimApprovalRate: number;
  generatedAt: string;
}

export interface PolicySummaryDto {
  summaryDate: string;
  totalPoliciesIssued: number;
  totalPoliciesActive: number;
  totalPoliciesExpired: number;
  totalPoliciesCancelled: number;
}

export interface PolicyReportResponse {
  startDate: string;
  endDate: string;
  overallPoliciesIssued: number;
  overallPoliciesActive: number;
  overallPoliciesExpired: number;
  overallPoliciesCancelled: number;
  dailySummaries: PolicySummaryDto[];
}

export interface ClaimSummaryDto {
  summaryDate: string;
  totalClaimsSubmitted: number;
  totalClaimsApproved: number;
  totalClaimsRejected: number;
  totalClaimsSettled: number;
  totalClaimedAmount: number;
  totalApprovedAmount: number;
}

export interface ClaimReportResponse {
  startDate: string;
  endDate: string;
  overallClaimsSubmitted: number;
  overallClaimsApproved: number;
  overallClaimsRejected: number;
  overallClaimsSettled: number;
  overallClaimedAmount: number;
  overallApprovedAmount: number;
  approvalRatePercentage: number;
  dailySummaries: ClaimSummaryDto[];
}

export interface PremiumSummaryDto {
  summaryDate: string;
  totalPaymentsCollected: number;
  totalPremiumAmount: number;
}

export interface PremiumReportResponse {
  startDate: string;
  endDate: string;
  overallPaymentsCollected: number;
  overallPremiumAmount: number;
  dailySummaries: PremiumSummaryDto[];
}
