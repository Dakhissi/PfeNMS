package com.farukgenc.boilerplate.springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceDto {
    private String id;
    private String name;
    private String description;
    private String ipAddress;
    private String subnetMask;
    private String macAddress;
    private InterfaceStatus status;
    private long inOctets; // Incoming bytes
    private long outOctets; // Outgoing bytes
    private int ifIndex; // SNMP ifIndex
    private int ifSpeed; // Interface speed in Mbps
    private String ifType; // Interface type (e.g., Ethernet, Serial)

    public enum InterfaceStatus {
        UP,
        DOWN,
        TESTING,
        UNKNOWN
    }
}
