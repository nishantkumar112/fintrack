package com.fintrack.fintrack_dashboard.security;

import com.fintrack.fintrack_dashboard.entity.User;
import com.fintrack.fintrack_dashboard.service.user.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler
        implements AuthenticationSuccessHandler {

    private final UserService userService;

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oauthUser =
                (OAuth2User) authentication.getPrincipal();

        String email =
                oauthUser.getAttribute("email");

        String name =
                oauthUser.getAttribute("name");

        User user =
                userService.createOAuthUserIfNotExists(
                        email,
                        name
                );

        String accessToken =
                jwtUtil.generateToken(user);

        String refreshToken =
                        jwtUtil.generateRefreshToken(user);

        response.sendRedirect(
                "http://localhost:5173/oauth-success"
                        + "?token=" + accessToken
                        + "&refreshToken=" + refreshToken
        );
    }
}