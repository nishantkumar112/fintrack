package com.fintrack.fintrack_dashboard.service.notification;

import com.fintrack.fintrack_dashboard.constant.NotificationType;
import com.fintrack.fintrack_dashboard.dto.common.PaginatedResponse;
import com.fintrack.fintrack_dashboard.dto.notification.CreateNotificationRequest;
import com.fintrack.fintrack_dashboard.dto.notification.NotificationResponse;
import com.fintrack.fintrack_dashboard.entity.Notification;
import com.fintrack.fintrack_dashboard.entity.Transaction;
import com.fintrack.fintrack_dashboard.entity.User;
import com.fintrack.fintrack_dashboard.exception.ResourceNotFoundException;
import com.fintrack.fintrack_dashboard.mapper.NotificationMapper;
import com.fintrack.fintrack_dashboard.respository.NotificationRepository;
import com.fintrack.fintrack_dashboard.respository.UserRepository;
import com.fintrack.fintrack_dashboard.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository notificationRepository;

    private final UserRepository userRepository;

    private final NotificationMapper notificationMapper;

    private final SecurityUtils securityUtils;

    @Override
    public void createNotification(
            CreateNotificationRequest request
    ) {

        log.info(
                "Creating notification for recipientId={}",
                request.getRecipientId()
        );

        User recipient = userRepository
                .findById(request.getRecipientId())
                .orElseThrow(() -> {

                    log.error(
                            "Recipient user not found with id={}",
                            request.getRecipientId()
                    );

                    return new ResourceNotFoundException(
                            "Recipient user not found"
                    );
                });

        Notification notification = Notification
                .builder()
                .recipient(recipient)
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .redirectUrl(request.getRedirectUrl())
                .readStatus(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        log.info(
                "Notification created successfully for recipientId={}",
                recipient.getId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<NotificationResponse>
    getMyNotifications(
            int page,
            int size,
            Boolean unreadOnly
    ) {

        User currentUser =
                securityUtils.getCurrentUser();

        log.info(
                "Fetching notifications for userId={}, page={}, size={}, unreadOnly={}",
                currentUser.getId(),
                page,
                size,
                unreadOnly
        );

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Notification> notifications;

        if (Boolean.TRUE.equals(unreadOnly)) {

            notifications =
                    notificationRepository
                            .findByRecipientAndReadStatusFalse(
                                    currentUser,
                                    pageable
                            );

        } else {

            notifications =
                    notificationRepository
                            .findByRecipient(
                                    currentUser,
                                    pageable
                            );
        }

        Page<NotificationResponse> responsePage =
                notifications.map(
                        notificationMapper::toResponse
                );

        return PaginatedResponse
                .<NotificationResponse>builder()
                .content(responsePage.getContent())
                .page(responsePage.getNumber())
                .size(responsePage.getSize())
                .totalElements(
                        responsePage.getTotalElements()
                )
                .totalPages(
                        responsePage.getTotalPages()
                )
                .first(responsePage.isFirst())
                .last(responsePage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {

        User currentUser =
                securityUtils.getCurrentUser();

        long unreadCount =
                notificationRepository
                        .countByRecipientAndReadStatusFalse(
                                currentUser
                        );

        log.info(
                "Unread notification count={} for userId={}",
                unreadCount,
                currentUser.getId()
        );

        return unreadCount;
    }

    @Override
    public void markAsRead(Long id) {

        User currentUser =
                securityUtils.getCurrentUser();

        log.info(
                "Marking notification as read. notificationId={}, userId={}",
                id,
                currentUser.getId()
        );

        Notification notification =
                notificationRepository
                        .findByIdAndRecipient(
                                id,
                                currentUser
                        )
                        .orElseThrow(() -> {

                            log.error(
                                    "Notification not found. notificationId={}, userId={}",
                                    id,
                                    currentUser.getId()
                            );

                            return new ResourceNotFoundException(
                                    "Notification not found"
                            );
                        });

        notification.setReadStatus(true);

        notificationRepository.save(notification);

        log.info(
                "Notification marked as read successfully. notificationId={}",
                id
        );
    }

    @Override
    public void markAllAsRead() {

        User currentUser =
                securityUtils.getCurrentUser();

        log.info(
                "Marking all notifications as read for userId={}",
                currentUser.getId()
        );

        notificationRepository.markAllAsRead(
                currentUser
        );

        log.info(
                "All notifications marked as read for userId={}",
                currentUser.getId()
        );
    }

    @Override
    public void notifyUser(
            User recipient,
            String title,
            String message,
            NotificationType type,
            String redirectUrl
    ) {

        CreateNotificationRequest request = new CreateNotificationRequest();

        request.setRecipientId(recipient.getId());
        request.setTitle(title);
        request.setMessage(message);
        request.setType(type);
        request.setRedirectUrl(redirectUrl);

        createNotification(request);
    }

    @Override
    public void notifyApprovers(Transaction transaction) {

        List<User> approvers = userRepository.findManagersAndAdmins();

        for (User approver : approvers) {

            if (approver.getId().equals(transaction.getUser().getId())) {
                continue;
            }

            notifyUser(
                    approver,
                    "New Transaction Pending",
                    "A new transaction requires approval.",
                    NotificationType.TRANSACTION_PENDING_APPROVAL,
                    "/transactions/" + transaction.getId()
            );
        }
    }
}