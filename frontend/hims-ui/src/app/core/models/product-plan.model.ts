export interface ProductRequest {
  name: string;
  description?: string;
}

export interface ProductResponse {
  productId: string;
  name: string;
  description: string;
  status: string;
}

export interface PlanRequest {
  productId: string;
  name: string;
  description?: string;
}

export interface PlanResponse {
  planId: string;
  productId: string;
  name: string;
  description: string;
}

export interface RuleResponse {
  id: string;
  name: string;
  description: string;
}

export interface PlanDetailResponse {
  planId: string;
  productId: string;
  productName: string;
  name: string;
  description: string;
  coverages: RuleResponse[];
  deductibles: RuleResponse[];
  copayments: RuleResponse[];
  exclusions: RuleResponse[];
  riders: RuleResponse[];
}

export interface PlanRuleRequest {
  ruleId: string;
}

export interface MasterRuleItem {
  id: string;
  name: string;
  description: string;
  coverageId?: string;
  deductibleId?: string;
  copaymentId?: string;
  exclusionId?: string;
  riderId?: string;
}

export type RuleCategory = 'coverages' | 'exclusions' | 'deductibles' | 'copayments' | 'riders';

export const RULE_CATEGORIES: { key: RuleCategory; label: string; icon: string }[] = [
  { key: 'coverages', label: 'Coverages', icon: 'health_and_safety' },
  { key: 'deductibles', label: 'Deductibles', icon: 'payments' },
  { key: 'copayments', label: 'Copayments', icon: 'price_check' },
  { key: 'exclusions', label: 'Exclusions', icon: 'block' },
  { key: 'riders', label: 'Riders & Add-ons', icon: 'add_moderator' }
];
