package com.farukgenc.boilerplate.springboot.service.discovery;

import com.farukgenc.boilerplate.springboot.dto.NetworkNodeDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for network discovery using Nmap
 */
public interface NmapDiscoveryService {

    /**
     * Discover devices using Nmap scan
     * @param targetSpec The target specification (IP, range, subnet)
     * @param options Nmap scan options
     * @param progressCallback Callback for reporting discovery progress
     * @return CompletableFuture with discovered nodes
     */
    CompletableFuture<List<NetworkNodeDto>> discoverDevices(String targetSpec,
                                                         String options,
                                                         ProgressCallback progressCallback);

    /**
     * Perform detailed scan of a device
     * @param ipAddress The IP address of the device
     * @param options Nmap scan options
     * @return CompletableFuture with detailed node information
     */
    CompletableFuture<NetworkNodeDto> scanDevice(String ipAddress, String options);

    /**
     * Perform OS detection on a device
     * @param ipAddress The IP address of the device
     * @return CompletableFuture with OS detection results
     */
    CompletableFuture<String> detectOperatingSystem(String ipAddress);

    /**
     * Scan for open ports and services
     * @param ipAddress The IP address of the device
     * @return CompletableFuture with port and service information
     */
    CompletableFuture<List<String>> scanServices(String ipAddress);
}
