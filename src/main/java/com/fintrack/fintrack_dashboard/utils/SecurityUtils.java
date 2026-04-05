package com.fintrack.fintrack_dashboard.utils;

import com.fintrack.fintrack_dashboard.entity.User;
import com.fintrack.fintrack_dashboard.constant.Role;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public User getCurrentUser() {

        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        throw new RuntimeException("User not authenticated");
    }

    public boolean isAdmin(User user) {
        return user.getRole() == Role.ADMIN;
    }

    public boolean isManager(User user) {
        return user.getRole() == Role.MANAGER;
    }

    public boolean isEmployee(User user) {
        return user.getRole() == Role.EMPLOYEE;
    }
}