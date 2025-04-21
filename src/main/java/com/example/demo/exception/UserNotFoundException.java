package com.example.demo.exception;

public class UserNotFoundException extends CustomException {
    public UserNotFoundException(String message) {
        super(message, "USER_NOT_FOUND");
    }
}
