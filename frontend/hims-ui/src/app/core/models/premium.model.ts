export type PaymentFrequency = 'ANNUAL' | 'MONTHLY' | 'QUARTERLY';

export type PremiumStatus = 'PENDING' | 'PARTIALLY_PAID' | 'PAID' | 'OVERDUE';

export type InstallmentStatus = 'PENDING' | 'PAID' | 'PARTIALLY_PAID' | 'OVERDUE';

export interface PremiumInstallmentResponse {
  installmentId: string;
  installmentNumber: number;
  amount: number;
  dueDate: string;
  paidAmount: number;
  outstandingAmount: number;
  status: InstallmentStatus;
}

export interface PremiumScheduleResponse {
  scheduleId: string;
  policyId: string;
  totalPremium: number;
  paymentFrequency: PaymentFrequency;
  numberOfInstallments: number;
  paidAmount: number;
  outstandingAmount: number;
  premiumStatus: PremiumStatus;
  startDate: string;
  endDate: string;
  createdAt: string;
  updatedAt?: string;
  installments: PremiumInstallmentResponse[];
}

export interface PremiumOutstandingResponse {
  policyId: string;
  scheduleId: string;
  totalPremium: number;
  paidAmount: number;
  outstandingAmount: number;
  status: PremiumStatus;
}

export interface PremiumScheduleCreateRequest {
  policyId: string;
  paymentFrequency: PaymentFrequency;
  startDate: string;
  endDate: string;
  basePremium: number;
  riderPremium?: number;
  riskLoading?: number;
  discount?: number;
  tax?: number;
}

export interface PremiumRecalculateRequest {
  basePremium?: number;
  riderPremium?: number;
  riskLoading?: number;
  discount?: number;
  tax?: number;
  paymentFrequency?: PaymentFrequency;
}

export interface InstallmentPaymentRequest {
  amount: number;
  paymentReference?: string;
}
