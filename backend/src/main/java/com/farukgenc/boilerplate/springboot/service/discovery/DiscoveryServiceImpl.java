package com.farukgenc.boilerplate.springboot.service.discovery;

import com.farukgenc.boilerplate.springboot.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Main implementation of discovery service that coordinates specialized discovery services
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscoveryServiceImpl implements DiscoveryService {

    private final SnmpDiscoveryService snmpDiscoveryService;
    private final NmapDiscoveryService nmapDiscoveryService;
    private final SimpMessagingTemplate messagingTemplate;

    // Store ongoing discoveries and their results
    private final Map<String, TopologyResponseDto> discoveryResults = new ConcurrentHashMap<>();
    private final Map<String, DiscoveryRequestDto> discoveryRequests = new ConcurrentHashMap<>();
    private final Map<String, Future<?>> discoveryTasks = new ConcurrentHashMap<>();

    @Override
    public String startDiscovery(DiscoveryRequestDto request) {
        // Generate unique discovery ID
        String discoveryId = UUID.randomUUID().toString();

        // Store request
        discoveryRequests.put(discoveryId, request);

        // Initialize empty result
        TopologyResponseDto initialResult = TopologyResponseDto.builder()
                .discoveryId(discoveryId)
                .nodes(new ArrayList<>())
                .connections(new ArrayList<>())
                .status("IN_PROGRESS")
                .build();
        discoveryResults.put(discoveryId, initialResult);

        // Start discovery in a separate thread
        Future<?> task = executeDiscoveryAsync(discoveryId, request);
        discoveryTasks.put(discoveryId, task);

        // Return discovery ID for tracking progress
        return discoveryId;
    }

    @Override
    public TopologyResponseDto getDiscoveryResult(String discoveryId) {
        return discoveryResults.getOrDefault(discoveryId,
            TopologyResponseDto.builder()
                .discoveryId(discoveryId)
                .status("NOT_FOUND")
                .build());
    }

    @Override
    public boolean cancelDiscovery(String discoveryId) {
        Future<?> task = discoveryTasks.get(discoveryId);
        if (task != null && !task.isDone()) {
            boolean cancelled = task.cancel(true);
            if (cancelled) {
                TopologyResponseDto result = discoveryResults.get(discoveryId);
                if (result != null) {
                    result.setStatus("CANCELLED");
                }
                sendProgressUpdate(discoveryId, DiscoveryProgressDto.builder()
                    .discoveryId(discoveryId)
                    .stage(DiscoveryProgressDto.DiscoveryStage.FAILED)
                    .statusMessage("Discovery cancelled by user")
                    .complete(true)
                    .build());
            }
            return cancelled;
        }
        return false;
    }

    @Async
    protected Future<?> executeDiscoveryAsync(String discoveryId, DiscoveryRequestDto request) {
        Instant startTime = Instant.now();
        Set<String> processedDevices = ConcurrentHashMap.newKeySet();
        Set<String> pendingDevices = ConcurrentHashMap.newKeySet();
        Set<String> discoveredDevices = ConcurrentHashMap.newKeySet();

        // Add initial target to pending
        pendingDevices.add(request.getTarget());

        try {
            // Send initial progress
            sendProgressUpdate(discoveryId, DiscoveryProgressDto.builder()
                .discoveryId(discoveryId)
                .percentComplete(0)
                .stage(DiscoveryProgressDto.DiscoveryStage.INITIALIZING)
                .currentActivity("Initializing discovery process")
                .devicesFound(0)
                .build());

            // Create thread pool for parallel discovery
            ExecutorService executorService =
                Executors.newFixedThreadPool(request.getThreadCount());

            // Set up progress counter
            AtomicInteger discoveredCount = new AtomicInteger(0);

            // Initial scan iteration - ICMP and Nmap discovery
            if (request.isUseIcmp() || request.isUseNmap()) {
                sendProgressUpdate(discoveryId, DiscoveryProgressDto.builder()
                    .discoveryId(discoveryId)
                    .percentComplete(5)
                    .stage(request.isUseNmap() ?
                           DiscoveryProgressDto.DiscoveryStage.NMAP_SCAN :
                           DiscoveryProgressDto.DiscoveryStage.ICMP_SCAN)
                    .currentActivity("Performing initial network scan")
                    .currentTarget(request.getTarget())
                    .devicesFound(0)
                    .build());

                // Use Nmap for initial host discovery if enabled
                List<NetworkNodeDto> initialNodes = new ArrayList<>();
                if (request.isUseNmap()) {
                    // Use Nmap to discover devices
                    initialNodes = nmapDiscoveryService.discoverDevices(
                        request.getTarget(),
                        request.getNmapOptions(),
                        progress -> sendProgressUpdate(discoveryId, progress)
                    ).get(); // We need to wait for initial scan
                }

                // Update the discovery result with initial nodes
                updateDiscoveryResults(discoveryId, initialNodes, Collections.emptyList());

                // Add discovered IPs to pending for SNMP scan
                initialNodes.forEach(node -> {
                    if (node.getIpAddress() != null && !node.getIpAddress().isEmpty()) {
                        pendingDevices.add(node.getIpAddress());
                        discoveredDevices.add(node.getIpAddress());
                    }
                });

                discoveredCount.addAndGet(initialNodes.size());

                sendProgressUpdate(discoveryId, DiscoveryProgressDto.builder()
                    .discoveryId(discoveryId)
                    .percentComplete(20)
                    .stage(DiscoveryProgressDto.DiscoveryStage.SNMP_DISCOVERY)
                    .currentActivity("Initial scan complete, found " + discoveredCount.get() + " devices")
                    .devicesFound(discoveredCount.get())
                    .build());
            }

            // SNMP Discovery - iterate through pending devices with bounded hop count
            int currentHop = 0;
            while (!pendingDevices.isEmpty() && currentHop < request.getMaxHops()) {
                currentHop++;

                // Get current batch of pending devices
                List<String> currentBatch = new ArrayList<>(pendingDevices);
                pendingDevices.clear();

                // Skip already processed devices
                currentBatch = currentBatch.stream()
                    .filter(ip -> !processedDevices.contains(ip))
                    .collect(Collectors.toList());

                if (currentBatch.isEmpty()) {
                    continue;
                }

                sendProgressUpdate(discoveryId, DiscoveryProgressDto.builder()
                    .discoveryId(discoveryId)
                    .percentComplete(20 + (60 * currentHop / request.getMaxHops()))
                    .stage(DiscoveryProgressDto.DiscoveryStage.SNMP_DISCOVERY)
                    .currentActivity("Scanning devices with SNMP (hop " + currentHop + " of " + request.getMaxHops() + ")")
                    .devicesFound(discoveredCount.get())
                    .build());

                // Process current batch in parallel using thread pool
                List<CompletableFuture<NetworkNodeDto>> futures = new ArrayList<>();

                // For each IP, scan with SNMP if enabled
                for (String ipAddress : currentBatch) {
                    processedDevices.add(ipAddress);

                    if (request.isUseSnmp()) {
                        CompletableFuture<NetworkNodeDto> future = CompletableFuture
                            .supplyAsync(() -> {
                                try {
                                    sendProgressUpdate(discoveryId, DiscoveryProgressDto.builder()
                                        .discoveryId(discoveryId)
                                        .currentTarget(ipAddress)
                                        .currentActivity("Scanning device with SNMP")
                                        .build());

                                    return snmpDiscoveryService.discoverDeviceDetails(
                                        ipAddress,
                                        request.isUseSnmpV3() ? null : request.getSnmpCommunity(),
                                        request.getSnmpVersion()
                                    ).get();
                                } catch (Exception e) {
                                    log.debug("SNMP discovery failed for {}: {}", ipAddress, e.getMessage());
                                    // Return basic node with just IP if SNMP fails
                                    return NetworkNodeDto.builder()
                                        .id(ipAddress)
                                        .ipAddress(ipAddress)
                                        .name(ipAddress)
                                        .deviceType(NetworkNodeDto.DeviceType.UNKNOWN)
                                        .reachable(true)
                                        .build();
                                }
                            }, executorService);

                        futures.add(future);
                    }
                }

                // Collect results from futures
                List<NetworkNodeDto> batchNodes = new ArrayList<>();
                List<NetworkConnectionDto> batchConnections = new ArrayList<>();

                for (CompletableFuture<NetworkNodeDto> future : futures) {
                    try {
                        NetworkNodeDto node = future.get();
                        if (node != null) {
                            batchNodes.add(node);
                            discoveredCount.incrementAndGet();

                            // If this is a network device, discover layer 2 and layer 3 neighbors
                            if (node.getDeviceType() == NetworkNodeDto.DeviceType.ROUTER ||
                                node.getDeviceType() == NetworkNodeDto.DeviceType.SWITCH) {

                                // Layer 2 discovery if enabled
                                if (request.isDiscoverLayer2() && request.isUseSnmp()) {
                                    try {
                                        List<NetworkNodeDto> l2Neighbors = snmpDiscoveryService.discoverLayer2Neighbors(
                                            node.getIpAddress(),
                                            request.isUseSnmpV3() ? null : request.getSnmpCommunity(),
                                            request.getSnmpVersion()
                                        ).get();

                                        // Add connections and new neighbors
                                        for (NetworkNodeDto neighbor : l2Neighbors) {
                                            // Add to pending if not already processed
                                            if (!processedDevices.contains(neighbor.getIpAddress())) {
                                                pendingDevices.add(neighbor.getIpAddress());
                                            }

                                            // Create layer 2 connection
                                            NetworkConnectionDto connection = NetworkConnectionDto.builder()
                                                .id(UUID.randomUUID().toString())
                                                .sourceNodeId(node.getId())
                                                .targetNodeId(neighbor.getId())
                                                .connectionType(NetworkConnectionDto.ConnectionType.LAYER2)
                                                .build();

                                            batchConnections.add(connection);
                                        }
                                    } catch (Exception e) {
                                        log.debug("Layer 2 discovery failed for {}: {}", node.getIpAddress(), e.getMessage());
                                    }
                                }

                                // Layer 3 discovery if enabled
                                if (request.isDiscoverLayer3() && request.isUseSnmp()) {
                                    try {
                                        List<NetworkNodeDto> l3Neighbors = snmpDiscoveryService.discoverLayer3Neighbors(
                                            node.getIpAddress(),
                                            request.isUseSnmpV3() ? null : request.getSnmpCommunity(),
                                            request.getSnmpVersion()
                                        ).get();

                                        // Add connections and new neighbors
                                        for (NetworkNodeDto neighbor : l3Neighbors) {
                                            // Add to pending if not already processed
                                            if (!processedDevices.contains(neighbor.getIpAddress())) {
                                                pendingDevices.add(neighbor.getIpAddress());
                                            }

                                            // Create layer 3 connection
                                            NetworkConnectionDto connection = NetworkConnectionDto.builder()
                                                .id(UUID.randomUUID().toString())
                                                .sourceNodeId(node.getId())
                                                .targetNodeId(neighbor.getId())
                                                .connectionType(NetworkConnectionDto.ConnectionType.LAYER3)
                                                .build();

                                            batchConnections.add(connection);
                                        }
                                    } catch (Exception e) {
                                        log.debug("Layer 3 discovery failed for {}: {}", node.getIpAddress(), e.getMessage());
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Failed to process device: " + e.getMessage());
                    }
                }

                // Update discovery results with this batch
                updateDiscoveryResults(discoveryId, batchNodes, batchConnections);

                // Update progress
                sendProgressUpdate(discoveryId, DiscoveryProgressDto.builder()
                    .discoveryId(discoveryId)
                    .percentComplete(20 + (60 * currentHop / request.getMaxHops()))
                    .stage(DiscoveryProgressDto.DiscoveryStage.SNMP_DISCOVERY)
                    .currentActivity("Completed hop " + currentHop + " of " + request.getMaxHops())
                    .devicesFound(discoveredCount.get())
                    .connectionsFound(batchConnections.size())
                    .build());
            }

            // Final LLDP discovery if enabled
            if (request.isUseLldp()) {
                sendProgressUpdate(discoveryId, DiscoveryProgressDto.builder()
                    .discoveryId(discoveryId)
                    .percentComplete(80)
                    .stage(DiscoveryProgressDto.DiscoveryStage.LLDP_DISCOVERY)
                    .currentActivity("Discovering LLDP connections")
                    .devicesFound(discoveredCount.get())
                    .build());

                // LLDP discovery would be implemented here
                // (Usually part of SNMP but could be a separate step)
            }

            // Finalize topology
            sendProgressUpdate(discoveryId, DiscoveryProgressDto.builder()
                .discoveryId(discoveryId)
                .percentComplete(90)
                .stage(DiscoveryProgressDto.DiscoveryStage.BUILDING_TOPOLOGY)
                .currentActivity("Finalizing network topology")
                .devicesFound(discoveredCount.get())
                .build());

            // Get final result and update it
            TopologyResponseDto finalResult = discoveryResults.get(discoveryId);
            finalResult.setStatus("COMPLETE");
            finalResult.setTotalDevicesDiscovered(discoveredCount.get());
            finalResult.setDiscoveryDurationMs(Duration.between(startTime, Instant.now()).toMillis());

            // Ensure uniqueness of nodes and connections
            deduplicateTopology(finalResult);

            // Shutdown thread pool
            executorService.shutdown();

            // Send final progress update
            sendProgressUpdate(discoveryId, DiscoveryProgressDto.builder()
                .discoveryId(discoveryId)
                .percentComplete(100)
                .stage(DiscoveryProgressDto.DiscoveryStage.COMPLETED)
                .currentActivity("Discovery completed successfully")
                .devicesFound(discoveredCount.get())
                .connectionsFound(finalResult.getConnections().size())
                .complete(true)
                .build());

            return CompletableFuture.completedFuture(finalResult);

        } catch (Exception e) {
            log.error("Discovery failed: " + e.getMessage(), e);

            // Update discovery result with error
            TopologyResponseDto errorResult = discoveryResults.get(discoveryId);
            if (errorResult != null) {
                errorResult.setStatus("FAILED");
                if (errorResult.getWarnings() == null) {
                    errorResult.setWarnings(new ArrayList<>());
                }
                errorResult.getWarnings().add("Discovery failed: " + e.getMessage());
            }

            // Send error progress update
            sendProgressUpdate(discoveryId, DiscoveryProgressDto.builder()
                .discoveryId(discoveryId)
                .stage(DiscoveryProgressDto.DiscoveryStage.FAILED)
                .statusMessage("Discovery failed: " + e.getMessage())
                .complete(true)
                .build());

            throw new RuntimeException("Discovery failed", e);
        }
    }

    /**
     * Update discovery results with newly found nodes and connections
     */
    private synchronized void updateDiscoveryResults(String discoveryId,
                                                   List<NetworkNodeDto> newNodes,
                                                   List<NetworkConnectionDto> newConnections) {
        TopologyResponseDto result = discoveryResults.get(discoveryId);
        if (result != null) {
            // Add new nodes
            if (result.getNodes() == null) {
                result.setNodes(new ArrayList<>());
            }
            result.getNodes().addAll(newNodes);

            // Add new connections
            if (result.getConnections() == null) {
                result.setConnections(new ArrayList<>());
            }
            result.getConnections().addAll(newConnections);
        }
    }

    /**
     * Ensure topology has unique nodes and connections
     */
    private void deduplicateTopology(TopologyResponseDto topology) {
        // Deduplicate nodes by IP address
        Map<String, NetworkNodeDto> uniqueNodes = new LinkedHashMap<>();
        for (NetworkNodeDto node : topology.getNodes()) {
            if (node.getIpAddress() != null && !node.getIpAddress().isEmpty()) {
                uniqueNodes.putIfAbsent(node.getIpAddress(), node);
            } else if (node.getMacAddress() != null && !node.getMacAddress().isEmpty()) {
                uniqueNodes.putIfAbsent(node.getMacAddress(), node);
            } else {
                uniqueNodes.putIfAbsent(node.getId(), node);
            }
        }

        // Replace nodes with unique set
        topology.setNodes(new ArrayList<>(uniqueNodes.values()));

        // Deduplicate connections
        Set<String> uniqueConnectionKeys = new HashSet<>();
        List<NetworkConnectionDto> uniqueConnections = new ArrayList<>();

        for (NetworkConnectionDto conn : topology.getConnections()) {
            String key1 = conn.getSourceNodeId() + "-" + conn.getTargetNodeId();
            String key2 = conn.getTargetNodeId() + "-" + conn.getSourceNodeId();

            if (!uniqueConnectionKeys.contains(key1) && !uniqueConnectionKeys.contains(key2)) {
                uniqueConnectionKeys.add(key1);
                uniqueConnections.add(conn);
            }
        }

        // Replace connections with unique set
        topology.setConnections(uniqueConnections);
    }

    /**
     * Send progress update via WebSocket
     */
    private void sendProgressUpdate(String discoveryId, DiscoveryProgressDto progress) {
        if (progress.getDiscoveryId() == null) {
            progress.setDiscoveryId(discoveryId);
        }

        // Log progress update
        if (progress.getCurrentActivity() != null) {
            log.debug("Discovery {}: {}", discoveryId, progress.getCurrentActivity());
        }

        // Send via WebSocket to specific discovery channel
        messagingTemplate.convertAndSend("/topic/discovery/" + discoveryId + "/progress", progress);
    }
}
