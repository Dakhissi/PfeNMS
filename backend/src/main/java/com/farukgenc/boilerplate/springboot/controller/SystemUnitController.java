package com.farukgenc.boilerplate.springboot.controller;

import com.farukgenc.boilerplate.springboot.dto.SystemUnitDto;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.service.device.SystemUnitService;
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
@RequestMapping("/api/system-units")
@RequiredArgsConstructor
@Tag(name = "System Unit Management", description = "APIs for managing device system units")
public class SystemUnitController {

    private final SystemUnitService systemUnitService;

    @PostMapping
    @Operation(summary = "Create a new system unit", description = "Creates a new system unit for a device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "System unit created successfully",
                    content = @Content(schema = @Schema(implementation = SystemUnitDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "409", description = "Unit index already exists")
    })
    public ResponseEntity<SystemUnitDto> createSystemUnit(
            @Valid @RequestBody SystemUnitDto systemUnitDto) {

        User user = SecurityUtils.getCurrentUser();
        SystemUnitDto createdSystemUnit = systemUnitService.createSystemUnit(systemUnitDto, user);
        return new ResponseEntity<>(createdSystemUnit, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a system unit", description = "Updates an existing system unit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System unit updated successfully",
                    content = @Content(schema = @Schema(implementation = SystemUnitDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "System unit not found"),
            @ApiResponse(responseCode = "409", description = "Unit index already exists")
    })
    public ResponseEntity<SystemUnitDto> updateSystemUnit(
            @Parameter(description = "System unit ID") @PathVariable Long id,
            @Valid @RequestBody SystemUnitDto systemUnitDto) {

        User user = SecurityUtils.getCurrentUser();
        SystemUnitDto updatedSystemUnit = systemUnitService.updateSystemUnit(id, systemUnitDto, user);
        return ResponseEntity.ok(updatedSystemUnit);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a system unit", description = "Deletes a system unit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "System unit deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "System unit not found")
    })
    public ResponseEntity<Void> deleteSystemUnit(
            @Parameter(description = "System unit ID") @PathVariable Long id) {

        User user = SecurityUtils.getCurrentUser();
        systemUnitService.deleteSystemUnit(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get system unit by ID", description = "Retrieves a specific system unit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System unit found",
                    content = @Content(schema = @Schema(implementation = SystemUnitDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "System unit not found")
    })
    public ResponseEntity<SystemUnitDto> getSystemUnitById(
            @Parameter(description = "System unit ID") @PathVariable Long id) {

        User user = SecurityUtils.getCurrentUser();
        SystemUnitDto systemUnit = systemUnitService.getSystemUnitById(id, user);
        return ResponseEntity.ok(systemUnit);
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Get system units by device", description = "Retrieves all system units for a specific device")
    @ApiResponse(responseCode = "200", description = "System units retrieved successfully")
    public ResponseEntity<List<SystemUnitDto>> getSystemUnitsByDevice(
            @Parameter(description = "Device ID") @PathVariable Long deviceId) {

        User user = SecurityUtils.getCurrentUser();
        List<SystemUnitDto> systemUnits = systemUnitService.getSystemUnitsByDevice(deviceId, user);
        return ResponseEntity.ok(systemUnits);
    }

    @GetMapping("/device/{deviceId}/search")
    @Operation(summary = "Search system units by name", description = "Searches system units by name pattern for a specific device")
    @ApiResponse(responseCode = "200", description = "System units retrieved successfully")
    public ResponseEntity<List<SystemUnitDto>> searchSystemUnitsByName(
            @Parameter(description = "Device ID") @PathVariable Long deviceId,
            @Parameter(description = "Search term") @RequestParam String name) {

        User user = SecurityUtils.getCurrentUser();
        List<SystemUnitDto> systemUnits = systemUnitService.searchSystemUnitsByName(deviceId, name, user);
        return ResponseEntity.ok(systemUnits);
    }

    @GetMapping("/device/{deviceId}/type/{type}")
    @Operation(summary = "Get system units by device and type", description = "Retrieves system units filtered by type for a specific device")
    @ApiResponse(responseCode = "200", description = "System units retrieved successfully")
    public ResponseEntity<List<SystemUnitDto>> getSystemUnitsByDeviceAndType(
            @Parameter(description = "Device ID") @PathVariable Long deviceId,
            @Parameter(description = "Unit type") @PathVariable String type) {

        User user = SecurityUtils.getCurrentUser();
        List<SystemUnitDto> systemUnits = systemUnitService.getSystemUnitsByDeviceAndType(deviceId, type, user);
        return ResponseEntity.ok(systemUnits);
    }

    @GetMapping("/device/{deviceId}/count")
    @Operation(summary = "Get system unit count for device", description = "Returns the number of system units for a specific device")
    @ApiResponse(responseCode = "200", description = "System unit count retrieved successfully")
    public ResponseEntity<Long> getSystemUnitCountByDevice(
            @Parameter(description = "Device ID") @PathVariable Long deviceId) {

        User user = SecurityUtils.getCurrentUser();
        Long count = systemUnitService.countSystemUnitsByDevice(deviceId, user);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/device/{deviceId}/exists/{unitIndex}")
    @Operation(summary = "Check if unit index exists", description = "Checks if a unit index already exists for the device")
    @ApiResponse(responseCode = "200", description = "Existence check completed")
    public ResponseEntity<Boolean> unitIndexExists(
            @Parameter(description = "Device ID") @PathVariable Long deviceId,
            @Parameter(description = "Unit index") @PathVariable Integer unitIndex) {

        User user = SecurityUtils.getCurrentUser();
        Boolean exists = systemUnitService.existsByDeviceAndUnitIndex(deviceId, unitIndex, user);
        return ResponseEntity.ok(exists);
    }
}
