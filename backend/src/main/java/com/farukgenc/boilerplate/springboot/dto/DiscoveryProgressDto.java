package com.farukgenc.boilerplate.springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryProgressDto {
    private String discoveryId;
    private int percentComplete;
    private String currentActivity;
    private int devicesFound;
    private int connectionsFound;
    private String currentTarget;
    private DiscoveryStage stage;
    private boolean complete;
    private String statusMessage;

    public enum DiscoveryStage {
        INITIALIZING,
        ICMP_SCAN,
        NMAP_SCAN,
        SNMP_DISCOVERY,
        LLDP_DISCOVERY,
        BUILDING_TOPOLOGY,
        COMPLETED,
        FAILED
    }
}
