package com.fintrack.fintrack_dashboard.respository;

import com.fintrack.fintrack_dashboard.entity.RefreshToken;
import com.fintrack.fintrack_dashboard.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);


    @Modifying
    @Transactional
    @Query("""
        DELETE FROM RefreshToken r
        WHERE r.expiryDate < :now
        OR r.revoked = true
    """)
    int deleteExpiredOrRevokedTokens(
            @Param("now")
            LocalDateTime now
    );
}