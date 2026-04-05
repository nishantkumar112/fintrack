package com.fintrack.fintrack_dashboard.mapper;

import com.fintrack.fintrack_dashboard.dto.user.CreateUserRequest;
import com.fintrack.fintrack_dashboard.dto.user.UserResponse;
import com.fintrack.fintrack_dashboard.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    // DTO → Entity
    public User toEntity(CreateUserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        return user;
    }

    // Entity → DTO
    public UserResponse toResponse(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setEmail(user.getEmail());
        res.setRole(user.getRole());
        return res;
    }
}