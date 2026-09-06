package com.healthinsurance.quotation.exception;

public class InvalidQuoteStatusException extends RuntimeException {
    public InvalidQuoteStatusException(String message) {
        super(message);
    }
}