package com.example.demo.exception;

public class InternalServerErrorException extends CustomException{
    public InternalServerErrorException(String message) {
        super(message, "INTERNAL_SERVER_ERROR");
    }
}
