package com.fintrack.fintrack_dashboard.respository;

import com.fintrack.fintrack_dashboard.entity.PasswordResetToken;
import com.fintrack.fintrack_dashboard.entity.User;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(
            String token
    );

    void deleteByUser(User user);

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM PasswordResetToken p
        WHERE p.expiryDate < :now
        OR p.used = true
    """)
    int cleanupExpiredTokens(
            @Param("now")
            LocalDateTime now
    );
}