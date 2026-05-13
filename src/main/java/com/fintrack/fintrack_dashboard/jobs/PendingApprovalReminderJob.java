package com.fintrack.fintrack_dashboard.jobs;

import com.fintrack.fintrack_dashboard.constant.NotificationType;
import com.fintrack.fintrack_dashboard.entity.Transaction;
import com.fintrack.fintrack_dashboard.entity.User;
import com.fintrack.fintrack_dashboard.respository.TransactionRepository;
import com.fintrack.fintrack_dashboard.respository.UserRepository;
import com.fintrack.fintrack_dashboard.service.notification.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PendingApprovalReminderJob
        implements Job {

    private final TransactionRepository transactionRepository;

    private final NotificationService notificationService;

    private final UserRepository userRepository;

    @Override
    public void execute(
            JobExecutionContext context
    ) {

        log.info(
                "Pending approval reminder job started"
        );

        LocalDateTime cutoff =
                LocalDateTime.now().minusDays(2);

        List<Transaction> transactions =
                transactionRepository
                        .findOldPendingTransactions(
                                cutoff
                        );

        if (transactions.isEmpty()) {

            log.info(
                    "No pending transactions found"
            );

            return;
        }

        List<User> approvers =
                userRepository
                        .findManagersAndAdmins();

        for (User approver : approvers) {

            notificationService.notifyUser(
                    approver,
                    "Pending Approvals Reminder",
                    "There are pending transactions awaiting approval.",
                    NotificationType.TRANSACTION_PENDING_APPROVAL,
                    "/transactions"
            );
        }

        log.info(
                "Pending approval reminders sent | transactions={}, approvers={}",
                transactions.size(),
                approvers.size()
        );
    }
}