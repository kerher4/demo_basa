package com.example.demo.exception;

import lombok.Getter;

@Getter
public abstract class CustomException extends RuntimeException {
    private final String errorCode;

    protected CustomException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
