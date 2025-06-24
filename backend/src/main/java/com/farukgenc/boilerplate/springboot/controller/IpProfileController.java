package com.farukgenc.boilerplate.springboot.controller;

import com.farukgenc.boilerplate.springboot.dto.IpProfileDto;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.service.device.IpProfileService;
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
@RequestMapping("/api/ip-profiles")
@RequiredArgsConstructor
@Tag(name = "IP Profile Management", description = "APIs for managing device IP profiles")
public class IpProfileController {

    private final IpProfileService ipProfileService;

    @PostMapping
    @Operation(summary = "Create a new IP profile", description = "Creates a new IP profile for a device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "IP profile created successfully",
                    content = @Content(schema = @Schema(implementation = IpProfileDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "409", description = "IP address already exists")
    })
    public ResponseEntity<IpProfileDto> createIpProfile(
            @Valid @RequestBody IpProfileDto ipProfileDto) {

        User user = SecurityUtils.getCurrentUser();
        IpProfileDto createdProfile = ipProfileService.createIpProfile(ipProfileDto, user);
        return new ResponseEntity<>(createdProfile, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an IP profile", description = "Updates an existing IP profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "IP profile updated successfully",
                    content = @Content(schema = @Schema(implementation = IpProfileDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "IP profile not found"),
            @ApiResponse(responseCode = "409", description = "IP address already exists")
    })
    public ResponseEntity<IpProfileDto> updateIpProfile(
            @Parameter(description = "IP profile ID") @PathVariable Long id,
            @Valid @RequestBody IpProfileDto ipProfileDto) {

        User user = SecurityUtils.getCurrentUser();
        IpProfileDto updatedProfile = ipProfileService.updateIpProfile(id, ipProfileDto, user);
        return ResponseEntity.ok(updatedProfile);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an IP profile", description = "Deletes an IP profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "IP profile deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "IP profile not found")
    })
    public ResponseEntity<Void> deleteIpProfile(
            @Parameter(description = "IP profile ID") @PathVariable Long id) {

        User user = SecurityUtils.getCurrentUser();
        ipProfileService.deleteIpProfile(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get IP profile by ID", description = "Retrieves a specific IP profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "IP profile found",
                    content = @Content(schema = @Schema(implementation = IpProfileDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "IP profile not found")
    })
    public ResponseEntity<IpProfileDto> getIpProfileById(
            @Parameter(description = "IP profile ID") @PathVariable Long id) {

        User user = SecurityUtils.getCurrentUser();
        IpProfileDto ipProfile = ipProfileService.getIpProfileById(id, user);
        return ResponseEntity.ok(ipProfile);
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Get IP profiles by device", description = "Retrieves all IP profiles for a specific device")
    @ApiResponse(responseCode = "200", description = "IP profiles retrieved successfully")
    public ResponseEntity<List<IpProfileDto>> getIpProfilesByDevice(
            @Parameter(description = "Device ID") @PathVariable Long deviceId) {

        User user = SecurityUtils.getCurrentUser();
        List<IpProfileDto> profiles = ipProfileService.getIpProfilesByDevice(deviceId, user);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/device/{deviceId}/forwarding/{forwarding}")
    @Operation(summary = "Get IP profiles by device and forwarding", description = "Retrieves IP profiles filtered by forwarding status for a specific device")
    @ApiResponse(responseCode = "200", description = "IP profiles retrieved successfully")
    public ResponseEntity<List<IpProfileDto>> getIpProfilesByDeviceAndForwarding(
            @Parameter(description = "Device ID") @PathVariable Long deviceId,
            @Parameter(description = "IP forwarding enabled") @PathVariable Boolean forwarding) {

        User user = SecurityUtils.getCurrentUser();
        List<IpProfileDto> profiles = ipProfileService.getIpProfilesByDeviceAndForwarding(deviceId, forwarding, user);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/device/{deviceId}/search")
    @Operation(summary = "Search IP profiles by address", description = "Searches IP profiles by address pattern for a specific device")
    @ApiResponse(responseCode = "200", description = "IP profiles retrieved successfully")
    public ResponseEntity<List<IpProfileDto>> searchIpProfilesByAddress(
            @Parameter(description = "Device ID") @PathVariable Long deviceId,
            @Parameter(description = "Search term") @RequestParam String address) {

        User user = SecurityUtils.getCurrentUser();
        List<IpProfileDto> profiles = ipProfileService.searchIpProfilesByAddress(deviceId, address, user);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/device/{deviceId}/count")
    @Operation(summary = "Get IP profile count for device", description = "Returns the number of IP profiles for a specific device")
    @ApiResponse(responseCode = "200", description = "IP profile count retrieved successfully")
    public ResponseEntity<Long> getIpProfileCountByDevice(
            @Parameter(description = "Device ID") @PathVariable Long deviceId) {

        User user = SecurityUtils.getCurrentUser();
        Long count = ipProfileService.countIpProfilesByDevice(deviceId, user);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/device/{deviceId}/exists")
    @Operation(summary = "Check if IP address exists", description = "Checks if an IP address already exists for the device")
    @ApiResponse(responseCode = "200", description = "Existence check completed")
    public ResponseEntity<Boolean> ipAddressExists(
            @Parameter(description = "Device ID") @PathVariable Long deviceId,
            @Parameter(description = "IP address") @RequestParam String ipAddress) {

        User user = SecurityUtils.getCurrentUser();
        Boolean exists = ipProfileService.existsByDeviceAndIpAddress(deviceId, ipAddress, user);
        return ResponseEntity.ok(exists);
    }
}
