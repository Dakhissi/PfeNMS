package com.farukgenc.boilerplate.springboot.dto;

import lombok.*;

/**
 * DTO for ping operation results
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PingResult {
    
    private String ipAddress;
    private boolean reachable;
    private long responseTime; // in milliseconds
    private String errorMessage;
    private int packetLoss; // percentage
    private String hostname;
}
