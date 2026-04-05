package com.fintrack.fintrack_dashboard.utils;

import com.fintrack.fintrack_dashboard.constant.TransactionStatus;
import com.fintrack.fintrack_dashboard.entity.Transaction;
import com.fintrack.fintrack_dashboard.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TransactionValidator {

    public void validateOwnership(Transaction transaction, User user, boolean isAdmin) {

        if (isAdmin) return;

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
    }

    public void validatePending(Transaction transaction) {

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new RuntimeException("Only pending transactions can be modified");
        }
    }

    public void validateManagerOrAdmin(boolean isAdmin, boolean isManager) {

        if (!(isAdmin || isManager)) {
            throw new RuntimeException("Only ADMIN or MANAGER can perform this action");
        }
    }
}