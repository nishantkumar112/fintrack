package com.fintrack.fintrack_dashboard.service.authorization;

import com.fintrack.fintrack_dashboard.constant.NotificationType;
import com.fintrack.fintrack_dashboard.constant.Role;
import com.fintrack.fintrack_dashboard.constant.UserStatus;
import com.fintrack.fintrack_dashboard.dto.auth.AuthResponse;
import com.fintrack.fintrack_dashboard.dto.auth.LoginRequest;
import com.fintrack.fintrack_dashboard.dto.auth.SignupRequest;
import com.fintrack.fintrack_dashboard.entity.User;
import com.fintrack.fintrack_dashboard.exception.BadRequestException;
import com.fintrack.fintrack_dashboard.mapper.UserMapper;
import com.fintrack.fintrack_dashboard.respository.UserRepository;
import com.fintrack.fintrack_dashboard.security.JwtUtil;
import com.fintrack.fintrack_dashboard.service.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log =
            LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            UserMapper userMapper,
            NotificationService notificationService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
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

        String token =
                jwtUtil.generateToken(
                        savedUser.getEmail()
                );

        return new AuthResponse(token);
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

        String token =
                jwtUtil.generateToken(
                        user.getEmail()
                );

        return new AuthResponse(token);
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