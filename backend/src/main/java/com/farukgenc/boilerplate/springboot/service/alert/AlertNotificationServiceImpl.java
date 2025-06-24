package com.farukgenc.boilerplate.springboot.service.alert;

import com.farukgenc.boilerplate.springboot.dto.AlertDto;
import com.farukgenc.boilerplate.springboot.event.AlertEvent;
import com.farukgenc.boilerplate.springboot.model.Alert;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of AlertNotificationService for sending real-time WebSocket notifications
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertNotificationServiceImpl implements AlertNotificationService {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final AlertRepository alertRepository;

    @Override
    public void sendAlertToUser(AlertDto alert, User user) {
        log.debug("Sending new alert notification to user: {} for alert: {}", 
                 user.getUsername(), alert.getAlertKey());
        
        try {
            simpMessagingTemplate.convertAndSendToUser(
                user.getUsername(),
                "/queue/alerts/new",
                alert
            );
            log.debug("Successfully sent new alert notification to user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to send alert notification to user: {}", user.getUsername(), e);
        }
    }

    @Override
    public void sendAlertUpdateToUser(AlertDto alert, User user) {
        log.debug("Sending alert update notification to user: {} for alert: {}", 
                 user.getUsername(), alert.getAlertKey());
        
        try {
            simpMessagingTemplate.convertAndSendToUser(
                user.getUsername(),
                "/queue/alerts/update",
                alert
            );
            log.debug("Successfully sent alert update notification to user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to send alert update notification to user: {}", user.getUsername(), e);
        }
    }

    @Override
    public void sendDeviceStatusUpdate(Long deviceId, String status, User user) {
        log.debug("Sending device status update to user: {} for device: {}", 
                 user.getUsername(), deviceId);
        
        try {
            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("deviceId", deviceId);
            statusUpdate.put("status", status);
            statusUpdate.put("timestamp", System.currentTimeMillis());
            
            simpMessagingTemplate.convertAndSendToUser(
                user.getUsername(),
                "/queue/device/status",
                statusUpdate
            );
            log.debug("Successfully sent device status update to user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to send device status update to user: {}", user.getUsername(), e);
        }
    }

    @Override
    public void sendAlertStatisticsUpdate(User user) {
        log.debug("Sending alert statistics update to user: {}", user.getUsername());
        
        try {
            Map<String, Object> statistics = new HashMap<>();
            // Get statistics directly from repository to avoid circular dependency
            statistics.put("activeCount", alertRepository.countByUserAndStatus(user, Alert.AlertStatus.ACTIVE));
            statistics.put("criticalCount", alertRepository.countActiveByUserAndSeverity(user, Alert.AlertSeverity.CRITICAL));
            statistics.put("unacknowledgedCount", alertRepository.findUnacknowledgedByUser(user).size());
            statistics.put("timestamp", System.currentTimeMillis());
            
            simpMessagingTemplate.convertAndSendToUser(
                user.getUsername(),
                "/queue/alerts/statistics",
                statistics
            );
            log.debug("Successfully sent alert statistics update to user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to send alert statistics update to user: {}", user.getUsername(), e);
        }
    }

    /**
     * Listen for alert events and send appropriate notifications
     */
    @Async
    @EventListener
    public void handleAlertEvent(AlertEvent event) {
        User user = event.getUser();
        AlertDto alert = event.getAlert();

        switch(event.getEventType()) {
            case NEW_ALERT:
                sendAlertToUser(alert, user);
                break;
            case UPDATED_ALERT:
                sendAlertUpdateToUser(alert, user);
                break;
            case STATISTICS_UPDATE:
                sendAlertStatisticsUpdate(user);
                break;
            default:
                log.warn("Unknown alert event type: {}", event.getEventType());
        }
    }
}
