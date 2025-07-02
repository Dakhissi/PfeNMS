package com.farukgenc.boilerplate.springboot.service.alert;

import com.farukgenc.boilerplate.springboot.dto.AlertDto;
import com.farukgenc.boilerplate.springboot.dto.AlertAcknowledgeRequest;
import com.farukgenc.boilerplate.springboot.model.Alert;
import com.farukgenc.boilerplate.springboot.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface AlertService {

    // Alert management
    AlertDto createAlert(Alert.AlertType type, Alert.AlertSeverity severity, 
                        String title, String description, Long sourceId, 
                        Alert.SourceType sourceType, String sourceName, User user);

    List<AlertDto> getAlertsByUser(User user);

    Page<AlertDto> getAlertsByUser(User user, Pageable pageable);

    List<AlertDto> getAlertsByStatus(User user, Alert.AlertStatus status);

    List<AlertDto> getAlertsBySeverity(User user, Alert.AlertSeverity severity);

    AlertDto getAlertById(Long id, User user);

    AlertDto acknowledgeAlert(Long id, AlertAcknowledgeRequest request, User user);

    AlertDto resolveAlert(Long id, User user);

    void clearAlert(Long id, User user);

    // Alert statistics
    long getActiveAlertCount(User user);

    long getCriticalAlertCount(User user);

    List<AlertDto> getUnacknowledgedAlerts(User user);

    List<AlertDto> getRecentAlerts(User user, LocalDateTime since);

    // Device monitoring (to be called by monitoring service)
    void checkDeviceStatus(Long deviceId, User user);

    void checkInterfaceStatus(Long interfaceId, User user);

    void checkSystemStatus(Long systemUnitId, User user);


    List<AlertDto> getAlertsByDevice(Long deviceId, User user);

    List<AlertDto> getAlertsBySystemUnit(Long systemUnitId, User user);

    List<AlertDto> getAlertsByInterface(Long interfaceId, User user);
}
