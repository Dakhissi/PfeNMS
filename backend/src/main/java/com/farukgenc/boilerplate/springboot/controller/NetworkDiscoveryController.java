package com.farukgenc.boilerplate.springboot.controller;

import com.farukgenc.boilerplate.springboot.dto.DiscoveryRequestDto;
import com.farukgenc.boilerplate.springboot.dto.TopologyResponseDto;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.service.discovery.DiscoveryService;
import com.farukgenc.boilerplate.springboot.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/network-discovery")
@RequiredArgsConstructor
@Tag(name = "Network Discovery API", description = "APIs for discovering and mapping network devices")
public class NetworkDiscoveryController {

    private final DiscoveryService discoveryService;

    @PostMapping("/start")
    @Operation(summary = "Start network discovery", description = "Initiates a network discovery process with the specified parameters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Discovery started successfully",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> startDiscovery(@RequestBody @Validated DiscoveryRequestDto request) {
        User user = SecurityUtils.getCurrentUser();
        log.info("Starting network discovery for target: {} requested by user: {}",
                request.getTarget(), user.getUsername());

        // Start the discovery process
        String discoveryId = discoveryService.startDiscovery(request);

        // Return the discovery ID for tracking progress
        Map<String, Object> response = new HashMap<>();
        response.put("discoveryId", discoveryId);
        response.put("message", "Discovery started successfully");
        response.put("websocketEndpoint", "/topic/discovery/" + discoveryId + "/progress");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{discoveryId}")
    @Operation(summary = "Get discovery results", description = "Retrieve the results of a network discovery process")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Discovery results retrieved successfully",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TopologyResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Discovery not found")
    })
    public ResponseEntity<TopologyResponseDto> getDiscoveryResults(@PathVariable String discoveryId) {
        log.debug("Getting discovery results for ID: {}", discoveryId);

        TopologyResponseDto result = discoveryService.getDiscoveryResult(discoveryId);

        if (result.getStatus() != null && !result.getStatus().equals("NOT_FOUND")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{discoveryId}")
    @Operation(summary = "Cancel discovery", description = "Cancel an ongoing discovery process")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Discovery cancelled successfully"),
        @ApiResponse(responseCode = "404", description = "Discovery not found or already completed")
    })
    public ResponseEntity<Map<String, Object>> cancelDiscovery(@PathVariable String discoveryId) {
        log.info("Cancelling discovery with ID: {}", discoveryId);

        boolean cancelled = discoveryService.cancelDiscovery(discoveryId);

        Map<String, Object> response = new HashMap<>();
        if (cancelled) {
            response.put("message", "Discovery cancelled successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Discovery not found or already completed");
            return ResponseEntity.notFound().build();
        }
    }
}
