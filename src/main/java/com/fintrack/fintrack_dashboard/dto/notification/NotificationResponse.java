package com.fintrack.fintrack_dashboard.dto.notification;

import com.fintrack.fintrack_dashboard.constant.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;

    private String title;

    private String message;

    private NotificationType type;

    private boolean readStatus;

    private String redirectUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}