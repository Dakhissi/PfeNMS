package com.farukgenc.boilerplate.springboot.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for SNMP polling async tasks and scheduling
 */
@Slf4j
@Configuration
@EnableAsync
@EnableScheduling
public class SnmpAsyncConfiguration {

    /**
     * Task executor for SNMP polling operations
     */
    @Bean(name = "snmpTaskExecutor")
    public Executor snmpTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("SNMP-Poll-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.setRejectedExecutionHandler((runnable, executor1) -> {
            log.warn("SNMP polling task rejected: {}", runnable);
            // Could implement a fallback strategy here
        });
        
        executor.initialize();
        log.info("SNMP Task Executor initialized with core pool size: {}, max pool size: {}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize());
        
        return executor;
    }
}
