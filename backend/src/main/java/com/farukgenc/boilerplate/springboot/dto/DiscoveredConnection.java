package com.farukgenc.boilerplate.springboot.dto;

import lombok.*;

/**
 * DTO for discovered network connections/topology
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveredConnection {

    private String sourceIp;
    private String targetIp;
    private String sourceInterface;
    private String targetInterface;
    private ConnectionType connectionType;
    private String protocol;
    private Integer sourcePort;
    private Integer targetPort;
    private String description;
    private Long bandwidth;
    private Double linkUtilization;

    public enum ConnectionType {
        DIRECT, ROUTED, WIRELESS, VPN, UNKNOWN
    }
}
