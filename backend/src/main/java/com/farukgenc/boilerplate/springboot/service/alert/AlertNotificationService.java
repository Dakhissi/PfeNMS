package com.farukgenc.boilerplate.springboot.service.alert;

import com.farukgenc.boilerplate.springboot.dto.AlertDto;
import com.farukgenc.boilerplate.springboot.event.AlertEvent;
import com.farukgenc.boilerplate.springboot.model.User;

/**
 * Service interface for sending real-time alert notifications via WebSocket
 */
public interface AlertNotificationService {
    
    /**
     * Send a new alert notification to the specific user
     * @param alert The alert to send
     * @param user The user to send the alert to
     */
    void sendAlertToUser(AlertDto alert, User user);
    
    /**
     * Send alert update (acknowledgment, resolution) to the specific user
     * @param alert The updated alert
     * @param user The user to send the update to
     */
    void sendAlertUpdateToUser(AlertDto alert, User user);
    
    /**
     * Send device status update to the specific user
     * @param deviceId The device ID
     * @param status The new status
     * @param user The user to send the update to
     */
    void sendDeviceStatusUpdate(Long deviceId, String status, User user);
    
    /**
     * Send alert statistics update to the specific user
     * @param user The user to send statistics to
     */
    void sendAlertStatisticsUpdate(User user);

    /**
     * Handle alert events and route them to appropriate notification methods
     * @param event The alert event to handle
     */
    void handleAlertEvent(AlertEvent event);
}
