package com.farukgenc.boilerplate.springboot.service.discovery;

import com.farukgenc.boilerplate.springboot.dto.DiscoveryProgressDto;
import com.farukgenc.boilerplate.springboot.dto.NetworkNodeDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for network discovery using SNMP protocol
 */
public interface SnmpDiscoveryService {

    /**
     * Discover devices and their connections using SNMP
     * @param targetIp The IP address or subnet to scan
     * @param community The SNMP community string
     * @param version SNMP version (1, 2, 3)
     * @param progressCallback Callback for reporting discovery progress
     * @return CompletableFuture with discovered nodes
     */
    CompletableFuture<List<NetworkNodeDto>> discoverDevices(String targetIp,
                                                          String community,
                                                          int version,
                                                          ProgressCallback progressCallback);

    /**
     * Discover device details using SNMP (more detailed information)
     * @param deviceIp The IP address of the device
     * @param community The SNMP community string
     * @param version SNMP version
     * @return CompletableFuture with discovered node details
     */
    CompletableFuture<NetworkNodeDto> discoverDeviceDetails(String deviceIp,
                                                         String community,
                                                         int version);

    /**
     * Discover Layer 2 connections using SNMP
     * @param deviceIp The IP address of the device
     * @param community The SNMP community string
     * @param version SNMP version
     * @return CompletableFuture with layer 2 neighbor information
     */
    CompletableFuture<List<NetworkNodeDto>> discoverLayer2Neighbors(String deviceIp,
                                                                String community,
                                                                int version);

    /**
     * Discover Layer 3 connections using SNMP
     * @param deviceIp The IP address of the device
     * @param community The SNMP community string
     * @param version SNMP version
     * @return CompletableFuture with layer 3 routing information
     */
    CompletableFuture<List<NetworkNodeDto>> discoverLayer3Neighbors(String deviceIp,
                                                                String community,
                                                                int version);
}
