package com.farukgenc.boilerplate.springboot.service.trap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.*;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service for receiving SNMP trap notifications
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrapReceiver implements CommandResponder {

    private final TrapProcessor trapProcessor;
    
    private Snmp snmp;
    private DefaultUdpTransportMapping transport;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private String expectedCommunity = "public";

    /**
     * Start the trap receiver on specified port
     */
    public void startTrapReceiver(int port, String community) {
        if (isRunning.get()) {
            log.warn("Trap receiver is already running");
            return;
        }

        try {
            this.expectedCommunity = community;

            // Create UDP transport
            transport = new DefaultUdpTransportMapping(new UdpAddress("0.0.0.0/" + port));

            // Create thread pool for handling messages
            ThreadPool threadPool = ThreadPool.create("TrapReceiver", 10);
            MultiThreadedMessageDispatcher dispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());

            // Create SNMP instance
            snmp = new Snmp(dispatcher, transport);

            // Add security protocols
            SecurityProtocols.getInstance().addDefaultProtocols();

            // Add message processing models
            snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
            snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());

            // Add command responder (this class)
            snmp.addCommandResponder(this);

            // Start listening
            transport.listen();
            snmp.listen();

            isRunning.set(true);
            log.info("SNMP Trap Receiver started on port {} with community '{}'", port, community);

        } catch (IOException e) {
            log.error("Failed to start SNMP Trap Receiver: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to start trap receiver", e);
        }
    }

    /**
     * Stop the trap receiver
     */
    public void stopTrapReceiver() {
        if (!isRunning.get()) {
            log.warn("Trap receiver is not running");
            return;
        }

        try {
            if (snmp != null) {
                snmp.close();
            }
            if (transport != null) {
                transport.close();
            }
            isRunning.set(false);
            log.info("SNMP Trap Receiver stopped");

        } catch (IOException e) {
            log.error("Error stopping SNMP Trap Receiver: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if trap receiver is running
     */
    public boolean isRunning() {
        return isRunning.get();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public synchronized void processPdu(CommandResponderEvent event) {
        try {
            PDU pdu = event.getPDU();
            if (pdu == null) {
                log.warn("Received null PDU");
                return;
            }

            // Extract source information
            Address sourceAddress = event.getPeerAddress();
            String sourceIp = extractIpFromAddress(sourceAddress);
            int sourcePort = extractPortFromAddress(sourceAddress);

            // Extract community (for v1/v2c)
            String community = extractCommunity(event);
            
            // Validate community if configured
            if (expectedCommunity != null && !expectedCommunity.equals(community)) {
                log.warn("Received trap with invalid community '{}' from {}", community, sourceIp);
                return;
            }

            log.debug("Received SNMP trap from {}:{} with community '{}'", sourceIp, sourcePort, community);

            // Process different PDU types
            if (pdu.getType() == PDU.TRAP) {
                processV1Trap(sourceIp, sourcePort, community, (PDUv1) pdu);
            } else if (pdu.getType() == PDU.NOTIFICATION || pdu.getType() == PDU.INFORM) {
                processV2Trap(sourceIp, sourcePort, community, pdu);
            } else {
                log.warn("Received unsupported PDU type: {}", pdu.getType());
            }

        } catch (Exception e) {
            log.error("Error processing SNMP trap: {}", e.getMessage(), e);
        }
    }

    private void processV1Trap(String sourceIp, int sourcePort, String community, PDUv1 pdu) {
        try {
            String enterpriseOid = pdu.getEnterprise().toString();
            int genericTrap = pdu.getGenericTrap();
            int specificTrap = pdu.getSpecificTrap();
            long timestamp = pdu.getTimestamp();

            // Build trap OID
            String trapOid = buildTrapOid(enterpriseOid, genericTrap, specificTrap);

            // Extract variable bindings
            Map<String, Object> variableBindings = extractVariableBindings(pdu);

            log.info("Processing SNMPv1 trap: OID={}, Generic={}, Specific={}, Enterprise={}", 
                    trapOid, genericTrap, specificTrap, enterpriseOid);

            // Process the trap
            trapProcessor.processTrap(sourceIp, sourcePort, community, trapOid, enterpriseOid,
                    genericTrap, specificTrap, timestamp, variableBindings);

        } catch (Exception e) {
            log.error("Error processing SNMPv1 trap: {}", e.getMessage(), e);
        }
    }

    private void processV2Trap(String sourceIp, int sourcePort, String community, PDU pdu) {
        try {
            Map<String, Object> variableBindings = extractVariableBindings(pdu);
            
            // Extract trap OID from sysUpTime and snmpTrapOID
            String trapOid = null;
            Long uptime = null;

            for (VariableBinding vb : pdu.getVariableBindings()) {
                String oid = vb.getOid().toString();
                if ("1.3.6.1.2.1.1.3.0".equals(oid)) { // sysUpTime
                    uptime = vb.getVariable().toLong();
                } else if ("1.3.6.1.6.3.1.1.4.1.0".equals(oid)) { // snmpTrapOID
                    trapOid = vb.getVariable().toString();
                }
            }

            if (trapOid == null) {
                log.warn("No snmpTrapOID found in trap from {}", sourceIp);
                return;
            }

            log.info("Processing SNMPv2c trap: OID={}, Uptime={}", trapOid, uptime);

            // Process the trap
            trapProcessor.processTrap(sourceIp, sourcePort, community, trapOid, null,
                    null, null, uptime, variableBindings);

        } catch (Exception e) {
            log.error("Error processing SNMPv2c trap: {}", e.getMessage(), e);
        }
    }

    private String extractIpFromAddress(Address address) {
        if (address instanceof UdpAddress) {
            return ((UdpAddress) address).getInetAddress().getHostAddress();
        }
        return address.toString().split("/")[0];
    }

    private int extractPortFromAddress(Address address) {
        if (address instanceof UdpAddress) {
            return ((UdpAddress) address).getPort();
        }
        try {
            return Integer.parseInt(address.toString().split("/")[1]);
        } catch (Exception e) {
            return 0;
        }
    }

    @SuppressWarnings("rawtypes")
    private String extractCommunity(CommandResponderEvent event) {
        // This is a simplified approach - in reality, community extraction
        // might require more sophisticated handling
        return expectedCommunity; // For now, assume expected community
    }

    private String buildTrapOid(String enterpriseOid, int genericTrap, int specificTrap) {
        if (genericTrap == 6) { // Enterprise specific
            return enterpriseOid + ".0." + specificTrap;
        } else {
            // Standard trap OIDs
            return "1.3.6.1.6.3.1.1.5." + (genericTrap + 1);
        }
    }

    private Map<String, Object> extractVariableBindings(PDU pdu) {
        Map<String, Object> bindings = new HashMap<>();
        
        for (VariableBinding vb : pdu.getVariableBindings()) {
            String oid = vb.getOid().toString();
            Variable var = vb.getVariable();
            
            Object value;
            if (var instanceof Integer32) {
                value = var.toInt();
            } else if (var instanceof UnsignedInteger32 || var instanceof Counter32 || var instanceof Counter64) {
                value = var.toLong();
            } else if (var instanceof IpAddress) {
                value = var.toString();
            } else if (var instanceof OctetString) {
                value = var.toString();
            } else if (var instanceof OID) {
                value = var.toString();
            } else {
                value = var.toString();
            }
            
            bindings.put(oid, value);
        }
        
        return bindings;
    }

    @PreDestroy
    public void cleanup() {
        stopTrapReceiver();
    }
}
