package com.farukgenc.boilerplate.springboot.service;

import com.farukgenc.boilerplate.springboot.dto.DeviceDto;
import com.farukgenc.boilerplate.springboot.mapper.DeviceMapper;
import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.model.UserRole;
import com.farukgenc.boilerplate.springboot.repository.DeviceRepository;
import com.farukgenc.boilerplate.springboot.service.device.DeviceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceImplTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceMapper deviceMapper;

    @InjectMocks
    private DeviceServiceImpl deviceService;

    private User testUser;
    private Device testDevice;
    private DeviceDto testDeviceDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .name("Test User")
                .userRole(UserRole.USER)
                .build();

        testDevice = Device.builder()
                .id(1L)
                .name("Test Router")
                .description("Test router description")
                .systemName("router-01")
                .status(Device.DeviceStatus.ACTIVE)
                .type(Device.DeviceType.ROUTER)
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testDeviceDto = DeviceDto.builder()
                .id(1L)
                .name("Test Router")
                .description("Test router description")
                .systemName("router-01")
                .status(Device.DeviceStatus.ACTIVE)
                .type(Device.DeviceType.ROUTER)
                .userId(1L)
                .userName("Test User")
                .build();
    }

    @Test
    void createDevice_Success() {
        // Given
        DeviceDto inputDto = DeviceDto.builder()
                .name("New Router")
                .description("New router description")
                .status(Device.DeviceStatus.ACTIVE)
                .type(Device.DeviceType.ROUTER)
                .build();

        Device deviceToSave = Device.builder()
                .name("New Router")
                .description("New router description")
                .status(Device.DeviceStatus.ACTIVE)
                .type(Device.DeviceType.ROUTER)
                .build();

        Device savedDevice = Device.builder()
                .id(2L)
                .name("New Router")
                .description("New router description")
                .status(Device.DeviceStatus.ACTIVE)
                .type(Device.DeviceType.ROUTER)
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        DeviceDto expectedDto = DeviceDto.builder()
                .id(2L)
                .name("New Router")
                .description("New router description")
                .status(Device.DeviceStatus.ACTIVE)
                .type(Device.DeviceType.ROUTER)
                .userId(1L)
                .userName("Test User")
                .build();

        when(deviceRepository.existsByNameAndUser("New Router", testUser)).thenReturn(false);
        when(deviceMapper.toEntity(inputDto)).thenReturn(deviceToSave);
        when(deviceRepository.save(any(Device.class))).thenReturn(savedDevice);
        when(deviceMapper.toDto(savedDevice)).thenReturn(expectedDto);

        // When
        DeviceDto result = deviceService.createDevice(inputDto, testUser);

        // Then
        assertNotNull(result);
        assertEquals("New Router", result.getName());
        assertEquals(2L, result.getId());
        assertEquals(1L, result.getUserId());

        verify(deviceRepository).existsByNameAndUser("New Router", testUser);
        verify(deviceMapper).toEntity(inputDto);
        verify(deviceRepository).save(any(Device.class));
        verify(deviceMapper).toDto(savedDevice);
    }

    @Test
    void createDevice_ThrowsException_WhenNameAlreadyExists() {
        // Given
        DeviceDto inputDto = DeviceDto.builder()
                .name("Existing Router")
                .description("Router description")
                .status(Device.DeviceStatus.ACTIVE)
                .type(Device.DeviceType.ROUTER)
                .build();

        when(deviceRepository.existsByNameAndUser("Existing Router", testUser)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> deviceService.createDevice(inputDto, testUser));

        assertEquals("Device with name 'Existing Router' already exists for this user", exception.getMessage());

        verify(deviceRepository).existsByNameAndUser("Existing Router", testUser);
        verify(deviceMapper, never()).toEntity(any());
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void updateDevice_Success() {
        // Given
        DeviceDto updateDto = DeviceDto.builder()
                .name("Updated Router")
                .description("Updated description")
                .status(Device.DeviceStatus.MAINTENANCE)
                .type(Device.DeviceType.ROUTER)
                .build();

        Device updatedDevice = Device.builder()
                .id(1L)
                .name("Updated Router")
                .description("Updated description")
                .status(Device.DeviceStatus.MAINTENANCE)
                .type(Device.DeviceType.ROUTER)
                .user(testUser)
                .build();

        DeviceDto expectedDto = DeviceDto.builder()
                .id(1L)
                .name("Updated Router")
                .description("Updated description")
                .status(Device.DeviceStatus.MAINTENANCE)
                .type(Device.DeviceType.ROUTER)
                .userId(1L)
                .userName("Test User")
                .build();

        when(deviceRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testDevice));
        when(deviceRepository.existsByNameAndUser("Updated Router", testUser)).thenReturn(false);
        when(deviceRepository.save(testDevice)).thenReturn(updatedDevice);
        when(deviceMapper.toDto(updatedDevice)).thenReturn(expectedDto);

        // When
        DeviceDto result = deviceService.updateDevice(1L, updateDto, testUser);

        // Then
        assertNotNull(result);
        assertEquals("Updated Router", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(Device.DeviceStatus.MAINTENANCE, result.getStatus());

        verify(deviceRepository).findByIdAndUser(1L, testUser);
        verify(deviceRepository).existsByNameAndUser("Updated Router", testUser);
        verify(deviceMapper).updateEntity(testDevice, updateDto);
        verify(deviceRepository).save(testDevice);
        verify(deviceMapper).toDto(updatedDevice);
    }

    @Test
    void updateDevice_ThrowsException_WhenDeviceNotFound() {
        // Given
        DeviceDto updateDto = DeviceDto.builder()
                .name("Updated Router")
                .description("Updated description")
                .status(Device.DeviceStatus.MAINTENANCE)
                .type(Device.DeviceType.ROUTER)
                .build();

        when(deviceRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> deviceService.updateDevice(999L, updateDto, testUser));

        assertEquals("Device not found or access denied", exception.getMessage());

        verify(deviceRepository).findByIdAndUser(999L, testUser);
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void deleteDevice_Success() {
        // Given
        when(deviceRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testDevice));

        // When
        deviceService.deleteDevice(1L, testUser);

        // Then
        verify(deviceRepository).findByIdAndUser(1L, testUser);
        verify(deviceRepository).delete(testDevice);
    }

    @Test
    void deleteDevice_ThrowsException_WhenDeviceNotFound() {
        // Given
        when(deviceRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> deviceService.deleteDevice(999L, testUser));

        assertEquals("Device not found or access denied", exception.getMessage());

        verify(deviceRepository).findByIdAndUser(999L, testUser);
        verify(deviceRepository, never()).delete(any());
    }

    @Test
    void getDeviceById_Success() {
        // Given
        when(deviceRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testDevice));
        when(deviceMapper.toDto(testDevice)).thenReturn(testDeviceDto);

        // When
        DeviceDto result = deviceService.getDeviceById(1L, testUser);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Router", result.getName());

        verify(deviceRepository).findByIdAndUser(1L, testUser);
        verify(deviceMapper).toDto(testDevice);
    }

    @Test
    void getDevicesByUser_Success() {
        // Given
        List<Device> devices = List.of(testDevice);
        List<DeviceDto> expectedDtos = List.of(testDeviceDto);

        when(deviceRepository.findByUser(testUser)).thenReturn(devices);
        when(deviceMapper.toDto(testDevice)).thenReturn(testDeviceDto);

        // When
        List<DeviceDto> result = deviceService.getDevicesByUser(testUser);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Router", result.get(0).getName());

        verify(deviceRepository).findByUser(testUser);
        verify(deviceMapper).toDto(testDevice);
    }

    @Test
    void getDevicesByUserPaged_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Device> devices = List.of(testDevice);
        Page<Device> devicePage = new PageImpl<>(devices, pageable, 1);

        when(deviceRepository.findByUser(testUser, pageable)).thenReturn(devicePage);
        when(deviceMapper.toDto(testDevice)).thenReturn(testDeviceDto);

        // When
        Page<DeviceDto> result = deviceService.getDevicesByUser(testUser, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Test Router", result.getContent().get(0).getName());

        verify(deviceRepository).findByUser(testUser, pageable);
    }

    @Test
    void getDevicesByUserAndStatus_Success() {
        // Given
        Device.DeviceStatus status = Device.DeviceStatus.ACTIVE;
        List<Device> devices = List.of(testDevice);

        when(deviceRepository.findByUserAndStatus(testUser, status)).thenReturn(devices);
        when(deviceMapper.toDto(testDevice)).thenReturn(testDeviceDto);

        // When
        List<DeviceDto> result = deviceService.getDevicesByUserAndStatus(testUser, status);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Device.DeviceStatus.ACTIVE, result.get(0).getStatus());

        verify(deviceRepository).findByUserAndStatus(testUser, status);
        verify(deviceMapper).toDto(testDevice);
    }

    @Test
    void getDevicesByUserAndType_Success() {
        // Given
        Device.DeviceType type = Device.DeviceType.ROUTER;
        List<Device> devices = List.of(testDevice);

        when(deviceRepository.findByUserAndType(testUser, type)).thenReturn(devices);
        when(deviceMapper.toDto(testDevice)).thenReturn(testDeviceDto);

        // When
        List<DeviceDto> result = deviceService.getDevicesByUserAndType(testUser, type);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Device.DeviceType.ROUTER, result.get(0).getType());

        verify(deviceRepository).findByUserAndType(testUser, type);
        verify(deviceMapper).toDto(testDevice);
    }

    @Test
    void searchDevicesByName_Success() {
        // Given
        String searchTerm = "router";
        List<Device> devices = List.of(testDevice);

        when(deviceRepository.findByUserAndNameContaining(testUser, searchTerm)).thenReturn(devices);
        when(deviceMapper.toDto(testDevice)).thenReturn(testDeviceDto);

        // When
        List<DeviceDto> result = deviceService.searchDevicesByName(testUser, searchTerm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getName().toLowerCase().contains(searchTerm.toLowerCase()));

        verify(deviceRepository).findByUserAndNameContaining(testUser, searchTerm);
        verify(deviceMapper).toDto(testDevice);
    }

    @Test
    void countDevicesByUser_Success() {
        // Given
        Long expectedCount = 5L;
        when(deviceRepository.countByUser(testUser)).thenReturn(expectedCount);

        // When
        Long result = deviceService.countDevicesByUser(testUser);

        // Then
        assertEquals(expectedCount, result);
        verify(deviceRepository).countByUser(testUser);
    }

    @Test
    void existsByNameAndUser_ReturnsTrue() {
        // Given
        String deviceName = "Existing Device";
        when(deviceRepository.existsByNameAndUser(deviceName, testUser)).thenReturn(true);

        // When
        boolean result = deviceService.existsByNameAndUser(deviceName, testUser);

        // Then
        assertTrue(result);
        verify(deviceRepository).existsByNameAndUser(deviceName, testUser);
    }

    @Test
    void existsByNameAndUser_ReturnsFalse() {
        // Given
        String deviceName = "Non-existing Device";
        when(deviceRepository.existsByNameAndUser(deviceName, testUser)).thenReturn(false);

        // When
        boolean result = deviceService.existsByNameAndUser(deviceName, testUser);

        // Then
        assertFalse(result);
        verify(deviceRepository).existsByNameAndUser(deviceName, testUser);
    }
}
