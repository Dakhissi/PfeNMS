package com.farukgenc.boilerplate.springboot.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.farukgenc.boilerplate.springboot.dto.DeviceDto;
import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.model.UserRole;
import com.farukgenc.boilerplate.springboot.repository.DeviceRepository;
import com.farukgenc.boilerplate.springboot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class DeviceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Device testDevice;

    @BeforeEach
    void setUp() {
        // Clean up repositories
        deviceRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .name("Test User")
                .password(passwordEncoder.encode("password"))
                .userRole(UserRole.USER)
                .build();
        testUser = userRepository.save(testUser);

        // Create test device
        testDevice = Device.builder()
                .name("Test Router")
                .description("Test router description")
                .systemName("router-01")
                .status(Device.DeviceStatus.ACTIVE)
                .type(Device.DeviceType.ROUTER)
                .user(testUser)
                .build();
        testDevice = deviceRepository.save(testDevice);
    }

    @Test
    @WithMockUser(username = "testuser")
    void createDevice_Success() throws Exception {
        DeviceDto deviceDto = DeviceDto.builder()
                .name("New Router")
                .description("New router description")
                .status(Device.DeviceStatus.ACTIVE)
                .type(Device.DeviceType.ROUTER)
                .build();

        mockMvc.perform(post("/api/devices")
                .with(user("testuser"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deviceDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Router"))
                .andExpect(jsonPath("$.description").value("New router description"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.type").value("ROUTER"))
                .andExpect(jsonPath("$.userId").value(testUser.getId()));
    }

    @Test
    @WithMockUser(username = "testuser")
    void createDevice_DuplicateName_ShouldReturnBadRequest() throws Exception {
        DeviceDto deviceDto = DeviceDto.builder()
                .name("Test Router") // Same name as existing device
                .description("Duplicate router")
                .status(Device.DeviceStatus.ACTIVE)
                .type(Device.DeviceType.ROUTER)
                .build();

        mockMvc.perform(post("/api/devices")
                .with(user("testuser"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deviceDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getDeviceById_Success() throws Exception {
        mockMvc.perform(get("/api/devices/{id}", testDevice.getId())
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testDevice.getId()))
                .andExpect(jsonPath("$.name").value("Test Router"))
                .andExpect(jsonPath("$.description").value("Test router description"))
                .andExpect(jsonPath("$.systemName").value("router-01"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.type").value("ROUTER"))
                .andExpect(jsonPath("$.userId").value(testUser.getId()));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateDevice_Success() throws Exception {
        DeviceDto updateDto = DeviceDto.builder()
                .name("Updated Router")
                .description("Updated description")
                .status(Device.DeviceStatus.MAINTENANCE)
                .type(Device.DeviceType.ROUTER)
                .build();

        mockMvc.perform(put("/api/devices/{id}", testDevice.getId())
                .with(user("testuser"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testDevice.getId()))
                .andExpect(jsonPath("$.name").value("Updated Router"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteDevice_Success() throws Exception {
        mockMvc.perform(delete("/api/devices/{id}", testDevice.getId())
                .with(user("testuser")))
                .andExpect(status().isNoContent());

        // Verify device is deleted
        mockMvc.perform(get("/api/devices/{id}", testDevice.getId())
                .with(user("testuser")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getAllDevices_Success() throws Exception {
        mockMvc.perform(get("/api/devices")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(testDevice.getId()))
                .andExpect(jsonPath("$[0].name").value("Test Router"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getDevicesByStatus_Success() throws Exception {
        mockMvc.perform(get("/api/devices/by-status/ACTIVE")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getDevicesByType_Success() throws Exception {
        mockMvc.perform(get("/api/devices/by-type/ROUTER")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("ROUTER"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void searchDevicesByName_Success() throws Exception {
        mockMvc.perform(get("/api/devices/search")
                .with(user("testuser"))
                .param("name", "router"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Router"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getDeviceCount_Success() throws Exception {
        mockMvc.perform(get("/api/devices/count")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deviceNameExists_ReturnsTrue() throws Exception {
        mockMvc.perform(get("/api/devices/exists/Test Router")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deviceNameExists_ReturnsFalse() throws Exception {
        mockMvc.perform(get("/api/devices/exists/Non-existing Device")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void createDevice_Unauthorized() throws Exception {
        DeviceDto deviceDto = DeviceDto.builder()
                .name("Unauthorized Device")
                .description("Should fail")
                .status(Device.DeviceStatus.ACTIVE)
                .type(Device.DeviceType.ROUTER)
                .build();

        mockMvc.perform(post("/api/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deviceDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getDeviceById_NotFound() throws Exception {
        mockMvc.perform(get("/api/devices/999")
                .with(user("testuser")))
                .andExpect(status().isBadRequest());
    }
}
