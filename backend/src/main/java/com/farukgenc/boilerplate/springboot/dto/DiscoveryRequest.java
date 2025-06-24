package com.farukgenc.boilerplate.springboot.dto;

import lombok.*;

import java.util.List;

/**
 * DTO for network discovery requests
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryRequest {

    private String name;
    private String description;
    
    // Network range settings
    private String networkRange; // e.g., "192.168.1.0/24"
    private List<String> specificIps; // Specific IP addresses to scan
    
    // Discovery methods
    @Builder.Default
    private boolean enablePing = true;
    @Builder.Default
    private boolean enableSnmp = true;
    @Builder.Default
    private boolean enablePortScan = false;
    
    // SNMP settings
    @Builder.Default
    private String snmpCommunity = "public";
    @Builder.Default
    private Integer snmpPort = 161;
    @Builder.Default
    private Integer snmpTimeout = 5000;
    @Builder.Default
    private Integer snmpRetries = 3;
    
    // Port scan settings
    private List<Integer> portsToScan; // Common ports: 22, 23, 80, 443, etc.
    
    // Threading and performance
    @Builder.Default
    private Integer maxThreads = 50;
    @Builder.Default
    private Integer timeout = 10000; // Overall timeout in ms
    
    // Discovery depth
    @Builder.Default
    private boolean discoverInterfaces = true;
    @Builder.Default
    private boolean discoverRoutes = false;
    @Builder.Default
    private boolean discoverArpTable = false;
}
