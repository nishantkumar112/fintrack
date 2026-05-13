package com.fintrack.fintrack_dashboard.jobs;

import com.fintrack.fintrack_dashboard.respository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationCleanupJob implements Job {

    private final NotificationRepository notificationRepository;

    @Override
    public void execute(JobExecutionContext context) {

        log.info("Notification cleanup job started");

        LocalDateTime threshold =
                LocalDateTime.now().minusDays(30);

        int deleted =
                notificationRepository.deleteOldNotifications(
                        threshold
                );

        log.info(
                "Notification cleanup completed | deleted={}",
                deleted
        );
    }
}