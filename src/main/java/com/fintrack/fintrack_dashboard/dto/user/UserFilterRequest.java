package com.fintrack.fintrack_dashboard.dto.user;

import com.fintrack.fintrack_dashboard.constant.Role;
import lombok.Data;

@Data
public class UserFilterRequest {

    private String name;
    private String email;
    private Role role;
    private String status;
}