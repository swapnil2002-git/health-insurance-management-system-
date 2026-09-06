package com.healthinsurance.premium.exception;

public class InvalidPremiumScheduleException extends RuntimeException {
    public InvalidPremiumScheduleException(String message) {
        super(message);
    }
}