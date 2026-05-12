package com.fintrack.fintrack_dashboard.mapper;

import com.fintrack.fintrack_dashboard.constant.Role;
import com.fintrack.fintrack_dashboard.constant.UserStatus;
import com.fintrack.fintrack_dashboard.dto.auth.SignupRequest;
import com.fintrack.fintrack_dashboard.dto.user.CreateUserRequest;
import com.fintrack.fintrack_dashboard.dto.user.UserResponse;
import com.fintrack.fintrack_dashboard.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(CreateUserRequest request) {

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());

        return user;
    }

    public User toEntity(SignupRequest request) {

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole(Role.EMPLOYEE);
        user.setStatus(UserStatus.ACTIVE);

        return user;
    }

    public UserResponse toResponse(User user) {

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }

    public void updateEntity(
            User user,
            CreateUserRequest request
    ) {

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
    }
}