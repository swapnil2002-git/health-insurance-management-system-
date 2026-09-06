package com.healthinsurance.product.exception;
public class CopaymentNotFoundException extends RuntimeException {
    public CopaymentNotFoundException(String message) {
        super(message);
    }
}