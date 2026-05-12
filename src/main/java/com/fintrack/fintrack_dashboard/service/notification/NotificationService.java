package com.fintrack.fintrack_dashboard.service.notification;

import com.fintrack.fintrack_dashboard.constant.NotificationType;
import com.fintrack.fintrack_dashboard.dto.notification.CreateNotificationRequest;
import com.fintrack.fintrack_dashboard.dto.notification.NotificationResponse;
import com.fintrack.fintrack_dashboard.dto.common.PaginatedResponse;
import com.fintrack.fintrack_dashboard.entity.Transaction;
import com.fintrack.fintrack_dashboard.entity.User;
import org.springframework.data.domain.Page;

public interface NotificationService {

    /**
     * Creates a new notification for a recipient user.
     *
     * @param request notification creation request
     */
    void createNotification(
            CreateNotificationRequest request
    );

    /**
     * Fetch paginated notifications of currently authenticated user.
     *
     * @param page page number (0-based)
     * @param size page size
     * @param unreadOnly filter unread notifications only
     * @return paginated notification response
     */
    PaginatedResponse<NotificationResponse> getMyNotifications(
            int page,
            int size,
            Boolean unreadOnly
    );

    /**
     * Returns unread notification count
     * for currently authenticated user.
     *
     * @return unread notification count
     */
    long getUnreadCount();

    /**
     * Marks a notification as read.
     *
     * @param id notification id
     */
    void markAsRead(Long id);

    /**
     * Marks all notifications as read
     * for currently authenticated user.
     */
    void markAllAsRead();

    void notifyUser(
            User recipient,
            String title,
            String message,
            NotificationType type,
            String redirectUrl
    );

    void notifyApprovers(Transaction transaction);
}