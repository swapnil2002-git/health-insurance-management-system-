export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  role: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  role: string;
}

export interface UserSummaryResponse {
  userId: string;
  username: string;
  email: string;
  role: string;
  status: string;
  createdAt: string;
}

export const SYSTEM_ROLES = [
  { value: 'CUSTOMER', label: 'Customer (Self Service)' },
  { value: 'AGENT', label: 'Insurance Agent' },
  { value: 'UNDERWRITER', label: 'Risk Underwriter' },
  { value: 'CLAIMS_OFFICER', label: 'Claims Adjudication Officer' },
  { value: 'POLICY_ADMINISTRATOR', label: 'Policy Administrator' },
  { value: 'FINANCE_OFFICER', label: 'Finance & Billing Officer' },
  { value: 'HEALTHCARE_PROVIDER', label: 'Healthcare Provider / Hospital' },
  { value: 'SYSTEM_ADMINISTRATOR', label: 'System Administrator' }
];
