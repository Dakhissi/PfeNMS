package com.farukgenc.boilerplate.springboot.service.alert;

import com.farukgenc.boilerplate.springboot.dto.AlertDto;
import com.farukgenc.boilerplate.springboot.dto.AlertAcknowledgeRequest;
import com.farukgenc.boilerplate.springboot.event.AlertEvent;
import com.farukgenc.boilerplate.springboot.mapper.AlertMapper;
import com.farukgenc.boilerplate.springboot.model.Alert;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final AlertMapper alertMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public AlertDto createAlert(Alert.AlertType type, Alert.AlertSeverity severity,
                               String title, String description, Long sourceId,
                               Alert.SourceType sourceType, String sourceName, User user) {
        
        // Only create alerts for CRITICAL, SEVERE, and WARNING levels - skip INFO
        if (severity == Alert.AlertSeverity.INFO) {
            log.debug("Skipping INFO level alert: {} for user: {}", title, user.getUsername());
            return null;
        }
        
        log.info("Creating alert: {} for user: {}", title, user.getUsername());

        // Generate unique alert key to prevent duplicates - improved key generation
        String alertKey = generateAlertKey(type, sourceId, sourceType, severity);

        // Check if similar alert already exists and is active (with time-based deduplication)
        Optional<Alert> existingAlert = alertRepository.findByAlertKeyAndUser(alertKey, user);
        
        if (existingAlert.isPresent()) {
            Alert alert = existingAlert.get();
            if (alert.getStatus() == Alert.AlertStatus.ACTIVE) {
                // Only update if last occurrence was more than 5 minutes ago (prevent spam)
                LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
                if (alert.getLastOccurrence().isBefore(fiveMinutesAgo)) {
                    // Update occurrence count and last occurrence time
                    alert.setOccurrenceCount(alert.getOccurrenceCount() + 1);
                    alert.setLastOccurrence(LocalDateTime.now());
                    // Update description with latest info
                    alert.setDescription(description);
                    Alert updatedAlert = alertRepository.save(alert);
                    
                    AlertDto alertDto = alertMapper.toDto(updatedAlert);
                    // Publish event for notifications instead of direct service call
                    publishAlertEvent(alertDto, user, AlertEvent.EventType.UPDATED_ALERT);

                    log.info("Updated existing alert occurrence count: {}", alertDto.getId());
                    return alertDto;
                } else {
                    log.debug("Suppressing duplicate alert within 5-minute window: {}", title);
                    return alertMapper.toDto(alert); // Return existing alert without update
                }
            }
        }

        // Create new alert
        Alert alert = Alert.builder()
                .type(type)
                .severity(severity)
                .status(Alert.AlertStatus.ACTIVE)
                .title(title)
                .description(description)
                .sourceId(sourceId)
                .sourceType(sourceType)
                .sourceName(sourceName)
                .alertKey(alertKey)
                .firstOccurrence(LocalDateTime.now())
                .lastOccurrence(LocalDateTime.now())
                .user(user)
                .build();

        Alert savedAlert = alertRepository.save(alert);
        AlertDto alertDto = alertMapper.toDto(savedAlert);
        
        // Publish event for notifications instead of direct service call
        publishAlertEvent(alertDto, user, AlertEvent.EventType.NEW_ALERT);

        log.info("Alert created successfully with ID: {}", savedAlert.getId());
        return alertDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertDto> getAlertsByUser(User user) {
        List<Alert> alerts = alertRepository.findByUser(user);
        return alerts.stream()
                .map(alertMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlertDto> getAlertsByUser(User user, Pageable pageable) {
        Page<Alert> alerts = alertRepository.findByUser(user, pageable);
        return alerts.map(alertMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertDto> getAlertsByStatus(User user, Alert.AlertStatus status) {
        List<Alert> alerts = alertRepository.findByUserAndStatus(user, status);
        return alerts.stream()
                .map(alertMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertDto> getAlertsBySeverity(User user, Alert.AlertSeverity severity) {
        List<Alert> alerts = alertRepository.findByUserAndSeverity(user, severity);
        return alerts.stream()
                .map(alertMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AlertDto getAlertById(Long id, User user) {
        Alert alert = alertRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found or access denied"));
        return alertMapper.toDto(alert);
    }

    @Override
    public AlertDto acknowledgeAlert(Long id, AlertAcknowledgeRequest request, User user) {
        Alert alert = alertRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found or access denied"));

        alert.setAcknowledged(true);
        alert.setAcknowledgedBy(user.getUsername());
        alert.setAcknowledgedAt(LocalDateTime.now());
        alert.setStatus(Alert.AlertStatus.ACKNOWLEDGED);

        Alert savedAlert = alertRepository.save(alert);
        AlertDto alertDto = alertMapper.toDto(savedAlert);
        
        // Publish event for notifications instead of direct service call
        publishAlertEvent(alertDto, user, AlertEvent.EventType.UPDATED_ALERT);

        log.info("Alert acknowledged: {} by user: {}", id, user.getUsername());
        return alertDto;
    }

    @Override
    public AlertDto resolveAlert(Long id, User user) {
        Alert alert = alertRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found or access denied"));

        alert.setStatus(Alert.AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolvedBy(user.getUsername());

        Alert savedAlert = alertRepository.save(alert);
        AlertDto alertDto = alertMapper.toDto(savedAlert);
        
        // Publish event for notifications instead of direct service call
        publishAlertEvent(alertDto, user, AlertEvent.EventType.UPDATED_ALERT);

        log.info("Alert resolved: {} by user: {}", id, user.getUsername());
        return alertDto;
    }

    @Override
    public void clearAlert(Long id, User user) {
        Alert alert = alertRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found or access denied"));

        alert.setStatus(Alert.AlertStatus.CLEARED);
        alertRepository.save(alert);
        
        log.info("Alert cleared: {} by user: {}", id, user.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveAlertCount(User user) {
        return alertRepository.countByUserAndStatus(user, Alert.AlertStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCriticalAlertCount(User user) {
        return alertRepository.countActiveByUserAndSeverity(user, Alert.AlertSeverity.CRITICAL);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertDto> getUnacknowledgedAlerts(User user) {
        List<Alert> alerts = alertRepository.findUnacknowledgedByUser(user);
        return alerts.stream()
                .map(alertMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertDto> getRecentAlerts(User user, LocalDateTime since) {
        List<Alert> alerts = alertRepository.findByUserAndStatusSince(user, Alert.AlertStatus.ACTIVE, since);
        return alerts.stream()
                .map(alertMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void checkDeviceStatus(Long deviceId, User user) {
        // This would typically involve SNMP polling or other monitoring logic
        // For now, this is a placeholder for the monitoring implementation
        log.debug("Checking device status for device ID: {}", deviceId);
        
        // Example: If device is down, create an alert
        // This logic would be implemented based on actual monitoring results
    }

    @Override
    public void checkInterfaceStatus(Long interfaceId, User user) {
        // Placeholder for interface monitoring logic
        log.debug("Checking interface status for interface ID: {}", interfaceId);
    }

    @Override
    public void checkSystemStatus(Long systemUnitId, User user) {
        // Placeholder for system unit monitoring logic
        log.debug("Checking system status for system unit ID: {}", systemUnitId);
    }

    // Private helper methods

    private String generateAlertKey(Alert.AlertType type, Long sourceId, Alert.SourceType sourceType, Alert.AlertSeverity severity) {
        return String.format("%s_%s_%d_%s", type.name(), sourceType.name(), sourceId, severity.name());
    }    private void publishAlertEvent(AlertDto alertDto, User user, AlertEvent.EventType eventType) {
        try {
            // Publish event instead of directly calling notification service
            eventPublisher.publishEvent(new AlertEvent(this, alertDto, user, eventType));
            log.debug("Published alert event of type {} for user {}", eventType, user.getUsername());
        } catch (Exception e) {
            log.warn("Failed to publish alert event", e);
        }
    }
}
