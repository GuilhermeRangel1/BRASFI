package com.brasfi.webapp.exception;

public class MissingRequiredFieldsException extends RuntimeException {
    public MissingRequiredFieldsException(String message) {
        super(message);
    }
}