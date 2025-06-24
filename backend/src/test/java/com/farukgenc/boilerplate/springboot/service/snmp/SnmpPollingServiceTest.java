package com.farukgenc.boilerplate.springboot.service.snmp;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceConfig;
import com.farukgenc.boilerplate.springboot.repository.DeviceConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnmpPollingServiceTest {

    @Mock
    private DeviceConfigRepository deviceConfigRepository;

    @Mock
    private SnmpClientService snmpClientService;

    @Mock
    private SystemInfoPollService systemInfoPollService;

    @Mock
    private SystemUnitPollService systemUnitPollService;

    @Mock
    private InterfacePollService interfacePollService;

    @Mock
    private IpProfilePollService ipProfilePollService;

    @Mock
    private IcmpProfilePollService icmpProfilePollService;

    @Mock
    private UdpProfilePollService udpProfilePollService;

    @InjectMocks
    private SnmpPollingService snmpPollingService;

    private Device device;
    private DeviceConfig config;

    @BeforeEach
    void setUp() {
        device = Device.builder()
                .id(1L)
                .name("Test Device")
                .description("Test Description")
                .build();

        config = DeviceConfig.builder()
                .id(1L)
                .device(device)
                .targetIp("192.168.1.1")
                .snmpPort(161)
                .communityString("public")
                .enabled(true)
                .pollInterval(300)
                .consecutiveFailures(0)
                .lastPollStatus(DeviceConfig.PollStatus.SUCCESS)
                .build();
    }

    @Test
    void testScheduledPollAllDevices_Success() {
        // Arrange
        when(deviceConfigRepository.findByEnabledTrue()).thenReturn(Arrays.asList(config));

        // Act
        snmpPollingService.scheduledPollAllDevices();

        // Assert
        verify(deviceConfigRepository).findByEnabledTrue();
    }

    @Test
    void testScheduledPollAllDevices_NoEnabledDevices() {
        // Arrange
        when(deviceConfigRepository.findByEnabledTrue()).thenReturn(Collections.emptyList());

        // Act
        snmpPollingService.scheduledPollAllDevices();

        // Assert
        verify(deviceConfigRepository).findByEnabledTrue();
    }

    @Test
    void testPollDevice_Success() {
        // Arrange
        config.setLastPollTime(LocalDateTime.now().minusMinutes(10)); // Make it eligible for polling
        when(snmpClientService.testConnection(config)).thenReturn(true);
        when(deviceConfigRepository.save(any(DeviceConfig.class))).thenReturn(config);

        // Act
        snmpPollingService.pollDevice(config);

        // Assert
        verify(snmpClientService).testConnection(config);
        verify(systemInfoPollService).pollDeviceSystemInfo(device, config);
        verify(interfacePollService).pollDeviceInterfaces(device, config);
        verify(systemUnitPollService).pollDeviceSystemUnits(device, config);
        verify(ipProfilePollService).pollDeviceIpProfile(device, config);
        verify(icmpProfilePollService).pollDeviceIcmpProfile(device, config);
        verify(udpProfilePollService).pollDeviceUdpProfile(device, config);
        verify(deviceConfigRepository).save(config);
    }

    @Test
    void testPollDevice_ConnectionFailure() {
        // Arrange
        config.setLastPollTime(LocalDateTime.now().minusMinutes(10));
        when(snmpClientService.testConnection(config)).thenReturn(false);
        when(deviceConfigRepository.save(any(DeviceConfig.class))).thenReturn(config);

        // Act
        snmpPollingService.pollDevice(config);

        // Assert
        verify(snmpClientService).testConnection(config);
        verify(systemInfoPollService, never()).pollDeviceSystemInfo(any(), any());
        verify(deviceConfigRepository).save(config);
    }

    @Test
    void testPollDevice_SkipDueToInterval() {
        // Arrange
        config.setLastPollTime(LocalDateTime.now().minusSeconds(30)); // Too recent
        config.setPollInterval(300); // 5 minutes

        // Act
        snmpPollingService.pollDevice(config);

        // Assert
        verify(snmpClientService, never()).testConnection(any());
        verify(systemInfoPollService, never()).pollDeviceSystemInfo(any(), any());
    }

    @Test
    void testPollDevice_SystemInfoException() {
        // Arrange
        config.setLastPollTime(LocalDateTime.now().minusMinutes(10));
        when(snmpClientService.testConnection(config)).thenReturn(true);
        doThrow(new RuntimeException("System info error")).when(systemInfoPollService).pollDeviceSystemInfo(device, config);
        when(deviceConfigRepository.save(any(DeviceConfig.class))).thenReturn(config);

        // Act
        snmpPollingService.pollDevice(config);

        // Assert
        verify(snmpClientService).testConnection(config);
        verify(systemInfoPollService).pollDeviceSystemInfo(device, config);
        verify(interfacePollService).pollDeviceInterfaces(device, config); // Should continue despite error
        verify(deviceConfigRepository).save(config);
    }

    @Test
    void testPollDeviceById_Found() {
        // Arrange
        when(deviceConfigRepository.findById(1L)).thenReturn(Optional.of(config));
        config.setLastPollTime(LocalDateTime.now().minusMinutes(10));
        when(snmpClientService.testConnection(config)).thenReturn(true);
        when(deviceConfigRepository.save(any(DeviceConfig.class))).thenReturn(config);

        // Act
        snmpPollingService.pollDeviceById(1L);

        // Assert
        verify(deviceConfigRepository).findById(1L);
        verify(snmpClientService).testConnection(config);
        verify(systemInfoPollService).pollDeviceSystemInfo(device, config);
    }

    @Test
    void testPollDeviceById_NotFound() {
        // Arrange
        when(deviceConfigRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        try {
            snmpPollingService.pollDeviceById(1L);
        } catch (RuntimeException e) {
            // Expected
        }

        verify(deviceConfigRepository).findById(1L);
        verify(snmpClientService, never()).testConnection(any());
    }

    @Test
    void testPollDeviceAsync_Success() {
        // Arrange
        config.setLastPollTime(LocalDateTime.now().minusMinutes(10));
        when(snmpClientService.testConnection(config)).thenReturn(true);
        when(deviceConfigRepository.save(any(DeviceConfig.class))).thenReturn(config);

        // Act
        snmpPollingService.pollDeviceAsync(config);

        // Assert
        verify(snmpClientService).testConnection(config);
        verify(systemInfoPollService).pollDeviceSystemInfo(device, config);
        verify(deviceConfigRepository).save(config);
    }    @Test
    void testGetPollingStatistics() {
        // Arrange
        when(deviceConfigRepository.count()).thenReturn(5L);
        when(deviceConfigRepository.findByEnabledTrue()).thenReturn(Arrays.asList(config, config, config));
        when(deviceConfigRepository.findByLastPollStatus(DeviceConfig.PollStatus.SUCCESS)).thenReturn(Arrays.asList(config, config));        // Act
        snmpPollingService.getPollingStatistics();

        // Assert
        verify(deviceConfigRepository).count();
        verify(deviceConfigRepository).findByEnabledTrue();
        verify(deviceConfigRepository).findByLastPollStatus(DeviceConfig.PollStatus.SUCCESS);
    }
}
