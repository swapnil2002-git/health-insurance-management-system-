import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateRiskAssessmentRequest, RiskAssessmentResponse } from '../models/risk.model';

@Injectable({
  providedIn: 'root'
})
export class RiskService {
  private readonly baseUrl = '/api/risk-assessments';

  constructor(private http: HttpClient) {}

  public createAssessment(request: CreateRiskAssessmentRequest): Observable<RiskAssessmentResponse> {
    return this.http.post<RiskAssessmentResponse>(this.baseUrl, request);
  }

  public getAssessment(id: string): Observable<RiskAssessmentResponse> {
    return this.http.get<RiskAssessmentResponse>(`${this.baseUrl}/${encodeURIComponent(id)}`);
  }

  public calculateRisk(id: string): Observable<RiskAssessmentResponse> {
    return this.http.post<RiskAssessmentResponse>(`${this.baseUrl}/${encodeURIComponent(id)}/calculate`, {});
  }
}
