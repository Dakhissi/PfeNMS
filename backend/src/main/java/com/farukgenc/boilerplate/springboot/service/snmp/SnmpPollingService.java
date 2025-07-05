package com.farukgenc.boilerplate.springboot.service.snmp;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceConfig;
import com.farukgenc.boilerplate.springboot.model.SystemInfo;
import com.farukgenc.boilerplate.springboot.repository.DeviceConfigRepository;
import com.farukgenc.boilerplate.springboot.repository.DeviceRepository;
import com.farukgenc.boilerplate.springboot.repository.SystemInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Main SNMP polling orchestrator service that coordinates all polling activities
 */
@Slf4j
@Service("snmpPollingService")
@RequiredArgsConstructor
public class SnmpPollingService {

    private final DeviceRepository deviceRepository;
    private final DeviceConfigRepository deviceConfigRepository;
    private final SnmpClientService snmpClientService;
    private final SystemInfoPollService systemInfoPollService;
    private final SystemInfoRepository  systemInfoRepository;
    private final SystemUnitPollService systemUnitPollService;
    private final InterfacePollService interfacePollService;
    private final IpProfilePollService ipProfilePollService;
    private final IcmpProfilePollService icmpProfilePollService;
    private final UdpProfilePollService udpProfilePollService;

    /**
     * Scheduled method to poll all enabled devices
     * Runs every 30 seconds for real-time monitoring
     */
    @Scheduled(fixedRate = 30000) // 30 seconds
    @Transactional
    public void scheduledPollAllDevices() {
        log.info("Starting scheduled SNMP polling for all devices");
        try {
            // Use join fetch to eagerly load device relationships
            List<DeviceConfig> enabledConfigs = deviceConfigRepository.findByEnabledTrueWithDevice();

            if (enabledConfigs.isEmpty()) {
                log.info("No enabled device configurations found for polling");
                return;
            }
            
            // Filter configs for devices that have monitoring enabled
            List<DeviceConfig> monitoringEnabledConfigs = enabledConfigs.stream()
                .filter(config -> config.getDevice().getMonitoringEnabled() != null && config.getDevice().getMonitoringEnabled())
                .toList();

            if (monitoringEnabledConfigs.isEmpty()) {
                log.info("No devices with monitoring enabled found for polling");
                return;
            }

            log.info("Found {} enabled device configurations with monitoring enabled to poll", monitoringEnabledConfigs.size());

            // Poll each device synchronously to avoid transaction conflicts
            for (DeviceConfig config : monitoringEnabledConfigs) {
                try {
                    pollDevice(config);
                } catch (Exception e) {
                    log.error("Error polling device {}: {}", config.getDevice().getName(), e.getMessage(), e);
                }
            }

            log.info("Completed polling of {} devices", monitoringEnabledConfigs.size());

        } catch (Exception e) {
            log.error("Error during scheduled polling: {}", e.getMessage(), e);
        }
    }

    /**
     * Poll a single device asynchronously - for manual triggering
     */
    @Async("snmpTaskExecutor")
    public CompletableFuture<Void> pollDeviceAsync(DeviceConfig config) {
        return CompletableFuture.runAsync(() -> pollDevice(config));
    }

    /**
     * Poll all data for a single device
     */
    @Transactional
    public void pollDevice(DeviceConfig config) {
        Device device = config.getDevice();

        // CRITICAL: Check if monitoring is enabled for this device
        if (device.getMonitoringEnabled() == null || !device.getMonitoringEnabled()) {
            log.debug("Monitoring is disabled for device: {} - skipping polling", device.getName());
            return;
        }

        log.debug("Starting SNMP polling for device: {} ({})", device.getName(), config.getTargetIp());
        
        try {
            // Check if we should poll based on interval
            if (!shouldPollDevice(config)) {
                log.debug("Skipping device {} - not time to poll yet", device.getName());
                return;
            }
            
            // Test connectivity first
            if (!snmpClientService.testConnection(config)) {
                handlePollingFailure(config, "SNMP connectivity test failed");
                return;
            }
            
            // Update last poll time and reset error count
            config.setLastPollTime(LocalDateTime.now());
            config.setConsecutiveFailures(0);
            config.setLastPollStatus(DeviceConfig.PollStatus.SUCCESS);
            config.setErrorMessage(null);
            
            // Poll system information
            try {
                SystemInfo sysinfo = systemInfoPollService.pollDeviceSystemInfo(device, config);
                log.debug("System info polling completed for device: {}", device.getName());
            } catch (Exception e) {
                log.warn("System info polling failed for device {}: {}", device.getName(), e.getMessage());
            }
            
            // Poll interfaces
            try {
                interfacePollService.pollDeviceInterfaces(device, config);
                log.debug("Interface polling completed for device: {}", device.getName());
            } catch (Exception e) {
                log.warn("Interface polling failed for device {}: {}", device.getName(), e.getMessage());
            }
            
            // Poll system units
            try {
                systemUnitPollService.pollDeviceSystemUnits(device, config);
                log.debug("System unit polling completed for device: {}", device.getName());
            } catch (Exception e) {
                log.warn("System unit polling failed for device {}: {}", device.getName(), e.getMessage());
            }
            
            // Poll IP profile
            try {
                ipProfilePollService.pollDeviceIpProfile(device, config);
                log.debug("IP profile polling completed for device: {}", device.getName());
            } catch (Exception e) {
                log.warn("IP profile polling failed for device {}: {}", device.getName(), e.getMessage());
            }
            
            // Poll ICMP profile
            try {
                icmpProfilePollService.pollDeviceIcmpProfile(device, config);
                log.debug("ICMP profile polling completed for device: {}", device.getName());
            } catch (Exception e) {
                log.warn("ICMP profile polling failed for device {}: {}", device.getName(), e.getMessage());
            }
            
            // Poll UDP profile
            try {
                udpProfilePollService.pollDeviceUdpProfile(device, config);
                log.debug("UDP profile polling completed for device: {}", device.getName());
            } catch (Exception e) {
                log.warn("UDP profile polling failed for device {}: {}", device.getName(), e.getMessage());
            }
            
            // Save updated config
            deviceConfigRepository.save(config);
            
            log.info("SNMP polling completed successfully for device: {} ({})", 
                device.getName(), config.getTargetIp());
            
        } catch (Exception e) {
            handlePollingFailure(config, "Polling failed: " + e.getMessage());
            log.error("SNMP polling failed for device {}: {}", device.getName(), e.getMessage(), e);
        }
    }

    /**
     * Poll a specific device by ID
     */
    @Transactional
    public void pollDeviceById(Long deviceId) {
        log.info("Starting on-demand polling for device ID: {}", deviceId);
        
        DeviceConfig config = deviceConfigRepository.findByDeviceId(deviceId)
            .orElseThrow(() -> new RuntimeException("Device configuration not found for device ID: " + deviceId));
        
        pollDevice(config);
    }

    /**
     * Poll multiple devices by their IDs
     */
    @Transactional
    public void pollDevicesByIds(List<Long> deviceIds) {
        log.info("Starting on-demand polling for {} devices", deviceIds.size());
        
        for (Long deviceId : deviceIds) {
            try {
                pollDeviceById(deviceId);
            } catch (Exception e) {
                log.error("Failed to poll device ID {}: {}", deviceId, e.getMessage());
            }
        }
    }

    /**
     * Test SNMP connectivity for a device
     */
    @Transactional(readOnly = true)
    public boolean testDeviceConnectivity(Long deviceId) {
        DeviceConfig config = deviceConfigRepository.findByDeviceId(deviceId)
            .orElseThrow(() -> new RuntimeException("Device configuration not found for device ID: " + deviceId));
        
        return snmpClientService.testConnection(config);
    }

    /**
     * Check if a device should be polled based on its poll interval
     */
    private boolean shouldPollDevice(DeviceConfig config) {
        if (config.getLastPollTime() == null) {
            return true; // Never polled before
        }
        
        LocalDateTime nextPollTime = config.getLastPollTime().plusSeconds(config.getPollInterval());
        return LocalDateTime.now().isAfter(nextPollTime);
    }

    /**
     * Handle polling failure and update device config
     */
    private void handlePollingFailure(DeviceConfig config, String errorMessage) {
        config.setLastPollTime(LocalDateTime.now());
        config.setLastPollStatus(DeviceConfig.PollStatus.FAILURE);
        config.setErrorMessage(errorMessage);
        config.setConsecutiveFailures(config.getConsecutiveFailures() + 1);
        
        // Disable device after 5 consecutive failures
        if (config.getConsecutiveFailures() >= 5) {
            config.setEnabled(false);
            log.warn("Disabled device {} after {} consecutive failures", 
                config.getDevice().getName(), config.getConsecutiveFailures());
        }
        
        deviceConfigRepository.save(config);
    }

    /**
     * Get polling statistics for all devices
     */
    @Transactional(readOnly = true)
    public PollingStatistics getPollingStatistics() {
        List<DeviceConfig> allConfigs = deviceConfigRepository.findAll();
        
        long totalDevices = allConfigs.size();
        long enabledDevices = allConfigs.stream().mapToLong(config -> config.getEnabled() ? 1 : 0).sum();
        long successfulPolls = allConfigs.stream()
            .mapToLong(config -> config.getLastPollStatus() == DeviceConfig.PollStatus.SUCCESS ? 1 : 0).sum();
        long failedPolls = allConfigs.stream()
            .mapToLong(config -> config.getLastPollStatus() == DeviceConfig.PollStatus.FAILURE ? 1 : 0).sum();
        
        return PollingStatistics.builder()
            .totalDevices(totalDevices)
            .enabledDevices(enabledDevices)
            .successfulPolls(successfulPolls)
            .failedPolls(failedPolls)
            .lastPollingRun(LocalDateTime.now())
            .build();
    }



    /**
     * Statistics for polling operations
     */
    @lombok.Data
    @lombok.Builder
    public static class PollingStatistics {
        private long totalDevices;
        private long enabledDevices;
        private long successfulPolls;
        private long failedPolls;
        private LocalDateTime lastPollingRun;
    }
}
