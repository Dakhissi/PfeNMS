package com.farukgenc.boilerplate.springboot.service.device;

import com.farukgenc.boilerplate.springboot.dto.AlertDto;
import com.farukgenc.boilerplate.springboot.dto.PingResult;
import com.farukgenc.boilerplate.springboot.dto.MibBrowserRequest;
import com.farukgenc.boilerplate.springboot.dto.MibBrowserResponse;
import com.farukgenc.boilerplate.springboot.model.*;
import com.farukgenc.boilerplate.springboot.repository.DeviceRepository;
import com.farukgenc.boilerplate.springboot.service.browser.MibService;
import com.farukgenc.boilerplate.springboot.service.alert.AlertNotificationService;
import com.farukgenc.boilerplate.springboot.service.alert.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for monitoring device health and triggering alerts
 * Secondary service to handle device monitoring
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceMonitoringService {

    private final DeviceRepository deviceRepository;
    private final AlertService alertService;
    private final MibService mibService;
    private final AlertNotificationService alertNotificationService;

    /**
     * Scheduled monitoring of all devices every 30 seconds
     * DISABLED - Using SnmpPollingService instead to avoid conflicts
     */
    // @Scheduled(fixedRate = 30000) // 30 seconds
    // @Transactional
    public void monitorAllDevices() {
        log.debug("Device monitoring is handled by SnmpPollingService");
    }

    /**
     * Monitor a specific device
     */
    @Transactional
    public void monitorDevice(Device device) {
        // CRITICAL: Check if monitoring is enabled for this device FIRST
        if (device.getMonitoringEnabled() == null || !device.getMonitoringEnabled()) {
            log.debug("Monitoring is disabled for device: {} - skipping monitoring", device.getName());
            return;
        }

        // Ensure device has config and user loaded
        if (device.getDeviceConfig() == null) {
            log.warn("Device {} has no configuration, skipping monitoring", device.getName());
            return;
        }

        log.debug("Monitoring device: {} ({})", device.getName(), device.getDeviceConfig().getTargetIp());
        
        // Ping test
        PingResult pingResult = pingHost(device.getDeviceConfig().getTargetIp(), 5000);
        
        if (!pingResult.isReachable()) {
            // Device is down
            device.setStatus(Device.DeviceStatus.INACTIVE);
            createDeviceDownAlert(device);
            if (device.getUser() != null) {
                alertNotificationService.sendDeviceStatusUpdate(device.getId(), "DOWN", device.getUser());
            }
        } else {
            // Device is up
            device.setStatus(Device.DeviceStatus.ACTIVE);

            // Check response time
            if (pingResult.getResponseTime() > 1000) { // Response time > 1 second
                createSlowResponseAlert(device, pingResult.getResponseTime());
            }
            
            // If SNMP is enabled, perform SNMP health checks
            if (device.getDeviceConfig().getEnabled() != null && device.getDeviceConfig().getEnabled()) {
                performSnmpHealthCheck(device);
            }
            
            if (device.getUser() != null) {
                alertNotificationService.sendDeviceStatusUpdate(device.getId(), "UP", device.getUser());
            }
        }
        
        // Update last monitoring time
        device.setLastMonitored(LocalDateTime.now());
        deviceRepository.save(device);
    }

    /**
     * Simple ping implementation using Java's InetAddress
     */
    private PingResult pingHost(String ipAddress, int timeout) {
        try {
            InetAddress inet = InetAddress.getByName(ipAddress);
            long startTime = System.currentTimeMillis();
            boolean reachable = inet.isReachable(timeout);
            long responseTime = System.currentTimeMillis() - startTime;
            
            return PingResult.builder()
                    .ipAddress(ipAddress)
                    .reachable(reachable)
                    .responseTime(reachable ? responseTime : -1)
                    .hostname(inet.getHostName())
                    .packetLoss(reachable ? 0 : 100)
                    .build();
        } catch (Exception e) {
            log.error("Error pinging host {}: {}", ipAddress, e.getMessage());
            return PingResult.builder()
                    .ipAddress(ipAddress)
                    .reachable(false)
                    .responseTime(-1)
                    .errorMessage(e.getMessage())
                    .packetLoss(100)
                    .build();
        }
    }

    /**
     * Perform SNMP-based health checks
     */
    private void performSnmpHealthCheck(Device device) {
        try {
            String community = device.getDeviceConfig().getCommunityString() != null ? 
                device.getDeviceConfig().getCommunityString() : "public";
            
            // Check system uptime
            MibBrowserRequest uptimeRequest = MibBrowserRequest.builder()
                    .targetIp(device.getDeviceConfig().getTargetIp())
                    .community(community)
                    .oid("1.3.6.1.2.1.1.3.0")
                    .snmpPort(device.getDeviceConfig().getSnmpPort())
                    .timeout(device.getDeviceConfig().getSnmpTimeout())
                    .retries(device.getDeviceConfig().getSnmpRetries())
                    .build();
            
            List<MibBrowserResponse> uptimeResult = mibService.performSnmpWalk(uptimeRequest, device.getUser());
            
            if (uptimeResult.isEmpty() || !uptimeResult.get(0).isSuccess()) {
                createSnmpUnreachableAlert(device);
                return;
            }
            
            // Check interface statuses
            MibBrowserRequest interfaceRequest = MibBrowserRequest.builder()
                    .targetIp(device.getDeviceConfig().getTargetIp())
                    .community(community)
                    .oid("1.3.6.1.2.1.2.2.1.8") // ifOperStatus
                    .snmpPort(device.getDeviceConfig().getSnmpPort())
                    .timeout(device.getDeviceConfig().getSnmpTimeout())
                    .retries(device.getDeviceConfig().getSnmpRetries())
                    .build();
            
            List<MibBrowserResponse> interfaceResult = mibService.performSnmpWalk(interfaceRequest, device.getUser());
            
            if (!interfaceResult.isEmpty()) {
                checkInterfaceStatuses(device, interfaceResult);
            }
            
        } catch (Exception e) {
            log.warn("SNMP health check failed for device {}: {}", device.getName(), e.getMessage());
            createSnmpUnreachableAlert(device);
        }
    }

    /**
     * Check interface statuses from SNMP walk result
     */
    private void checkInterfaceStatuses(Device device, List<MibBrowserResponse> interfaceResult) {
        for (MibBrowserResponse response : interfaceResult) {
            if (response.isSuccess() && "2".equals(response.getValue())) { // Interface down (1=up, 2=down)
                String interfaceIndex = extractInterfaceIndex(response.getOid());
                createInterfaceDownAlert(device, interfaceIndex);
            }
        }
    }

    /**
     * Extract interface index from OID
     */
    private String extractInterfaceIndex(String oid) {
        String[] parts = oid.split("\\.");
        return parts[parts.length - 1];
    }

    /**
     * Create device down alert
     */
    private void createDeviceDownAlert(Device device) {
        AlertDto alert = alertService.createAlert(
            Alert.AlertType.DEVICE_DOWN,
            Alert.AlertSeverity.CRITICAL,
            "Device Down",
            String.format("Device %s (%s) is not responding to ping", 
                         device.getName(), device.getDeviceConfig().getTargetIp()),
            device.getId(),
            Alert.SourceType.DEVICE,
            device.getName(),
            device.getUser()
        );
        
        alertNotificationService.sendAlertToUser(alert, device.getUser());
    }

    /**
     * Create slow response alert
     */
    private void createSlowResponseAlert(Device device, long responseTime) {
        AlertDto alert = alertService.createAlert(
            Alert.AlertType.PERFORMANCE,
            Alert.AlertSeverity.WARNING,
            "Slow Response",
            String.format("Device %s (%s) has slow response time: %d ms", 
                         device.getName(), device.getDeviceConfig().getTargetIp(), responseTime),
            device.getId(),
            Alert.SourceType.DEVICE,
            device.getName(),
            device.getUser()
        );
        
        alertNotificationService.sendAlertToUser(alert, device.getUser());
    }

    /**
     * Create SNMP unreachable alert
     */
    private void createSnmpUnreachableAlert(Device device) {
        AlertDto alert = alertService.createAlert(
            Alert.AlertType.CONNECTIVITY,
            Alert.AlertSeverity.WARNING,
            "SNMP Unreachable",
            String.format("Device %s (%s) is not responding to SNMP requests", 
                         device.getName(), device.getDeviceConfig().getTargetIp()),
            device.getId(),
            Alert.SourceType.DEVICE,
            device.getName(),
            device.getUser()
        );
        
        alertNotificationService.sendAlertToUser(alert, device.getUser());
    }

    /**
     * Create interface down alert
     */
    private void createInterfaceDownAlert(Device device, String interfaceIndex) {
        AlertDto alert = alertService.createAlert(
            Alert.AlertType.INTERFACE_DOWN,
            Alert.AlertSeverity.WARNING,
            "Interface Down",
            String.format("Interface %s on device %s (%s) is down", 
                         interfaceIndex, device.getName(), device.getDeviceConfig().getTargetIp()),
            device.getId(),
            Alert.SourceType.INTERFACE,
            device.getName() + " Interface " + interfaceIndex,
            device.getUser()
        );
        
        alertNotificationService.sendAlertToUser(alert, device.getUser());
    }

    /**
     * Manually trigger monitoring for a specific device
     */
    public void triggerDeviceMonitoring(Long deviceId, User user) {
        Device device = deviceRepository.findByIdAndUser(deviceId, user)
            .orElseThrow(() -> new IllegalArgumentException("Device not found"));
        
        monitorDevice(device);
    }
}
