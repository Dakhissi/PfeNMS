package com.farukgenc.boilerplate.springboot.service.trap;

import com.farukgenc.boilerplate.springboot.dto.AlertDto;
import com.farukgenc.boilerplate.springboot.model.*;
import com.farukgenc.boilerplate.springboot.repository.DeviceRepository;
import com.farukgenc.boilerplate.springboot.repository.TrapEventRepository;
import com.farukgenc.boilerplate.springboot.service.alert.AlertService;
import com.farukgenc.boilerplate.springboot.service.alert.AlertNotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Service for processing SNMP trap events and creating alerts
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrapProcessor {

    private final TrapEventRepository trapEventRepository;
    private final DeviceRepository deviceRepository;
    private final AlertService alertService;
    private final AlertNotificationService alertNotificationService;
    private final ObjectMapper objectMapper;

    /**
     * Process a received trap and create/update trap event
     */
    public void processTrap(String sourceIp, int sourcePort, String community, String trapOid,
                           String enterpriseOid, Integer genericTrap, Integer specificTrap,
                           Long timestamp, Map<String, Object> variableBindings) {
        
        log.debug("Processing trap from {}:{} - OID: {}", sourceIp, sourcePort, trapOid);

        try {
            // Generate hash key for duplicate detection
            String hashKey = generateHashKey(sourceIp, trapOid, variableBindings);

            // Check for duplicate within last 5 minutes
            Optional<TrapEvent> existingTrap = trapEventRepository.findByHashKey(hashKey);
            
            if (existingTrap.isPresent()) {
                handleDuplicateTrap(existingTrap.get());
                return;
            }

            // Find associated device
            Device device = findDeviceByIp(sourceIp);
            User user = device != null ? device.getUser() : null;

            // Determine trap type and severity
            TrapEvent.TrapType trapType = determineTrapType(trapOid, genericTrap, specificTrap);
            TrapEvent.TrapSeverity severity = determineTrapSeverity(trapType, variableBindings);

            // Create new trap event
            TrapEvent trapEvent = TrapEvent.builder()
                    .sourceIp(sourceIp)
                    .sourcePort(sourcePort)
                    .community(community)
                    .trapOid(trapOid)
                    .enterpriseOid(enterpriseOid)
                    .genericTrap(genericTrap)
                    .specificTrap(specificTrap)
                    .timestamp(timestamp)
                    .uptime(timestamp)
                    .trapType(trapType)
                    .severity(severity)
                    .message(generateTrapMessage(trapType, sourceIp, variableBindings))
                    .rawData(formatRawData(sourceIp, trapOid, variableBindings))
                    .variableBindings(serializeVariableBindings(variableBindings))
                    .processed(false)
                    .alertCreated(false)
                    .duplicateCount(1)
                    .lastOccurrence(LocalDateTime.now())
                    .hashKey(hashKey)
                    .device(device)
                    .user(user)
                    .build();

            // Save trap event
            trapEvent = trapEventRepository.save(trapEvent);
            log.info("Created new trap event: {} from {}", trapEvent.getId(), sourceIp);

            // Create alert if needed
            if (shouldCreateAlert(trapType, severity)) {
                createAlertFromTrap(trapEvent);
            }

            // Mark as processed
            trapEvent.setProcessed(true);
            trapEventRepository.save(trapEvent);

        } catch (Exception e) {
            log.error("Error processing trap from {}: {}", sourceIp, e.getMessage(), e);
        }
    }

    private void handleDuplicateTrap(TrapEvent existingTrap) {
        existingTrap.setDuplicateCount(existingTrap.getDuplicateCount() + 1);
        existingTrap.setLastOccurrence(LocalDateTime.now());
        trapEventRepository.save(existingTrap);
        
        log.debug("Updated duplicate count for trap {} to {}", 
                 existingTrap.getId(), existingTrap.getDuplicateCount());
    }

    private Device findDeviceByIp(String sourceIp) {
        return deviceRepository.findByTargetIp(sourceIp).orElse(null);
    }

    private TrapEvent.TrapType determineTrapType(String trapOid, Integer genericTrap, Integer specificTrap) {
        // Handle standard traps (SNMPv1 generic traps)
        if (genericTrap != null) {
            return switch (genericTrap) {
                case 0 -> TrapEvent.TrapType.COLD_START;
                case 1 -> TrapEvent.TrapType.WARM_START;
                case 2 -> TrapEvent.TrapType.LINK_DOWN;
                case 3 -> TrapEvent.TrapType.LINK_UP;
                case 4 -> TrapEvent.TrapType.AUTHENTICATION_FAILURE;
                case 5 -> TrapEvent.TrapType.EGP_NEIGHBOR_LOSS;
                case 6 -> TrapEvent.TrapType.ENTERPRISE_SPECIFIC;
                default -> TrapEvent.TrapType.UNKNOWN;
            };
        }

        // Handle SNMPv2c/v3 traps by OID
        if (trapOid != null) {
            if (trapOid.contains("1.3.6.1.6.3.1.1.5.1")) return TrapEvent.TrapType.COLD_START;
            if (trapOid.contains("1.3.6.1.6.3.1.1.5.2")) return TrapEvent.TrapType.WARM_START;
            if (trapOid.contains("1.3.6.1.6.3.1.1.5.3")) return TrapEvent.TrapType.LINK_DOWN;
            if (trapOid.contains("1.3.6.1.6.3.1.1.5.4")) return TrapEvent.TrapType.LINK_UP;
            if (trapOid.contains("1.3.6.1.6.3.1.1.5.5")) return TrapEvent.TrapType.AUTHENTICATION_FAILURE;
            if (trapOid.contains("1.3.6.1.6.3.1.1.5.6")) return TrapEvent.TrapType.EGP_NEIGHBOR_LOSS;
            
            // Check for common enterprise-specific patterns
            if (trapOid.contains("temperature") || trapOid.contains("temp")) return TrapEvent.TrapType.TEMPERATURE_ALARM;
            if (trapOid.contains("fan")) return TrapEvent.TrapType.FAN_FAILURE;
            if (trapOid.contains("power")) return TrapEvent.TrapType.POWER_FAILURE;
            if (trapOid.contains("cpu")) return TrapEvent.TrapType.CPU_HIGH;
            if (trapOid.contains("memory") || trapOid.contains("mem")) return TrapEvent.TrapType.MEMORY_LOW;
            if (trapOid.contains("disk")) return TrapEvent.TrapType.DISK_FULL;
            if (trapOid.contains("interface") || trapOid.contains("port")) return TrapEvent.TrapType.INTERFACE_DOWN;
            if (trapOid.contains("config")) return TrapEvent.TrapType.CONFIGURATION_CHANGE;
            if (trapOid.contains("restart") || trapOid.contains("reboot")) return TrapEvent.TrapType.SYSTEM_RESTART;
        }

        return TrapEvent.TrapType.UNKNOWN;
    }

    private TrapEvent.TrapSeverity determineTrapSeverity(TrapEvent.TrapType trapType, Map<String, Object> variableBindings) {
        return switch (trapType) {
            case COLD_START, WARM_START, DEVICE_DOWN, POWER_FAILURE, FAN_FAILURE -> TrapEvent.TrapSeverity.CRITICAL;
            case LINK_DOWN, INTERFACE_DOWN, SYSTEM_RESTART, AUTHENTICATION_FAILURE -> TrapEvent.TrapSeverity.MAJOR;
            case TEMPERATURE_ALARM, CPU_HIGH, MEMORY_LOW, DISK_FULL -> TrapEvent.TrapSeverity.MINOR;
            case LINK_UP, INTERFACE_UP, DEVICE_UP, CONFIGURATION_CHANGE -> TrapEvent.TrapSeverity.WARNING;
            case EGP_NEIGHBOR_LOSS, THRESHOLD_EXCEEDED -> TrapEvent.TrapSeverity.WARNING;
            default -> TrapEvent.TrapSeverity.INFO;
        };
    }

    private String generateTrapMessage(TrapEvent.TrapType trapType, String sourceIp, Map<String, Object> variableBindings) {
        String baseMessage = switch (trapType) {
            case COLD_START -> "Device cold start detected";
            case WARM_START -> "Device warm start detected";
            case LINK_DOWN -> "Network link down";
            case LINK_UP -> "Network link up";
            case INTERFACE_DOWN -> "Interface down";
            case INTERFACE_UP -> "Interface up";
            case DEVICE_DOWN -> "Device is down";
            case DEVICE_UP -> "Device is up";
            case AUTHENTICATION_FAILURE -> "SNMP authentication failure";
            case TEMPERATURE_ALARM -> "Temperature alarm";
            case FAN_FAILURE -> "Fan failure detected";
            case POWER_FAILURE -> "Power failure detected";
            case CPU_HIGH -> "High CPU utilization";
            case MEMORY_LOW -> "Low memory condition";
            case DISK_FULL -> "Disk space full";
            case CONFIGURATION_CHANGE -> "Configuration change detected";
            case SYSTEM_RESTART -> "System restart detected";
            default -> "SNMP trap received";
        };

        return String.format("%s from device %s", baseMessage, sourceIp);
    }

    private String formatRawData(String sourceIp, String trapOid, Map<String, Object> variableBindings) {
        StringBuilder sb = new StringBuilder();
        sb.append("Source: ").append(sourceIp).append("\n");
        sb.append("Trap OID: ").append(trapOid).append("\n");
        sb.append("Variable Bindings:\n");
        
        variableBindings.forEach((oid, value) -> 
            sb.append("  ").append(oid).append(" = ").append(value).append("\n"));
        
        return sb.toString();
    }

    private String serializeVariableBindings(Map<String, Object> variableBindings) {
        try {
            return objectMapper.writeValueAsString(variableBindings);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize variable bindings: {}", e.getMessage());
            return "{}";
        }
    }

    private boolean shouldCreateAlert(TrapEvent.TrapType trapType, TrapEvent.TrapSeverity severity) {
        // Create alerts for critical, major, and minor severity traps
        return severity == TrapEvent.TrapSeverity.CRITICAL ||
               severity == TrapEvent.TrapSeverity.MAJOR ||
               severity == TrapEvent.TrapSeverity.MINOR;
    }

    private void createAlertFromTrap(TrapEvent trapEvent) {
        try {
            if (trapEvent.getUser() == null) {
                log.warn("Cannot create alert for trap {} - no associated user", trapEvent.getId());
                return;
            }

            // Map trap severity to alert severity
            Alert.AlertSeverity alertSeverity = mapTrapSeverityToAlertSeverity(trapEvent.getSeverity());
            
            // Map trap type to alert type
            Alert.AlertType alertType = mapTrapTypeToAlertType(trapEvent.getTrapType());

            // Create alert
            AlertDto alert = alertService.createAlert(
                alertType,
                alertSeverity,
                "SNMP Trap: " + trapEvent.getTrapType().name(),
                trapEvent.getMessage(),
                trapEvent.getDevice() != null ? trapEvent.getDevice().getId() : null,
                Alert.SourceType.DEVICE,
                trapEvent.getSourceIp(),
                trapEvent.getUser()
            );

            // Update trap event with alert information
            trapEvent.setAlertCreated(true);
            trapEvent.setAlertId(alert.getId());
            trapEventRepository.save(trapEvent);

            // Send notification for the alert
            alertNotificationService.sendAlertToUser(alert, trapEvent.getUser());

            log.info("Created alert {} for trap event {}", alert.getId(), trapEvent.getId());

        } catch (Exception e) {
            log.error("Failed to create alert for trap event {}: {}", trapEvent.getId(), e.getMessage(), e);
        }
    }

    private Alert.AlertSeverity mapTrapSeverityToAlertSeverity(TrapEvent.TrapSeverity trapSeverity) {
        return switch (trapSeverity) {
            case CRITICAL -> Alert.AlertSeverity.CRITICAL;
            case MAJOR -> Alert.AlertSeverity.MAJOR;
            case MINOR -> Alert.AlertSeverity.MINOR;
            case WARNING -> Alert.AlertSeverity.WARNING;
            case INFO, CLEARED -> Alert.AlertSeverity.INFO;
        };
    }

    private Alert.AlertType mapTrapTypeToAlertType(TrapEvent.TrapType trapType) {
        return switch (trapType) {
            case COLD_START, WARM_START, SYSTEM_RESTART -> Alert.AlertType.SYSTEM_UP;
            case LINK_DOWN, INTERFACE_DOWN -> Alert.AlertType.INTERFACE_DOWN;
            case LINK_UP, INTERFACE_UP -> Alert.AlertType.INTERFACE_UP;
            case DEVICE_DOWN -> Alert.AlertType.DEVICE_DOWN;
            case DEVICE_UP -> Alert.AlertType.DEVICE_UP;
            case AUTHENTICATION_FAILURE -> Alert.AlertType.CONNECTIVITY;
            case TEMPERATURE_ALARM, FAN_FAILURE, POWER_FAILURE -> Alert.AlertType.SYSTEM_DOWN;
            case CPU_HIGH, MEMORY_LOW -> Alert.AlertType.PERFORMANCE;
            case CONFIGURATION_CHANGE -> Alert.AlertType.CONFIGURATION_CHANGED;
            default -> Alert.AlertType.CONNECTIVITY;
        };
    }

    private String generateHashKey(String sourceIp, String trapOid, Map<String, Object> variableBindings) {
        try {
            String input = sourceIp + ":" + trapOid + ":" + getCurrentTimeWindow();
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.warn("MD5 algorithm not available, using simple hash", e);
            return String.valueOf((sourceIp + trapOid).hashCode());
        }
    }

    private long getCurrentTimeWindow() {
        // Create 5-minute time windows for duplicate detection
        return System.currentTimeMillis() / (5 * 60 * 1000);
    }
}
