package com.farukgenc.boilerplate.springboot.service.snmp;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceConfig;
import com.farukgenc.boilerplate.springboot.model.SystemUnit;
import com.farukgenc.boilerplate.springboot.repository.SystemUnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.smi.Variable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for polling and updating hardware system units via SNMP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemUnitPollService {

    private final SnmpClientService snmpClientService;
    private final SystemUnitRepository systemUnitRepository;    // Entity MIB OIDs for physical hardware units
    private static final String ENT_PHYSICAL_INDEX_OID = "1.3.6.1.2.1.47.1.1.1.1.1";     // entPhysicalIndex
    private static final String ENT_PHYSICAL_DESCR_OID = "1.3.6.1.2.1.47.1.1.1.1.2";     // entPhysicalDescr
    private static final String ENT_PHYSICAL_VENDOR_TYPE_OID = "1.3.6.1.2.1.47.1.1.1.1.3"; // entPhysicalVendorType
    private static final String ENT_PHYSICAL_CLASS_OID = "1.3.6.1.2.1.47.1.1.1.1.5";     // entPhysicalClass
    private static final String ENT_PHYSICAL_NAME_OID = "1.3.6.1.2.1.47.1.1.1.1.7";      // entPhysicalName
    private static final String ENT_PHYSICAL_HW_REV_OID = "1.3.6.1.2.1.47.1.1.1.1.8";    // entPhysicalHardwareRev
    private static final String ENT_PHYSICAL_FW_REV_OID = "1.3.6.1.2.1.47.1.1.1.1.9";    // entPhysicalFirmwareRev
    private static final String ENT_PHYSICAL_SW_REV_OID = "1.3.6.1.2.1.47.1.1.1.1.10";   // entPhysicalSoftwareRev
    private static final String ENT_PHYSICAL_SERIAL_NUM_OID = "1.3.6.1.2.1.47.1.1.1.1.11"; // entPhysicalSerialNum
    private static final String ENT_PHYSICAL_MFG_NAME_OID = "1.3.6.1.2.1.47.1.1.1.1.12"; // entPhysicalMfgName
    private static final String ENT_PHYSICAL_MODEL_NAME_OID = "1.3.6.1.2.1.47.1.1.1.1.13"; // entPhysicalModelName
    private static final String ENT_PHYSICAL_ALIAS_OID = "1.3.6.1.2.1.47.1.1.1.1.14";    // entPhysicalAlias
    private static final String ENT_PHYSICAL_ASSET_ID_OID = "1.3.6.1.2.1.47.1.1.1.1.15"; // entPhysicalAssetID
    private static final String ENT_PHYSICAL_IS_FRU_OID = "1.3.6.1.2.1.47.1.1.1.1.16";   // entPhysicalIsFRU

    /**
     * Poll and update hardware system units for a device
     */
    @Transactional
    public void pollDeviceSystemUnits(Device device, DeviceConfig config) {
        log.debug("Polling system units for device: {}", device.getName());
        
        try {
            // Get physical entity indices first
            Map<String, Variable> entityIndices = snmpClientService.snmpWalk(config, ENT_PHYSICAL_INDEX_OID, 100);
            
            if (entityIndices.isEmpty()) {
                log.warn("No physical entities found for device: {}", device.getName());
                return;
            }
            
            Set<Integer> activeUnitIndices = new HashSet<>();
            List<SystemUnit> unitsToSave = new ArrayList<>();
            
            for (Map.Entry<String, Variable> entry : entityIndices.entrySet()) {
                String oid = entry.getKey();
                Integer unitIndex = entry.getValue().toInt();
                
                if (unitIndex != null && unitIndex > 0) {
                    activeUnitIndices.add(unitIndex);
                    
                    SystemUnit systemUnit = pollSingleSystemUnit(device, config, unitIndex, oid);
                    if (systemUnit != null) {
                        unitsToSave.add(systemUnit);
                    }
                }
            }
            
            // Save or update system units
            if (!unitsToSave.isEmpty()) {
                systemUnitRepository.saveAll(unitsToSave);
                log.info("Updated {} system units for device: {}", unitsToSave.size(), device.getName());
            }
            
            // Remove units that are no longer present
            List<SystemUnit> existingUnits = systemUnitRepository.findByDevice(device);
            List<SystemUnit> unitsToRemove = existingUnits.stream()
                .filter(unit -> !activeUnitIndices.contains(unit.getUnitIndex()))
                .toList();
            
            if (!unitsToRemove.isEmpty()) {
                systemUnitRepository.deleteAll(unitsToRemove);
                log.info("Removed {} stale system units for device: {}", unitsToRemove.size(), device.getName());
            }
            
        } catch (Exception e) {
            log.error("Failed to poll system units for device {}: {}", device.getName(), e.getMessage(), e);
            // Don't throw RuntimeException to prevent transaction rollback issues
        }
    }

    /**
     * Poll a single system unit and return the SystemUnit entity
     */
    private SystemUnit pollSingleSystemUnit(Device device, DeviceConfig config, Integer unitIndex, String indexOid) {
        try {
            // Extract unit index from OID
            String suffix = "." + unitIndex;
            
            // Prepare OIDs to query
            List<String> oidsToQuery = Arrays.asList(
                ENT_PHYSICAL_DESCR_OID + suffix,
                ENT_PHYSICAL_VENDOR_TYPE_OID + suffix,
                ENT_PHYSICAL_CLASS_OID + suffix,
                ENT_PHYSICAL_NAME_OID + suffix,
                ENT_PHYSICAL_HW_REV_OID + suffix,
                ENT_PHYSICAL_FW_REV_OID + suffix,
                ENT_PHYSICAL_SW_REV_OID + suffix,
                ENT_PHYSICAL_SERIAL_NUM_OID + suffix,
                ENT_PHYSICAL_MFG_NAME_OID + suffix,
                ENT_PHYSICAL_MODEL_NAME_OID + suffix,
                ENT_PHYSICAL_ALIAS_OID + suffix,
                ENT_PHYSICAL_ASSET_ID_OID + suffix,
                ENT_PHYSICAL_IS_FRU_OID + suffix
            );
            
            Map<String, Variable> unitData = snmpClientService.snmpGetMultiple(config, oidsToQuery);
            
            if (unitData.isEmpty()) {
                log.warn("No data retrieved for system unit {} on device {}", unitIndex, device.getName());
                return null;
            }
            
            // Find existing unit or create new one
            Optional<SystemUnit> existingUnit = systemUnitRepository
                .findByDeviceAndUnitIndex(device, unitIndex);
            
            SystemUnit systemUnit = existingUnit.orElse(
                SystemUnit.builder()
                    .device(device)
                    .unitIndex(unitIndex)
                    .build()
            );
            
            // Update unit properties
            updateSystemUnitFromSnmpData(systemUnit, unitData, suffix);
            
            return systemUnit;
            
        } catch (Exception e) {
            log.error("Failed to poll system unit {} for device {}: {}", unitIndex, device.getName(), e.getMessage());
            return null;
        }
    }

    /**
     * Update SystemUnit entity with SNMP data
     */
    private void updateSystemUnitFromSnmpData(SystemUnit systemUnit, 
                                            Map<String, Variable> data, 
                                            String suffix) {
        
        // Physical Description
        Variable entPhysicalDescr = data.get(ENT_PHYSICAL_DESCR_OID + suffix);
        if (entPhysicalDescr != null) {
            systemUnit.setUnitDescription(entPhysicalDescr.toString());
        }
        
        // Physical Class (type)
        Variable entPhysicalClass = data.get(ENT_PHYSICAL_CLASS_OID + suffix);
        if (entPhysicalClass != null) {
            systemUnit.setUnitType(mapPhysicalClass(entPhysicalClass.toInt()));
        }
        
        // Physical Name
        Variable entPhysicalName = data.get(ENT_PHYSICAL_NAME_OID + suffix);
        if (entPhysicalName != null) {
            systemUnit.setUnitName(entPhysicalName.toString());
        }
        
        // Hardware Revision
        Variable entPhysicalHwRev = data.get(ENT_PHYSICAL_HW_REV_OID + suffix);
        if (entPhysicalHwRev != null) {
            systemUnit.setUnitHwVersion(entPhysicalHwRev.toString());
        }
        
        // Firmware Revision
        Variable entPhysicalFwRev = data.get(ENT_PHYSICAL_FW_REV_OID + suffix);
        if (entPhysicalFwRev != null) {
            systemUnit.setUnitFwVersion(entPhysicalFwRev.toString());
        }
        
        // Software Revision
        Variable entPhysicalSwRev = data.get(ENT_PHYSICAL_SW_REV_OID + suffix);
        if (entPhysicalSwRev != null) {
            systemUnit.setUnitSwVersion(entPhysicalSwRev.toString());
        }
        
        // Serial Number
        Variable entPhysicalSerialNum = data.get(ENT_PHYSICAL_SERIAL_NUM_OID + suffix);
        if (entPhysicalSerialNum != null) {
            systemUnit.setUnitSerialNumber(entPhysicalSerialNum.toString());
        }
        
        // Manufacturer Name
        Variable entPhysicalMfgName = data.get(ENT_PHYSICAL_MFG_NAME_OID + suffix);
        if (entPhysicalMfgName != null) {
            systemUnit.setUnitMfgName(entPhysicalMfgName.toString());
        }
        
        // Model Name
        Variable entPhysicalModelName = data.get(ENT_PHYSICAL_MODEL_NAME_OID + suffix);
        if (entPhysicalModelName != null) {
            systemUnit.setUnitModelName(entPhysicalModelName.toString());
        }
        
        // Physical Alias
        Variable entPhysicalAlias = data.get(ENT_PHYSICAL_ALIAS_OID + suffix);
        if (entPhysicalAlias != null) {
            systemUnit.setUnitAlias(entPhysicalAlias.toString());
        }
        
        // Asset ID
        Variable entPhysicalAssetId = data.get(ENT_PHYSICAL_ASSET_ID_OID + suffix);
        if (entPhysicalAssetId != null) {
            systemUnit.setUnitAssetId(entPhysicalAssetId.toString());
        }
        
        // Is FRU (Field Replaceable Unit)
        Variable entPhysicalIsFru = data.get(ENT_PHYSICAL_IS_FRU_OID + suffix);
        if (entPhysicalIsFru != null) {
            systemUnit.setUnitIsFru(entPhysicalIsFru.toInt() == 1);
        }
    }

    /**
     * Map SNMP physical class integer to human-readable string
     */
    private String mapPhysicalClass(Integer classValue) {
        if (classValue == null) {
            return "unknown";
        }

        return switch (classValue) {
            case 1 -> "other";
            case 2 -> "unknown";
            case 3 -> "chassis";
            case 4 -> "backplane";
            case 5 -> "container";
            case 6 -> "powerSupply";
            case 7 -> "fan";
            case 8 -> "sensor";
            case 9 -> "module";
            case 10 -> "port";
            case 11 -> "stack";
            case 12 -> "cpu";
            default -> "unknown(" + classValue + ")";
        };
    }
}
