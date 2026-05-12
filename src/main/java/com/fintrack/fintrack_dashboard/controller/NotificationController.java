package com.fintrack.fintrack_dashboard.controller;

import com.fintrack.fintrack_dashboard.dto.common.PaginatedResponse;
import com.fintrack.fintrack_dashboard.dto.notification.NotificationResponse;
import com.fintrack.fintrack_dashboard.service.notification.NotificationService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class NotificationController {

    private final NotificationService
            notificationService;

    /**
     * Fetch paginated notifications
     * of authenticated user.
     */
    @GetMapping
    public ResponseEntity<
            PaginatedResponse<NotificationResponse>
            > getNotifications(

            @RequestParam(defaultValue = "0")
            @Min(
                    value = 0,
                    message = "Page number cannot be negative"
            )
            int page,

            @RequestParam(defaultValue = "10")
            @Min(
                    value = 1,
                    message = "Page size must be at least 1"
            )
            @Max(
                    value = 100,
                    message = "Page size cannot exceed 100"
            )
            int size,

            @RequestParam(required = false)
            Boolean unreadOnly
    ) {

        log.info(
                "Fetching notifications. page={}, size={}, unreadOnly={}",
                page,
                size,
                unreadOnly
        );

        PaginatedResponse<NotificationResponse>
                response =
                notificationService
                        .getMyNotifications(
                                page,
                                size,
                                unreadOnly
                        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get unread notification count.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long>
    getUnreadCount() {

        log.info(
                "Fetching unread notification count"
        );

        long unreadCount =
                notificationService
                        .getUnreadCount();

        return ResponseEntity.ok(
                unreadCount
        );
    }

    /**
     * Mark a notification as read.
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void>
    markAsRead(
            @PathVariable Long id
    ) {

        log.info(
                "Marking notification as read. notificationId={}",
                id
        );

        notificationService.markAsRead(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Mark all notifications as read.
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void>
    markAllAsRead() {

        log.info(
                "Marking all notifications as read"
        );

        notificationService.markAllAsRead();

        return ResponseEntity.noContent().build();
    }
}