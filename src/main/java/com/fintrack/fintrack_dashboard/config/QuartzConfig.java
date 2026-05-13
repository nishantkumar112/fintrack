package com.fintrack.fintrack_dashboard.config;

import lombok.RequiredArgsConstructor;

import org.quartz.spi.TriggerFiredBundle;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
@RequiredArgsConstructor
public class QuartzConfig {

    private final ApplicationContext applicationContext;

    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {

        AutowireCapableBeanFactory beanFactory =
                applicationContext
                        .getAutowireCapableBeanFactory();

        return new SpringBeanJobFactory() {

            @Override
            protected Object createJobInstance(
                    TriggerFiredBundle bundle
            ) throws Exception {

                Object job =
                        super.createJobInstance(
                                bundle
                        );

                beanFactory.autowireBean(job);

                return job;
            }
        };
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(
            SpringBeanJobFactory jobFactory
    ) {

        SchedulerFactoryBean factory =
                new SchedulerFactoryBean();

        factory.setJobFactory(
                jobFactory
        );

        return factory;
    }
}