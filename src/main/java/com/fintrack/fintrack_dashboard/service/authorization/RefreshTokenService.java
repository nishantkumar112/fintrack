package com.fintrack.fintrack_dashboard.service.authorization;

import com.fintrack.fintrack_dashboard.entity.RefreshToken;
import com.fintrack.fintrack_dashboard.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyRefreshToken(String token);

    void revokeUserTokens(User user);
}