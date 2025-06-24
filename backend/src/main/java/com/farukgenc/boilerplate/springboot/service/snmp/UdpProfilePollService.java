package com.farukgenc.boilerplate.springboot.service.snmp;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceConfig;
import com.farukgenc.boilerplate.springboot.model.UdpProfile;
import com.farukgenc.boilerplate.springboot.repository.UdpProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.smi.Variable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for polling and updating UDP profile information via SNMP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UdpProfilePollService {

    private final SnmpClientService snmpClientService;
    private final UdpProfileRepository udpProfileRepository;

    // UDP MIB OIDs
    private static final String UDP_IN_DATAGRAMS_OID = "1.3.6.1.2.1.7.1.0";          // udpInDatagrams
    private static final String UDP_NO_PORTS_OID = "1.3.6.1.2.1.7.2.0";              // udpNoPorts
    private static final String UDP_IN_ERRORS_OID = "1.3.6.1.2.1.7.3.0";             // udpInErrors
    private static final String UDP_OUT_DATAGRAMS_OID = "1.3.6.1.2.1.7.4.0";         // udpOutDatagrams
      // UDP Table OIDs (for connection information)
    private static final String UDP_LOCAL_ADDRESS_OID = "1.3.6.1.2.1.7.5.1.1";       // udpLocalAddress
    private static final String UDP_LOCAL_PORT_OID = "1.3.6.1.2.1.7.5.1.2";          // udpLocalPort

    /**
     * Poll and update UDP profile information for a device
     */
    @Transactional
    public void pollDeviceUdpProfile(Device device, DeviceConfig config) {
        log.debug("Polling UDP profile for device: {}", device.getName());
        
        try {
            // Get UDP statistics
            List<String> udpOids = Arrays.asList(
                UDP_IN_DATAGRAMS_OID, UDP_NO_PORTS_OID, 
                UDP_IN_ERRORS_OID, UDP_OUT_DATAGRAMS_OID
            );
            
            Map<String, Variable> udpData = snmpClientService.snmpGetMultiple(config, udpOids);
            
            if (udpData.isEmpty()) {
                log.warn("No UDP data retrieved for device: {}", device.getName());
                return;
            }
            
            // Find existing UDP profile or create new one
            Optional<UdpProfile> existingUdpProfile = udpProfileRepository.findByDeviceId(device.getId());
            
            UdpProfile udpProfile = existingUdpProfile.orElse(
                UdpProfile.builder()
                    .device(device)
                    .udpEntryStatus(UdpProfile.UdpEntryStatus.VALID)
                    .build()
            );
            
            // Update UDP profile properties
            updateUdpProfileFromSnmpData(udpProfile, udpData);
            
            // Try to get UDP connection info (this may not be available on all devices)
            try {
                pollUdpConnectionInfo(udpProfile, config);
            } catch (Exception e) {
                log.debug("UDP connection info not available for device {}: {}", device.getName(), e.getMessage());
            }
            
            // Save the UDP profile
            udpProfileRepository.save(udpProfile);
            log.info("Updated UDP profile for device: {}", device.getName());
            
        } catch (Exception e) {
            log.error("Failed to poll UDP profile for device {}: {}", device.getName(), e.getMessage(), e);
            throw new RuntimeException("UDP profile polling failed for device: " + device.getName(), e);
        }
    }

    /**
     * Update UdpProfile entity with SNMP data
     */
    private void updateUdpProfileFromSnmpData(UdpProfile udpProfile, Map<String, Variable> data) {
        // UDP Input statistics
        Variable udpInDatagrams = data.get(UDP_IN_DATAGRAMS_OID);
        if (udpInDatagrams != null) {
            udpProfile.setUdpInDatagrams(udpInDatagrams.toLong());
        }
        
        Variable udpNoPorts = data.get(UDP_NO_PORTS_OID);
        if (udpNoPorts != null) {
            udpProfile.setUdpNoPorts(udpNoPorts.toLong());
        }
        
        Variable udpInErrors = data.get(UDP_IN_ERRORS_OID);
        if (udpInErrors != null) {
            udpProfile.setUdpInErrors(udpInErrors.toLong());
        }
        
        // UDP Output statistics
        Variable udpOutDatagrams = data.get(UDP_OUT_DATAGRAMS_OID);
        if (udpOutDatagrams != null) {
            udpProfile.setUdpOutDatagrams(udpOutDatagrams.toLong());
        }
    }
    
    /**
     * Poll UDP connection information from UDP table
     */
    private void pollUdpConnectionInfo(UdpProfile udpProfile, DeviceConfig config) {
        try {
            // Try to get the first UDP connection entry for basic info
            // In a real implementation, you might want to get multiple entries or specific ones
            List<String> connectionOids = Arrays.asList(
                UDP_LOCAL_ADDRESS_OID + ".1",  // First entry
                UDP_LOCAL_PORT_OID + ".1"      // First entry
            );
            
            Map<String, Variable> connectionData = snmpClientService.snmpGetMultiple(config, connectionOids);
            
            Variable localAddress = connectionData.get(UDP_LOCAL_ADDRESS_OID + ".1");
            if (localAddress != null) {
                udpProfile.setUdpLocalAddress(localAddress.toString());
            }
            
            Variable localPort = connectionData.get(UDP_LOCAL_PORT_OID + ".1");
            if (localPort != null) {
                udpProfile.setUdpLocalPort(localPort.toInt());
            }
            
            // Set default values for remote address and port if not available
            if (udpProfile.getUdpRemoteAddress() == null) {
                udpProfile.setUdpRemoteAddress("0.0.0.0");
            }
            if (udpProfile.getUdpRemotePort() == null) {
                udpProfile.setUdpRemotePort(0);
            }
            
        } catch (Exception e) {
            log.debug("Could not retrieve UDP connection info: {}", e.getMessage());
            // Set default values if connection info is not available
            if (udpProfile.getUdpLocalAddress() == null) {
                udpProfile.setUdpLocalAddress("0.0.0.0");
            }
            if (udpProfile.getUdpLocalPort() == null) {
                udpProfile.setUdpLocalPort(0);
            }
            if (udpProfile.getUdpRemoteAddress() == null) {
                udpProfile.setUdpRemoteAddress("0.0.0.0");
            }
            if (udpProfile.getUdpRemotePort() == null) {
                udpProfile.setUdpRemotePort(0);
            }
        }
    }
}
