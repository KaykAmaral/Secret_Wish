package com.example.springApp.exception;

public class RateLimitException extends RuntimeException {

    public RateLimitException(String message) {
        super(message);
    }

}
