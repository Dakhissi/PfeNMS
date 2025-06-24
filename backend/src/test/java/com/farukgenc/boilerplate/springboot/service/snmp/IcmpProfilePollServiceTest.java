package com.farukgenc.boilerplate.springboot.service.snmp;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceConfig;
import com.farukgenc.boilerplate.springboot.model.IcmpProfile;
import com.farukgenc.boilerplate.springboot.repository.IcmpProfileRepository;
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
class IcmpProfilePollServiceTest {

    @Mock
    private SnmpClientService snmpClientService;

    @Mock
    private IcmpProfileRepository icmpProfileRepository;

    @InjectMocks
    private IcmpProfilePollService icmpProfilePollService;

    private Device device;
    private DeviceConfig config;
    private IcmpProfile existingProfile;

    @BeforeEach
    void setUp() {
        device = Device.builder()
                .id(1L)
                .name("Test Device")
                .description("Test Description")
                .build();        config = DeviceConfig.builder()
                .id(1L)
                .device(device)
                .targetIp("192.168.1.1")
                .snmpPort(161)
                .communityString("public")
                .enabled(true)
                .build();

        existingProfile = IcmpProfile.builder()
                .id(1L)
                .device(device)
                .icmpInMsgs(100L)
                .icmpInErrors(5L)
                .build();
    }

    @Test
    void testPollDeviceIcmpProfile_Success() {
        // Arrange
        Map<String, Variable> icmpData = createMockIcmpData();
        when(snmpClientService.snmpGetMultiple(eq(config), any())).thenReturn(icmpData);
        when(icmpProfileRepository.findByDeviceId(device.getId())).thenReturn(Optional.of(existingProfile));
        when(icmpProfileRepository.save(any(IcmpProfile.class))).thenReturn(existingProfile);

        // Act
        icmpProfilePollService.pollDeviceIcmpProfile(device, config);

        // Assert
        verify(snmpClientService).snmpGetMultiple(eq(config), any());
        verify(icmpProfileRepository).findByDeviceId(device.getId());
        verify(icmpProfileRepository).save(any(IcmpProfile.class));
    }

    @Test
    void testPollDeviceIcmpProfile_NewProfile() {
        // Arrange
        Map<String, Variable> icmpData = createMockIcmpData();
        when(snmpClientService.snmpGetMultiple(eq(config), any())).thenReturn(icmpData);
        when(icmpProfileRepository.findByDeviceId(device.getId())).thenReturn(Optional.empty());
        when(icmpProfileRepository.save(any(IcmpProfile.class))).thenReturn(existingProfile);

        // Act
        icmpProfilePollService.pollDeviceIcmpProfile(device, config);

        // Assert
        verify(snmpClientService).snmpGetMultiple(eq(config), any());
        verify(icmpProfileRepository).findByDeviceId(device.getId());
        verify(icmpProfileRepository).save(any(IcmpProfile.class));
    }

    @Test
    void testPollDeviceIcmpProfile_EmptyData() {
        // Arrange
        when(snmpClientService.snmpGetMultiple(eq(config), any())).thenReturn(new HashMap<>());

        // Act
        icmpProfilePollService.pollDeviceIcmpProfile(device, config);

        // Assert
        verify(snmpClientService).snmpGetMultiple(eq(config), any());
        verify(icmpProfileRepository, never()).save(any());
    }

    @Test
    void testPollDeviceIcmpProfile_SnmpException() {
        // Arrange
        when(snmpClientService.snmpGetMultiple(eq(config), any())).thenThrow(new RuntimeException("SNMP error"));

        // Act & Assert
        try {
            icmpProfilePollService.pollDeviceIcmpProfile(device, config);
        } catch (RuntimeException e) {
            // Expected
        }

        verify(snmpClientService).snmpGetMultiple(eq(config), any());
        verify(icmpProfileRepository, never()).save(any());
    }

    @Test
    void testPollDeviceIcmpProfile_PartialData() {
        // Arrange
        Map<String, Variable> partialData = new HashMap<>();
        Variable mockVariable = mock(Variable.class);
        when(mockVariable.toLong()).thenReturn(150L);
        partialData.put("1.3.6.1.2.1.5.1.0", mockVariable); // icmpInMsgs only

        when(snmpClientService.snmpGetMultiple(eq(config), any())).thenReturn(partialData);
        when(icmpProfileRepository.findByDeviceId(device.getId())).thenReturn(Optional.of(existingProfile));
        when(icmpProfileRepository.save(any(IcmpProfile.class))).thenReturn(existingProfile);

        // Act
        icmpProfilePollService.pollDeviceIcmpProfile(device, config);

        // Assert
        verify(snmpClientService).snmpGetMultiple(eq(config), any());
        verify(icmpProfileRepository).findByDeviceId(device.getId());
        verify(icmpProfileRepository).save(any(IcmpProfile.class));
    }

    private Map<String, Variable> createMockIcmpData() {
        Map<String, Variable> data = new HashMap<>();
        
        // Mock ICMP input statistics
        Variable icmpInMsgs = mock(Variable.class);
        when(icmpInMsgs.toLong()).thenReturn(150L);
        data.put("1.3.6.1.2.1.5.1.0", icmpInMsgs);
        
        Variable icmpInErrors = mock(Variable.class);
        when(icmpInErrors.toLong()).thenReturn(8L);
        data.put("1.3.6.1.2.1.5.2.0", icmpInErrors);
        
        Variable icmpInDestUnreachs = mock(Variable.class);
        when(icmpInDestUnreachs.toLong()).thenReturn(3L);
        data.put("1.3.6.1.2.1.5.3.0", icmpInDestUnreachs);
        
        Variable icmpInEchos = mock(Variable.class);
        when(icmpInEchos.toLong()).thenReturn(25L);
        data.put("1.3.6.1.2.1.5.8.0", icmpInEchos);
        
        Variable icmpInEchoReps = mock(Variable.class);
        when(icmpInEchoReps.toLong()).thenReturn(24L);
        data.put("1.3.6.1.2.1.5.9.0", icmpInEchoReps);
        
        // Mock ICMP output statistics
        Variable icmpOutMsgs = mock(Variable.class);
        when(icmpOutMsgs.toLong()).thenReturn(140L);
        data.put("1.3.6.1.2.1.5.14.0", icmpOutMsgs);
        
        Variable icmpOutErrors = mock(Variable.class);
        when(icmpOutErrors.toLong()).thenReturn(2L);
        data.put("1.3.6.1.2.1.5.15.0", icmpOutErrors);
        
        Variable icmpOutEchos = mock(Variable.class);
        when(icmpOutEchos.toLong()).thenReturn(30L);
        data.put("1.3.6.1.2.1.5.21.0", icmpOutEchos);
        
        Variable icmpOutEchoReps = mock(Variable.class);
        when(icmpOutEchoReps.toLong()).thenReturn(29L);
        data.put("1.3.6.1.2.1.5.22.0", icmpOutEchoReps);

        return data;
    }
}
