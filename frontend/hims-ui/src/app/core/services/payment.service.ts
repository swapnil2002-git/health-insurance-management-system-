import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import {
  PaymentInitiateRequest,
  PaymentConfirmRequest,
  RefundRequest,
  PaymentResponse,
  RefundResponse
} from '../models/payment.model';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private readonly RECENT_PAYMENTS_KEY = 'hims_recent_payments';

  constructor(private http: HttpClient) {}

  // 1. Initiate Payment
  initiatePayment(request: PaymentInitiateRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>('/api/payments', request).pipe(
      tap((res) => this.cacheRecentPayment(res))
    );
  }

  // 2. Confirm Payment
  confirmPayment(id: string, request: PaymentConfirmRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`/api/payments/${id}/confirm`, request).pipe(
      tap((res) => this.cacheRecentPayment(res))
    );
  }

  // 3. Process Refund
  processRefund(id: string, request: RefundRequest): Observable<RefundResponse> {
    return this.http.post<RefundResponse>(`/api/payments/${id}/refund`, request);
  }

  // 4. Get Payment by ID
  getPayment(id: string): Observable<PaymentResponse> {
    return this.http.get<PaymentResponse>(`/api/payments/${id}`).pipe(
      tap((res) => this.cacheRecentPayment(res))
    );
  }

  // 5. Get Payments by Policy ID
  getPaymentsByPolicy(policyId: string): Observable<PaymentResponse[]> {
    return this.http.get<PaymentResponse[]>(`/api/payments/policy/${policyId}`).pipe(
      tap((list) => {
        if (Array.isArray(list)) {
          list.forEach((p) => this.cacheRecentPayment(p));
        }
      })
    );
  }

  // 6. Get All Payments from DB
  getAllPayments(): Observable<PaymentResponse[]> {
    return this.http.get<PaymentResponse[]>('/api/payments').pipe(
      tap((list) => {
        if (Array.isArray(list)) {
          list.forEach((p) => this.cacheRecentPayment(p));
        }
      })
    );
  }

  // Session storage caching for UI lookup convenience
  private cacheRecentPayment(pmt: PaymentResponse): void {
    try {
      const stored = sessionStorage.getItem(this.RECENT_PAYMENTS_KEY);
      let list: PaymentResponse[] = stored ? JSON.parse(stored) : [];
      list = list.filter((p) => p.paymentId !== pmt.paymentId);
      list.unshift(pmt);
      if (list.length > 25) list = list.slice(0, 25);
      sessionStorage.setItem(this.RECENT_PAYMENTS_KEY, JSON.stringify(list));
    } catch {
      // Ignore sessionStorage issues
    }
  }

  getRecentPayments(): PaymentResponse[] {
    try {
      const stored = sessionStorage.getItem(this.RECENT_PAYMENTS_KEY);
      return stored ? JSON.parse(stored) : [];
    } catch {
      return [];
    }
  }
}
