package com.fintrack.fintrack_dashboard.dto.notification;

import com.fintrack.fintrack_dashboard.constant.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateNotificationRequest {

    private Long recipientId;

    private String title;

    private String message;

    private NotificationType type;

    private String redirectUrl;
}