package com.example.demo.exception;

public class UsernameAlreadyExistsException extends CustomException {
    public UsernameAlreadyExistsException(String message) {
        super(message, "USERNAME_ALREADY_EXISTS");
    }
}
