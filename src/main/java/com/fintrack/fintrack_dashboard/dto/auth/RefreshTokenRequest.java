package com.fintrack.fintrack_dashboard.dto.auth;

import lombok.Data;

@Data
public class RefreshTokenRequest {

    private String refreshToken;
}