package com.fintrack.fintrack_dashboard.dto.auth;

import com.fintrack.fintrack_dashboard.dto.user.UserResponse;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;

    private String refreshToken;

    private String tokenType;

    private UserResponse user;
}