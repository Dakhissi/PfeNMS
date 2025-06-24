package com.farukgenc.boilerplate.springboot.controller;

import com.farukgenc.boilerplate.springboot.dto.*;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.service.discovery.DiscoveryService;
import com.farukgenc.boilerplate.springboot.service.browser.MibService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.util.List;

/**
 * REST controller for network discovery operations
 */
@Slf4j
@RestController
@RequestMapping("/api/discovery")
@RequiredArgsConstructor
@Tag(name = "Network Discovery", description = "APIs for network topology discovery using SNMP and Nmap")
public class DiscoveryController {

    private final DiscoveryService discoveryService;
    private final MibService mibService;

    @PostMapping("/start")
    @Operation(summary = "Start network discovery",
            description = "Starts an asynchronous network discovery process using the specified configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Discovery started successfully",
                    content = @Content(schema = @Schema(implementation = DiscoveryStatus.class))),
            @ApiResponse(responseCode = "400", description = "Invalid discovery request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<DiscoveryStatus> startDiscovery(
            @Valid @RequestBody DiscoveryRequest request) {
        User user = SecurityUtils.getCurrentUser();

        log.info("Starting discovery for user: {} with name: {}", user.getUsername(), request.getName());

        // Convert DiscoveryRequest to DiscoveryRequestDto
        DiscoveryRequestDto requestDto = convertToRequestDto(request, user);

        // Call the service with the correct parameter type
        String discoveryId = discoveryService.startDiscovery(requestDto);

        // Return initial status
        DiscoveryStatus status = DiscoveryStatus.builder()
                .discoveryId(discoveryId)
                .status("PENDING")
                .phase("INITIALIZATION")
                .message("Discovery request accepted and queued for processing")
                .websocketEndpoint("/topic/discovery/" + discoveryId + "/progress")
                .build();

        return new ResponseEntity<>(status, HttpStatus.ACCEPTED);
    }

    private DiscoveryRequestDto convertToRequestDto(DiscoveryRequest request, User user) {
        DiscoveryRequestDto dto = new DiscoveryRequestDto();
        dto.setTarget(request.getNetworkRange());
        dto.setUseSnmp(request.isEnableSnmp());
        dto.setUseNmap(request.isEnablePortScan());
        dto.setUseIcmp(request.isEnablePing());
        dto.setUseLldp(false); // Not available in request
        
        // Map SNMP settings from request fields
        dto.setSnmpCommunity(request.getSnmpCommunity());
        dto.setSnmpVersion(2); // Default to v2c
        dto.setSnmpPort(request.getSnmpPort());
        dto.setSnmpTimeout(request.getSnmpTimeout());
        dto.setSnmpRetries(request.getSnmpRetries());
        
        // Set defaults for missing fields
        dto.setMaxHops(3);
        dto.setDiscoverLayer2(request.isDiscoverInterfaces());
        dto.setDiscoverLayer3(request.isDiscoverRoutes());
        dto.setThreadCount(request.getMaxThreads());
        
        return dto;
    }

    @GetMapping("/status/{discoveryId}")
    @Operation(summary = "Get discovery status and result",
            description = "Gets the current status and result of a discovery process")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TopologyResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Discovery not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<TopologyResponseDto> getDiscoveryStatus(
            @Parameter(description = "Discovery ID") @PathVariable String discoveryId) {
        User user = SecurityUtils.getCurrentUser();

        log.debug("Getting discovery status for ID: {} by user: {}", discoveryId, user.getUsername());
        TopologyResponseDto result = discoveryService.getDiscoveryResult(discoveryId);
        if (result == null || "NOT_FOUND".equals(result.getStatus())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/cancel/{discoveryId}")
    @Operation(summary = "Cancel discovery",
            description = "Cancels an ongoing discovery process")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Discovery cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Discovery not found"),
            @ApiResponse(responseCode = "400", description = "Discovery cannot be cancelled"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<String> cancelDiscovery(
            @Parameter(description = "Discovery ID") @PathVariable String discoveryId) {
        User user = SecurityUtils.getCurrentUser();

        log.info("Cancelling discovery ID: {} by user: {}", discoveryId, user.getUsername());
        boolean cancelled = discoveryService.cancelDiscovery(discoveryId);
        if (cancelled) {
            return ResponseEntity.ok("Discovery cancelled successfully");
        } else {
            return ResponseEntity.badRequest().body("Discovery cannot be cancelled or not found");
        }
    }

    @PostMapping("/ping")
    @Operation(summary = "Ping a host",
            description = "Performs a simple ping test to check if a host is reachable")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ping completed",
                    content = @Content(schema = @Schema(implementation = PingResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid IP address"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<PingResult> pingHost(
            @Parameter(description = "IP address to ping") @RequestParam String ipAddress,
            @Parameter(description = "Timeout in milliseconds", example = "5000")
            @RequestParam(defaultValue = "5000") int timeout) {
        User user = SecurityUtils.getCurrentUser();

        log.debug("Pinging host: {} by user: {}", ipAddress, user.getUsername());
        
        // Simple ping implementation using Java's InetAddress
        try {
            InetAddress inet = InetAddress.getByName(ipAddress);
            long startTime = System.currentTimeMillis();
            boolean reachable = inet.isReachable(timeout);
            long responseTime = System.currentTimeMillis() - startTime;
            
            PingResult result = PingResult.builder()
                    .ipAddress(ipAddress)
                    .reachable(reachable)
                    .responseTime(reachable ? responseTime : -1)
                    .hostname(inet.getHostName())
                    .packetLoss(reachable ? 0 : 100)
                    .build();
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error pinging host {}: {}", ipAddress, e.getMessage());
            PingResult result = PingResult.builder()
                    .ipAddress(ipAddress)
                    .reachable(false)
                    .responseTime(-1)
                    .errorMessage(e.getMessage())
                    .packetLoss(100)
                    .build();
            return ResponseEntity.ok(result);
        }
    }

    @PostMapping("/snmp-walk")
    @Operation(summary = "Perform SNMP walk",
            description = "Performs an SNMP walk operation on a device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SNMP walk completed",
                    content = @Content(schema = @Schema(implementation = MibBrowserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<MibBrowserResponse>> snmpWalk(
            @Parameter(description = "IP address of the device") @RequestParam String ipAddress,
            @Parameter(description = "SNMP community string", example = "public")
            @RequestParam(defaultValue = "public") String community,
            @Parameter(description = "Starting OID", example = "1.3.6.1.2.1.1")
            @RequestParam(defaultValue = "1.3.6.1.2.1.1") String oid) {
        User user = SecurityUtils.getCurrentUser();

        log.debug("Performing SNMP walk on: {} by user: {}", ipAddress, user.getUsername());
        
        // Create MibBrowserRequest for the SNMP walk
        MibBrowserRequest request = MibBrowserRequest.builder()
                .targetIp(ipAddress)
                .community(community)
                .oid(oid)
                .snmpPort(161)
                .timeout(5000)
                .retries(3)
                .build();
        
        try {
            List<MibBrowserResponse> result = mibService.performSnmpWalk(request, user);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error performing SNMP walk on {}: {}", ipAddress, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
