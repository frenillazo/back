package com.acainfo.shared.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Dedicated single-thread executor for the AI LaTeX pipeline: one job at a time
 * (the server has 2 vCPUs and a job can take minutes between Claude and tectonic).
 *
 * <p>Declaring this bean would normally remove Boot's auto-configured
 * applicationTaskExecutor and starve the unqualified @Async methods (emails,
 * auto-reservations) behind minutes-long jobs; {@code spring.task.execution.mode=force}
 * in application.properties keeps the default executor for everything else.</p>
 */
@Configuration
public class AiJobExecutorConfig {

    @Bean("aiJobExecutor")
    public Executor aiJobExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("ai-job-");
        executor.initialize();
        return executor;
    }
}
