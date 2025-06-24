package com.farukgenc.boilerplate.springboot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.farukgenc.boilerplate.springboot.dto.DeviceDto;
import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.service.device.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeviceService deviceService;    @Autowired
    private ObjectMapper objectMapper;

    private DeviceDto testDeviceDto;

    @BeforeEach
    void setUp() {

        testDeviceDto = DeviceDto.builder()
                .id(1L)
                .name("Test Router")
                .description("Test router description")
                .systemName("router-01")
                .status(Device.DeviceStatus.ACTIVE)
                .type(Device.DeviceType.ROUTER)
                .userId(1L)
                .userName("Test User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(username = "testuser")
    void createDevice_Success() throws Exception {
        // Given
        DeviceDto inputDto = DeviceDto.builder()
                .name("New Router")
                .description("New router description")
                .status(Device.DeviceStatus.ACTIVE)
                .type(Device.DeviceType.ROUTER)
                .build();

        DeviceDto createdDto = DeviceDto.builder()
                .id(2L)
                .name("New Router")
                .description("New router description")
                .status(Device.DeviceStatus.ACTIVE)
                .type(Device.DeviceType.ROUTER)
                .userId(1L)
                .userName("Test User")
                .build();

        when(deviceService.createDevice(any(DeviceDto.class), any(User.class))).thenReturn(createdDto);        // When & Then
        mockMvc.perform(post("/api/devices")
                .with(user("testuser"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("New Router"))
                .andExpect(jsonPath("$.description").value("New router description"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.type").value("ROUTER"))
                .andExpect(jsonPath("$.userId").value(1L));

        verify(deviceService).createDevice(any(DeviceDto.class), any(User.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void createDevice_ValidationError() throws Exception {
        // Given
        DeviceDto invalidDto = DeviceDto.builder()
                .description("Router without name")
                .status(Device.DeviceStatus.ACTIVE)
                .type(Device.DeviceType.ROUTER)                .build(); // Missing required name

        // When & Then
        mockMvc.perform(post("/api/devices")
                .with(user("testuser"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(deviceService, never()).createDevice(any(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateDevice_Success() throws Exception {
        // Given
        DeviceDto updateDto = DeviceDto.builder()
                .name("Updated Router")
                .description("Updated description")
                .status(Device.DeviceStatus.MAINTENANCE)
                .type(Device.DeviceType.ROUTER)
                .build();

        DeviceDto updatedDto = DeviceDto.builder()
                .id(1L)
                .name("Updated Router")
                .description("Updated description")
                .status(Device.DeviceStatus.MAINTENANCE)
                .type(Device.DeviceType.ROUTER)
                .userId(1L)
                .userName("Test User")
                .build();

        when(deviceService.updateDevice(eq(1L), any(DeviceDto.class), any(User.class))).thenReturn(updatedDto);        // When & Then
        mockMvc.perform(put("/api/devices/1")
                .with(user("testuser"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Router"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));

        verify(deviceService).updateDevice(eq(1L), any(DeviceDto.class), any(User.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteDevice_Success() throws Exception {
        // Given
        doNothing().when(deviceService).deleteDevice(eq(1L), any(User.class));

        // When & Then
        mockMvc.perform(delete("/api/devices/1")
                .with(user("testuser")))
                .andExpect(status().isNoContent());

        verify(deviceService).deleteDevice(eq(1L), any(User.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getDeviceById_Success() throws Exception {
        // Given
        when(deviceService.getDeviceById(eq(1L), any(User.class))).thenReturn(testDeviceDto);

        // When & Then
        mockMvc.perform(get("/api/devices/1")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Router"))
                .andExpect(jsonPath("$.description").value("Test router description"))
                .andExpect(jsonPath("$.systemName").value("router-01"));

        verify(deviceService).getDeviceById(eq(1L), any(User.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getAllDevices_Success() throws Exception {
        // Given
        List<DeviceDto> devices = List.of(testDeviceDto);
        when(deviceService.getDevicesByUser(any(User.class))).thenReturn(devices);

        // When & Then
        mockMvc.perform(get("/api/devices")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Router"));

        verify(deviceService).getDevicesByUser(any(User.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getDevicesPaged_Success() throws Exception {
        // Given
        List<DeviceDto> devices = List.of(testDeviceDto);
        Page<DeviceDto> devicePage = new PageImpl<>(devices, PageRequest.of(0, 10), 1);
        when(deviceService.getDevicesByUser(any(User.class), any())).thenReturn(devicePage);

        // When & Then
        mockMvc.perform(get("/api/devices/paged")
                .with(user("testuser"))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(deviceService).getDevicesByUser(any(User.class), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getDevicesByStatus_Success() throws Exception {
        // Given
        List<DeviceDto> devices = List.of(testDeviceDto);
        when(deviceService.getDevicesByUserAndStatus(any(User.class), eq(Device.DeviceStatus.ACTIVE)))
                .thenReturn(devices);

        // When & Then
        mockMvc.perform(get("/api/devices/by-status/ACTIVE")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(deviceService).getDevicesByUserAndStatus(any(User.class), eq(Device.DeviceStatus.ACTIVE));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getDevicesByType_Success() throws Exception {
        // Given
        List<DeviceDto> devices = List.of(testDeviceDto);
        when(deviceService.getDevicesByUserAndType(any(User.class), eq(Device.DeviceType.ROUTER)))
                .thenReturn(devices);

        // When & Then
        mockMvc.perform(get("/api/devices/by-type/ROUTER")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("ROUTER"));

        verify(deviceService).getDevicesByUserAndType(any(User.class), eq(Device.DeviceType.ROUTER));
    }

    @Test
    @WithMockUser(username = "testuser")
    void searchDevicesByName_Success() throws Exception {
        // Given
        List<DeviceDto> devices = List.of(testDeviceDto);
        when(deviceService.searchDevicesByName(any(User.class), eq("router")))
                .thenReturn(devices);

        // When & Then
        mockMvc.perform(get("/api/devices/search")
                .with(user("testuser"))
                .param("name", "router"))                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Router"));

        verify(deviceService).searchDevicesByName(any(User.class), eq("router"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getDeviceCount_Success() throws Exception {
        // Given
        when(deviceService.countDevicesByUser(any(User.class))).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/devices/count")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(deviceService).countDevicesByUser(any(User.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deviceNameExists_ReturnsTrue() throws Exception {
        // Given
        when(deviceService.existsByNameAndUser(eq("existing-device"), any(User.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/devices/exists/existing-device")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(deviceService).existsByNameAndUser(eq("existing-device"), any(User.class));
    }

    @Test
    void createDevice_Unauthorized() throws Exception {
        // Given
        DeviceDto inputDto = DeviceDto.builder()
                .name("New Router")
                .description("New router description")
                .status(Device.DeviceStatus.ACTIVE)
                .type(Device.DeviceType.ROUTER)
                .build();

        // When & Then
        mockMvc.perform(post("/api/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isUnauthorized());

        verify(deviceService, never()).createDevice(any(), any());
    }
}
