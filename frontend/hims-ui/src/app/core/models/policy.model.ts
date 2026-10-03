export type PolicyStatus = 'DRAFT' | 'PENDING_PAYMENT' | 'ACTIVE' | 'EXPIRED' | 'CANCELLED';

export type EndorsementType =
  | 'ADD_MEMBER'
  | 'REMOVE_MEMBER'
  | 'ADDRESS_CHANGE'
  | 'NOMINEE_CHANGE'
  | 'COVERAGE_CHANGE'
  | 'RIDER_ADDITION';

export type EndorsementStatus = 'REQUESTED' | 'PENDING' | 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED' | 'APPLIED';

export type CancellationStatus = 'REQUESTED' | 'VALIDATED' | 'PENDING_APPROVAL' | 'APPROVED' | 'REFUND_CALCULATED' | 'CANCELLED' | 'REJECTED';

export type RenewalStatus = 'PENDING' | 'ACCEPTED' | 'COMPLETED' | 'REJECTED';

export interface PolicyMemberResponse {
  policyMemberId: string;
  memberId: string;
}

export interface PolicyCoverageResponse {
  policyCoverageId: string;
  coverageName: string;
  coverageAmount: number;
  deductible: number;
}

export interface PolicyBeneficiaryResponse {
  policyBeneficiaryId: string;
  beneficiaryName: string;
  relationship: string;
  percentage: number;
}

export interface PolicyResponse {
  policyId: string;
  policyNumber: string;
  customerId: string;
  planId: string;
  quoteId: string;
  status: PolicyStatus | string;
  effectiveDate: string;
  expiryDate: string;
  createdAt: string;
  members: PolicyMemberResponse[];
  coverages: PolicyCoverageResponse[];
  beneficiaries: PolicyBeneficiaryResponse[];
}

export interface PolicyMemberRequest {
  memberId: string;
}

export interface PolicyCoverageRequest {
  coverageName: string;
  coverageAmount: number;
  deductible: number;
}

export interface PolicyBeneficiaryRequest {
  beneficiaryName: string;
  relationship: string;
  percentage: number;
}

export interface PolicyCreateRequest {
  customerId: string;
  quoteId: string;
  planId: string;
  effectiveDate: string;
  expiryDate: string;
  members: PolicyMemberRequest[];
  coverages?: PolicyCoverageRequest[];
  beneficiaries?: PolicyBeneficiaryRequest[];
}

export interface PolicyEndorsementRequest {
  endorsementType: EndorsementType;
  description: string;
  changeData?: Record<string, any>;
  requestedBy?: string;
}

export interface PolicyEndorsementResponse {
  endorsementId: string;
  policyId: string;
  policyNumber: string;
  endorsementType: EndorsementType;
  status: EndorsementStatus;
  description: string;
  changeData?: Record<string, any>;
  revisedPremium?: number;
  requestedBy?: string;
  approvedBy?: string;
  rejectionReason?: string;
  createdAt: string;
  updatedAt?: string;
  appliedAt?: string;
}

export interface EndorsementApprovalRequest {
  approvedBy: string;
  notes?: string;
}

export interface EndorsementRejectRequest {
  rejectionReason: string;
  notes?: string;
}

export interface PolicyCancellationRequest {
  reason: string;
  requestedBy?: string;
}

export interface PolicyCancellationResponse {
  cancellationId: string;
  policyId: string;
  policyNumber: string;
  status: CancellationStatus;
  reason: string;
  refundAmount?: number;
  refundTransactionId?: string;
  requestedBy?: string;
  approvedBy?: string;
  rejectionReason?: string;
  createdAt: string;
  updatedAt?: string;
  cancelledAt?: string;
}

export interface CancellationApprovalRequest {
  approvedBy: string;
  refundAmount?: number;
  notes?: string;
}

export interface CancellationRejectRequest {
  rejectionReason: string;
  notes?: string;
}

export interface RenewalEligibilityResponse {
  policyId: string;
  policyNumber: string;
  eligible: boolean;
  reason?: string;
  currentExpiryDate: string;
  daysUntilExpiry: number;
}

export interface PolicyRenewalQuoteRequest {
  requestedBy?: string;
  coverageAdjustment?: number;
}

export interface PolicyRenewalResponse {
  renewalId: string;
  policyId: string;
  policyNumber: string;
  status: RenewalStatus;
  renewalQuoteId?: string;
  renewalPremium?: number;
  paymentId?: string;
  newEffectiveDate: string;
  newExpiryDate: string;
  rejectionReason?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface RenewalPaymentRequest {
  paymentId: string;
  amountPaid: number;
}

export interface RenewalRejectRequest {
  rejectionReason: string;
}
