package com.fintrack.fintrack_dashboard.respository;

import com.fintrack.fintrack_dashboard.entity.Notification;
import com.fintrack.fintrack_dashboard.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    /**
     * Fetch paginated notifications
     * for a recipient user.
     */
    Page<Notification> findByRecipient(
            User recipient,
            Pageable pageable
    );

    /**
     * Fetch unread paginated notifications
     * for a recipient user.
     */
    Page<Notification>
    findByRecipientAndReadStatusFalse(
            User recipient,
            Pageable pageable
    );

    /**
     * Count unread notifications
     * for a recipient user.
     */
    long countByRecipientAndReadStatusFalse(
            User recipient
    );

    /**
     * Fetch notification by id and recipient.
     * Used for ownership validation.
     */
    Optional<Notification>
    findByIdAndRecipient(
            Long id,
            User recipient
    );

    /**
     * Mark all unread notifications as read
     * for a recipient user.
     */
    @Modifying(clearAutomatically = true,
            flushAutomatically = true)
    @Query("""
            UPDATE Notification n
            SET n.readStatus = true
            WHERE n.recipient = :recipient
            AND n.readStatus = false
            """)
    int markAllAsRead(
            @Param("recipient")
            User recipient
    );

    /**
     * Count notifications by read status.
     */
    long countByRecipientAndReadStatus(
            User recipient,
            boolean readStatus
    );

    /**
     * Delete notifications
     * of a specific user.
     */
    void deleteByRecipient(
            User recipient
    );
}