import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  ProductRequest,
  ProductResponse,
  PlanRequest,
  PlanResponse,
  PlanDetailResponse,
  PlanRuleRequest,
  RuleResponse,
  MasterRuleItem,
  RuleCategory
} from '../models/product-plan.model';

@Injectable({
  providedIn: 'root'
})
export class ProductPlanService {
  private readonly productsUrl = '/api/products';
  private readonly plansUrl = '/api/plans';
  private readonly rulesUrl = '/api/rules';

  constructor(private http: HttpClient) {}

  // 1. Products
  public getProducts(): Observable<ProductResponse[]> {
    return this.http.get<ProductResponse[]>(this.productsUrl);
  }

  public getProduct(productId: string): Observable<ProductResponse> {
    return this.http.get<ProductResponse>(`${this.productsUrl}/${encodeURIComponent(productId)}`);
  }

  public createProduct(request: ProductRequest): Observable<ProductResponse> {
    return this.http.post<ProductResponse>(this.productsUrl, request);
  }

  public updateProduct(productId: string, request: ProductRequest): Observable<ProductResponse> {
    return this.http.put<ProductResponse>(`${this.productsUrl}/${encodeURIComponent(productId)}`, request);
  }

  // 2. Plans
  public getPlans(): Observable<PlanResponse[]> {
    return this.http.get<PlanResponse[]>(this.plansUrl);
  }

  public getPlan(planId: string): Observable<PlanDetailResponse> {
    return this.http.get<PlanDetailResponse>(`${this.plansUrl}/${encodeURIComponent(planId)}`);
  }

  public getPlansByProduct(productId: string): Observable<PlanResponse[]> {
    return this.http.get<PlanResponse[]>(`${this.plansUrl}/product/${encodeURIComponent(productId)}`);
  }

  public createPlan(request: PlanRequest): Observable<PlanResponse> {
    return this.http.post<PlanResponse>(this.plansUrl, request);
  }

  public updatePlan(planId: string, request: PlanRequest): Observable<PlanResponse> {
    return this.http.put<PlanResponse>(`${this.plansUrl}/${encodeURIComponent(planId)}`, request);
  }

  // 3. Plan-Rule Mapping
  public mapRuleToPlan(planId: string, category: RuleCategory, ruleId: string): Observable<RuleResponse> {
    const body: PlanRuleRequest = { ruleId };
    return this.http.post<RuleResponse>(`${this.plansUrl}/${encodeURIComponent(planId)}/${category}`, body);
  }

  // 4. Master Rules
  public getMasterRules(category: RuleCategory): Observable<MasterRuleItem[]> {
    return this.http.get<any[]>(`${this.rulesUrl}/${category}`).pipe(
      map((items) =>
        (items || []).map((item) => ({
          id: item.id || item.coverageId || item.deductibleId || item.copaymentId || item.exclusionId || item.riderId,
          name: item.name,
          description: item.description || ''
        }))
      )
    );
  }
}
