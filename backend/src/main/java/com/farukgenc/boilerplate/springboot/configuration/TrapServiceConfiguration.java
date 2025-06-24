package com.farukgenc.boilerplate.springboot.configuration;

import com.farukgenc.boilerplate.springboot.service.trap.TrapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Configuration class to automatically start the SNMP trap receiver when the application starts
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrapServiceConfiguration {

    private final TrapService trapService;

    /**
     * Start the trap receiver when the application is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startTrapReceiver() {
        try {
            log.info("Starting SNMP trap receiver...");
            trapService.startTrapReceiver();
            log.info("SNMP trap receiver started successfully");
        } catch (Exception e) {
            log.error("Failed to start SNMP trap receiver: {}", e.getMessage(), e);
        }
    }
}
