package com.farukgenc.boilerplate.springboot.dto;

import lombok.*;

/**
 * DTO for discovered network interfaces
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveredInterface {

    private Integer ifIndex;
    private String ifName;
    private String ifDescr;
    private String ifAlias;
    private InterfaceType ifType;
    private Long ifSpeed;
    private String ifMacAddress;
    private InterfaceStatus ifAdminStatus;
    private InterfaceStatus ifOperStatus;
    private String ipAddress;
    private String subnetMask;
    private Long ifInOctets;
    private Long ifOutOctets;
    private Long ifInErrors;
    private Long ifOutErrors;

    public enum InterfaceType {
        ETHERNET, LOOPBACK, TUNNEL, PPP, ATM, 
        FRAME_RELAY, ISDN, OTHER, UNKNOWN
    }

    public enum InterfaceStatus {
        UP, DOWN, TESTING, UNKNOWN, DORMANT, NOT_PRESENT, LOWER_LAYER_DOWN
    }
}
