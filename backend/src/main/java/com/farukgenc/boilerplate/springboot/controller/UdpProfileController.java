package com.farukgenc.boilerplate.springboot.controller;

import com.farukgenc.boilerplate.springboot.dto.UdpProfileDto;
import com.farukgenc.boilerplate.springboot.model.UdpProfile;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.service.device.UdpProfileService;
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
@RequestMapping("/api/udp-profiles")
@RequiredArgsConstructor
@Tag(name = "UDP Profile Management", description = "APIs for managing device UDP profiles")
public class UdpProfileController {

    private final UdpProfileService udpProfileService;

    @PostMapping
    @Operation(summary = "Create a new UDP profile", description = "Creates a new UDP profile for a device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "UDP profile created successfully",
                    content = @Content(schema = @Schema(implementation = UdpProfileDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "409", description = "UDP address/port combination already exists")
    })
    public ResponseEntity<UdpProfileDto> createUdpProfile(
            @Valid @RequestBody UdpProfileDto udpProfileDto) {

        User user = SecurityUtils.getCurrentUser();
        UdpProfileDto createdProfile = udpProfileService.createUdpProfile(udpProfileDto, user);
        return new ResponseEntity<>(createdProfile, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a UDP profile", description = "Updates an existing UDP profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UDP profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UdpProfileDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "UDP profile not found"),
            @ApiResponse(responseCode = "409", description = "UDP address/port combination already exists")
    })
    public ResponseEntity<UdpProfileDto> updateUdpProfile(
            @Parameter(description = "UDP profile ID") @PathVariable Long id,
            @Valid @RequestBody UdpProfileDto udpProfileDto) {

        User user = SecurityUtils.getCurrentUser();
        UdpProfileDto updatedProfile = udpProfileService.updateUdpProfile(id, udpProfileDto, user);
        return ResponseEntity.ok(updatedProfile);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a UDP profile", description = "Deletes a UDP profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "UDP profile deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "UDP profile not found")
    })
    public ResponseEntity<Void> deleteUdpProfile(
            @Parameter(description = "UDP profile ID") @PathVariable Long id) {

        User user = SecurityUtils.getCurrentUser();
        udpProfileService.deleteUdpProfile(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get UDP profile by ID", description = "Retrieves a specific UDP profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UDP profile found",
                    content = @Content(schema = @Schema(implementation = UdpProfileDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "UDP profile not found")
    })
    public ResponseEntity<UdpProfileDto> getUdpProfileById(
            @Parameter(description = "UDP profile ID") @PathVariable Long id) {

        User user = SecurityUtils.getCurrentUser();
        UdpProfileDto udpProfile = udpProfileService.getUdpProfileById(id, user);
        return ResponseEntity.ok(udpProfile);
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Get UDP profiles by device", description = "Retrieves all UDP profiles for a specific device")
    @ApiResponse(responseCode = "200", description = "UDP profiles retrieved successfully")
    public ResponseEntity<List<UdpProfileDto>> getUdpProfilesByDevice(
            @Parameter(description = "Device ID") @PathVariable Long deviceId) {

        User user = SecurityUtils.getCurrentUser();
        List<UdpProfileDto> profiles = udpProfileService.getUdpProfilesByDevice(deviceId, user);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/device/{deviceId}/status/{status}")
    @Operation(summary = "Get UDP profiles by device and status", description = "Retrieves UDP profiles filtered by entry status for a specific device")
    @ApiResponse(responseCode = "200", description = "UDP profiles retrieved successfully")
    public ResponseEntity<List<UdpProfileDto>> getUdpProfilesByDeviceAndStatus(
            @Parameter(description = "Device ID") @PathVariable Long deviceId,
            @Parameter(description = "UDP entry status") @PathVariable UdpProfile.UdpEntryStatus status) {

        User user = SecurityUtils.getCurrentUser();
        List<UdpProfileDto> profiles = udpProfileService.getUdpProfilesByDeviceAndStatus(deviceId, status, user);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/device/{deviceId}/search")
    @Operation(summary = "Search UDP profiles by address", description = "Searches UDP profiles by local address for a specific device")
    @ApiResponse(responseCode = "200", description = "UDP profiles retrieved successfully")
    public ResponseEntity<List<UdpProfileDto>> searchUdpProfilesByAddress(
            @Parameter(description = "Device ID") @PathVariable Long deviceId,
            @Parameter(description = "Search term") @RequestParam String address) {

        User user = SecurityUtils.getCurrentUser();
        List<UdpProfileDto> profiles = udpProfileService.searchUdpProfilesByAddress(deviceId, address, user);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/device/{deviceId}/count")
    @Operation(summary = "Get UDP profile count for device", description = "Returns the number of UDP profiles for a specific device")
    @ApiResponse(responseCode = "200", description = "UDP profile count retrieved successfully")
    public ResponseEntity<Long> getUdpProfileCountByDevice(
            @Parameter(description = "Device ID") @PathVariable Long deviceId) {

        User user = SecurityUtils.getCurrentUser();
        Long count = udpProfileService.countUdpProfilesByDevice(deviceId, user);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/device/{deviceId}/exists")
    @Operation(summary = "Check if UDP address/port exists", description = "Checks if a UDP local address and port combination already exists for the device")
    @ApiResponse(responseCode = "200", description = "Existence check completed")
    public ResponseEntity<Boolean> udpAddressPortExists(
            @Parameter(description = "Device ID") @PathVariable Long deviceId,
            @Parameter(description = "Local address") @RequestParam String localAddress,
            @Parameter(description = "Local port") @RequestParam Integer localPort) {

        User user = SecurityUtils.getCurrentUser();
        Boolean exists = udpProfileService.existsByDeviceAndLocalAddressAndPort(deviceId, localAddress, localPort, user);
        return ResponseEntity.ok(exists);
    }
}
