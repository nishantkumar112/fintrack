package com.fintrack.fintrack_dashboard.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl
        implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendPasswordResetEmail(
            String to,
            String resetLink
    ) {
try {
    SimpleMailMessage message =
            new SimpleMailMessage();

    message.setTo(to);

    message.setSubject("Reset Your Password");

    message.setText(
            """
                    Click the link below to reset your password:
                    
                    %s
                    
                    This link expires in 15 minutes.
                    """.formatted(resetLink)
    );

    mailSender.send(message);
} catch (Exception e) {
    log.error(
            "Failed to send password reset email | to={}",
            to,
            e
    );
}
    }
}