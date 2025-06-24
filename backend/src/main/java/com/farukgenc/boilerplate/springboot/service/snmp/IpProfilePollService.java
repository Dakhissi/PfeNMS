package com.farukgenc.boilerplate.springboot.service.snmp;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceConfig;
import com.farukgenc.boilerplate.springboot.model.IpProfile;
import com.farukgenc.boilerplate.springboot.repository.IpProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.smi.Variable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for polling and updating IP profile information via SNMP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IpProfilePollService {

    private final SnmpClientService snmpClientService;
    private final IpProfileRepository ipProfileRepository;

    // IP MIB OIDs
    private static final String IP_FORWARDING_OID = "1.3.6.1.2.1.4.1.0";            // ipForwarding
    private static final String IP_DEFAULT_TTL_OID = "1.3.6.1.2.1.4.2.0";           // ipDefaultTTL
    private static final String IP_IN_RECEIVES_OID = "1.3.6.1.2.1.4.3.0";           // ipInReceives
    private static final String IP_IN_HDR_ERRORS_OID = "1.3.6.1.2.1.4.4.0";         // ipInHdrErrors
    private static final String IP_IN_ADDR_ERRORS_OID = "1.3.6.1.2.1.4.5.0";        // ipInAddrErrors
    private static final String IP_FORW_DATAGRAMS_OID = "1.3.6.1.2.1.4.6.0";        // ipForwDatagrams
    private static final String IP_IN_UNKNOWN_PROTOS_OID = "1.3.6.1.2.1.4.7.0";     // ipInUnknownProtos
    private static final String IP_IN_DISCARDS_OID = "1.3.6.1.2.1.4.8.0";           // ipInDiscards
    private static final String IP_IN_DELIVERS_OID = "1.3.6.1.2.1.4.9.0";           // ipInDelivers
    private static final String IP_OUT_REQUESTS_OID = "1.3.6.1.2.1.4.10.0";         // ipOutRequests
    private static final String IP_OUT_DISCARDS_OID = "1.3.6.1.2.1.4.11.0";         // ipOutDiscards
    private static final String IP_OUT_NO_ROUTES_OID = "1.3.6.1.2.1.4.12.0";        // ipOutNoRoutes
    private static final String IP_REASM_TIMEOUT_OID = "1.3.6.1.2.1.4.13.0";        // ipReasmTimeout
    private static final String IP_REASM_REQDS_OID = "1.3.6.1.2.1.4.14.0";          // ipReasmReqds
    private static final String IP_REASM_OKS_OID = "1.3.6.1.2.1.4.15.0";            // ipReasmOKs
    private static final String IP_REASM_FAILS_OID = "1.3.6.1.2.1.4.16.0";          // ipReasmFails
    private static final String IP_FRAG_OKS_OID = "1.3.6.1.2.1.4.17.0";             // ipFragOKs
    private static final String IP_FRAG_FAILS_OID = "1.3.6.1.2.1.4.18.0";           // ipFragFails
    private static final String IP_FRAG_CREATES_OID = "1.3.6.1.2.1.4.19.0";         // ipFragCreates
    private static final String IP_ROUTING_DISCARDS_OID = "1.3.6.1.2.1.4.23.0";     // ipRoutingDiscards

    /**
     * Poll and update IP profile information for a device
     */
    @Transactional
    public void pollDeviceIpProfile(Device device, DeviceConfig config) {
        log.debug("Polling IP profile for device: {}", device.getName());
        
        try {
            // Get IP statistics
            List<String> ipOids = Arrays.asList(
                IP_FORWARDING_OID, IP_DEFAULT_TTL_OID, IP_IN_RECEIVES_OID,
                IP_IN_HDR_ERRORS_OID, IP_IN_ADDR_ERRORS_OID, IP_FORW_DATAGRAMS_OID,
                IP_IN_UNKNOWN_PROTOS_OID, IP_IN_DISCARDS_OID, IP_IN_DELIVERS_OID,
                IP_OUT_REQUESTS_OID, IP_OUT_DISCARDS_OID, IP_OUT_NO_ROUTES_OID,
                IP_REASM_TIMEOUT_OID, IP_REASM_REQDS_OID, IP_REASM_OKS_OID,
                IP_REASM_FAILS_OID, IP_FRAG_OKS_OID, IP_FRAG_FAILS_OID,
                IP_FRAG_CREATES_OID, IP_ROUTING_DISCARDS_OID
            );
            
            Map<String, Variable> ipData = snmpClientService.snmpGetMultiple(config, ipOids);
            
            if (ipData.isEmpty()) {
                log.warn("No IP data retrieved for device: {}", device.getName());
                return;
            }
            
            // Find existing IP profile or create new one
            Optional<IpProfile> existingIpProfile = ipProfileRepository.findByDeviceId(device.getId());
            
            IpProfile ipProfile = existingIpProfile.orElse(
                IpProfile.builder()
                    .device(device)
                    .build()
            );
            
            // Update IP profile properties
            updateIpProfileFromSnmpData(ipProfile, ipData);
            
            // Save the IP profile
            ipProfileRepository.save(ipProfile);
            log.info("Updated IP profile for device: {}", device.getName());
            
        } catch (Exception e) {
            log.error("Failed to poll IP profile for device {}: {}", device.getName(), e.getMessage(), e);
            throw new RuntimeException("IP profile polling failed for device: " + device.getName(), e);
        }
    }

    /**
     * Update IpProfile entity with SNMP data
     */
    private void updateIpProfileFromSnmpData(IpProfile ipProfile, Map<String, Variable> data) {
          // IP Forwarding
        Variable ipForwarding = data.get(IP_FORWARDING_OID);
        if (ipForwarding != null) {
            ipProfile.setIpForwarding(ipForwarding.toInt() == 1);
        }
        
        // IP Default TTL
        Variable ipDefaultTtl = data.get(IP_DEFAULT_TTL_OID);
        if (ipDefaultTtl != null) {
            ipProfile.setIpDefaultTTL(ipDefaultTtl.toInt());
        }
        
        // IP Input Statistics
        Variable ipInReceives = data.get(IP_IN_RECEIVES_OID);
        if (ipInReceives != null) {
            ipProfile.setIpInReceives(ipInReceives.toLong());
        }
        
        Variable ipInHdrErrors = data.get(IP_IN_HDR_ERRORS_OID);
        if (ipInHdrErrors != null) {
            ipProfile.setIpInHdrErrors(ipInHdrErrors.toLong());
        }
        
        Variable ipInAddrErrors = data.get(IP_IN_ADDR_ERRORS_OID);
        if (ipInAddrErrors != null) {
            ipProfile.setIpInAddrErrors(ipInAddrErrors.toLong());
        }
        
        Variable ipForwDatagrams = data.get(IP_FORW_DATAGRAMS_OID);
        if (ipForwDatagrams != null) {
            ipProfile.setIpForwDatagrams(ipForwDatagrams.toLong());
        }
        
        Variable ipInUnknownProtos = data.get(IP_IN_UNKNOWN_PROTOS_OID);
        if (ipInUnknownProtos != null) {
            ipProfile.setIpInUnknownProtos(ipInUnknownProtos.toLong());
        }
        
        Variable ipInDiscards = data.get(IP_IN_DISCARDS_OID);
        if (ipInDiscards != null) {
            ipProfile.setIpInDiscards(ipInDiscards.toLong());
        }
        
        Variable ipInDelivers = data.get(IP_IN_DELIVERS_OID);
        if (ipInDelivers != null) {
            ipProfile.setIpInDelivers(ipInDelivers.toLong());
        }
        
        // IP Output Statistics
        Variable ipOutRequests = data.get(IP_OUT_REQUESTS_OID);
        if (ipOutRequests != null) {
            ipProfile.setIpOutRequests(ipOutRequests.toLong());
        }
        
        Variable ipOutDiscards = data.get(IP_OUT_DISCARDS_OID);
        if (ipOutDiscards != null) {
            ipProfile.setIpOutDiscards(ipOutDiscards.toLong());
        }
        
        Variable ipOutNoRoutes = data.get(IP_OUT_NO_ROUTES_OID);
        if (ipOutNoRoutes != null) {
            ipProfile.setIpOutNoRoutes(ipOutNoRoutes.toLong());
        }
        
        // IP Reassembly Statistics
        Variable ipReasmTimeout = data.get(IP_REASM_TIMEOUT_OID);
        if (ipReasmTimeout != null) {
            ipProfile.setIpReasmTimeout(ipReasmTimeout.toInt());
        }
        
        Variable ipReasmReqds = data.get(IP_REASM_REQDS_OID);
        if (ipReasmReqds != null) {
            ipProfile.setIpReasmReqds(ipReasmReqds.toLong());
        }
        
        Variable ipReasmOks = data.get(IP_REASM_OKS_OID);
        if (ipReasmOks != null) {
            ipProfile.setIpReasmOKs(ipReasmOks.toLong());
        }
        
        Variable ipReasmFails = data.get(IP_REASM_FAILS_OID);
        if (ipReasmFails != null) {
            ipProfile.setIpReasmFails(ipReasmFails.toLong());
        }
        
        // IP Fragmentation Statistics
        Variable ipFragOks = data.get(IP_FRAG_OKS_OID);
        if (ipFragOks != null) {
            ipProfile.setIpFragOKs(ipFragOks.toLong());
        }
        
        Variable ipFragFails = data.get(IP_FRAG_FAILS_OID);
        if (ipFragFails != null) {
            ipProfile.setIpFragFails(ipFragFails.toLong());
        }
          Variable ipFragCreates = data.get(IP_FRAG_CREATES_OID);
        if (ipFragCreates != null) {
            ipProfile.setIpFragCreates(ipFragCreates.toLong());
        }
    }
}
