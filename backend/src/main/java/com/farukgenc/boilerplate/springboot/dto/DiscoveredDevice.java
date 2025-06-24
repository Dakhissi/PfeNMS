package com.farukgenc.boilerplate.springboot.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * DTO for discovered devices during network discovery
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveredDevice {    private String ipAddress;
    private String hostname;
    private String macAddress;
    private String vendor;
    private DeviceType deviceType;
    private String osInfo;
    private String status; // UP, DOWN, UNKNOWN
    
    // SNMP information
    private String sysName;
    private String sysDescr;
    private String sysObjectId;
    private String sysContact;
    private String sysLocation;
    private Long sysUptime;
    private boolean snmpEnabled;
    
    // System information setters for SNMP data
    public void setSystemName(String systemName) { this.sysName = systemName; }
    public void setSystemDescription(String systemDescription) { this.sysDescr = systemDescription; }
    public void setSystemContact(String systemContact) { this.sysContact = systemContact; }
    public void setSystemLocation(String systemLocation) { this.sysLocation = systemLocation; }
    public void setSnmpEnabled(boolean snmpEnabled) { this.snmpEnabled = snmpEnabled; }
    
    // Network interfaces
    private List<DiscoveredInterface> interfaces;
    
    // Open ports (if port scanning enabled)
    private List<Integer> openPorts;
    
    // Response times
    private Long pingResponseTime; // in ms
    private Long snmpResponseTime; // in ms
    private Long responseTime; // General response time for builder compatibility
    
    // Discovery method that found this device
    private DiscoveryMethod discoveryMethod;
    
    // Additional properties
    private Map<String, String> additionalProperties;

    public enum DeviceType {
        ROUTER, SWITCH, SERVER, WORKSTATION, PRINTER, 
        FIREWALL, ACCESS_POINT, UNKNOWN
    }

    public enum DiscoveryMethod {
        PING, SNMP, PORT_SCAN, ARP_TABLE, ROUTE_TABLE
    }
}
