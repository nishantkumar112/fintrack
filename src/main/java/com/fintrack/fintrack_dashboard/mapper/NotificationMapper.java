package com.fintrack.fintrack_dashboard.mapper;

import com.fintrack.fintrack_dashboard.dto.notification.NotificationResponse;
import com.fintrack.fintrack_dashboard.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationResponse toResponse(
            Notification notification
    ) {

        return NotificationResponse
                .builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .readStatus(notification.isReadStatus())
                .redirectUrl(notification.getRedirectUrl())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }

    public Notification toEntity(
            NotificationResponse notificationResponse
    ) {

        return Notification
                .builder()
                .id(notificationResponse.getId())
                .title(notificationResponse.getTitle())
                .message(notificationResponse.getMessage())
                .type(notificationResponse.getType())
                .readStatus(notificationResponse.isReadStatus())
                .redirectUrl(notificationResponse.getRedirectUrl())
                .createdAt(notificationResponse.getCreatedAt())
                .updatedAt(notificationResponse.getUpdatedAt())
                .build();
    }
}
