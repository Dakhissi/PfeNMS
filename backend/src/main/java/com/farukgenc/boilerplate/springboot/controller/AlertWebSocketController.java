package com.farukgenc.boilerplate.springboot.controller;

import com.farukgenc.boilerplate.springboot.dto.AlertDto;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.service.alert.AlertService;
import com.farukgenc.boilerplate.springboot.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * WebSocket controller for real-time alert notifications
 * Provides user-specific alert subscriptions and real-time notifications
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class AlertWebSocketController {

    private final AlertService alertService;

    /**
     * Subscribe to user's device alerts
     * Returns current unacknowledged alerts on subscription
     */
    @SubscribeMapping("/user/queue/alerts")
    public List<AlertDto> subscribeToUserAlerts() {
        User user = SecurityUtils.getCurrentUser();
        log.info("User {} subscribed to device alerts", user.getUsername());
        
        // Send current unacknowledged alerts for user's devices on subscription
        return alertService.getUnacknowledgedAlerts(user);
    }

    /**
     * Get recent alerts for user's devices
     */
    @MessageMapping("/alerts/recent")
    @SendToUser("/queue/alerts/recent")
    public List<AlertDto> getRecentAlerts(
            @Payload Map<String, Object> payload) {
        User user = SecurityUtils.getCurrentUser();
        log.debug("User {} requested recent alerts", user.getUsername());
        
        // Extract hours from payload, default to 24 hours
        int hours = payload.containsKey("hours") ? 
            Integer.parseInt(payload.get("hours").toString()) : 24;
        
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return alertService.getRecentAlerts(user, since);
    }

    /**
     * Get alert statistics for user's devices
     */
    @MessageMapping("/alerts/statistics")
    @SendToUser("/queue/alerts/statistics")
    public AlertStatistics getAlertStatistics() {
        User user = SecurityUtils.getCurrentUser();
        log.debug("User {} requested alert statistics", user.getUsername());
        
        return AlertStatistics.builder()
                .activeCount(alertService.getActiveAlertCount(user))
                .criticalCount(alertService.getCriticalAlertCount(user))
                .unacknowledgedCount(alertService.getUnacknowledgedAlerts(user).size())
                .build();
    }

    /**
     * Get alerts for a specific device
     */
    @MessageMapping("/alerts/device")
    @SendToUser("/queue/alerts/device")
    public List<AlertDto> getDeviceAlerts(
            @Payload Map<String, Object> payload) {
        User user = SecurityUtils.getCurrentUser();
        log.debug("User {} requested alerts for device", user.getUsername());
        
        if (!payload.containsKey("deviceId")) {
            throw new IllegalArgumentException("deviceId is required");
        }
        
        Long deviceId = Long.parseLong(payload.get("deviceId").toString());
        // For now, return all user alerts until device-specific method is implemented
        return alertService.getUnacknowledgedAlerts(user);
    }

    /**
     * Inner class for alert statistics
     */
    public static class AlertStatistics {
        private long activeCount;
        private long criticalCount;
        private long unacknowledgedCount;

        public static AlertStatisticsBuilder builder() {
            return new AlertStatisticsBuilder();
        }

        public static class AlertStatisticsBuilder {
            private long activeCount;
            private long criticalCount;
            private long unacknowledgedCount;

            public AlertStatisticsBuilder activeCount(long activeCount) {
                this.activeCount = activeCount;
                return this;
            }

            public AlertStatisticsBuilder criticalCount(long criticalCount) {
                this.criticalCount = criticalCount;
                return this;
            }

            public AlertStatisticsBuilder unacknowledgedCount(long unacknowledgedCount) {
                this.unacknowledgedCount = unacknowledgedCount;
                return this;
            }

            public AlertStatistics build() {
                AlertStatistics stats = new AlertStatistics();
                stats.activeCount = this.activeCount;
                stats.criticalCount = this.criticalCount;
                stats.unacknowledgedCount = this.unacknowledgedCount;
                return stats;
            }
        }

        // Getters
        public long getActiveCount() { return activeCount; }
        public long getCriticalCount() { return criticalCount; }
        public long getUnacknowledgedCount() { return unacknowledgedCount; }
    }
}