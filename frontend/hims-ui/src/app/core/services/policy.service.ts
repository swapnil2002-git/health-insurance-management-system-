import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, map } from 'rxjs';
import {
  PolicyResponse,
  PolicyCreateRequest,
  PolicyEndorsementRequest,
  PolicyEndorsementResponse,
  EndorsementApprovalRequest,
  EndorsementRejectRequest,
  PolicyCancellationRequest,
  PolicyCancellationResponse,
  CancellationApprovalRequest,
  CancellationRejectRequest,
  RenewalEligibilityResponse,
  PolicyRenewalQuoteRequest,
  PolicyRenewalResponse,
  RenewalPaymentRequest,
  RenewalRejectRequest
} from '../models/policy.model';

@Injectable({
  providedIn: 'root'
})
export class PolicyService {
  private readonly baseUrl = '/api/policies';
  private readonly RECENT_POLICIES_KEY = 'hims_recent_policies';

  constructor(private http: HttpClient) {}

  // 1. Core Policy CRUD & Lifecycle
  createPolicy(request: PolicyCreateRequest): Observable<PolicyResponse> {
    return this.http.post<PolicyResponse>(this.baseUrl, request).pipe(
      tap((policy) => this.cacheRecentPolicy(policy))
    );
  }

  getPolicy(id: string): Observable<PolicyResponse> {
    return this.http.get<PolicyResponse>(`${this.baseUrl}/${id}`).pipe(
      tap((policy) => this.cacheRecentPolicy(policy))
    );
  }

  getAllPolicies(): Observable<PolicyResponse[]> {
    return this.http.get<PolicyResponse[]>(this.baseUrl);
  }

  getPoliciesByCustomerId(customerId: string): Observable<PolicyResponse[]> {
    const trimmed = customerId?.trim() || '';
    return this.http.get<PolicyResponse[]>(`${this.baseUrl}?customerId=${encodeURIComponent(trimmed)}`).pipe(
      map((policies) => {
        if (!trimmed) return policies;
        return policies.filter(
          (p) => p.customerId && p.customerId.toLowerCase() === trimmed.toLowerCase()
        );
      }),
      tap((policies) => policies.forEach((p) => this.cacheRecentPolicy(p)))
    );
  }

  issuePolicy(id: string): Observable<PolicyResponse> {
    return this.http.post<PolicyResponse>(`${this.baseUrl}/${id}/issue`, {}).pipe(
      tap((policy) => this.cacheRecentPolicy(policy))
    );
  }

  activatePolicy(id: string): Observable<PolicyResponse> {
    return this.http.post<PolicyResponse>(`${this.baseUrl}/${id}/activate`, {}).pipe(
      tap((policy) => this.cacheRecentPolicy(policy))
    );
  }

  renewPolicyDirect(id: string): Observable<PolicyResponse> {
    return this.http.post<PolicyResponse>(`${this.baseUrl}/${id}/renew`, {}).pipe(
      tap((policy) => this.cacheRecentPolicy(policy))
    );
  }

  cancelPolicyDirect(id: string, request: PolicyCancellationRequest): Observable<PolicyResponse> {
    return this.http.post<PolicyResponse>(`${this.baseUrl}/${id}/cancel`, request).pipe(
      tap((policy) => this.cacheRecentPolicy(policy))
    );
  }

  // 2. Endorsement Management
  requestEndorsement(policyId: string, request: PolicyEndorsementRequest): Observable<PolicyEndorsementResponse> {
    return this.http.post<PolicyEndorsementResponse>(`${this.baseUrl}/${policyId}/endorsements`, request);
  }

  getEndorsements(policyId: string): Observable<PolicyEndorsementResponse[]> {
    return this.http.get<PolicyEndorsementResponse[]>(`${this.baseUrl}/${policyId}/endorsements`);
  }

  getEndorsement(policyId: string, endorsementId: string): Observable<PolicyEndorsementResponse> {
    return this.http.get<PolicyEndorsementResponse>(`${this.baseUrl}/${policyId}/endorsements/${endorsementId}`);
  }

  approveEndorsement(
    policyId: string,
    endorsementId: string,
    request: EndorsementApprovalRequest
  ): Observable<PolicyEndorsementResponse> {
    return this.http.post<PolicyEndorsementResponse>(
      `${this.baseUrl}/${policyId}/endorsements/${endorsementId}/approve`,
      request
    );
  }

  rejectEndorsement(
    policyId: string,
    endorsementId: string,
    request: EndorsementRejectRequest
  ): Observable<PolicyEndorsementResponse> {
    return this.http.post<PolicyEndorsementResponse>(
      `${this.baseUrl}/${policyId}/endorsements/${endorsementId}/reject`,
      request
    );
  }

  // 3. Cancellation Management
  requestCancellation(policyId: string, request: PolicyCancellationRequest): Observable<PolicyCancellationResponse> {
    return this.http.post<PolicyCancellationResponse>(`${this.baseUrl}/${policyId}/cancellation`, request);
  }

  getCancellation(policyId: string): Observable<PolicyCancellationResponse> {
    return this.http.get<PolicyCancellationResponse>(`${this.baseUrl}/${policyId}/cancellation`);
  }

  approveCancellation(
    policyId: string,
    request: CancellationApprovalRequest
  ): Observable<PolicyCancellationResponse> {
    return this.http.post<PolicyCancellationResponse>(`${this.baseUrl}/${policyId}/cancellation/approve`, request);
  }

  rejectCancellation(
    policyId: string,
    request: CancellationRejectRequest
  ): Observable<PolicyCancellationResponse> {
    return this.http.post<PolicyCancellationResponse>(`${this.baseUrl}/${policyId}/cancellation/reject`, request);
  }

  // 4. Renewal Lifecycle Management
  checkRenewalEligibility(policyId: string): Observable<RenewalEligibilityResponse> {
    return this.http.get<RenewalEligibilityResponse>(`${this.baseUrl}/${policyId}/renewals/eligibility`);
  }

  generateRenewalQuote(
    policyId: string,
    request?: PolicyRenewalQuoteRequest
  ): Observable<PolicyRenewalResponse> {
    return this.http.post<PolicyRenewalResponse>(`${this.baseUrl}/${policyId}/renewals/quote`, request || {});
  }

  getRenewals(policyId: string): Observable<PolicyRenewalResponse[]> {
    return this.http.get<PolicyRenewalResponse[]>(`${this.baseUrl}/${policyId}/renewals`);
  }

  getRenewal(policyId: string, renewalId: string): Observable<PolicyRenewalResponse> {
    return this.http.get<PolicyRenewalResponse>(`${this.baseUrl}/${policyId}/renewals/${renewalId}`);
  }

  acceptRenewal(policyId: string, renewalId: string): Observable<PolicyRenewalResponse> {
    return this.http.post<PolicyRenewalResponse>(`${this.baseUrl}/${policyId}/renewals/${renewalId}/accept`, {});
  }

  completeRenewal(
    policyId: string,
    renewalId: string,
    request: RenewalPaymentRequest
  ): Observable<PolicyRenewalResponse> {
    return this.http.post<PolicyRenewalResponse>(
      `${this.baseUrl}/${policyId}/renewals/${renewalId}/complete`,
      request
    );
  }

  rejectRenewal(
    policyId: string,
    renewalId: string,
    request: RenewalRejectRequest
  ): Observable<PolicyRenewalResponse> {
    return this.http.post<PolicyRenewalResponse>(
      `${this.baseUrl}/${policyId}/renewals/${renewalId}/reject`,
      request
    );
  }

  // Cache helper
  private cacheRecentPolicy(policy: PolicyResponse): void {
    try {
      const stored = sessionStorage.getItem(this.RECENT_POLICIES_KEY);
      let list: PolicyResponse[] = stored ? JSON.parse(stored) : [];
      list = list.filter((p) => p.policyId !== policy.policyId);
      list.unshift(policy);
      if (list.length > 20) list = list.slice(0, 20);
      sessionStorage.setItem(this.RECENT_POLICIES_KEY, JSON.stringify(list));
    } catch {
      // Ignore sessionStorage errors
    }
  }

  getRecentPolicies(): PolicyResponse[] {
    try {
      const stored = sessionStorage.getItem(this.RECENT_POLICIES_KEY);
      return stored ? JSON.parse(stored) : [];
    } catch {
      return [];
    }
  }
}
