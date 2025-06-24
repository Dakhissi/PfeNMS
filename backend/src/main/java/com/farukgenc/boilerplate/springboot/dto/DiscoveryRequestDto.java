package com.farukgenc.boilerplate.springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryRequestDto {
    private String target; // IP, subnet, or hostname (e.g., "192.168.1.0/24", "example.com")

    // Discovery methods to use
    private boolean useSnmp = true;
    private boolean useNmap = true;
    private boolean useIcmp = true;
    private boolean useLldp = false;

    // SNMP settings
    private String snmpCommunity = "public";
    private int snmpVersion = 2; // 1, 2c, or 3
    private int snmpPort = 161;
    private int snmpTimeout = 1500; // milliseconds
    private int snmpRetries = 2;

    // SNMPv3 specific settings
    private boolean useSnmpV3 = false;
    private String snmpV3Username;
    private String snmpV3AuthProtocol; // MD5, SHA
    private String snmpV3AuthPassword;
    private String snmpV3PrivProtocol; // DES, AES
    private String snmpV3PrivPassword;
    private int snmpV3SecurityLevel = 3; // 1=noAuthNoPriv, 2=authNoPriv, 3=authPriv

    // Nmap settings
    private String nmapOptions = "-sn -T4"; // Default to ping scan with normal timing

    // Discovery depth and scope
    private int maxHops = 3; // Maximum number of hops from initial target
    private boolean discoverLayer2 = true; // Discover layer 2 connections (switches, etc)
    private boolean discoverLayer3 = true; // Discover layer 3 connections (routers)

    // Scan optimization
    private int threadCount = 10; // Number of concurrent discovery threads
}
