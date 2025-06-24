package com.farukgenc.boilerplate.springboot.service.discovery;

import com.farukgenc.boilerplate.springboot.dto.DiscoveryRequestDto;
import com.farukgenc.boilerplate.springboot.dto.TopologyResponseDto;

import java.util.concurrent.CompletableFuture;

/**
 * Base interface for network discovery services
 */
public interface DiscoveryService {

    /**
     * Start a network discovery process
     * @param request The discovery request parameters
     * @return A unique discovery ID that can be used to track progress via WebSocket
     */
    String startDiscovery(DiscoveryRequestDto request);

    /**
     * Get the current discovery result (complete or in-progress)
     * @param discoveryId The ID of the discovery process
     * @return The current topology data
     */
    TopologyResponseDto getDiscoveryResult(String discoveryId);

    /**
     * Cancel an ongoing discovery process
     * @param discoveryId The ID of the discovery to cancel
     * @return true if successfully canceled, false otherwise
     */
    boolean cancelDiscovery(String discoveryId);
}
