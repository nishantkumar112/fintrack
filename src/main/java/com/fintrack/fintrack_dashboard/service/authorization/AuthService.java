package com.fintrack.fintrack_dashboard.service.authorization;

import com.fintrack.fintrack_dashboard.constant.NotificationType;
import com.fintrack.fintrack_dashboard.constant.Role;
import com.fintrack.fintrack_dashboard.constant.UserStatus;
import com.fintrack.fintrack_dashboard.dto.auth.*;
import com.fintrack.fintrack_dashboard.entity.PasswordResetToken;
import com.fintrack.fintrack_dashboard.entity.RefreshToken;
import com.fintrack.fintrack_dashboard.entity.User;
import com.fintrack.fintrack_dashboard.exception.BadRequestException;
import com.fintrack.fintrack_dashboard.mapper.UserMapper;
import com.fintrack.fintrack_dashboard.respository.PasswordResetTokenRepository;
import com.fintrack.fintrack_dashboard.respository.UserRepository;
import com.fintrack.fintrack_dashboard.security.JwtUtil;
import com.fintrack.fintrack_dashboard.service.email.EmailService;
import com.fintrack.fintrack_dashboard.service.notification.NotificationService;
import com.fintrack.fintrack_dashboard.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log =
            LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SecurityUtils securityUtils;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final EmailService emailService;
    private PasswordResetTokenRepository refreshTokenRepository;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil, SecurityUtils securityUtils,
            UserMapper userMapper,
            NotificationService notificationService, RefreshTokenService refreshTokenService, PasswordResetTokenRepository passwordResetTokenRepository, EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.securityUtils = securityUtils;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
        this.refreshTokenService = refreshTokenService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
    }

    public AuthResponse signup(SignupRequest request) {

        log.info(
                "Signup attempt | email={}",
                request.getEmail()
        );

        validateSignupRequest(request);

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {

            log.warn(
                    "Signup failed | email already exists={}",
                    request.getEmail()
            );

            throw new BadRequestException(
                    "Email already registered"
            );
        }

        User user =
                userMapper.toEntity(request);

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        User savedUser =
                userRepository.save(user);

        notificationService.notifyUser(
                savedUser,
                "Welcome to FinTrack",
                "Your account has been created successfully.",
                NotificationType.USER_CREATED,
                "/dashboard"
        );

        log.info(
                "User registered successfully | userId={}",
                savedUser.getId()
        );

        String accessToken =
                jwtUtil.generateToken(user);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        log.info(
                "Login attempt | email={}",
                request.getEmail()
        );

        validateLoginRequest(request);

        User user =
                userRepository.findByEmail(
                                request.getEmail()
                        )
                        .orElseThrow(() -> {

                            log.warn(
                                    "Login failed | email not found={}",
                                    request.getEmail()
                            );

                            return new BadRequestException(
                                    "Invalid email or password"
                            );
                        });

        if (
                !passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                )
        ) {

            log.warn(
                    "Login failed | invalid password for email={}",
                    request.getEmail()
            );

            throw new BadRequestException(
                    "Invalid email or password"
            );
        }

        if (
                user.getStatus() != UserStatus.ACTIVE
        ) {

            log.warn(
                    "Login blocked | inactive userId={}",
                    user.getId()
            );

            throw new BadRequestException(
                    "Account is inactive"
            );
        }

        log.info(
                "Login successful | userId={}",
                user.getId()
        );

        String accessToken =
                jwtUtil.generateToken(user);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .build();
    }


    @Transactional
    public void forgotPassword(
            ForgotPasswordRequest request
    ) {

        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {

                    passwordResetTokenRepository.deleteByUser(user);

                    String token =
                            UUID.randomUUID().toString();

                    PasswordResetToken resetToken =
                            PasswordResetToken.builder()
                                    .token(token)
                                    .user(user)
                                    .used(false)
                                    .createdAt(LocalDateTime.now())
                                    .expiryDate(
                                            LocalDateTime.now().plusMinutes(15)
                                    )
                                    .build();

                    passwordResetTokenRepository.save(resetToken);

                    String resetLink =
                            "http://localhost:5173/reset-password?token="
                                    + token;

                    emailService.sendPasswordResetEmail(
                            user.getEmail(),
                            resetLink
                    );
                });
    }


    @Transactional
    public void resetPassword(
            ResetPasswordRequest request
    ) {

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findByToken(request.getToken())
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Invalid reset token"
                                )
                        );

        if (resetToken.isUsed()) {
            throw new BadRequestException(
                    "Reset token already used"
            );
        }

        if (resetToken.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            throw new BadRequestException(
                    "Reset token expired"
            );
        }

        User user = resetToken.getUser();

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);

        resetToken.setUsed(true);

        passwordResetTokenRepository.save(resetToken);

        refreshTokenRepository.deleteByUser(user);
    }

    public void logout() {

        User currentUser =
                securityUtils.getCurrentUser();        refreshTokenService
                .revokeUserTokens(currentUser);
    }
    public AuthResponse refreshToken(
            RefreshTokenRequest request
    ) {

        RefreshToken refreshToken =
                refreshTokenService.verifyRefreshToken(
                        request.getRefreshToken()
                );

        User user = refreshToken.getUser();

        String accessToken =
                jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .build();
    }



    private void validateSignupRequest(
            SignupRequest request
    ) {

        if (
                request.getName() == null
                        || request.getName().isBlank()
        ) {

            throw new BadRequestException(
                    "Name is required"
            );
        }

        if (
                request.getEmail() == null
                        || request.getEmail().isBlank()
        ) {

            throw new BadRequestException(
                    "Email is required"
            );
        }

        if (
                request.getPassword() == null
                        || request.getPassword().isBlank()
        ) {

            throw new BadRequestException(
                    "Password is required"
            );
        }
    }

    private void validateLoginRequest(
            LoginRequest request
    ) {

        if (
                request.getEmail() == null
                        || request.getEmail().isBlank()
        ) {

            throw new BadRequestException(
                    "Email is required"
            );
        }

        if (
                request.getPassword() == null
                        || request.getPassword().isBlank()
        ) {

            throw new BadRequestException(
                    "Password is required"
            );
        }
    }
}