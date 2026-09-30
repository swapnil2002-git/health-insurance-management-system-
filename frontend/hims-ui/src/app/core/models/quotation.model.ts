export type QuoteStatus = 'DRAFT' | 'CALCULATED' | 'ACCEPTED' | 'REJECTED' | 'EXPIRED';

export interface QuoteMemberRequest {
  memberName: string;
  dateOfBirth: string; // YYYY-MM-DD
  relationship: string;
  gender: string;
}

export interface CreateQuoteRequest {
  customerId: string;
  planId: string;
  members: QuoteMemberRequest[];
}

export interface QuoteMemberResponse {
  quoteMemberId: string;
  memberName: string;
  dateOfBirth: string;
  relationship: string;
  gender: string;
}

export interface QuotePremiumResponse {
  quotePremiumId: string;
  calculatedPremium: number;
  calculationDetails: string;
  calculatedAt: string;
}

export interface QuoteVersionResponse {
  versionId: string;
  versionNumber: number;
  premiumAtVersion: number;
  reason: string;
  createdAt: string;
}

export interface QuoteResponse {
  quoteId: string;
  customerId: string;
  planId: string;
  quoteNumber: string;
  status: QuoteStatus;
  totalPremium: number | null;
  createdAt: string;
  expiresAt: string;
  acceptedAt?: string | null;
  rejectedAt?: string | null;
  members: QuoteMemberResponse[];
  premiums: QuotePremiumResponse[];
  versions: QuoteVersionResponse[];
}
