package com.fintrack.fintrack_dashboard.service.authorization;

import com.fintrack.fintrack_dashboard.entity.RefreshToken;
import com.fintrack.fintrack_dashboard.entity.User;
import com.fintrack.fintrack_dashboard.exception.BadRequestException;
import com.fintrack.fintrack_dashboard.respository.RefreshTokenRepository;

import com.fintrack.fintrack_dashboard.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl
        implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtUtil jwtUtil;

    @Override
    public RefreshToken createRefreshToken(User user) {

        String token =
                jwtUtil.generateRefreshToken(user);
        RefreshToken refreshToken =
                RefreshToken.builder()
                        .token(token)
                        .user(user)
                        .revoked(false)
                        .createdAt(LocalDateTime.now())
                        .expiryDate(
                                LocalDateTime.now().plusDays(7)
                        )
                        .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyRefreshToken(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByToken(token)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Invalid refresh token"
                                )
                        );

        if (refreshToken.isRevoked()) {
            throw new BadRequestException(
                    "Refresh token revoked"
            );
        }

        if (refreshToken.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            throw new BadRequestException(
                    "Refresh token expired"
            );
        }

        return refreshToken;
    }

    @Override
    public void revokeUserTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}