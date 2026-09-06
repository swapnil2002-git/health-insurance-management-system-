package com.healthinsurance.policy.exception;

public class InvalidPolicyStateException extends RuntimeException {
    public InvalidPolicyStateException(String message) {
        super(message);
    }
}