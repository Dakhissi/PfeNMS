package com.farukgenc.boilerplate.springboot.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for discovery process status
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryStatus {
    
    private String discoveryId;
    private String status; // PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
    private String phase; // PING_SCAN, SNMP_DISCOVERY, PORT_SCAN, ANALYSIS
    private int totalHosts;
    private int scannedHosts;
    private int discoveredDevices;
    private double progressPercentage;
    private String message;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String errorMessage;
    //websocketEndpoint
    private String websocketEndpoint; // URL for WebSocket connection to receive updates
}
