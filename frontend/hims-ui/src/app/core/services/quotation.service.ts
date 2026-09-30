import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateQuoteRequest, QuoteResponse } from '../models/quotation.model';

@Injectable({
  providedIn: 'root'
})
export class QuotationService {
  private readonly baseUrl = '/api/quotes';

  constructor(private http: HttpClient) {}

  public createQuote(request: CreateQuoteRequest): Observable<QuoteResponse> {
    return this.http.post<QuoteResponse>(this.baseUrl, request);
  }

  public getQuote(quoteId: string): Observable<QuoteResponse> {
    return this.http.get<QuoteResponse>(`${this.baseUrl}/${encodeURIComponent(quoteId)}`);
  }

  public getAllQuotes(): Observable<QuoteResponse[]> {
    return this.http.get<QuoteResponse[]>(this.baseUrl);
  }

  public calculatePremium(quoteId: string): Observable<QuoteResponse> {
    return this.http.post<QuoteResponse>(`${this.baseUrl}/${encodeURIComponent(quoteId)}/calculate`, {});
  }

  public acceptQuote(quoteId: string): Observable<QuoteResponse> {
    return this.http.post<QuoteResponse>(`${this.baseUrl}/${encodeURIComponent(quoteId)}/accept`, {});
  }

  public rejectQuote(quoteId: string): Observable<QuoteResponse> {
    return this.http.post<QuoteResponse>(`${this.baseUrl}/${encodeURIComponent(quoteId)}/reject`, {});
  }
}
