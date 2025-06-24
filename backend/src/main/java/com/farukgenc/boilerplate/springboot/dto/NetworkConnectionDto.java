package com.farukgenc.boilerplate.springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkConnectionDto {
    private String id;
    private String sourceNodeId;
    private String targetNodeId;
    private String sourceInterface;
    private String targetInterface;
    private ConnectionType connectionType;
    private int metric; // Routing metric or cost
    private String protocol; // e.g., OSPF, BGP, Static
    private boolean isRedundant;

    public enum ConnectionType {
        LAYER2,
        LAYER3,
        WIRELESS,
        VIRTUAL,
        UNKNOWN
    }
}
