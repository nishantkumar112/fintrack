package com.fintrack.fintrack_dashboard.dto.auth;

import lombok.Data;

import java.time.LocalDateTime;

public class AuthResponse {
    private String token;
    private LocalDateTime createdAt;
    public AuthResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}