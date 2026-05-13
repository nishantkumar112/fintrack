package com.fintrack.fintrack_dashboard.service.user;

import com.fintrack.fintrack_dashboard.constant.NotificationType;
import com.fintrack.fintrack_dashboard.constant.Role;
import com.fintrack.fintrack_dashboard.dto.common.PaginatedResponse;
import com.fintrack.fintrack_dashboard.dto.user.CreateUserRequest;
import com.fintrack.fintrack_dashboard.dto.user.UserFilterRequest;
import com.fintrack.fintrack_dashboard.dto.user.UserResponse;
import com.fintrack.fintrack_dashboard.entity.User;
import com.fintrack.fintrack_dashboard.exception.BadRequestException;
import com.fintrack.fintrack_dashboard.exception.ForbiddenException;
import com.fintrack.fintrack_dashboard.exception.ResourceNotFoundException;
import com.fintrack.fintrack_dashboard.mapper.UserMapper;
import com.fintrack.fintrack_dashboard.respository.UserRepository;
import com.fintrack.fintrack_dashboard.service.notification.NotificationService;
import com.fintrack.fintrack_dashboard.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger log =
            LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    private final SecurityUtils securityUtils;

    private final NotificationService notificationService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            SecurityUtils securityUtils,
            NotificationService notificationService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.securityUtils = securityUtils;
        this.notificationService = notificationService;
    }

    public UserResponse createUser(CreateUserRequest request) {

        log.info(
                "Creating user with email={}",
                request.getEmail()
        );

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {

            log.warn(
                    "Email already exists={}",
                    request.getEmail()
            );

            throw new BadRequestException(
                    "Email already exists"
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
                "/profile"
        );

        log.info(
                "User created successfully | id={}",
                savedUser.getId()
        );

        return userMapper.toResponse(savedUser);
    }

    public PaginatedResponse<UserResponse> getUsers(
            UserFilterRequest filter,
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        User currentUser =
                securityUtils.getCurrentUser();

        log.info(
                "Fetching users | page={}, size={}, sortBy={}, direction={}",
                page,
                size,
                sortBy,
                direction
        );

        if (!securityUtils.isAdmin(currentUser)) {

            log.warn(
                    "Unauthorized access attempt | userId={}",
                    currentUser.getId()
            );

            throw new ForbiddenException(
                    "Only ADMIN can view users"
            );
        }

        Sort sort =
                direction.equalsIgnoreCase("desc")
                        ? Sort.by(sortBy).descending()
                        : Sort.by(sortBy).ascending();

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        sort
                );

        Page<User> users =
                userRepository.findAll(
                        UserSpecification.getUsers(filter),
                        pageable
                );

        Page<UserResponse> responsePage =
                users.map(userMapper::toResponse);

        return PaginatedResponse
                .<UserResponse>builder()
                .content(responsePage.getContent())
                .page(responsePage.getNumber())
                .size(responsePage.getSize())
                .totalElements(responsePage.getTotalElements())
                .totalPages(responsePage.getTotalPages())
                .first(responsePage.isFirst())
                .last(responsePage.isLast())
                .build();
    }

    public UserResponse getUser(Long id) {

        User currentUser =
                securityUtils.getCurrentUser();

        log.info(
                "Fetching user | id={}",
                id
        );

        User user =
                getUserOrThrow(id);

        if (
                !securityUtils.isAdmin(currentUser)
                        && !currentUser.getId().equals(id)
        ) {

            log.warn(
                    "Access denied | requesterId={}, targetUserId={}",
                    currentUser.getId(),
                    id
            );

            throw new ForbiddenException(
                    "Access denied"
            );
        }

        return userMapper.toResponse(user);
    }

    public UserResponse updateUser(
            Long id,
            CreateUserRequest request
    ) {

        User currentUser =
                securityUtils.getCurrentUser();

        log.info(
                "Updating user | id={}",
                id
        );

        User user =
                getUserOrThrow(id);

        if (
                !securityUtils.isAdmin(currentUser)
                        && !currentUser.getId().equals(id)
        ) {

            log.warn(
                    "Unauthorized update attempt | requesterId={}, targetUserId={}",
                    currentUser.getId(),
                    id
            );

            throw new ForbiddenException(
                    "Access denied"
            );
        }

        user.setName(
                request.getName()
        );

        user.setEmail(
                request.getEmail()
        );

        if (securityUtils.isAdmin(currentUser)) {

            user.setRole(
                    request.getRole()
            );
        }

        if (
                request.getPassword() != null
                        && !request.getPassword().isBlank()
        ) {

            user.setPassword(
                    passwordEncoder.encode(
                            request.getPassword()
                    )
            );
        }

        User updatedUser =
                userRepository.save(user);
//
//        notificationService.notifyUser(
//                updatedUser,
//                "Profile Updated",
//                "Your profile information has been updated successfully.",
//                NotificationType.USER_UPDATED,
//                "/profile"
//        );

        log.info(
                "User updated successfully | id={}",
                updatedUser.getId()
        );

        return userMapper.toResponse(updatedUser);
    }

    public void deleteUser(Long id) {

        User currentUser =
                securityUtils.getCurrentUser();

        log.warn(
                "Delete request | targetUserId={}, requesterId={}",
                id,
                currentUser.getId()
        );

        if (!securityUtils.isAdmin(currentUser)) {

            log.error(
                    "Unauthorized delete attempt | requesterId={}",
                    currentUser.getId()
            );

            throw new ForbiddenException(
                    "Only ADMIN can delete users"
            );
        }

        User user =
                getUserOrThrow(id);

//        notificationService.notifyUser(
//                user,
//                "Account Deleted",
//                "Your account has been deleted by an administrator.",
//                NotificationType.USER_DELETED,
//                "/login"
//        );

        userRepository.delete(user);

        log.warn(
                "User deleted successfully | id={}",
                id
        );
    }

    public UserResponse getCurrentUser() {

        User user =
                securityUtils.getCurrentUser();

        log.info(
                "Fetching current user | id={}",
                user.getId()
        );

        return userMapper.toResponse(user);
    }

    private User getUserOrThrow(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn(
                            "User not found | id={}",
                            id
                    );

                    return new ResourceNotFoundException(
                            "User not found"
                    );
                });
    }

    public User createOAuthUserIfNotExists(
            String email,
            String name
    ) {

        return userRepository.findByEmail(email)
                .orElseGet(() -> {

                    User user = new User();

                    user.setEmail(email);
                    user.setName(name);

                    user.setPassword("");

                    user.setRole(Role.EMPLOYEE);

                    return userRepository.save(user);
                });
    }
}