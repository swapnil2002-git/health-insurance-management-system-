export type PaymentMethod = 'CREDIT_CARD' | 'DEBIT_CARD' | 'NET_BANKING' | 'UPI';

export type PaymentStatus = 'INITIATED' | 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';

export interface PaymentInitiateRequest {
  policyId: string;
  installmentId: string;
  amount: number;
  paymentMethod: PaymentMethod;
}

export interface PaymentConfirmRequest {
  gatewayReference?: string;
  isSuccess: boolean;
  success?: boolean;
  failureReason?: string;
}

export interface RefundRequest {
  amount: number;
  reason?: string;
}

export interface PaymentResponse {
  paymentId: string;
  policyId: string;
  amount: number;
  currency: string;
  paymentMethod: PaymentMethod;
  status: PaymentStatus;
  gatewayReference?: string;
  failureReason?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface RefundResponse {
  refundId: string;
  paymentId: string;
  amount: number;
  reason?: string;
  refundReference: string;
  createdAt: string;
}
