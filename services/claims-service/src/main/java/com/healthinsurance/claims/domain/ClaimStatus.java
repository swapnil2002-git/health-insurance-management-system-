package com.healthinsurance.claims.domain;

public enum ClaimStatus {
    SUBMITTED,
    VALIDATING,
    ELIGIBILITY_CHECK,
    PROVIDER_CHECK,
    ADJUDICATION,
    APPROVED,
    REJECTED,
    REFERRED,
    PAYMENT_PENDING,
    SETTLED
}
