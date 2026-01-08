package com.insurance_policy.exception;

public class InvalidPolicyStateException extends RuntimeException {
    public InvalidPolicyStateException(String message) {
        super(message);
    }
}
