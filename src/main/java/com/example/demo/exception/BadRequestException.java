package com.example.demo.exception;

public class BadRequestException extends CustomException{
    public BadRequestException(String message) {
        super(message, "BAD_REQUEST");
    }
}
