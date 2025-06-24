package com.farukgenc.boilerplate.springboot.service.snmp;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceConfig;
import com.farukgenc.boilerplate.springboot.model.UdpProfile;
import com.farukgenc.boilerplate.springboot.repository.UdpProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snmp4j.smi.Variable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UdpProfilePollServiceTest {

    @Mock
    private SnmpClientService snmpClientService;

    @Mock
    private UdpProfileRepository udpProfileRepository;

    @InjectMocks
    private UdpProfilePollService udpProfilePollService;

    private Device device;
    private DeviceConfig config;
    private UdpProfile existingProfile;

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
                .build();

        existingProfile = UdpProfile.builder()
                .id(1L)
                .device(device)
                .udpInDatagrams(1000L)
                .udpInErrors(2L)
                .udpEntryStatus(UdpProfile.UdpEntryStatus.VALID)
                .build();
    }

    @Test
    void testPollDeviceUdpProfile_Success() {
        // Arrange
        Map<String, Variable> udpData = createMockUdpData();
        when(snmpClientService.snmpGetMultiple(eq(config), any())).thenReturn(udpData);
        when(udpProfileRepository.findByDeviceId(device.getId())).thenReturn(Optional.of(existingProfile));
        when(udpProfileRepository.save(any(UdpProfile.class))).thenReturn(existingProfile);

        // Act
        udpProfilePollService.pollDeviceUdpProfile(device, config);

        // Assert
        verify(snmpClientService, atLeastOnce()).snmpGetMultiple(eq(config), any());
        verify(udpProfileRepository).findByDeviceId(device.getId());
        verify(udpProfileRepository).save(any(UdpProfile.class));
    }

    @Test
    void testPollDeviceUdpProfile_NewProfile() {
        // Arrange
        Map<String, Variable> udpData = createMockUdpData();
        when(snmpClientService.snmpGetMultiple(eq(config), any())).thenReturn(udpData);
        when(udpProfileRepository.findByDeviceId(device.getId())).thenReturn(Optional.empty());
        when(udpProfileRepository.save(any(UdpProfile.class))).thenReturn(existingProfile);

        // Act
        udpProfilePollService.pollDeviceUdpProfile(device, config);

        // Assert
        verify(snmpClientService, atLeastOnce()).snmpGetMultiple(eq(config), any());
        verify(udpProfileRepository).findByDeviceId(device.getId());
        verify(udpProfileRepository).save(any(UdpProfile.class));
    }

    @Test
    void testPollDeviceUdpProfile_EmptyData() {
        // Arrange
        when(snmpClientService.snmpGetMultiple(eq(config), any())).thenReturn(new HashMap<>());

        // Act
        udpProfilePollService.pollDeviceUdpProfile(device, config);

        // Assert
        verify(snmpClientService).snmpGetMultiple(eq(config), any());
        verify(udpProfileRepository, never()).save(any());
    }

    @Test
    void testPollDeviceUdpProfile_SnmpException() {
        // Arrange
        when(snmpClientService.snmpGetMultiple(eq(config), any())).thenThrow(new RuntimeException("SNMP error"));

        // Act & Assert
        try {
            udpProfilePollService.pollDeviceUdpProfile(device, config);
        } catch (RuntimeException e) {
            // Expected
        }

        verify(snmpClientService).snmpGetMultiple(eq(config), any());
        verify(udpProfileRepository, never()).save(any());
    }

    @Test
    void testPollDeviceUdpProfile_WithConnectionInfo() {
        // Arrange
        Map<String, Variable> udpData = createMockUdpData();
        Map<String, Variable> connectionData = createMockConnectionData();
        
        when(snmpClientService.snmpGetMultiple(eq(config), any()))
            .thenReturn(udpData)
            .thenReturn(connectionData);
        when(udpProfileRepository.findByDeviceId(device.getId())).thenReturn(Optional.of(existingProfile));
        when(udpProfileRepository.save(any(UdpProfile.class))).thenReturn(existingProfile);

        // Act
        udpProfilePollService.pollDeviceUdpProfile(device, config);

        // Assert
        verify(snmpClientService, times(2)).snmpGetMultiple(eq(config), any());
        verify(udpProfileRepository).findByDeviceId(device.getId());
        verify(udpProfileRepository).save(any(UdpProfile.class));
    }

    @Test
    void testPollDeviceUdpProfile_ConnectionInfoFailure() {
        // Arrange
        Map<String, Variable> udpData = createMockUdpData();
        
        when(snmpClientService.snmpGetMultiple(eq(config), any()))
            .thenReturn(udpData)
            .thenThrow(new RuntimeException("Connection info error"));
        when(udpProfileRepository.findByDeviceId(device.getId())).thenReturn(Optional.of(existingProfile));
        when(udpProfileRepository.save(any(UdpProfile.class))).thenReturn(existingProfile);

        // Act
        udpProfilePollService.pollDeviceUdpProfile(device, config);

        // Assert
        verify(snmpClientService, times(2)).snmpGetMultiple(eq(config), any());
        verify(udpProfileRepository).findByDeviceId(device.getId());
        verify(udpProfileRepository).save(any(UdpProfile.class));
    }

    private Map<String, Variable> createMockUdpData() {
        Map<String, Variable> data = new HashMap<>();
        
        Variable udpInDatagrams = mock(Variable.class);
        when(udpInDatagrams.toLong()).thenReturn(1200L);
        data.put("1.3.6.1.2.1.7.1.0", udpInDatagrams);
        
        Variable udpNoPorts = mock(Variable.class);
        when(udpNoPorts.toLong()).thenReturn(15L);
        data.put("1.3.6.1.2.1.7.2.0", udpNoPorts);
        
        Variable udpInErrors = mock(Variable.class);
        when(udpInErrors.toLong()).thenReturn(3L);
        data.put("1.3.6.1.2.1.7.3.0", udpInErrors);
        
        Variable udpOutDatagrams = mock(Variable.class);
        when(udpOutDatagrams.toLong()).thenReturn(1100L);
        data.put("1.3.6.1.2.1.7.4.0", udpOutDatagrams);

        return data;
    }

    private Map<String, Variable> createMockConnectionData() {
        Map<String, Variable> data = new HashMap<>();
        
        Variable localAddress = mock(Variable.class);
        when(localAddress.toString()).thenReturn("192.168.1.10");
        data.put("1.3.6.1.2.1.7.5.1.1.1", localAddress);
        
        Variable localPort = mock(Variable.class);
        when(localPort.toInt()).thenReturn(53);
        data.put("1.3.6.1.2.1.7.5.1.2.1", localPort);

        return data;
    }
}
