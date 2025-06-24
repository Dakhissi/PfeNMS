package com.farukgenc.boilerplate.springboot.service.snmp;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceConfig;
import com.farukgenc.boilerplate.springboot.model.IcmpProfile;
import com.farukgenc.boilerplate.springboot.repository.IcmpProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.smi.Variable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for polling and updating ICMP profile information via SNMP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IcmpProfilePollService {

    private final SnmpClientService snmpClientService;
    private final IcmpProfileRepository icmpProfileRepository;

    // ICMP MIB OIDs
    private static final String ICMP_IN_MSGS_OID = "1.3.6.1.2.1.5.1.0";              // icmpInMsgs
    private static final String ICMP_IN_ERRORS_OID = "1.3.6.1.2.1.5.2.0";            // icmpInErrors
    private static final String ICMP_IN_DEST_UNREACHS_OID = "1.3.6.1.2.1.5.3.0";     // icmpInDestUnreachs
    private static final String ICMP_IN_TIME_EXCDS_OID = "1.3.6.1.2.1.5.4.0";        // icmpInTimeExcds
    private static final String ICMP_IN_PARM_PROBS_OID = "1.3.6.1.2.1.5.5.0";        // icmpInParmProbs
    private static final String ICMP_IN_SRC_QUENCHS_OID = "1.3.6.1.2.1.5.6.0";       // icmpInSrcQuenchs
    private static final String ICMP_IN_REDIRECTS_OID = "1.3.6.1.2.1.5.7.0";         // icmpInRedirects
    private static final String ICMP_IN_ECHOS_OID = "1.3.6.1.2.1.5.8.0";             // icmpInEchos
    private static final String ICMP_IN_ECHO_REPS_OID = "1.3.6.1.2.1.5.9.0";         // icmpInEchoReps
    private static final String ICMP_IN_TIMESTAMPS_OID = "1.3.6.1.2.1.5.10.0";       // icmpInTimestamps
    private static final String ICMP_IN_TIMESTAMP_REPS_OID = "1.3.6.1.2.1.5.11.0";   // icmpInTimestampReps
    private static final String ICMP_IN_ADDR_MASKS_OID = "1.3.6.1.2.1.5.12.0";       // icmpInAddrMasks
    private static final String ICMP_IN_ADDR_MASK_REPS_OID = "1.3.6.1.2.1.5.13.0";   // icmpInAddrMaskReps
    private static final String ICMP_OUT_MSGS_OID = "1.3.6.1.2.1.5.14.0";            // icmpOutMsgs
    private static final String ICMP_OUT_ERRORS_OID = "1.3.6.1.2.1.5.15.0";          // icmpOutErrors
    private static final String ICMP_OUT_DEST_UNREACHS_OID = "1.3.6.1.2.1.5.16.0";   // icmpOutDestUnreachs
    private static final String ICMP_OUT_TIME_EXCDS_OID = "1.3.6.1.2.1.5.17.0";      // icmpOutTimeExcds
    private static final String ICMP_OUT_PARM_PROBS_OID = "1.3.6.1.2.1.5.18.0";      // icmpOutParmProbs
    private static final String ICMP_OUT_SRC_QUENCHS_OID = "1.3.6.1.2.1.5.19.0";     // icmpOutSrcQuenchs
    private static final String ICMP_OUT_REDIRECTS_OID = "1.3.6.1.2.1.5.20.0";       // icmpOutRedirects
    private static final String ICMP_OUT_ECHOS_OID = "1.3.6.1.2.1.5.21.0";           // icmpOutEchos
    private static final String ICMP_OUT_ECHO_REPS_OID = "1.3.6.1.2.1.5.22.0";       // icmpOutEchoReps
    private static final String ICMP_OUT_TIMESTAMPS_OID = "1.3.6.1.2.1.5.23.0";      // icmpOutTimestamps
    private static final String ICMP_OUT_TIMESTAMP_REPS_OID = "1.3.6.1.2.1.5.24.0";  // icmpOutTimestampReps
    private static final String ICMP_OUT_ADDR_MASKS_OID = "1.3.6.1.2.1.5.25.0";      // icmpOutAddrMasks
    private static final String ICMP_OUT_ADDR_MASK_REPS_OID = "1.3.6.1.2.1.5.26.0";  // icmpOutAddrMaskReps

    /**
     * Poll and update ICMP profile information for a device
     */
    @Transactional
    public void pollDeviceIcmpProfile(Device device, DeviceConfig config) {
        log.debug("Polling ICMP profile for device: {}", device.getName());
        
        try {
            // Get ICMP statistics
            List<String> icmpOids = Arrays.asList(
                ICMP_IN_MSGS_OID, ICMP_IN_ERRORS_OID, ICMP_IN_DEST_UNREACHS_OID,
                ICMP_IN_TIME_EXCDS_OID, ICMP_IN_PARM_PROBS_OID, ICMP_IN_SRC_QUENCHS_OID,
                ICMP_IN_REDIRECTS_OID, ICMP_IN_ECHOS_OID, ICMP_IN_ECHO_REPS_OID,
                ICMP_IN_TIMESTAMPS_OID, ICMP_IN_TIMESTAMP_REPS_OID, ICMP_IN_ADDR_MASKS_OID,
                ICMP_IN_ADDR_MASK_REPS_OID, ICMP_OUT_MSGS_OID, ICMP_OUT_ERRORS_OID,
                ICMP_OUT_DEST_UNREACHS_OID, ICMP_OUT_TIME_EXCDS_OID, ICMP_OUT_PARM_PROBS_OID,
                ICMP_OUT_SRC_QUENCHS_OID, ICMP_OUT_REDIRECTS_OID, ICMP_OUT_ECHOS_OID,
                ICMP_OUT_ECHO_REPS_OID, ICMP_OUT_TIMESTAMPS_OID, ICMP_OUT_TIMESTAMP_REPS_OID,
                ICMP_OUT_ADDR_MASKS_OID, ICMP_OUT_ADDR_MASK_REPS_OID
            );
            
            Map<String, Variable> icmpData = snmpClientService.snmpGetMultiple(config, icmpOids);
            
            if (icmpData.isEmpty()) {
                log.warn("No ICMP data retrieved for device: {}", device.getName());
                return;
            }
            
            // Find existing ICMP profile or create new one
            Optional<IcmpProfile> existingIcmpProfile = icmpProfileRepository.findByDeviceId(device.getId());
            
            IcmpProfile icmpProfile = existingIcmpProfile.orElse(
                IcmpProfile.builder()
                    .device(device)
                    .build()
            );
            
            // Update ICMP profile properties
            updateIcmpProfileFromSnmpData(icmpProfile, icmpData);
            
            // Save the ICMP profile
            icmpProfileRepository.save(icmpProfile);
            log.info("Updated ICMP profile for device: {}", device.getName());
            
        } catch (Exception e) {
            log.error("Failed to poll ICMP profile for device {}: {}", device.getName(), e.getMessage(), e);
            throw new RuntimeException("ICMP profile polling failed for device: " + device.getName(), e);
        }
    }

    /**
     * Update IcmpProfile entity with SNMP data
     */
    private void updateIcmpProfileFromSnmpData(IcmpProfile icmpProfile, Map<String, Variable> data) {
        // ICMP Input Statistics
        Variable icmpInMsgs = data.get(ICMP_IN_MSGS_OID);
        if (icmpInMsgs != null) {
            icmpProfile.setIcmpInMsgs(icmpInMsgs.toLong());
        }
        
        Variable icmpInErrors = data.get(ICMP_IN_ERRORS_OID);
        if (icmpInErrors != null) {
            icmpProfile.setIcmpInErrors(icmpInErrors.toLong());
        }
        
        Variable icmpInDestUnreachs = data.get(ICMP_IN_DEST_UNREACHS_OID);
        if (icmpInDestUnreachs != null) {
            icmpProfile.setIcmpInDestUnreachs(icmpInDestUnreachs.toLong());
        }
        
        Variable icmpInTimeExcds = data.get(ICMP_IN_TIME_EXCDS_OID);
        if (icmpInTimeExcds != null) {
            icmpProfile.setIcmpInTimeExcds(icmpInTimeExcds.toLong());
        }
        
        Variable icmpInParmProbs = data.get(ICMP_IN_PARM_PROBS_OID);
        if (icmpInParmProbs != null) {
            icmpProfile.setIcmpInParmProbs(icmpInParmProbs.toLong());
        }
        
        Variable icmpInSrcQuenchs = data.get(ICMP_IN_SRC_QUENCHS_OID);
        if (icmpInSrcQuenchs != null) {
            icmpProfile.setIcmpInSrcQuenchs(icmpInSrcQuenchs.toLong());
        }
        
        Variable icmpInRedirects = data.get(ICMP_IN_REDIRECTS_OID);
        if (icmpInRedirects != null) {
            icmpProfile.setIcmpInRedirects(icmpInRedirects.toLong());
        }
        
        Variable icmpInEchos = data.get(ICMP_IN_ECHOS_OID);
        if (icmpInEchos != null) {
            icmpProfile.setIcmpInEchos(icmpInEchos.toLong());
        }
        
        Variable icmpInEchoReps = data.get(ICMP_IN_ECHO_REPS_OID);
        if (icmpInEchoReps != null) {
            icmpProfile.setIcmpInEchoReps(icmpInEchoReps.toLong());
        }
        
        Variable icmpInTimestamps = data.get(ICMP_IN_TIMESTAMPS_OID);
        if (icmpInTimestamps != null) {
            icmpProfile.setIcmpInTimestamps(icmpInTimestamps.toLong());
        }
        
        Variable icmpInTimestampReps = data.get(ICMP_IN_TIMESTAMP_REPS_OID);
        if (icmpInTimestampReps != null) {
            icmpProfile.setIcmpInTimestampReps(icmpInTimestampReps.toLong());
        }
        
        Variable icmpInAddrMasks = data.get(ICMP_IN_ADDR_MASKS_OID);
        if (icmpInAddrMasks != null) {
            icmpProfile.setIcmpInAddrMasks(icmpInAddrMasks.toLong());
        }
        
        Variable icmpInAddrMaskReps = data.get(ICMP_IN_ADDR_MASK_REPS_OID);
        if (icmpInAddrMaskReps != null) {
            icmpProfile.setIcmpInAddrMaskReps(icmpInAddrMaskReps.toLong());
        }
        
        // ICMP Output Statistics
        Variable icmpOutMsgs = data.get(ICMP_OUT_MSGS_OID);
        if (icmpOutMsgs != null) {
            icmpProfile.setIcmpOutMsgs(icmpOutMsgs.toLong());
        }
        
        Variable icmpOutErrors = data.get(ICMP_OUT_ERRORS_OID);
        if (icmpOutErrors != null) {
            icmpProfile.setIcmpOutErrors(icmpOutErrors.toLong());
        }
        
        Variable icmpOutDestUnreachs = data.get(ICMP_OUT_DEST_UNREACHS_OID);
        if (icmpOutDestUnreachs != null) {
            icmpProfile.setIcmpOutDestUnreachs(icmpOutDestUnreachs.toLong());
        }
        
        Variable icmpOutTimeExcds = data.get(ICMP_OUT_TIME_EXCDS_OID);
        if (icmpOutTimeExcds != null) {
            icmpProfile.setIcmpOutTimeExcds(icmpOutTimeExcds.toLong());
        }
        
        Variable icmpOutParmProbs = data.get(ICMP_OUT_PARM_PROBS_OID);
        if (icmpOutParmProbs != null) {
            icmpProfile.setIcmpOutParmProbs(icmpOutParmProbs.toLong());
        }
        
        Variable icmpOutSrcQuenchs = data.get(ICMP_OUT_SRC_QUENCHS_OID);
        if (icmpOutSrcQuenchs != null) {
            icmpProfile.setIcmpOutSrcQuenchs(icmpOutSrcQuenchs.toLong());
        }
        
        Variable icmpOutRedirects = data.get(ICMP_OUT_REDIRECTS_OID);
        if (icmpOutRedirects != null) {
            icmpProfile.setIcmpOutRedirects(icmpOutRedirects.toLong());
        }
        
        Variable icmpOutEchos = data.get(ICMP_OUT_ECHOS_OID);
        if (icmpOutEchos != null) {
            icmpProfile.setIcmpOutEchos(icmpOutEchos.toLong());
        }
        
        Variable icmpOutEchoReps = data.get(ICMP_OUT_ECHO_REPS_OID);
        if (icmpOutEchoReps != null) {
            icmpProfile.setIcmpOutEchoReps(icmpOutEchoReps.toLong());
        }
        
        Variable icmpOutTimestamps = data.get(ICMP_OUT_TIMESTAMPS_OID);
        if (icmpOutTimestamps != null) {
            icmpProfile.setIcmpOutTimestamps(icmpOutTimestamps.toLong());
        }
        
        Variable icmpOutTimestampReps = data.get(ICMP_OUT_TIMESTAMP_REPS_OID);
        if (icmpOutTimestampReps != null) {
            icmpProfile.setIcmpOutTimestampReps(icmpOutTimestampReps.toLong());
        }
        
        Variable icmpOutAddrMasks = data.get(ICMP_OUT_ADDR_MASKS_OID);
        if (icmpOutAddrMasks != null) {
            icmpProfile.setIcmpOutAddrMasks(icmpOutAddrMasks.toLong());
        }
        
        Variable icmpOutAddrMaskReps = data.get(ICMP_OUT_ADDR_MASK_REPS_OID);
        if (icmpOutAddrMaskReps != null) {
            icmpProfile.setIcmpOutAddrMaskReps(icmpOutAddrMaskReps.toLong());
        }
    }
}
