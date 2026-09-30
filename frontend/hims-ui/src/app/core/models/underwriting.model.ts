export type CaseStatus = 'OPEN' | 'UNDER_REVIEW' | 'COMPLETED';

export type UnderwritingDecisionType =
  | 'APPROVED'
  | 'APPROVED_WITH_LOADING'
  | 'APPROVED_WITH_EXCLUSION'
  | 'REFERRED'
  | 'REJECTED';

export interface CreateUnderwritingCaseRequest {
  quoteId: string;
  customerId: string;
  assessmentId: string;
}

export interface ApproveUnderwritingRequest {
  decisionType: UnderwritingDecisionType;
  reason?: string;
  notes?: string;
}

export interface RejectUnderwritingRequest {
  reason: string;
  notes?: string;
}

export interface ReferUnderwritingRequest {
  reason: string;
  notes?: string;
}

export interface UnderwritingDecisionResponse {
  decisionId: string;
  decisionType: UnderwritingDecisionType;
  reason: string;
  notes: string;
  decidedAt: string;
}

export interface UnderwritingCaseResponse {
  caseId: string;
  quoteId: string;
  customerId: string;
  assessmentId: string;
  status: CaseStatus;
  createdAt: string;
  updatedAt?: string;
  completedAt?: string;
  decisions: UnderwritingDecisionResponse[];
}
