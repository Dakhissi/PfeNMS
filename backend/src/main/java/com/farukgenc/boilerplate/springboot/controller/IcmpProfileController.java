package com.farukgenc.boilerplate.springboot.controller;

import com.farukgenc.boilerplate.springboot.dto.IcmpProfileDto;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.service.device.IcmpProfileService;
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
@RequestMapping("/api/icmp-profiles")
@RequiredArgsConstructor
@Tag(name = "ICMP Profile Management", description = "APIs for managing device ICMP profiles")
public class IcmpProfileController {

    private final IcmpProfileService icmpProfileService;

    @PostMapping
    @Operation(summary = "Create a new ICMP profile", description = "Creates a new ICMP profile for a device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "ICMP profile created successfully",
                    content = @Content(schema = @Schema(implementation = IcmpProfileDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    public ResponseEntity<IcmpProfileDto> createIcmpProfile(
            @Valid @RequestBody IcmpProfileDto icmpProfileDto) {

        User user = SecurityUtils.getCurrentUser();
        IcmpProfileDto createdProfile = icmpProfileService.createIcmpProfile(icmpProfileDto, user);
        return new ResponseEntity<>(createdProfile, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an ICMP profile", description = "Updates an existing ICMP profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ICMP profile updated successfully",
                    content = @Content(schema = @Schema(implementation = IcmpProfileDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "ICMP profile not found")
    })
    public ResponseEntity<IcmpProfileDto> updateIcmpProfile(
            @Parameter(description = "ICMP profile ID") @PathVariable Long id,
            @Valid @RequestBody IcmpProfileDto icmpProfileDto) {

        User user = SecurityUtils.getCurrentUser();
        IcmpProfileDto updatedProfile = icmpProfileService.updateIcmpProfile(id, icmpProfileDto, user);
        return ResponseEntity.ok(updatedProfile);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an ICMP profile", description = "Deletes an ICMP profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "ICMP profile deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "ICMP profile not found")
    })
    public ResponseEntity<Void> deleteIcmpProfile(
            @Parameter(description = "ICMP profile ID") @PathVariable Long id) {

        User user = SecurityUtils.getCurrentUser();
        icmpProfileService.deleteIcmpProfile(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ICMP profile by ID", description = "Retrieves a specific ICMP profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ICMP profile found",
                    content = @Content(schema = @Schema(implementation = IcmpProfileDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "ICMP profile not found")
    })
    public ResponseEntity<IcmpProfileDto> getIcmpProfileById(
            @Parameter(description = "ICMP profile ID") @PathVariable Long id) {

        User user = SecurityUtils.getCurrentUser();
        IcmpProfileDto icmpProfile = icmpProfileService.getIcmpProfileById(id, user);
        return ResponseEntity.ok(icmpProfile);
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Get ICMP profiles by device", description = "Retrieves all ICMP profiles for a specific device")
    @ApiResponse(responseCode = "200", description = "ICMP profiles retrieved successfully")
    public ResponseEntity<List<IcmpProfileDto>> getIcmpProfilesByDevice(
            @Parameter(description = "Device ID") @PathVariable Long deviceId) {

        User user = SecurityUtils.getCurrentUser();
        List<IcmpProfileDto> profiles = icmpProfileService.getIcmpProfilesByDevice(deviceId, user);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/device/{deviceId}/count")
    @Operation(summary = "Get ICMP profile count for device", description = "Returns the number of ICMP profiles for a specific device")
    @ApiResponse(responseCode = "200", description = "ICMP profile count retrieved successfully")
    public ResponseEntity<Long> getIcmpProfileCountByDevice(
            @Parameter(description = "Device ID") @PathVariable Long deviceId) {

        User user = SecurityUtils.getCurrentUser();
        Long count = icmpProfileService.countIcmpProfilesByDevice(deviceId, user);
        return ResponseEntity.ok(count);
    }
}
