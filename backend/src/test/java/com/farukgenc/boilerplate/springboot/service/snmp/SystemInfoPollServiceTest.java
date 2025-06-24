package com.farukgenc.boilerplate.springboot.service.snmp;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceConfig;
import com.farukgenc.boilerplate.springboot.model.SystemInfo;
import com.farukgenc.boilerplate.springboot.repository.SystemInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.TimeTicks;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SystemInfoPollServiceTest {

    @Mock
    private SnmpClientService snmpClientService;

    @Mock
    private SystemInfoRepository systemInfoRepository;

    @InjectMocks
    private SystemInfoPollService systemInfoPollService;

    private Device testDevice;
    private DeviceConfig testConfig;

    @BeforeEach
    void setUp() {
        testDevice = Device.builder()
            .id(1L)
            .name("Test Device")
            .build();

        testConfig = DeviceConfig.builder()
            .id(1L)
            .device(testDevice)
            .targetIp("192.168.1.1")
            .snmpPort(161)
            .communityString("public")
            .build();
    }

    @Test
    void testPollDeviceSystemInfo_Success() {
        // Arrange
        Map<String, Variable> systemData = new HashMap<>();
        systemData.put("1.3.6.1.2.1.1.1.0", new OctetString("Linux Test Device 5.4.0"));
        systemData.put("1.3.6.1.2.1.1.2.0", new OctetString("1.3.6.1.4.1.8072.3.2.10"));
        systemData.put("1.3.6.1.2.1.1.3.0", new TimeTicks(123456));
        systemData.put("1.3.6.1.2.1.1.4.0", new OctetString("admin@example.com"));
        systemData.put("1.3.6.1.2.1.1.5.0", new OctetString("test-device"));
        systemData.put("1.3.6.1.2.1.1.6.0", new OctetString("Data Center"));
        systemData.put("1.3.6.1.2.1.1.7.0", new Integer32(72));

        when(snmpClientService.snmpGetMultiple(eq(testConfig), anyList()))
            .thenReturn(systemData);
        when(systemInfoRepository.findByDeviceId(testDevice.getId()))
            .thenReturn(Optional.empty());

        // Act
        systemInfoPollService.pollDeviceSystemInfo(testDevice, testConfig);

        // Assert
        verify(snmpClientService).snmpGetMultiple(eq(testConfig), anyList());
        verify(systemInfoRepository).save(any(SystemInfo.class));
    }

    @Test
    void testPollDeviceSystemInfo_NoData() {
        // Arrange
        when(snmpClientService.snmpGetMultiple(eq(testConfig), anyList()))
            .thenReturn(new HashMap<>());

        // Act
        systemInfoPollService.pollDeviceSystemInfo(testDevice, testConfig);

        // Assert
        verify(snmpClientService).snmpGetMultiple(eq(testConfig), anyList());
        verify(systemInfoRepository, never()).save(any(SystemInfo.class));
    }

    @Test
    void testPollDeviceSystemInfo_UpdateExisting() {
        // Arrange
        SystemInfo existingSystemInfo = SystemInfo.builder()
            .id(1L)
            .device(testDevice)
            .sysDescr("Old Description")
            .build();

        Map<String, Variable> systemData = new HashMap<>();
        systemData.put("1.3.6.1.2.1.1.1.0", new OctetString("Updated Description"));
        systemData.put("1.3.6.1.2.1.1.5.0", new OctetString("updated-name"));

        when(snmpClientService.snmpGetMultiple(eq(testConfig), anyList()))
            .thenReturn(systemData);
        when(systemInfoRepository.findByDeviceId(testDevice.getId()))
            .thenReturn(Optional.of(existingSystemInfo));

        // Act
        systemInfoPollService.pollDeviceSystemInfo(testDevice, testConfig);

        // Assert
        verify(systemInfoRepository).save(eq(existingSystemInfo));
        assertEquals("Updated Description", existingSystemInfo.getSysDescr());
        assertEquals("updated-name", existingSystemInfo.getSysName());
    }

    @Test
    void testPollDeviceSystemInfo_Exception() {
        // Arrange
        when(snmpClientService.snmpGetMultiple(eq(testConfig), anyList()))
            .thenThrow(new RuntimeException("SNMP error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            systemInfoPollService.pollDeviceSystemInfo(testDevice, testConfig);
        });
    }

    @Test
    void testPollDeviceSystemInfo_PartialData() {
        // Arrange
        Map<String, Variable> systemData = new HashMap<>();
        systemData.put("1.3.6.1.2.1.1.1.0", new OctetString("Partial Description"));
        // Missing other fields to test partial updates

        when(snmpClientService.snmpGetMultiple(eq(testConfig), anyList()))
            .thenReturn(systemData);
        when(systemInfoRepository.findByDeviceId(testDevice.getId()))
            .thenReturn(Optional.empty());

        // Act
        systemInfoPollService.pollDeviceSystemInfo(testDevice, testConfig);

        // Assert
        verify(systemInfoRepository).save(any(SystemInfo.class));
    }
}
