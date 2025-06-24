package com.farukgenc.boilerplate.springboot.controller;

import com.farukgenc.boilerplate.springboot.dto.DiscoveryProgressDto;
import com.farukgenc.boilerplate.springboot.service.discovery.DiscoveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket controller for handling discovery-related WebSocket messages
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class DiscoveryWebSocketController {

    private final DiscoveryService discoveryService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle client subscription to discovery progress
     * This allows clients to get the current status when they first subscribe
     */
    @MessageMapping("/discovery/{discoveryId}/subscribe")
    public void handleSubscription(@DestinationVariable String discoveryId) {
        log.debug("Client subscribed to discovery progress for ID: {}", discoveryId);

        // Send current status
        var result = discoveryService.getDiscoveryResult(discoveryId);

        // Create a progress DTO based on the current state
        DiscoveryProgressDto progress = DiscoveryProgressDto.builder()
            .discoveryId(discoveryId)
            .currentActivity("Current discovery status")
            .devicesFound((int)result.getTotalDevicesDiscovered())
            .connectionsFound(result.getConnections() != null ? result.getConnections().size() : 0)
            .stage(getStageFromStatus(result.getStatus()))
            .complete("COMPLETE".equals(result.getStatus()) || "FAILED".equals(result.getStatus()))
            .build();

        // Send to the specific client channel
        messagingTemplate.convertAndSend("/topic/discovery/" + discoveryId + "/progress", progress);
    }

    /**
     * Handle client request to cancel a discovery
     */
    @MessageMapping("/discovery/{discoveryId}/cancel")
    public void handleCancelRequest(@DestinationVariable String discoveryId) {
        log.info("Received WebSocket request to cancel discovery: {}", discoveryId);

        boolean cancelled = discoveryService.cancelDiscovery(discoveryId);

        Map<String, Object> response = new HashMap<>();
        response.put("cancelled", cancelled);
        response.put("message", cancelled ? "Discovery cancelled" : "Could not cancel discovery");

        // Send response to client-specific channel
        messagingTemplate.convertAndSend("/topic/discovery/" + discoveryId + "/cancel", response);
    }

    /**
     * Map result status to progress stage
     */
    private DiscoveryProgressDto.DiscoveryStage getStageFromStatus(String status) {
        if (status == null) {
            return DiscoveryProgressDto.DiscoveryStage.INITIALIZING;
        }

        switch (status) {
            case "IN_PROGRESS":
                return DiscoveryProgressDto.DiscoveryStage.SNMP_DISCOVERY;
            case "COMPLETE":
                return DiscoveryProgressDto.DiscoveryStage.COMPLETED;
            case "FAILED":
                return DiscoveryProgressDto.DiscoveryStage.FAILED;
            case "CANCELLED":
                return DiscoveryProgressDto.DiscoveryStage.FAILED;
            default:
                return DiscoveryProgressDto.DiscoveryStage.INITIALIZING;
        }
    }
}
