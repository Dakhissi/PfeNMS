package com.farukgenc.boilerplate.springboot.controller;

import com.farukgenc.boilerplate.springboot.service.snmp.SnmpPollingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * REST controller for SNMP polling operations
 */
@RestController
@RequestMapping("/api/v1/snmp")
@RequiredArgsConstructor
@Tag(name = "SNMP Polling", description = "SNMP polling and monitoring operations")
public class SnmpPollingController {

    private final SnmpPollingService snmpPollingService;

    /**
     * Trigger SNMP polling for a specific device
     */
    @PostMapping("/poll/device/{deviceId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Poll specific device", description = "Trigger SNMP polling for a specific device")
    public ResponseEntity<Map<String, Object>> pollDevice(
            @Parameter(description = "Device ID", required = true)
            @PathVariable Long deviceId) {
        
        try {
            snmpPollingService.pollDeviceById(deviceId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Device polling initiated successfully",
                "deviceId", deviceId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Device polling failed: " + e.getMessage(),
                "deviceId", deviceId
            ));
        }
    }

    /**
     * Trigger SNMP polling for multiple devices
     */
    @PostMapping("/poll/devices")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Poll multiple devices", description = "Trigger SNMP polling for multiple devices")
    public ResponseEntity<Map<String, Object>> pollDevices(
            @Parameter(description = "List of device IDs", required = true)
            @Valid @RequestBody PollDevicesRequest request) {
        
        try {
            snmpPollingService.pollDevicesByIds(request.getDeviceIds());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Device polling initiated for " + request.getDeviceIds().size() + " devices",
                "deviceIds", request.getDeviceIds()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Device polling failed: " + e.getMessage(),
                "deviceIds", request.getDeviceIds()
            ));
        }
    }

    /**
     * Test SNMP connectivity for a device
     */
    @PostMapping("/test/device/{deviceId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Test device connectivity", description = "Test SNMP connectivity to a specific device")
    public ResponseEntity<Map<String, Object>> testDeviceConnectivity(
            @Parameter(description = "Device ID", required = true)
            @PathVariable Long deviceId) {
        
        try {
            boolean connected = snmpPollingService.testDeviceConnectivity(deviceId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "connected", connected,
                "message", connected ? "Device is reachable via SNMP" : "Device is not reachable via SNMP",
                "deviceId", deviceId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "connected", false,
                "message", "Connectivity test failed: " + e.getMessage(),
                "deviceId", deviceId
            ));
        }
    }

    /**
     * Get SNMP polling statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get polling statistics", description = "Get SNMP polling statistics and status")
    public ResponseEntity<SnmpPollingService.PollingStatistics> getPollingStatistics() {
        try {
            SnmpPollingService.PollingStatistics stats = snmpPollingService.getPollingStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Request DTO for polling multiple devices
     */
    public static class PollDevicesRequest {
        private List<Long> deviceIds;

        public List<Long> getDeviceIds() {
            return deviceIds;
        }

        public void setDeviceIds(List<Long> deviceIds) {
            this.deviceIds = deviceIds;
        }
    }
}
