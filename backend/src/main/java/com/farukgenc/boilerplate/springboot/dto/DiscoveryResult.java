package com.farukgenc.boilerplate.springboot.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for discovery results
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryResult {    private Long id;
    private String discoveryId;
    private String name;
    private String description;
    private String networkRange;
    private DiscoveryStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime discoveryTime;
    private Long duration; // in milliseconds
    private String errorMessage;
    private boolean success;
    
    // Discovery statistics
    private Integer totalIpsScanned;
    private Integer devicesFound;
    private Integer totalDevicesFound;
    private Integer interfacesFound;
    private Integer connectionsFound;
    
    // Discovered topology
    private List<DiscoveredDevice> devices;
    private List<DiscoveredConnection> connections;
    
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;

    public enum DiscoveryStatus {
        PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
    }
}
