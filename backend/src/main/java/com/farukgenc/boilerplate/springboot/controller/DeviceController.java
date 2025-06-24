package com.farukgenc.boilerplate.springboot.controller;

import com.farukgenc.boilerplate.springboot.dto.DeviceDto;
import com.farukgenc.boilerplate.springboot.dto.DeviceCreateRequest;
import com.farukgenc.boilerplate.springboot.dto.DeviceResponse;
import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.service.device.DeviceService;
import com.farukgenc.boilerplate.springboot.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Tag(name = "Device Management", description = "APIs for managing network devices")
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    @Operation(summary = "Create a new device", description = "Creates a new network device for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Device created successfully",
                    content = @Content(schema = @Schema(implementation = DeviceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "409", description = "Device name already exists")
    })
    public ResponseEntity<DeviceResponse> createDevice(@Valid @RequestBody DeviceCreateRequest deviceCreateRequest) {
        User user = SecurityUtils.getCurrentUser();
        DeviceResponse createdDevice = deviceService.createDeviceWithConfig(deviceCreateRequest, user);
        return new ResponseEntity<>(createdDevice, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a device", description = "Updates an existing device owned by the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device updated successfully",
                    content = @Content(schema = @Schema(implementation = DeviceDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "409", description = "Device name already exists")
    })
    public ResponseEntity<DeviceDto> updateDevice(
            @Parameter(description = "Device ID") @PathVariable Long id,
            @Valid @RequestBody DeviceDto deviceDto) {
        User user = SecurityUtils.getCurrentUser();
        DeviceDto updatedDevice = deviceService.updateDevice(id, deviceDto, user);
        return ResponseEntity.ok(updatedDevice);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a device", description = "Deletes a device owned by the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Device deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    public ResponseEntity<Void> deleteDevice(@Parameter(description = "Device ID") @PathVariable Long id) {
        User user = SecurityUtils.getCurrentUser();
        deviceService.deleteDevice(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get device by ID", description = "Retrieves a specific device owned by the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device found",
                    content = @Content(schema = @Schema(implementation = DeviceDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    public ResponseEntity<DeviceDto> getDeviceById(
            @Parameter(description = "Device ID") @PathVariable Long id) {
        User user = SecurityUtils.getCurrentUser();
        DeviceDto device = deviceService.getDeviceById(id, user);
        return ResponseEntity.ok(device);
    }

    @GetMapping
    @Operation(summary = "Get all devices with pagination", description = "Retrieves all devices owned by the authenticated user with pagination support")
    @ApiResponse(responseCode = "200", description = "Devices retrieved successfully")
    public ResponseEntity<Page<DeviceDto>> getAllDevices(
            @Parameter(description = "Page number (0-based)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field", example = "name") 
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction", example = "asc") 
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        User user = SecurityUtils.getCurrentUser();
        
        // Create safe pageable with validated parameters
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        // Validate sortBy field to prevent PropertyReferenceException
        String validSortBy = validateSortField(sortBy);
        Sort sort = Sort.by(direction, validSortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<DeviceDto> devices = deviceService.getDevicesByUser(user, pageable);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/by-status/{status}")
    @Operation(summary = "Get devices by status", description = "Retrieves devices filtered by status")
    @ApiResponse(responseCode = "200", description = "Devices retrieved successfully")
    public ResponseEntity<List<DeviceDto>> getDevicesByStatus(
            @Parameter(description = "Device status") @PathVariable Device.DeviceStatus status) {
        User user = SecurityUtils.getCurrentUser();
        List<DeviceDto> devices = deviceService.getDevicesByUserAndStatus(user, status);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/by-type/{type}")
    @Operation(summary = "Get devices by type", description = "Retrieves devices filtered by type")
    @ApiResponse(responseCode = "200", description = "Devices retrieved successfully")
    public ResponseEntity<List<DeviceDto>> getDevicesByType(
            @Parameter(description = "Device type") @PathVariable Device.DeviceType type) {
        User user = SecurityUtils.getCurrentUser();
        List<DeviceDto> devices = deviceService.getDevicesByUserAndType(user, type);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/search")
    @Operation(summary = "Search devices by name", description = "Searches devices by name pattern")
    @ApiResponse(responseCode = "200", description = "Devices retrieved successfully")
    public ResponseEntity<List<DeviceDto>> searchDevicesByName(
            @Parameter(description = "Search term") @RequestParam String name) {
        User user = SecurityUtils.getCurrentUser();
        List<DeviceDto> devices = deviceService.searchDevicesByName(user, name);
        return ResponseEntity.ok(devices);
    }

    @PostMapping("/{id}/monitor")
    @Operation(summary = "Trigger device monitoring", description = "Manually triggers monitoring for a specific device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monitoring triggered successfully"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<String> triggerDeviceMonitoring(
            @Parameter(description = "Device ID") @PathVariable Long id) {
        User user = SecurityUtils.getCurrentUser();

        try {
            deviceService.triggerMonitoring(id, user);
            return ResponseEntity.ok("Device monitoring triggered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Validates the sort field to prevent PropertyReferenceException
     * @param sortBy the requested sort field
     * @return a valid sort field name
     */
    private String validateSortField(String sortBy) {
        // Define allowed sort fields for Device entity
        Set<String> allowedSortFields = Set.of(
            "id", "name", "description", "systemObjectId", "systemUptime", 
            "systemContact", "systemName", "systemLocation", "systemServices",
            "lastMonitored", "monitoringEnabled", "status", "type", 
            "createdAt", "updatedAt"
        );
        
        // Return valid field or default to 'id' if invalid
        return allowedSortFields.contains(sortBy) ? sortBy : "id";
    }
}
