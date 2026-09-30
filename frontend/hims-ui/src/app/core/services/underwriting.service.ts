import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import {
  CreateUnderwritingCaseRequest,
  ApproveUnderwritingRequest,
  RejectUnderwritingRequest,
  ReferUnderwritingRequest,
  UnderwritingCaseResponse
} from '../models/underwriting.model';

const RECENT_CASES_KEY = 'hims_recent_underwriting_cases';

@Injectable({
  providedIn: 'root'
})
export class UnderwritingService {
  private readonly baseUrl = '/api/underwriting/cases';

  constructor(private http: HttpClient) {}

  public getAllCases(): Observable<UnderwritingCaseResponse[]> {
    return this.http.get<UnderwritingCaseResponse[]>(this.baseUrl);
  }

  public getCasesByCustomer(customerId: string): Observable<UnderwritingCaseResponse[]> {
    return this.http.get<UnderwritingCaseResponse[]>(`${this.baseUrl}/customer/${encodeURIComponent(customerId)}`);
  }

  public getCasesByQuote(quoteId: string): Observable<UnderwritingCaseResponse[]> {
    return this.http.get<UnderwritingCaseResponse[]>(`${this.baseUrl}/quote/${encodeURIComponent(quoteId)}`);
  }

  public createCase(request: CreateUnderwritingCaseRequest): Observable<UnderwritingCaseResponse> {
    return this.http.post<UnderwritingCaseResponse>(this.baseUrl, request).pipe(
      tap((res) => this.recordRecentCase(res))
    );
  }

  public getCase(caseId: string): Observable<UnderwritingCaseResponse> {
    return this.http.get<UnderwritingCaseResponse>(`${this.baseUrl}/${encodeURIComponent(caseId)}`).pipe(
      tap((res) => this.recordRecentCase(res))
    );
  }

  public approveCase(caseId: string, request: ApproveUnderwritingRequest): Observable<UnderwritingCaseResponse> {
    return this.http.post<UnderwritingCaseResponse>(`${this.baseUrl}/${encodeURIComponent(caseId)}/approve`, request).pipe(
      tap((res) => this.recordRecentCase(res))
    );
  }

  public rejectCase(caseId: string, request: RejectUnderwritingRequest): Observable<UnderwritingCaseResponse> {
    return this.http.post<UnderwritingCaseResponse>(`${this.baseUrl}/${encodeURIComponent(caseId)}/reject`, request).pipe(
      tap((res) => this.recordRecentCase(res))
    );
  }

  public referCase(caseId: string, request: ReferUnderwritingRequest): Observable<UnderwritingCaseResponse> {
    return this.http.post<UnderwritingCaseResponse>(`${this.baseUrl}/${encodeURIComponent(caseId)}/refer`, request).pipe(
      tap((res) => this.recordRecentCase(res))
    );
  }

  public getRecentCases(): UnderwritingCaseResponse[] {
    try {
      const data = sessionStorage.getItem(RECENT_CASES_KEY);
      return data ? JSON.parse(data) : [];
    } catch {
      return [];
    }
  }

  public recordRecentCase(item: UnderwritingCaseResponse): void {
    try {
      let cases = this.getRecentCases();
      cases = cases.filter((c) => c.caseId !== item.caseId);
      cases.unshift(item);
      if (cases.length > 20) cases = cases.slice(0, 20);
      sessionStorage.setItem(RECENT_CASES_KEY, JSON.stringify(cases));
    } catch {}
  }
}
