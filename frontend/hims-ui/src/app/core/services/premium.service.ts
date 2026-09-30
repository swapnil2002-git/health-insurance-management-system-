import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import {
  PremiumScheduleResponse,
  PremiumOutstandingResponse,
  PremiumInstallmentResponse,
  PremiumScheduleCreateRequest,
  PremiumRecalculateRequest,
  InstallmentPaymentRequest
} from '../models/premium.model';

@Injectable({
  providedIn: 'root'
})
export class PremiumService {
  private readonly RECENT_SCHEDULES_KEY = 'hims_recent_premium_schedules';

  constructor(private http: HttpClient) {}

  // 1. Create Premium Schedule manually
  createPremiumSchedule(request: PremiumScheduleCreateRequest): Observable<PremiumScheduleResponse> {
    return this.http.post<PremiumScheduleResponse>('/api/premium-schedules', request).pipe(
      tap((res) => this.cacheRecentSchedule(res))
    );
  }

  // 2. Get Premium Schedule for a Policy
  getPremiumByPolicy(policyId: string): Observable<PremiumScheduleResponse> {
    return this.http.get<PremiumScheduleResponse>(`/api/policies/${policyId}/premium`).pipe(
      tap((res) => this.cacheRecentSchedule(res))
    );
  }

  // 2b. Get Premium Schedule by Schedule ID
  getPremiumScheduleById(scheduleId: string): Observable<PremiumScheduleResponse> {
    return this.http.get<PremiumScheduleResponse>(`/api/premium-schedules/${scheduleId}`).pipe(
      tap((res) => this.cacheRecentSchedule(res))
    );
  }

  // 2c. Get All Premium Schedules from DB
  getAllSchedules(): Observable<PremiumScheduleResponse[]> {
    return this.http.get<PremiumScheduleResponse[]>('/api/premium-schedules').pipe(
      tap((list) => {
        if (Array.isArray(list)) {
          list.forEach((s) => this.cacheRecentSchedule(s));
        }
      })
    );
  }

  // 3. Get Outstanding Balance for a Policy
  getOutstanding(policyId: string): Observable<PremiumOutstandingResponse> {
    return this.http.get<PremiumOutstandingResponse>(`/api/policies/${policyId}/premium/outstanding`);
  }

  // 4. Recalculate Premium
  recalculatePremium(policyId: string, request: PremiumRecalculateRequest): Observable<PremiumScheduleResponse> {
    return this.http.post<PremiumScheduleResponse>(`/api/policies/${policyId}/premium/recalculate`, request).pipe(
      tap((res) => this.cacheRecentSchedule(res))
    );
  }

  // 5. Record Payment for an Installment
  recordInstallmentPayment(
    installmentId: string,
    request: InstallmentPaymentRequest
  ): Observable<PremiumInstallmentResponse> {
    return this.http.post<PremiumInstallmentResponse>(
      `/api/premium-schedules/installments/${installmentId}/pay`,
      request
    );
  }

  // Session storage caching for convenience in UI
  private cacheRecentSchedule(sched: PremiumScheduleResponse): void {
    try {
      const stored = sessionStorage.getItem(this.RECENT_SCHEDULES_KEY);
      let list: PremiumScheduleResponse[] = stored ? JSON.parse(stored) : [];
      list = list.filter((s) => s.scheduleId !== sched.scheduleId && s.policyId !== sched.policyId);
      list.unshift(sched);
      if (list.length > 20) list = list.slice(0, 20);
      sessionStorage.setItem(this.RECENT_SCHEDULES_KEY, JSON.stringify(list));
    } catch {
      // Ignore sessionStorage issues
    }
  }

  getRecentSchedules(): PremiumScheduleResponse[] {
    try {
      const stored = sessionStorage.getItem(this.RECENT_SCHEDULES_KEY);
      return stored ? JSON.parse(stored) : [];
    } catch {
      return [];
    }
  }
}
