package com.farukgenc.boilerplate.springboot.service.snmp;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceConfig;
import com.farukgenc.boilerplate.springboot.model.SystemInfo;
import com.farukgenc.boilerplate.springboot.repository.SystemInfoRepository;
import com.farukgenc.boilerplate.springboot.utils.SnmpDataParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.smi.Variable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for polling and updating system information via SNMP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemInfoPollService {

    private final SnmpClientService snmpClientService;
    private final SystemInfoRepository systemInfoRepository;
    private final SnmpDataParser snmpDataParser;

    // System MIB OIDs
    private static final String SYS_DESCR_OID = "1.3.6.1.2.1.1.1.0";      // sysDescr
    private static final String SYS_OBJECT_ID_OID = "1.3.6.1.2.1.1.2.0";  // sysObjectID
    private static final String SYS_UP_TIME_OID = "1.3.6.1.2.1.1.3.0";    // sysUpTime
    private static final String SYS_CONTACT_OID = "1.3.6.1.2.1.1.4.0";    // sysContact
    private static final String SYS_NAME_OID = "1.3.6.1.2.1.1.5.0";       // sysName
    private static final String SYS_LOCATION_OID = "1.3.6.1.2.1.1.6.0";   // sysLocation
    private static final String SYS_SERVICES_OID = "1.3.6.1.2.1.1.7.0";   // sysServices

    // Host Resources MIB OIDs
    private static final String HR_SYSTEM_UPTIME_OID = "1.3.6.1.2.1.25.1.1.0";         // hrSystemUptime
    private static final String HR_SYSTEM_DATE_OID = "1.3.6.1.2.1.25.1.2.0";           // hrSystemDate
    private static final String HR_SYSTEM_INITIAL_LOAD_DEVICE_OID = "1.3.6.1.2.1.25.1.3.0"; // hrSystemInitialLoadDevice
    private static final String HR_SYSTEM_INITIAL_LOAD_PARAMETERS_OID = "1.3.6.1.2.1.25.1.4.0"; // hrSystemInitialLoadParameters
    private static final String HR_SYSTEM_NUM_USERS_OID = "1.3.6.1.2.1.25.1.5.0";      // hrSystemNumUsers
    private static final String HR_SYSTEM_PROCESSES_OID = "1.3.6.1.2.1.25.1.6.0";      // hrSystemProcesses
    private static final String HR_SYSTEM_MAX_PROCESSES_OID = "1.3.6.1.2.1.25.1.7.0";  // hrSystemMaxProcesses

    /**
     * Poll and update system information for a device
     */
    @Transactional
    public void pollDeviceSystemInfo(Device device, DeviceConfig config) {
        log.debug("Polling system information for device: {}", device.getName());
        
        try {
            // Get system information
            List<String> systemOids = Arrays.asList(
                SYS_DESCR_OID, SYS_OBJECT_ID_OID, SYS_UP_TIME_OID,
                SYS_CONTACT_OID, SYS_NAME_OID, SYS_LOCATION_OID, SYS_SERVICES_OID,
                HR_SYSTEM_UPTIME_OID, HR_SYSTEM_DATE_OID, HR_SYSTEM_INITIAL_LOAD_DEVICE_OID,
                HR_SYSTEM_INITIAL_LOAD_PARAMETERS_OID, HR_SYSTEM_NUM_USERS_OID,
                HR_SYSTEM_PROCESSES_OID, HR_SYSTEM_MAX_PROCESSES_OID
            );
            
            Map<String, Variable> systemData = snmpClientService.snmpGetMultiple(config, systemOids);
            
            if (systemData.isEmpty()) {
                log.warn("No system data retrieved for device: {}", device.getName());
                return;
            }
            
            // Find existing system info or create new one
            Optional<SystemInfo> existingSystemInfo = systemInfoRepository.findByDeviceId(device.getId());
            
            SystemInfo systemInfo = existingSystemInfo.orElse(
                SystemInfo.builder()
                    .device(device)
                    .build()
            );
            
            // Update system info properties
            updateSystemInfoFromSnmpData(systemInfo, systemData);
            systemInfo.setLastPolled(LocalDateTime.now());
            
            // Save the system info
            systemInfoRepository.save(systemInfo);
            log.info("Updated system information for device: {}", device.getName());
            
        } catch (Exception e) {
            log.error("Failed to poll system information for device {}: {}", device.getName(), e.getMessage(), e);
            throw new RuntimeException("System information polling failed for device: " + device.getName(), e);
        }
    }

    /**
     * Update SystemInfo entity with SNMP data
     */
    private void updateSystemInfoFromSnmpData(SystemInfo systemInfo, Map<String, Variable> data) {
        
        // System Description
        Variable sysDescr = data.get(SYS_DESCR_OID);
        if (sysDescr != null) {
            String descr = sysDescr.toString();
            // Parse hex values if needed
            if (snmpDataParser.isHexFormat(descr)) {
                descr = snmpDataParser.parseHexToString(descr);
            }
            systemInfo.setSysDescr(descr);
        }
        
        // System Object ID
        Variable sysObjectId = data.get(SYS_OBJECT_ID_OID);
        if (sysObjectId != null) {
            systemInfo.setSysObjectId(sysObjectId.toString());
        }
        
        // System Uptime - use enhanced parsing
        Variable sysUpTime = data.get(SYS_UP_TIME_OID);
        if (sysUpTime != null) {
            systemInfo.setSysUpTime(sysUpTime.toLong());
            // Store human-readable uptime as well
            String uptimeString = snmpDataParser.parseTimeTicks(sysUpTime.toString());
            log.debug("Device {} uptime: {}", systemInfo.getDevice().getName(), uptimeString);
        }
        
        // System Contact
        Variable sysContact = data.get(SYS_CONTACT_OID);
        if (sysContact != null) {
            String contact = sysContact.toString();
            if (snmpDataParser.isHexFormat(contact)) {
                contact = snmpDataParser.parseHexToString(contact);
            }
            systemInfo.setSysContact(contact);
        }
        
        // System Name
        Variable sysName = data.get(SYS_NAME_OID);
        if (sysName != null) {
            String name = sysName.toString();
            if (snmpDataParser.isHexFormat(name)) {
                name = snmpDataParser.parseHexToString(name);
            }
            systemInfo.setSysName(name);
        }
        
        // System Location
        Variable sysLocation = data.get(SYS_LOCATION_OID);
        if (sysLocation != null) {
            String location = sysLocation.toString();
            if (snmpDataParser.isHexFormat(location)) {
                location = snmpDataParser.parseHexToString(location);
            }
            systemInfo.setSysLocation(location);
        }
        
        // System Services
        Variable sysServices = data.get(SYS_SERVICES_OID);
        if (sysServices != null) {
            systemInfo.setSysServices(sysServices.toInt());
        }
        
        // Host Resources - System Uptime
        Variable hrSystemUptime = data.get(HR_SYSTEM_UPTIME_OID);
        if (hrSystemUptime != null) {
            systemInfo.setHrSystemUptime(hrSystemUptime.toLong());
        }
        
        // Host Resources - System Date
        Variable hrSystemDate = data.get(HR_SYSTEM_DATE_OID);
        if (hrSystemDate != null) {
            systemInfo.setHrSystemDate(parseSnmpDateAndTime(hrSystemDate.toString()));
        }
        
        // Host Resources - Initial Load Device
        Variable hrSystemInitialLoadDevice = data.get(HR_SYSTEM_INITIAL_LOAD_DEVICE_OID);
        if (hrSystemInitialLoadDevice != null) {
            systemInfo.setHrSystemInitialLoadDevice(hrSystemInitialLoadDevice.toInt());
        }
        
        // Host Resources - Initial Load Parameters
        Variable hrSystemInitialLoadParameters = data.get(HR_SYSTEM_INITIAL_LOAD_PARAMETERS_OID);
        if (hrSystemInitialLoadParameters != null) {
            systemInfo.setHrSystemInitialLoadParameters(hrSystemInitialLoadParameters.toString());
        }
        
        // Host Resources - Number of Users
        Variable hrSystemNumUsers = data.get(HR_SYSTEM_NUM_USERS_OID);
        if (hrSystemNumUsers != null) {
            systemInfo.setHrSystemNumUsers(hrSystemNumUsers.toInt());
        }
        
        // Host Resources - Number of Processes
        Variable hrSystemProcesses = data.get(HR_SYSTEM_PROCESSES_OID);
        if (hrSystemProcesses != null) {
            systemInfo.setHrSystemProcesses(hrSystemProcesses.toInt());
        }
        
        // Host Resources - Max Processes
        Variable hrSystemMaxProcesses = data.get(HR_SYSTEM_MAX_PROCESSES_OID);
        if (hrSystemMaxProcesses != null) {
            systemInfo.setHrSystemMaxProcesses(hrSystemMaxProcesses.toInt());
        }
    }

    /**
     * Parse SNMP DateAndTime format to LocalDateTime
     */
    private LocalDateTime parseSnmpDateAndTime(String dateTimeString) {
        try {
            if (dateTimeString == null || dateTimeString.length() < 16) {
                return null;
            }
            
            // Extract bytes from hex string (assuming it's in hex format)
            byte[] bytes = parseHexString(dateTimeString);
            if (bytes.length < 8) {
                return null;
            }
            
            int year = ((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);
            int month = bytes[2] & 0xFF;
            int day = bytes[3] & 0xFF;
            int hour = bytes[4] & 0xFF;
            int minute = bytes[5] & 0xFF;
            int second = bytes[6] & 0xFF;
            
            // Validate date components
            if (year < 1900 || year > 2100 || month < 1 || month > 12 || 
                day < 1 || day > 31 || hour > 23 || minute > 59 || second > 60) {
                return null;
            }
            
            return LocalDateTime.of(year, month, day, hour, minute, second);
            
        } catch (Exception e) {
            log.warn("Failed to parse SNMP DateAndTime '{}': {}", dateTimeString, e.getMessage());
            return null;
        }
    }

    /**
     * Parse hex string to byte array
     */
    private byte[] parseHexString(String hexString) {
        String cleaned = hexString.replaceAll("[^0-9a-fA-F]", "");
        byte[] result = new byte[cleaned.length() / 2];
        
        for (int i = 0; i < cleaned.length(); i += 2) {
            result[i / 2] = (byte) Integer.parseInt(cleaned.substring(i, i + 2), 16);
        }
        
        return result;
    }
}
