package com.farukgenc.boilerplate.springboot.controller;

import com.farukgenc.boilerplate.springboot.dto.AlertDto;
import com.farukgenc.boilerplate.springboot.dto.AlertAcknowledgeRequest;
import com.farukgenc.boilerplate.springboot.model.Alert;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.service.alert.AlertService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Tag(name = "Alert Management", description = "APIs for managing system alerts and notifications")
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @Operation(summary = "Get alerts with pagination", description = "Get all alerts for the authenticated user with pagination support")
    @ApiResponse(responseCode = "200", description = "Alerts retrieved successfully")
    public ResponseEntity<Page<AlertDto>> getAlerts(Pageable pageable) {
        User user = SecurityUtils.getCurrentUser();
        Page<AlertDto> alerts = alertService.getAlertsByUser(user, pageable);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get alert by ID", description = "Get a specific alert by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alert found",
                    content = @Content(schema = @Schema(implementation = AlertDto.class))),
            @ApiResponse(responseCode = "404", description = "Alert not found")
    })
    public ResponseEntity<AlertDto> getAlertById(
            @Parameter(description = "Alert ID") @PathVariable Long id) {
        User user = SecurityUtils.getCurrentUser();
        AlertDto alert = alertService.getAlertById(id, user);
        return ResponseEntity.ok(alert);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get alerts by status", description = "Get alerts filtered by status")
    @ApiResponse(responseCode = "200", description = "Alerts retrieved successfully")
    public ResponseEntity<List<AlertDto>> getAlertsByStatus(
            @Parameter(description = "Alert status") @PathVariable Alert.AlertStatus status) {
        User user = SecurityUtils.getCurrentUser();
        List<AlertDto> alerts = alertService.getAlertsByStatus(user, status);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/severity/{severity}")
    @Operation(summary = "Get alerts by severity", description = "Get alerts filtered by severity")
    @ApiResponse(responseCode = "200", description = "Alerts retrieved successfully")
    public ResponseEntity<List<AlertDto>> getAlertsBySeverity(
            @Parameter(description = "Alert severity") @PathVariable Alert.AlertSeverity severity) {
        User user = SecurityUtils.getCurrentUser();
        List<AlertDto> alerts = alertService.getAlertsBySeverity(user, severity);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/unacknowledged")
    @Operation(summary = "Get unacknowledged alerts", description = "Get all unacknowledged alerts")
    @ApiResponse(responseCode = "200", description = "Unacknowledged alerts retrieved successfully")
    public ResponseEntity<List<AlertDto>> getUnacknowledgedAlerts() {
        User user = SecurityUtils.getCurrentUser();
        List<AlertDto> alerts = alertService.getUnacknowledgedAlerts(user);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent alerts", description = "Get recent alerts since specified time")
    @ApiResponse(responseCode = "200", description = "Recent alerts retrieved successfully")
    public ResponseEntity<List<AlertDto>> getRecentAlerts(
            @Parameter(description = "Since timestamp") @RequestParam(required = false) LocalDateTime since) {
        User user = SecurityUtils.getCurrentUser();

        LocalDateTime sinceTime = since != null ? since : LocalDateTime.now().minusHours(24);
        List<AlertDto> alerts = alertService.getRecentAlerts(user, sinceTime);
        return ResponseEntity.ok(alerts);
    }

    @PostMapping("/{id}/acknowledge")
    @Operation(summary = "Acknowledge alert", description = "Acknowledge an alert")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alert acknowledged successfully",
                    content = @Content(schema = @Schema(implementation = AlertDto.class))),
            @ApiResponse(responseCode = "404", description = "Alert not found")
    })
    public ResponseEntity<AlertDto> acknowledgeAlert(
            @Parameter(description = "Alert ID") @PathVariable Long id,
            @Valid @RequestBody AlertAcknowledgeRequest request) {
        User user = SecurityUtils.getCurrentUser();
        AlertDto alert = alertService.acknowledgeAlert(id, request, user);
        return ResponseEntity.ok(alert);
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Resolve alert", description = "Mark an alert as resolved")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alert resolved successfully",
                    content = @Content(schema = @Schema(implementation = AlertDto.class))),
            @ApiResponse(responseCode = "404", description = "Alert not found")
    })
    public ResponseEntity<AlertDto> resolveAlert(
            @Parameter(description = "Alert ID") @PathVariable Long id) {
        User user = SecurityUtils.getCurrentUser();
        AlertDto alert = alertService.resolveAlert(id, user);
        return ResponseEntity.ok(alert);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Clear alert", description = "Clear an alert (mark as cleared)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Alert cleared successfully"),
            @ApiResponse(responseCode = "404", description = "Alert not found")
    })
    public ResponseEntity<Void> clearAlert(
            @Parameter(description = "Alert ID") @PathVariable Long id) {
        User user = SecurityUtils.getCurrentUser();
        alertService.clearAlert(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get alert statistics", description = "Get alert statistics and counts")
    @ApiResponse(responseCode = "200", description = "Alert statistics retrieved successfully")
    public ResponseEntity<Map<String, Object>> getAlertStatistics() {
        User user = SecurityUtils.getCurrentUser();
        Map<String, Object> statistics = Map.of(
                "activeCount", alertService.getActiveAlertCount(user),
                "criticalCount", alertService.getCriticalAlertCount(user),
                "unacknowledgedCount", alertService.getUnacknowledgedAlerts(user).size()
        );
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Get alerts by device", description = "Get all alerts for a specific device")
    @ApiResponse(responseCode = "200", description = "Alerts retrieved successfully")
    public ResponseEntity<List<AlertDto>> getAlertsByDevice(
            @Parameter(description = "Device ID") @PathVariable Long deviceId) {
        User user = SecurityUtils.getCurrentUser();
        List<AlertDto> alerts = alertService.getAlertsByDevice(deviceId, user);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/interface/{interfaceId}")
    @Operation(summary = "Get alerts by interface", description = "Get all alerts for a specific device interface")
    @ApiResponse(responseCode = "200", description = "Alerts retrieved successfully")
    public ResponseEntity<List<AlertDto>> getAlertsByInterface(
            @Parameter(description = "Device Interface ID") @PathVariable Long interfaceId) {
        User user = SecurityUtils.getCurrentUser();
        List<AlertDto> alerts = alertService.getAlertsByInterface(interfaceId, user);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/system-unit/{systemUnitId}")
    @Operation(summary = "Get alerts by system unit", description = "Get all alerts for a specific system unit")
    @ApiResponse(responseCode = "200", description = "Alerts retrieved successfully")
    public ResponseEntity<List<AlertDto>> getAlertsBySystemUnit(
            @Parameter(description = "System Unit ID") @PathVariable Long systemUnitId) {
        User user = SecurityUtils.getCurrentUser();
        List<AlertDto> alerts = alertService.getAlertsBySystemUnit(systemUnitId, user);
        return ResponseEntity.ok(alerts);
    }


}
