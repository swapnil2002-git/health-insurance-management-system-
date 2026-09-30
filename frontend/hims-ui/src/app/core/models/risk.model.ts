export type AssessmentStatus = 'CREATED' | 'IN_PROGRESS' | 'CALCULATED' | 'COMPLETED';
export type RiskClassification = 'LOW' | 'MEDIUM' | 'HIGH' | 'VERY_HIGH';

export interface RiskFactorRequest {
  factorName: string;
  factorValue: string;
  description?: string;
}

export interface CreateRiskAssessmentRequest {
  customerId: string;
  quoteId: string;
  factors: RiskFactorRequest[];
}

export interface RiskFactorResponse {
  riskFactorId: string;
  factorName: string;
  factorValue: string;
  description?: string;
}

export interface RiskScoreResponse {
  riskScoreId: string;
  score: number;
  classification: RiskClassification;
  calculatedAt: string;
}

export interface RiskAssessmentResponse {
  assessmentId: string;
  customerId: string;
  quoteId: string;
  status: AssessmentStatus;
  classification?: RiskClassification;
  createdAt: string;
  updatedAt?: string;
  completedAt?: string;
  factors: RiskFactorResponse[];
  scores: RiskScoreResponse[];
}
