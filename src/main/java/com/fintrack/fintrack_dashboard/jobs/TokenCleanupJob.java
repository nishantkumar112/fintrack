package com.fintrack.fintrack_dashboard.jobs;

import com.fintrack.fintrack_dashboard.respository.PasswordResetTokenRepository;
import com.fintrack.fintrack_dashboard.respository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupJob implements Job {

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    @Override
    public void execute(JobExecutionContext context) {
        int deletedRefreshTokens =
                refreshTokenRepository.deleteExpiredOrRevokedTokens(
                     LocalDateTime.now(
                     )
                );
        int deletedPasswordRestTokens =
                passwordResetTokenRepository.cleanupExpiredTokens(
                        LocalDateTime.now()
                );

        log.info(
                "Password reset token cleanup completed | deleted={}",
                deletedRefreshTokens
        );
        log.info(
                "Refresh token cleanup completed | deleted={}",
                deletedPasswordRestTokens
        );
    }
}