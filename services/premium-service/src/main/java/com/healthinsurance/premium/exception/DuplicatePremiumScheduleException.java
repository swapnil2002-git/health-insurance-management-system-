package com.healthinsurance.premium.exception;

public class DuplicatePremiumScheduleException extends RuntimeException {
    public DuplicatePremiumScheduleException(String message) {
        super(message);
    }
}