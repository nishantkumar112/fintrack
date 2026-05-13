package com.fintrack.fintrack_dashboard.service.scheduler;

import com.fintrack.fintrack_dashboard.jobs.PendingApprovalReminderJob;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.quartz.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final Scheduler scheduler;

    @PostConstruct
    public void init() {

        try {

            schedulePendingApprovalReminderJob();

        } catch (SchedulerException e) {

            log.error(
                    "Failed to initialize quartz jobs",
                    e
            );
        }
    }

    private void schedulePendingApprovalReminderJob()
            throws SchedulerException {

        JobDetail jobDetail =
                JobBuilder.newJob(
                                PendingApprovalReminderJob.class
                        )
                        .withIdentity(
                                "pendingApprovalReminderJob"
                        )
                        .storeDurably()
                        .build();

        Trigger trigger =
                TriggerBuilder.newTrigger()
                        .withIdentity(
                                "pendingApprovalReminderTrigger"
                        )
                        .withSchedule(
                                CronScheduleBuilder
                                        .cronSchedule(
                                                "0 0 9 * * ?"
                                        )
                        )
                        .build();

        scheduler.scheduleJob(
                jobDetail,
                trigger
        );

        log.info(
                "Pending approval reminder job scheduled"
        );
    }
}