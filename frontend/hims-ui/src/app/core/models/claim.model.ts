export type ClaimType = 'CASHLESS' | 'REIMBURSEMENT';

export type ClaimStatus =
  | 'SUBMITTED'
  | 'VALIDATING'
  | 'ELIGIBILITY_CHECK'
  | 'PROVIDER_CHECK'
  | 'ADJUDICATION'
  | 'APPROVED'
  | 'REJECTED'
  | 'REFERRED'
  | 'PAYMENT_PENDING'
  | 'SETTLED';

export type AdjudicationDecision = 'APPROVED' | 'REJECTED' | 'REFERRED';

export type PayeeType = 'PROVIDER' | 'MEMBER';

export type PaymentStatus = 'PENDING' | 'PROCESSED' | 'FAILED';

export type ValidationStatus = 'PASSED' | 'FAILED' | 'WARNING';

export interface ClaimServiceRequest {
  serviceCode: string;
  serviceDescription: string;
  serviceDate: string; // YYYY-MM-DD
  unitPrice: number;
  quantity?: number;
}

export interface ClaimServiceResponse {
  claimServiceId: string;
  claimId: string;
  serviceCode: string;
  serviceDescription: string;
  serviceDate: string;
  unitPrice: number;
  quantity: number;
  totalAmount: number;
  createdAt: string;
}

export interface ClaimDiagnosisRequest {
  diagnosisCode: string;
  description: string;
  primary?: boolean;
}

export interface ClaimDiagnosisResponse {
  diagnosisId: string;
  claimId: string;
  diagnosisCode: string;
  description: string;
  primary: boolean;
  createdAt: string;
}

export interface ClaimDocumentRequest {
  documentId: string;
  documentType: string;
  documentName: string;
}

export interface ClaimDocumentResponse {
  claimDocumentId: string;
  claimId: string;
  documentId: string;
  documentType: string;
  documentName: string;
  uploadedAt: string;
}

export interface ClaimCreateRequest {
  policyId: string;
  memberId: string;
  providerId: string;
  claimType: ClaimType;
  serviceDate: string; // YYYY-MM-DD
  admissionDate?: string;
  dischargeDate?: string;
  totalClaimAmount: number;
  remarks?: string;
  serviceLines?: ClaimServiceRequest[];
  diagnoses?: ClaimDiagnosisRequest[];
}

export interface ClaimValidationResponse {
  validationId: string;
  claimId: string;
  ruleName: string;
  status: ValidationStatus;
  message: string;
  validatedAt: string;
}

export interface ClaimAdjudicationResponse {
  adjudicationId: string;
  claimId: string;
  submittedAmount: number;
  allowedAmount: number;
  deductibleAmount: number;
  copayAmount: number;
  copayPercentage: number;
  payableAmount: number;
  customerResponsibility: number;
  decision: AdjudicationDecision;
  reason?: string;
  adjudicatedAt: string;
}

export interface ClaimSettlementRequest {
  paidAmount: number;
  payeeType: PayeeType;
  paymentReferenceNumber?: string;
}

export interface ClaimPaymentResponse {
  claimPaymentId: string;
  claimId: string;
  paymentReferenceNumber?: string;
  paidAmount: number;
  payeeType: PayeeType;
  paymentStatus: PaymentStatus;
  settledAt?: string;
  createdAt: string;
}

export interface ExplanationOfBenefitsResponse {
  eobId: string;
  claimId: string;
  eobNumber: string;
  claimAmount: number;
  allowedAmount: number;
  deductible: number;
  copay: number;
  insurancePayment: number;
  customerResponsibility: number;
  remarks?: string;
  generatedAt: string;
}

export interface ClaimResponse {
  claimId: string;
  claimNumber: string;
  policyId: string;
  memberId: string;
  providerId: string;
  claimType: ClaimType;
  status: ClaimStatus;
  serviceDate: string;
  admissionDate?: string;
  dischargeDate?: string;
  totalClaimAmount: number;
  approvedAmount?: number;
  remarks?: string;
  createdAt: string;
  updatedAt?: string;

  serviceLines?: ClaimServiceResponse[];
  diagnoses?: ClaimDiagnosisResponse[];
  documents?: ClaimDocumentResponse[];
  validations?: ClaimValidationResponse[];
  adjudication?: ClaimAdjudicationResponse;
  payments?: ClaimPaymentResponse[];
  explanationOfBenefits?: ExplanationOfBenefitsResponse;
}
