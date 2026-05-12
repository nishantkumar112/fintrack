package com.fintrack.fintrack_dashboard.dto.user;

import com.fintrack.fintrack_dashboard.constant.Role;
import com.fintrack.fintrack_dashboard.constant.UserStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private UserStatus status;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}