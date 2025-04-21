package com.example.demo.exception;

public class InvalidPasswordException extends CustomException {
    public InvalidPasswordException(String message) {
        super(message, "INVALID_PASSWORD");
    }
}
