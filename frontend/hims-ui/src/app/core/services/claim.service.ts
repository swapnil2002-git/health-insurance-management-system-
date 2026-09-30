import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ClaimCreateRequest,
  ClaimResponse,
  ClaimStatus,
  ClaimServiceRequest,
  ClaimServiceResponse,
  ClaimDiagnosisRequest,
  ClaimDiagnosisResponse,
  ClaimDocumentRequest,
  ClaimDocumentResponse,
  ClaimValidationResponse,
  ClaimAdjudicationResponse,
  ClaimSettlementRequest,
  ClaimPaymentResponse,
  ExplanationOfBenefitsResponse
} from '../models/claim.model';

@Injectable({
  providedIn: 'root'
})
export class ClaimService {
  constructor(private http: HttpClient) {}

  // ================= Submission & Retrieval =================
  createClaim(request: ClaimCreateRequest, idempotencyKey?: string): Observable<ClaimResponse> {
    let headers = new HttpHeaders();
    if (idempotencyKey) {
      headers = headers.set('Idempotency-Key', idempotencyKey);
    }
    return this.http.post<ClaimResponse>('/api/claims', request, { headers });
  }

  getClaimById(claimId: string): Observable<ClaimResponse> {
    return this.http.get<ClaimResponse>(`/api/claims/${claimId}`);
  }

  getClaimByNumber(claimNumber: string): Observable<ClaimResponse> {
    return this.http.get<ClaimResponse>(`/api/claims/number/${claimNumber}`);
  }

  getAllClaims(status?: ClaimStatus): Observable<ClaimResponse[]> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<ClaimResponse[]>('/api/claims', { params });
  }

  getClaimsByPolicyId(policyId: string): Observable<ClaimResponse[]> {
    return this.http.get<ClaimResponse[]>(`/api/claims/policy/${policyId}`);
  }

  getClaimsByMemberId(memberId: string): Observable<ClaimResponse[]> {
    return this.http.get<ClaimResponse[]>(`/api/claims/member/${memberId}`);
  }

  // ================= Items, Diagnoses & Documents =================
  addServiceLine(claimId: string, request: ClaimServiceRequest): Observable<ClaimServiceResponse> {
    return this.http.post<ClaimServiceResponse>(`/api/claims/${claimId}/service-lines`, request);
  }

  addDiagnosis(claimId: string, request: ClaimDiagnosisRequest): Observable<ClaimDiagnosisResponse> {
    return this.http.post<ClaimDiagnosisResponse>(`/api/claims/${claimId}/diagnoses`, request);
  }

  addDocumentReference(claimId: string, request: ClaimDocumentRequest): Observable<ClaimDocumentResponse> {
    return this.http.post<ClaimDocumentResponse>(`/api/claims/${claimId}/documents`, request);
  }

  // ================= Validation & External Verification =================
  validateClaim(claimId: string): Observable<ClaimValidationResponse[]> {
    return this.http.post<ClaimValidationResponse[]>(`/api/claims/${claimId}/validate`, {});
  }

  verifyEligibility(claimId: string): Observable<ClaimValidationResponse[]> {
    return this.http.post<ClaimValidationResponse[]>(`/api/claims/${claimId}/verify-eligibility`, {});
  }

  // ================= Adjudication =================
  adjudicateClaim(claimId: string): Observable<ClaimAdjudicationResponse> {
    return this.http.post<ClaimAdjudicationResponse>(`/api/claims/${claimId}/adjudicate`, {});
  }

  getAdjudication(claimId: string): Observable<ClaimAdjudicationResponse> {
    return this.http.get<ClaimAdjudicationResponse>(`/api/claims/${claimId}/adjudication`);
  }

  // ================= Settlement, EOB & Payments =================
  settleClaim(claimId: string, request: ClaimSettlementRequest): Observable<ClaimPaymentResponse> {
    return this.http.post<ClaimPaymentResponse>(`/api/claims/${claimId}/settle`, request);
  }

  getEobByClaimId(claimId: string): Observable<ExplanationOfBenefitsResponse> {
    return this.http.get<ExplanationOfBenefitsResponse>(`/api/claims/${claimId}/eob`);
  }

  getEobByNumber(eobNumber: string): Observable<ExplanationOfBenefitsResponse> {
    return this.http.get<ExplanationOfBenefitsResponse>(`/api/claims/eob/${eobNumber}`);
  }

  getPaymentsByClaimId(claimId: string): Observable<ClaimPaymentResponse[]> {
    return this.http.get<ClaimPaymentResponse[]>(`/api/claims/${claimId}/payments`);
  }
}
