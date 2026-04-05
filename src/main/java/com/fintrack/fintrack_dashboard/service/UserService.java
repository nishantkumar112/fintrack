package com.fintrack.fintrack_dashboard.service;

import com.fintrack.fintrack_dashboard.entity.User;
import com.fintrack.fintrack_dashboard.respository.UserRepository;
import com.fintrack.fintrack_dashboard.dto.user.*;
import com.fintrack.fintrack_dashboard.mapper.UserMapper;
import com.fintrack.fintrack_dashboard.utils.SecurityUtils;
import com.fintrack.fintrack_dashboard.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final SecurityUtils securityUtils;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserMapper userMapper,
                       SecurityUtils securityUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.securityUtils = securityUtils;
    }

    // ============================
    // CREATE USER
    // ============================
    public UserResponse createUser(CreateUserRequest request) {

        log.info("Creating user with email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Email already exists: {}", request.getEmail());
            throw new BadRequestException("Email already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        log.info("User created successfully with id: {}", savedUser.getId());

        return userMapper.toResponse(savedUser);
    }

    // ============================
    // GET USERS (PAGING + FILTER)
    // ============================
    public Page<UserResponse> getUsers(UserFilterRequest filter,
                                       int page,
                                       int size,
                                       String sortBy,
                                       String direction) {

        User currentUser = securityUtils.getCurrentUser();

        log.info("Fetching users | page: {}, size: {}, sortBy: {}, direction: {}, filter: {}",
                page, size, sortBy, direction, filter);

        if (!securityUtils.isAdmin(currentUser)) {
            log.warn("Unauthorized access attempt by userId: {}", currentUser.getId());
            throw new ForbiddenException("Only ADMIN can view users");
        }

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> users = userRepository.findAll(
                UserSpecification.getUsers(filter),
                pageable
        );

        log.info("Fetched {} users", users.getTotalElements());

        return users.map(userMapper::toResponse);
    }

    // ============================
    // GET USER
    // ============================
    public UserResponse getUser(Long id) {

        User currentUser = securityUtils.getCurrentUser();

        log.info("Fetching user with id: {}", id);

        User user = getUserOrThrow(id);

        if (!securityUtils.isAdmin(currentUser) &&
                !currentUser.getId().equals(id)) {

            log.warn("Access denied | requesterId: {}, targetUserId: {}",
                    currentUser.getId(), id);

            throw new ForbiddenException("Access denied");
        }

        return userMapper.toResponse(user);
    }

    // ============================
    // UPDATE USER
    // ============================
    public UserResponse updateUser(Long id, CreateUserRequest request) {

        User currentUser = securityUtils.getCurrentUser();

        log.info("Updating user with id: {}", id);

        User user = getUserOrThrow(id);

        if (!securityUtils.isAdmin(currentUser) &&
                !currentUser.getId().equals(id)) {

            log.warn("Unauthorized update attempt | requesterId: {}, targetUserId: {}",
                    currentUser.getId(), id);

            throw new ForbiddenException("Access denied");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // Prevent role escalation
        if (securityUtils.isAdmin(currentUser)) {
            user.setRole(request.getRole());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);

        log.info("User updated successfully | id: {}", id);

        return userMapper.toResponse(updatedUser);
    }

    // ============================
    // DELETE USER
    // ============================
    public void deleteUser(Long id) {

        User currentUser = securityUtils.getCurrentUser();

        log.warn("Delete request for userId: {} by requesterId: {}", id, currentUser.getId());

        if (!securityUtils.isAdmin(currentUser)) {
            log.error("Unauthorized delete attempt by userId: {}", currentUser.getId());
            throw new ForbiddenException("Only ADMIN can delete users");
        }

        User user = getUserOrThrow(id);

        userRepository.delete(user);

        log.warn("User deleted successfully | id: {}", id);
    }

    // ============================
    // CURRENT USER
    // ============================
    public UserResponse getCurrentUser() {

        User user = securityUtils.getCurrentUser();

        log.info("Fetching current user | id: {}", user.getId());

        return userMapper.toResponse(user);
    }

    // ============================
    // HELPER
    // ============================
    private User getUserOrThrow(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new ResourceNotFoundException("User not found");
                });
    }
}