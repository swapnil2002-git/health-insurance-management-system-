import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ClaimReportResponse,
  DashboardSummary,
  PolicyReportResponse,
  PremiumReportResponse
} from '../models/report.model';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private readonly baseUrl = '/api/reports';

  constructor(private http: HttpClient) {}

  getDashboardSummary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${this.baseUrl}/dashboard-summary`);
  }

  getPolicyReport(startDate?: string, endDate?: string): Observable<PolicyReportResponse> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    return this.http.get<PolicyReportResponse>(`${this.baseUrl}/policies`, { params });
  }

  getClaimReport(startDate?: string, endDate?: string): Observable<ClaimReportResponse> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    return this.http.get<ClaimReportResponse>(`${this.baseUrl}/claims`, { params });
  }

  getPremiumReport(startDate?: string, endDate?: string): Observable<PremiumReportResponse> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    return this.http.get<PremiumReportResponse>(`${this.baseUrl}/premiums`, { params });
  }
}
