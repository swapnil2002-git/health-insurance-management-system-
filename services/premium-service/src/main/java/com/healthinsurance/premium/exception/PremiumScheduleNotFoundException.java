package com.healthinsurance.premium.exception;

public class PremiumScheduleNotFoundException extends RuntimeException {
    public PremiumScheduleNotFoundException(String message) {
        super(message);
    }
}