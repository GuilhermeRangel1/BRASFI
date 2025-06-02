package com.brasfi.webapp.exception;

public class InvalidCpfFormatException extends RuntimeException {
    public InvalidCpfFormatException(String message) {
        super(message);
    }
}