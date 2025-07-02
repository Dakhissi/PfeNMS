package com.farukgenc.boilerplate.springboot.service.snmp;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceConfig;
import com.farukgenc.boilerplate.springboot.model.DeviceInterface;
import com.farukgenc.boilerplate.springboot.repository.DeviceInterfaceRepository;
import com.farukgenc.boilerplate.springboot.utils.SnmpDataParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.smi.Variable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for polling and updating device interface information via SNMP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterfacePollService {

    private final SnmpClientService snmpClientService;
    private final DeviceInterfaceRepository deviceInterfaceRepository;
    private final SnmpDataParser snmpDataParser;
    private final DuplicatePreventionService duplicatePreventionService;

    // Interface MIB OIDs
    private static final String IF_INDEX_OID = "1.3.6.1.2.1.2.2.1.1";        // ifIndex
    private static final String IF_DESCR_OID = "1.3.6.1.2.1.2.2.1.2";        // ifDescr
    private static final String IF_TYPE_OID = "1.3.6.1.2.1.2.2.1.3";         // ifType
    private static final String IF_MTU_OID = "1.3.6.1.2.1.2.2.1.4";          // ifMtu
    private static final String IF_SPEED_OID = "1.3.6.1.2.1.2.2.1.5";        // ifSpeed
    private static final String IF_PHYS_ADDRESS_OID = "1.3.6.1.2.1.2.2.1.6"; // ifPhysAddress
    private static final String IF_ADMIN_STATUS_OID = "1.3.6.1.2.1.2.2.1.7"; // ifAdminStatus
    private static final String IF_OPER_STATUS_OID = "1.3.6.1.2.1.2.2.1.8";  // ifOperStatus
    private static final String IF_LAST_CHANGE_OID = "1.3.6.1.2.1.2.2.1.9";  // ifLastChange
    private static final String IF_IN_OCTETS_OID = "1.3.6.1.2.1.2.2.1.10";   // ifInOctets
    private static final String IF_IN_UCAST_PKTS_OID = "1.3.6.1.2.1.2.2.1.11"; // ifInUcastPkts
    private static final String IF_IN_DISCARDS_OID = "1.3.6.1.2.1.2.2.1.13"; // ifInDiscards
    private static final String IF_IN_ERRORS_OID = "1.3.6.1.2.1.2.2.1.14";   // ifInErrors
    private static final String IF_OUT_OCTETS_OID = "1.3.6.1.2.1.2.2.1.16";  // ifOutOctets
    private static final String IF_OUT_UCAST_PKTS_OID = "1.3.6.1.2.1.2.2.1.17"; // ifOutUcastPkts
    private static final String IF_OUT_DISCARDS_OID = "1.3.6.1.2.1.2.2.1.19"; // ifOutDiscards
    private static final String IF_OUT_ERRORS_OID = "1.3.6.1.2.1.2.2.1.20";  // ifOutErrors

    /**
     * Poll and update all interfaces for a device
     */
    @Transactional
    public void pollDeviceInterfaces(Device device, DeviceConfig config) {
        log.debug("Polling interfaces for device: {}", device.getName());
        
        try {
            // Get interface indices first
            Map<String, Variable> interfaceIndices = snmpClientService.snmpWalk(config, IF_INDEX_OID, 100);
            
            if (interfaceIndices.isEmpty()) {
                log.warn("No interfaces found for device: {}", device.getName());
                return;
            }
            
            Set<Integer> activeInterfaceIndices = new HashSet<>();
            List<DeviceInterface> interfacesToSave = new ArrayList<>();
            
            for (Map.Entry<String, Variable> entry : interfaceIndices.entrySet()) {
                String oid = entry.getKey();
                Integer ifIndex = entry.getValue().toInt();
                
                if (ifIndex != null && ifIndex > 0) {
                    activeInterfaceIndices.add(ifIndex);
                    
                    DeviceInterface deviceInterface = pollSingleInterface(device, config, ifIndex, oid);
                    if (deviceInterface != null) {
                        interfacesToSave.add(deviceInterface);
                    }
                }
            }
            
            // Save or update interfaces
            if (!interfacesToSave.isEmpty()) {
                deviceInterfaceRepository.saveAll(interfacesToSave);
                log.info("Updated {} interfaces for device: {}", interfacesToSave.size(), device.getName());
            }
            
            // Use duplicate prevention service to clean up stale interfaces
            duplicatePreventionService.cleanupStaleInterfaces(device, activeInterfaceIndices);
            
        } catch (Exception e) {
            log.error("Failed to poll interfaces for device {}: {}", device.getName(), e.getMessage(), e);
            throw new RuntimeException("Interface polling failed for device: " + device.getName(), e);
        }
    }

    /**
     * Poll a single interface and return the DeviceInterface entity
     */
    private DeviceInterface pollSingleInterface(Device device, DeviceConfig config, Integer ifIndex, String indexOid) {
        try {
            // Extract interface index from OID
            String suffix = "." + ifIndex;
            
            // Prepare OIDs to query
            List<String> oidsToQuery = Arrays.asList(
                IF_DESCR_OID + suffix,
                IF_TYPE_OID + suffix,
                IF_MTU_OID + suffix,
                IF_SPEED_OID + suffix,
                IF_PHYS_ADDRESS_OID + suffix,
                IF_ADMIN_STATUS_OID + suffix,
                IF_OPER_STATUS_OID + suffix,
                IF_LAST_CHANGE_OID + suffix,
                IF_IN_OCTETS_OID + suffix,
                IF_IN_UCAST_PKTS_OID + suffix,
                IF_IN_DISCARDS_OID + suffix,
                IF_IN_ERRORS_OID + suffix,
                IF_OUT_OCTETS_OID + suffix,
                IF_OUT_UCAST_PKTS_OID + suffix,
                IF_OUT_DISCARDS_OID + suffix,
                IF_OUT_ERRORS_OID + suffix
            );
            
            Map<String, Variable> interfaceData = snmpClientService.snmpGetMultiple(config, oidsToQuery);
            
            if (interfaceData.isEmpty()) {
                log.warn("No data retrieved for interface {} on device {}", ifIndex, device.getName());
                return null;
            }
              // Find existing interface or create new one using duplicate prevention
            DeviceInterface deviceInterface = duplicatePreventionService.getOrCreateInterface(device, ifIndex);
            
            // Update interface properties
            updateInterfaceFromSnmpData(deviceInterface, interfaceData, suffix);
            
            return deviceInterface;
            
        } catch (Exception e) {
            log.error("Failed to poll interface {} for device {}: {}", ifIndex, device.getName(), e.getMessage());
            return null;
        }
    }    /**
     * Update DeviceInterface entity with SNMP data
     */
    private void updateInterfaceFromSnmpData(DeviceInterface deviceInterface, 
                                           Map<String, Variable> data, 
                                           String suffix) {
        
        // Interface description
        Variable ifDescr = data.get(IF_DESCR_OID + suffix);
        if (ifDescr != null) {
            String descrRaw = ifDescr.toString();
            String parsedDescr = snmpDataParser.isHexFormat(descrRaw)
                    ? snmpDataParser.parseHexToString(descrRaw)
                    : descrRaw;
            deviceInterface.setIfDescr(truncateString(parsedDescr, 1000));
        }
        
        // Interface type
        Variable ifType = data.get(IF_TYPE_OID + suffix);
        if (ifType != null) {
            deviceInterface.setIfType(mapInterfaceType(ifType.toInt()));
        }
        
        // MTU
        Variable ifMtu = data.get(IF_MTU_OID + suffix);
        if (ifMtu != null) {
            deviceInterface.setIfMtu(ifMtu.toInt());
        }
        
        // Speed
        Variable ifSpeed = data.get(IF_SPEED_OID + suffix);
        if (ifSpeed != null) {
            deviceInterface.setIfSpeed(ifSpeed.toLong());
        }
        
        // Physical Address (MAC)
        Variable ifPhysAddress = data.get(IF_PHYS_ADDRESS_OID + suffix);
        if (ifPhysAddress != null) {
            String rawMac = ifPhysAddress.toString();
            // Use enhanced parser for better hex to MAC conversion
            String formattedMac = snmpDataParser.formatMacAddress(rawMac);
            deviceInterface.setIfPhysAddress(truncateString(formattedMac, 500));
        }
        
        // Administrative Status
        Variable ifAdminStatus = data.get(IF_ADMIN_STATUS_OID + suffix);
        if (ifAdminStatus != null) {
            deviceInterface.setIfAdminStatus(mapInterfaceStatus(ifAdminStatus.toInt()));
        }
        
        // Operational Status
        Variable ifOperStatus = data.get(IF_OPER_STATUS_OID + suffix);
        if (ifOperStatus != null) {
            deviceInterface.setIfOperStatus(mapInterfaceStatus(ifOperStatus.toInt()));
        }
        
        // Statistics
        updateInterfaceStatistics(deviceInterface, data, suffix);
    }    /**
     * Update interface statistics
     */
    private void updateInterfaceStatistics(DeviceInterface deviceInterface, 
                                         Map<String, Variable> data, 
                                         String suffix) {
        
        Variable inOctets = data.get(IF_IN_OCTETS_OID + suffix);
        if (inOctets != null) {
            deviceInterface.setIfInOctets(inOctets.toLong());
        }
        
        Variable inUcastPkts = data.get(IF_IN_UCAST_PKTS_OID + suffix);
        if (inUcastPkts != null) {
            deviceInterface.setIfInUcastPkts(inUcastPkts.toLong());
        }
        
        Variable inDiscards = data.get(IF_IN_DISCARDS_OID + suffix);
        if (inDiscards != null) {
            deviceInterface.setIfInDiscards(inDiscards.toLong());
        }
        
        Variable inErrors = data.get(IF_IN_ERRORS_OID + suffix);
        if (inErrors != null) {
            deviceInterface.setIfInErrors(inErrors.toLong());
        }
        
        Variable outOctets = data.get(IF_OUT_OCTETS_OID + suffix);
        if (outOctets != null) {
            deviceInterface.setIfOutOctets(outOctets.toLong());
        }
        
        Variable outUcastPkts = data.get(IF_OUT_UCAST_PKTS_OID + suffix);
        if (outUcastPkts != null) {
            deviceInterface.setIfOutUcastPkts(outUcastPkts.toLong());
        }
        
        Variable outDiscards = data.get(IF_OUT_DISCARDS_OID + suffix);
        if (outDiscards != null) {
            deviceInterface.setIfOutDiscards(outDiscards.toLong());
        }
        
        Variable outErrors = data.get(IF_OUT_ERRORS_OID + suffix);
        if (outErrors != null) {
            deviceInterface.setIfOutErrors(outErrors.toLong());
        }
    }    /**
     * Map SNMP interface type to enum
     */
    private DeviceInterface.InterfaceType mapInterfaceType(Integer typeValue) {
        if (typeValue == null) return DeviceInterface.InterfaceType.OTHER;
        
        return switch (typeValue) {
            case 1 -> DeviceInterface.InterfaceType.OTHER;
            case 6 -> DeviceInterface.InterfaceType.ETHERNET_CSMACD;
            case 23 -> DeviceInterface.InterfaceType.PPP;
            case 24 -> DeviceInterface.InterfaceType.SOFTWARE_LOOPBACK;
            case 37 -> DeviceInterface.InterfaceType.FDDI;
            case 131 -> DeviceInterface.InterfaceType.ULTRA;
            default -> DeviceInterface.InterfaceType.OTHER;
        };
    }

    /**
     * Map SNMP interface status to enum
     */
    private DeviceInterface.InterfaceStatus mapInterfaceStatus(Integer statusValue) {
        if (statusValue == null) return DeviceInterface.InterfaceStatus.DOWN;
        
        return switch (statusValue) {
            case 1 -> DeviceInterface.InterfaceStatus.UP;
            case 2 -> DeviceInterface.InterfaceStatus.DOWN;
            case 3 -> DeviceInterface.InterfaceStatus.TESTING;
            default -> DeviceInterface.InterfaceStatus.DOWN;
        };
    }

    /**
     * Safely truncate a string to prevent database column length violations
     */
    private String truncateString(String input, int maxLength) {
        if (input == null) return null;
        if (input.length() <= maxLength) return input;
        
        String truncated = input.substring(0, maxLength);
        log.warn("Truncated string from {} to {} characters: '{}'", 
                input.length(), maxLength, truncated);
        return truncated;
    }
}
