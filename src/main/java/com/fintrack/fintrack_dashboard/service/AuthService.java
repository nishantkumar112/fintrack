package com.fintrack.fintrack_dashboard.service;

import com.fintrack.fintrack_dashboard.constant.Role;
import com.fintrack.fintrack_dashboard.entity.User;
import com.fintrack.fintrack_dashboard.respository.UserRepository;
import com.fintrack.fintrack_dashboard.security.JwtUtil;
import com.fintrack.fintrack_dashboard.dto.auth.*;
import com.fintrack.fintrack_dashboard.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse signup(SignupRequest request) {

        log.info("Signup attempt for email: {}", request.getEmail());

        validateSignupRequest(request);

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Signup failed - email already exists: {}", request.getEmail());
            throw new BadRequestException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.EMPLOYEE); // default role

        User savedUser = userRepository.save(user);

        log.info("User registered successfully with id: {}", savedUser.getId());

        String token = jwtUtil.generateToken(savedUser.getEmail());

        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {

        log.info("Login attempt for email: {}", request.getEmail());

        validateLoginRequest(request);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed - email not found: {}", request.getEmail());
                    return new BadRequestException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed - incorrect password for email: {}", request.getEmail());
            throw new BadRequestException("Invalid email or password");
        }

        log.info("Login successful for userId: {}", user.getId());

        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(token);
    }

    private void validateSignupRequest(SignupRequest request) {

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Name is required");
        }
    }

    private void validateLoginRequest(LoginRequest request) {

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }
    }
}