package com.farukgenc.boilerplate.springboot.service.snmp;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceConfig;
import com.farukgenc.boilerplate.springboot.model.SystemUnit;
import com.farukgenc.boilerplate.springboot.repository.SystemUnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snmp4j.smi.Variable;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemUnitPollServiceTest {

    @Mock
    private SnmpClientService snmpClientService;

    @Mock
    private SystemUnitRepository systemUnitRepository;

    @InjectMocks
    private SystemUnitPollService systemUnitPollService;

    private Device device;
    private DeviceConfig config;
    private SystemUnit existingUnit;

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
                .build();        existingUnit = SystemUnit.builder()
                .id(1L)
                .device(device)
                .unitIndex(1)
                .unitName("CPU 1")
                .unitType("Processor")
                .unitDescription("Intel CPU")
                .build();
    }

    @Test
    void testPollDeviceSystemUnits_Success() {        // Arrange
        Map<String, Variable> systemData = createMockSystemUnitData();
        when(snmpClientService.snmpWalk(eq(config), anyString(), anyInt())).thenReturn(systemData);
        when(systemUnitRepository.findByDeviceAndUnitIndex(device, 1)).thenReturn(Optional.of(existingUnit));
        when(systemUnitRepository.save(any(SystemUnit.class))).thenReturn(existingUnit);

        // Act
        systemUnitPollService.pollDeviceSystemUnits(device, config);

        // Assert
        verify(snmpClientService, times(4)).snmpWalk(eq(config), anyString(), anyInt()); // 4 walks for different tables
        verify(systemUnitRepository).findByDeviceAndUnitIndex(device, 1);
        verify(systemUnitRepository).save(any(SystemUnit.class));
    }    @Test
    void testPollDeviceSystemUnits_NewUnit() {
        // Arrange
        Map<String, Variable> systemData = createMockSystemUnitData();
        when(snmpClientService.snmpWalk(eq(config), anyString(), anyInt())).thenReturn(systemData);
        when(systemUnitRepository.findByDeviceAndUnitIndex(device, 1)).thenReturn(Optional.empty());
        when(systemUnitRepository.save(any(SystemUnit.class))).thenReturn(existingUnit);

        // Act
        systemUnitPollService.pollDeviceSystemUnits(device, config);

        // Assert
        verify(snmpClientService, times(4)).snmpWalk(eq(config), anyString(), anyInt());
        verify(systemUnitRepository).findByDeviceAndUnitIndex(device, 1);
        verify(systemUnitRepository).save(any(SystemUnit.class));
    }

    @Test
    void testPollDeviceSystemUnits_EmptyData() {
        // Arrange
        when(snmpClientService.snmpWalk(eq(config), anyString(), anyInt())).thenReturn(new HashMap<>());

        // Act
        systemUnitPollService.pollDeviceSystemUnits(device, config);

        // Assert
        verify(snmpClientService, times(4)).snmpWalk(eq(config), anyString(), anyInt());
        verify(systemUnitRepository, never()).save(any());
    }

    @Test
    void testPollDeviceSystemUnits_SnmpException() {
        // Arrange
        when(snmpClientService.snmpWalk(eq(config), anyString(), anyInt())).thenThrow(new RuntimeException("SNMP error"));

        // Act & Assert
        try {
            systemUnitPollService.pollDeviceSystemUnits(device, config);
        } catch (RuntimeException e) {
            // Expected
        }

        verify(snmpClientService).snmpWalk(eq(config), anyString(), anyInt());
        verify(systemUnitRepository, never()).save(any());
    }

    @Test
    void testPollDeviceSystemUnits_PartialData() {
        // Arrange
        Map<String, Variable> partialData = new HashMap<>();
        Variable mockName = mock(Variable.class);
        when(mockName.toString()).thenReturn("CPU 2");
        partialData.put("1.3.6.1.2.1.47.1.1.1.1.7.1", mockName); // hrDeviceDescr.1

        when(snmpClientService.snmpWalk(eq(config), anyString(), anyInt()))
            .thenReturn(partialData)
            .thenReturn(new HashMap<>())
            .thenReturn(new HashMap<>())
            .thenReturn(new HashMap<>());
        
        when(systemUnitRepository.findByDeviceAndUnitIndex(device, 1)).thenReturn(Optional.of(existingUnit));
        when(systemUnitRepository.save(any(SystemUnit.class))).thenReturn(existingUnit);

        // Act
        systemUnitPollService.pollDeviceSystemUnits(device, config);

        // Assert
        verify(snmpClientService, times(4)).snmpWalk(eq(config), anyString(), anyInt());
        verify(systemUnitRepository).findByDeviceAndUnitIndex(device, 1);
        verify(systemUnitRepository).save(any(SystemUnit.class));
    }

    @Test
    void testPollDeviceSystemUnits_MultipleUnits() {
        // Arrange
        Map<String, Variable> systemData = createMockMultipleUnitsData();
        when(snmpClientService.snmpWalk(eq(config), anyString(), anyInt())).thenReturn(systemData);
        when(systemUnitRepository.findByDeviceAndUnitIndex(device, 1)).thenReturn(Optional.of(existingUnit));
        when(systemUnitRepository.findByDeviceAndUnitIndex(device, 2)).thenReturn(Optional.empty());
        when(systemUnitRepository.save(any(SystemUnit.class))).thenReturn(existingUnit);

        // Act
        systemUnitPollService.pollDeviceSystemUnits(device, config);

        // Assert
        verify(snmpClientService, times(4)).snmpWalk(eq(config), anyString(), anyInt());
        verify(systemUnitRepository).findByDeviceAndUnitIndex(device, 1);
        verify(systemUnitRepository).findByDeviceAndUnitIndex(device, 2);
        verify(systemUnitRepository, times(2)).save(any(SystemUnit.class));
    }

    private Map<String, Variable> createMockSystemUnitData() {
        Map<String, Variable> data = new HashMap<>();
        
        // hrDeviceDescr.1
        Variable deviceDescr = mock(Variable.class);
        when(deviceDescr.toString()).thenReturn("Intel CPU");
        data.put("1.3.6.1.2.1.47.1.1.1.1.7.1", deviceDescr);
        
        // hrDeviceType.1 
        Variable deviceType = mock(Variable.class);
        when(deviceType.toInt()).thenReturn(3); // hrDeviceProcessor
        data.put("1.3.6.1.2.1.25.3.2.1.2.1", deviceType);
        
        // hrDeviceStatus.1
        Variable deviceStatus = mock(Variable.class);
        when(deviceStatus.toInt()).thenReturn(2); // running
        data.put("1.3.6.1.2.1.25.3.2.1.5.1", deviceStatus);
        
        // hrProcessorLoad.1
        Variable processorLoad = mock(Variable.class);
        when(processorLoad.toInt()).thenReturn(25);
        data.put("1.3.6.1.2.1.25.3.3.1.2.1", processorLoad);

        return data;
    }

    private Map<String, Variable> createMockMultipleUnitsData() {
        Map<String, Variable> data = new HashMap<>();
        
        // Unit 1
        Variable deviceDescr1 = mock(Variable.class);
        when(deviceDescr1.toString()).thenReturn("Intel CPU");
        data.put("1.3.6.1.2.1.47.1.1.1.1.7.1", deviceDescr1);
        
        Variable deviceType1 = mock(Variable.class);
        when(deviceType1.toInt()).thenReturn(3);
        data.put("1.3.6.1.2.1.25.3.2.1.2.1", deviceType1);
        
        Variable deviceStatus1 = mock(Variable.class);
        when(deviceStatus1.toInt()).thenReturn(2);
        data.put("1.3.6.1.2.1.25.3.2.1.5.1", deviceStatus1);
        
        // Unit 2
        Variable deviceDescr2 = mock(Variable.class);
        when(deviceDescr2.toString()).thenReturn("Network Interface");
        data.put("1.3.6.1.2.1.47.1.1.1.1.7.2", deviceDescr2);
        
        Variable deviceType2 = mock(Variable.class);
        when(deviceType2.toInt()).thenReturn(6); // hrDeviceNetwork
        data.put("1.3.6.1.2.1.25.3.2.1.2.2", deviceType2);
        
        Variable deviceStatus2 = mock(Variable.class);
        when(deviceStatus2.toInt()).thenReturn(2);
        data.put("1.3.6.1.2.1.25.3.2.1.5.2", deviceStatus2);

        return data;
    }
}
