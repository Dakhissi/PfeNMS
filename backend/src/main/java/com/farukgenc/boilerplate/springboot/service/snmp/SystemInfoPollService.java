package com.farukgenc.boilerplate.springboot.service.snmp;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceConfig;
import com.farukgenc.boilerplate.springboot.model.SystemInfo;
import com.farukgenc.boilerplate.springboot.repository.DeviceRepository;
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
 * Service for polling and updating system information via SNMP.
 *
 * <p>The method now follows three clearly separated steps:</p>
 * <ol>
 *     <li>Collect raw SNMP data.</li>
 *     <li>Populate / persist {@link SystemInfo}.</li>
 *     <li>Project the snapshot onto the owning {@link Device} and persist it <em>after</em>
 *     the SystemInfo has been saved (so that a device row is never left pointing
 *     to a half‑baked SystemInfo).</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemInfoPollService {

    private final SnmpClientService snmpClientService;
    private final DeviceRepository deviceRepository;
    private final SystemInfoRepository systemInfoRepository;
    private final SnmpDataParser snmpDataParser;

    // ---------------------------------------------------------------------
    // System MIB OIDs (RFC 1213)
    // ---------------------------------------------------------------------
    private static final String SYS_DESCR_OID     = "1.3.6.1.2.1.1.1.0"; // sysDescr
    private static final String SYS_OBJECT_ID_OID = "1.3.6.1.2.1.1.2.0"; // sysObjectID
    private static final String SYS_UP_TIME_OID   = "1.3.6.1.2.1.1.3.0"; // sysUpTime
    private static final String SYS_CONTACT_OID   = "1.3.6.1.2.1.1.4.0"; // sysContact
    private static final String SYS_NAME_OID      = "1.3.6.1.2.1.1.5.0"; // sysName
    private static final String SYS_LOCATION_OID  = "1.3.6.1.2.1.1.6.0"; // sysLocation
    private static final String SYS_SERVICES_OID  = "1.3.6.1.2.1.1.7.0"; // sysServices

    // ---------------------------------------------------------------------
    // Host‑Resources MIB OIDs (RFC 2790)
    // ---------------------------------------------------------------------
    private static final String HR_SYSTEM_UPTIME_OID              = "1.3.6.1.2.1.25.1.1.0";
    private static final String HR_SYSTEM_DATE_OID                = "1.3.6.1.2.1.25.1.2.0";
    private static final String HR_SYSTEM_INITIAL_LOAD_DEVICE_OID = "1.3.6.1.2.1.25.1.3.0";
    private static final String HR_SYSTEM_INITIAL_LOAD_PARAMS_OID = "1.3.6.1.2.1.25.1.4.0";
    private static final String HR_SYSTEM_NUM_USERS_OID           = "1.3.6.1.2.1.25.1.5.0";
    private static final String HR_SYSTEM_PROCESSES_OID           = "1.3.6.1.2.1.25.1.6.0";
    private static final String HR_SYSTEM_MAX_PROCESSES_OID       = "1.3.6.1.2.1.25.1.7.0";

    private static final List<String> ALL_OIDS = List.of(
            // System MIB
            SYS_DESCR_OID, SYS_OBJECT_ID_OID, SYS_UP_TIME_OID, SYS_CONTACT_OID,
            SYS_NAME_OID, SYS_LOCATION_OID, SYS_SERVICES_OID,
            // Host‑Resources MIB
            HR_SYSTEM_UPTIME_OID, HR_SYSTEM_DATE_OID, HR_SYSTEM_INITIAL_LOAD_DEVICE_OID,
            HR_SYSTEM_INITIAL_LOAD_PARAMS_OID, HR_SYSTEM_NUM_USERS_OID,
            HR_SYSTEM_PROCESSES_OID, HR_SYSTEM_MAX_PROCESSES_OID
    );

    /**
     * Poll a device and update both {@code system_info} and the backing {@code device}
     * table atomically.
     *
     * @return the freshly persisted snapshot or {@code null} when nothing could be collected.
     */
    @Transactional
    public SystemInfo pollDeviceSystemInfo(Device device, DeviceConfig config) {
        log.debug("Polling system information for device '{}' (id={})", device.getName(), device.getId());

        try {
            Map<String, Variable> snmpData = snmpClientService.snmpGetMultiple(config, ALL_OIDS);
            if (snmpData.isEmpty()) {
                log.warn("No SNMP data returned for device '{}'", device.getName());
                return null;
            }

            // 1) Load or create a SystemInfo aggregate ----------------------
            SystemInfo systemInfo = systemInfoRepository.findByDeviceId(device.getId())
                    .orElseGet(() -> SystemInfo.builder().device(device).build());

            // 2) Populate & persist SystemInfo -----------------------------
            populateSystemInfo(systemInfo, snmpData);
            systemInfo.setLastPolled(LocalDateTime.now());
            systemInfoRepository.save(systemInfo); // always save – INSERT or UPDATE

            // 3) Project the snapshot onto Device & persist ----------------
            projectToDevice(device, systemInfo);
            deviceRepository.save(device); // keeps detached entities safe; noop on managed ones

            log.info("System information updated for '{}' (id={})", device.getName(), device.getId());
            return systemInfo;
        } catch (Exception ex) {
            log.error("Polling system information failed for '{}': {}", device.getName(), ex.getMessage(), ex);
            return null; // do NOT propagate; caller decides whether to retry
        }
    }

    // ---------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------

    /**
     * Copy raw SNMP values into the SystemInfo JPA entity.
     */
    private void populateSystemInfo(SystemInfo si, Map<String, Variable> d) {
        // sysDescr ---------------------------------------------------------
        setIfPresent(d.get(SYS_DESCR_OID), v -> si.setSysDescr(decoded(v)));
        // sysObjectID ------------------------------------------------------
        setIfPresent(d.get(SYS_OBJECT_ID_OID), v -> si.setSysObjectId(v.toString()));
        // sysUpTime --------------------------------------------------------
        setIfPresent(d.get(SYS_UP_TIME_OID), v -> si.setSysUpTime(v.toLong()));
        // sysContact / sysName / sysLocation ------------------------------
        setIfPresent(d.get(SYS_CONTACT_OID),  v -> si.setSysContact(decoded(v)));
        setIfPresent(d.get(SYS_NAME_OID),     v -> si.setSysName(decoded(v)));
        setIfPresent(d.get(SYS_LOCATION_OID), v -> si.setSysLocation(decoded(v)));
        // sysServices ------------------------------------------------------
        setIfPresent(d.get(SYS_SERVICES_OID), v -> si.setSysServices(v.toInt()));
        // Host‑Resources ---------------------------------------------------
        setIfPresent(d.get(HR_SYSTEM_UPTIME_OID),              v -> si.setHrSystemUptime(v.toLong()));
        setIfPresent(d.get(HR_SYSTEM_DATE_OID),                v -> si.setHrSystemDate(parseSnmpDateAndTime(v.toString())));
        setIfPresent(d.get(HR_SYSTEM_INITIAL_LOAD_DEVICE_OID), v -> si.setHrSystemInitialLoadDevice(v.toInt()));
        setIfPresent(d.get(HR_SYSTEM_INITIAL_LOAD_PARAMS_OID), v -> si.setHrSystemInitialLoadParameters(v.toString()));
        setIfPresent(d.get(HR_SYSTEM_NUM_USERS_OID),           v -> si.setHrSystemNumUsers(v.toInt()));
        setIfPresent(d.get(HR_SYSTEM_PROCESSES_OID),           v -> si.setHrSystemProcesses(v.toInt()));
        setIfPresent(d.get(HR_SYSTEM_MAX_PROCESSES_OID),       v -> si.setHrSystemMaxProcesses(v.toInt()));
    }

    /**
     * Copy relevant SystemInfo fields back to the owning Device.
     * Kept in a single place so we never forget to update future additions.
     */
    private void projectToDevice(Device device, SystemInfo si) {
        // Uptime – prefer sysUpTime, fall back to hrSystemUptime -------------
        Long ticks = Optional.ofNullable(si.getSysUpTime()).orElse(si.getHrSystemUptime());
        if (ticks != null) {
            device.setSystemUptime(ticks / 100L); // TimeTicks (1/100s) → seconds
        }

        device.setSystemLocation(si.getSysLocation());
        device.setSystemName(si.getSysName());
        device.setSystemContact(si.getSysContact());
        device.setSystemObjectId(si.getSysObjectId());
        device.setDescription(si.getSysDescr());
        device.setSystemServices(si.getSysServices());
    }

    // ---------------------------------------------------------------------
    // Utility methods
    // ---------------------------------------------------------------------

    private void setIfPresent(Variable var, java.util.function.Consumer<Variable> consumer) {
        if (var != null) consumer.accept(var);
    }

    private String decoded(Variable v) {
        String str = v.toString();
        return snmpDataParser.isHexFormat(str) ? snmpDataParser.parseHexToString(str) : str;
    }

    /**
     * Parse SNMP DateAndTime textual representation (usually hex) into a {@link LocalDateTime}.
     * Returns {@code null} when the value is malformed.
     */
    private LocalDateTime parseSnmpDateAndTime(String hex) {
        try {
            if (hex == null || hex.length() < 16) return null;
            byte[] bytes = toBytes(hex);
            if (bytes.length < 8) return null;
            int year   = ((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);
            int month  = bytes[2] & 0xFF;
            int day    = bytes[3] & 0xFF;
            int hour   = bytes[4] & 0xFF;
            int minute = bytes[5] & 0xFF;
            int second = bytes[6] & 0xFF;
            if (month < 1 || month > 12 || day < 1 || day > 31 || hour > 23 || minute > 59 || second > 60)
                return null;
            return LocalDateTime.of(year, month, day, hour, minute, second);
        } catch (Exception ex) {
            log.debug("Failed to parse SNMP DateAndTime '{}': {}", hex, ex.getMessage());
            return null;
        }
    }

    private byte[] toBytes(String hex) {
        String cleaned = hex.replaceAll("[^0-9a-fA-F]", "");
        if ((cleaned.length() & 1) == 1) cleaned = "0" + cleaned; // pad
        byte[] out = new byte[cleaned.length() / 2];
        for (int i = 0; i < cleaned.length(); i += 2) {
            out[i / 2] = (byte) Integer.parseInt(cleaned.substring(i, i + 2), 16);
        }
        return out;
    }
}
