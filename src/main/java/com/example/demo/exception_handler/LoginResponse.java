package com.example.demo.exception_handler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class LoginResponse {
    private LocalDateTime timestamp;

    private int status;

    private String message;

    private String username;

    private List<String> role;
}
