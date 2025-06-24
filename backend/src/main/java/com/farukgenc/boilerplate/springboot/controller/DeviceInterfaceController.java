package com.farukgenc.boilerplate.springboot.controller;

import com.farukgenc.boilerplate.springboot.dto.DeviceInterfaceDto;
import com.farukgenc.boilerplate.springboot.model.DeviceInterface;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.service.device.DeviceInterfaceService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/device-interfaces")
@RequiredArgsConstructor
@Tag(name = "Device Interface Management", description = "APIs for managing device network interfaces")
public class DeviceInterfaceController {

    private final DeviceInterfaceService interfaceService;

    @PostMapping
    @Operation(summary = "Create a new device interface", description = "Creates a new network interface for a device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Interface created successfully",
                    content = @Content(schema = @Schema(implementation = DeviceInterfaceDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "409", description = "Interface index already exists")
    })
    public ResponseEntity<DeviceInterfaceDto> createInterface(
            @Valid @RequestBody DeviceInterfaceDto interfaceDto) {

        User user = SecurityUtils.getCurrentUser();
        DeviceInterfaceDto createdInterface = interfaceService.createInterface(interfaceDto, user);
        return new ResponseEntity<>(createdInterface, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a device interface", description = "Updates an existing device interface")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interface updated successfully",
                    content = @Content(schema = @Schema(implementation = DeviceInterfaceDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Interface not found"),
            @ApiResponse(responseCode = "409", description = "Interface index already exists")
    })
    public ResponseEntity<DeviceInterfaceDto> updateInterface(
            @Parameter(description = "Interface ID") @PathVariable Long id,
            @Valid @RequestBody DeviceInterfaceDto interfaceDto) {

        User user = SecurityUtils.getCurrentUser();
        DeviceInterfaceDto updatedInterface = interfaceService.updateInterface(id, interfaceDto, user);
        return ResponseEntity.ok(updatedInterface);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a device interface", description = "Deletes a device interface")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Interface deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Interface not found")
    })
    public ResponseEntity<Void> deleteInterface(
            @Parameter(description = "Interface ID") @PathVariable Long id) {

        User user = SecurityUtils.getCurrentUser();
        interfaceService.deleteInterface(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get interface by ID", description = "Retrieves a specific device interface")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interface found",
                    content = @Content(schema = @Schema(implementation = DeviceInterfaceDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Interface not found")
    })
    public ResponseEntity<DeviceInterfaceDto> getInterfaceById(
            @Parameter(description = "Interface ID") @PathVariable Long id) {

        User user = SecurityUtils.getCurrentUser();
        DeviceInterfaceDto deviceInterface = interfaceService.getInterfaceById(id, user);
        return ResponseEntity.ok(deviceInterface);
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Get interfaces by device", description = "Retrieves all interfaces for a specific device")
    @ApiResponse(responseCode = "200", description = "Interfaces retrieved successfully")
    public ResponseEntity<List<DeviceInterfaceDto>> getInterfacesByDevice(
            @Parameter(description = "Device ID") @PathVariable Long deviceId) {

        User user = SecurityUtils.getCurrentUser();
        List<DeviceInterfaceDto> interfaces = interfaceService.getInterfacesByDevice(deviceId, user);
        return ResponseEntity.ok(interfaces);
    }

    @GetMapping("/device/{deviceId}/status/{status}")
    @Operation(summary = "Get interfaces by device and status", description = "Retrieves interfaces filtered by status for a specific device")
    @ApiResponse(responseCode = "200", description = "Interfaces retrieved successfully")
    public ResponseEntity<List<DeviceInterfaceDto>> getInterfacesByDeviceAndStatus(
            @Parameter(description = "Device ID") @PathVariable Long deviceId,
            @Parameter(description = "Interface status") @PathVariable DeviceInterface.InterfaceStatus status) {

        User user = SecurityUtils.getCurrentUser();
        List<DeviceInterfaceDto> interfaces = interfaceService.getInterfacesByDeviceAndStatus(deviceId, status, user);
        return ResponseEntity.ok(interfaces);
    }

    @GetMapping("/device/{deviceId}/search")
    @Operation(summary = "Search interfaces by description", description = "Searches interfaces by description pattern for a specific device")
    @ApiResponse(responseCode = "200", description = "Interfaces retrieved successfully")
    public ResponseEntity<List<DeviceInterfaceDto>> searchInterfacesByDescription(
            @Parameter(description = "Device ID") @PathVariable Long deviceId,
            @Parameter(description = "Search term") @RequestParam String description) {

        User user = SecurityUtils.getCurrentUser();
        List<DeviceInterfaceDto> interfaces = interfaceService.searchInterfacesByDescription(deviceId, description, user);
        return ResponseEntity.ok(interfaces);
    }

    @GetMapping("/device/{deviceId}/count")
    @Operation(summary = "Get interface count for device", description = "Returns the number of interfaces for a specific device")
    @ApiResponse(responseCode = "200", description = "Interface count retrieved successfully")
    public ResponseEntity<Long> getInterfaceCountByDevice(
            @Parameter(description = "Device ID") @PathVariable Long deviceId) {

        User user = SecurityUtils.getCurrentUser();
        Long count = interfaceService.countInterfacesByDevice(deviceId, user);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/device/{deviceId}/exists/{ifIndex}")
    @Operation(summary = "Check if interface index exists", description = "Checks if an interface index already exists for the device")
    @ApiResponse(responseCode = "200", description = "Existence check completed")
    public ResponseEntity<Boolean> interfaceIndexExists(
            @Parameter(description = "Device ID") @PathVariable Long deviceId,
            @Parameter(description = "Interface index") @PathVariable Integer ifIndex) {

        User user = SecurityUtils.getCurrentUser();
        Boolean exists = interfaceService.existsByDeviceAndIfIndex(deviceId, ifIndex, user);
        return ResponseEntity.ok(exists);
    }
}
