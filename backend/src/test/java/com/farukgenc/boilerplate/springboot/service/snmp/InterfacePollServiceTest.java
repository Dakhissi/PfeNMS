package com.farukgenc.boilerplate.springboot.service.snmp;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceConfig;
import com.farukgenc.boilerplate.springboot.model.DeviceInterface;
import com.farukgenc.boilerplate.springboot.repository.DeviceInterfaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Integer32;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InterfacePollServiceTest {

    @Mock
    private SnmpClientService snmpClientService;

    @Mock
    private DeviceInterfaceRepository deviceInterfaceRepository;

    @InjectMocks
    private InterfacePollService interfacePollService;

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
    void testPollDeviceInterfaces_Success() {
        // Arrange
        Map<String, Variable> interfaceIndices = new HashMap<>();
        interfaceIndices.put("1.3.6.1.2.1.2.2.1.1.1", new Integer32(1));
        interfaceIndices.put("1.3.6.1.2.1.2.2.1.1.2", new Integer32(2));

        Map<String, Variable> interfaceData = new HashMap<>();
        interfaceData.put("1.3.6.1.2.1.2.2.1.2.1", new OctetString("eth0"));
        interfaceData.put("1.3.6.1.2.1.2.2.1.3.1", new Integer32(6));
        interfaceData.put("1.3.6.1.2.1.2.2.1.4.1", new Integer32(1500));

        when(snmpClientService.snmpWalk(eq(testConfig), anyString(), anyInt()))
            .thenReturn(interfaceIndices);
        when(snmpClientService.snmpGetMultiple(eq(testConfig), anyList()))
            .thenReturn(interfaceData);
        when(deviceInterfaceRepository.findByDeviceId(testDevice.getId()))
            .thenReturn(new ArrayList<>());

        // Act
        interfacePollService.pollDeviceInterfaces(testDevice, testConfig);

        // Assert
        verify(snmpClientService).snmpWalk(eq(testConfig), eq("1.3.6.1.2.1.2.2.1.1"), eq(100));
        verify(snmpClientService, times(2)).snmpGetMultiple(eq(testConfig), anyList());
        verify(deviceInterfaceRepository).saveAll(anyList());
    }

    @Test
    void testPollDeviceInterfaces_NoInterfaces() {
        // Arrange
        when(snmpClientService.snmpWalk(eq(testConfig), anyString(), anyInt()))
            .thenReturn(new HashMap<>());

        // Act
        interfacePollService.pollDeviceInterfaces(testDevice, testConfig);

        // Assert
        verify(snmpClientService).snmpWalk(eq(testConfig), eq("1.3.6.1.2.1.2.2.1.1"), eq(100));
        verify(deviceInterfaceRepository, never()).saveAll(anyList());
    }

    @Test
    void testPollDeviceInterfaces_Exception() {
        // Arrange
        when(snmpClientService.snmpWalk(eq(testConfig), anyString(), anyInt()))
            .thenThrow(new RuntimeException("SNMP error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            interfacePollService.pollDeviceInterfaces(testDevice, testConfig);
        });
    }

    @Test
    void testPollDeviceInterfaces_RemoveStaleInterfaces() {
        // Arrange
        Map<String, Variable> interfaceIndices = new HashMap<>();
        interfaceIndices.put("1.3.6.1.2.1.2.2.1.1.1", new Integer32(1));

        DeviceInterface staleInterface = DeviceInterface.builder()
            .id(2L)
            .device(testDevice)
            .ifIndex(2)
            .ifDescr("eth1")
            .build();

        List<DeviceInterface> existingInterfaces = Arrays.asList(staleInterface);

        when(snmpClientService.snmpWalk(eq(testConfig), anyString(), anyInt()))
            .thenReturn(interfaceIndices);
        when(snmpClientService.snmpGetMultiple(eq(testConfig), anyList()))
            .thenReturn(new HashMap<>());
        when(deviceInterfaceRepository.findByDeviceId(testDevice.getId()))
            .thenReturn(existingInterfaces);

        // Act
        interfacePollService.pollDeviceInterfaces(testDevice, testConfig);

        // Assert
        verify(deviceInterfaceRepository).deleteAll(eq(Arrays.asList(staleInterface)));
    }

    @Test
    void testPollDeviceInterfaces_UpdateExistingInterface() {
        // Arrange
        Map<String, Variable> interfaceIndices = new HashMap<>();
        interfaceIndices.put("1.3.6.1.2.1.2.2.1.1.1", new Integer32(1));

        Map<String, Variable> interfaceData = new HashMap<>();
        interfaceData.put("1.3.6.1.2.1.2.2.1.2.1", new OctetString("eth0-updated"));
        interfaceData.put("1.3.6.1.2.1.2.2.1.3.1", new Integer32(6));

        DeviceInterface existingInterface = DeviceInterface.builder()
            .id(1L)
            .device(testDevice)
            .ifIndex(1)
            .ifDescr("eth0")
            .build();

        when(snmpClientService.snmpWalk(eq(testConfig), anyString(), anyInt()))
            .thenReturn(interfaceIndices);
        when(snmpClientService.snmpGetMultiple(eq(testConfig), anyList()))
            .thenReturn(interfaceData);
        when(deviceInterfaceRepository.findByDeviceIdAndIfIndex(testDevice.getId(), 1))
            .thenReturn(Optional.of(existingInterface));
        when(deviceInterfaceRepository.findByDeviceId(testDevice.getId()))
            .thenReturn(Arrays.asList(existingInterface));

        // Act
        interfacePollService.pollDeviceInterfaces(testDevice, testConfig);

        // Assert
        verify(deviceInterfaceRepository).saveAll(anyList());
        assertEquals("eth0-updated", existingInterface.getIfDescr());
    }
}
