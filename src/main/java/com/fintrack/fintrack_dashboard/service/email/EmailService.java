package com.fintrack.fintrack_dashboard.service.email;

public interface EmailService {

    void sendPasswordResetEmail(
            String to,
            String resetLink
    );
}