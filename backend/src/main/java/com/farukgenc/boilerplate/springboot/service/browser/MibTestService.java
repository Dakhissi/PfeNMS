package com.farukgenc.boilerplate.springboot.service.browser;

import com.farukgenc.boilerplate.springboot.dto.MibBrowserRequest;
import com.farukgenc.boilerplate.springboot.dto.MibBrowserResponse;
import com.farukgenc.boilerplate.springboot.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for testing MIB functionality with common OIDs
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MibTestService {

    private final MibService mibService;

    /**
     * Test standard system OIDs against a target device
     */
    public List<MibBrowserResponse> testSystemOids(String targetIp, String community, User user) {
        log.info("Testing system OIDs against target: {}", targetIp);

        List<MibBrowserResponse> results = new ArrayList<>();

        // Standard system OIDs to test
        String[] systemOids = {
            "1.3.6.1.2.1.1.1.0", // sysDescr
            "1.3.6.1.2.1.1.2.0", // sysObjectID
            "1.3.6.1.2.1.1.3.0", // sysUpTime
            "1.3.6.1.2.1.1.4.0", // sysContact
            "1.3.6.1.2.1.1.5.0", // sysName
            "1.3.6.1.2.1.1.6.0", // sysLocation
            "1.3.6.1.2.1.1.7.0"  // sysServices
        };

        for (String oid : systemOids) {
            MibBrowserRequest request = MibBrowserRequest.builder()
                    .targetIp(targetIp)
                    .community(community)
                    .oid(oid)
                    .timeout(5000)
                    .retries(3)
                    .build();

            MibBrowserResponse response = mibService.performSnmpGet(request, user);
            results.add(response);
        }

        return results;
    }

    /**
     * Test interface OIDs (walk ifTable)
     */
    public List<MibBrowserResponse> testInterfaceOids(String targetIp, String community, User user) {
        log.info("Testing interface OIDs against target: {}", targetIp);

        MibBrowserRequest request = MibBrowserRequest.builder()
                .targetIp(targetIp)
                .community(community)
                .oid("1.3.6.1.2.1.2.2.1") // ifTable
                .timeout(10000)
                .retries(3)
                .build();

        return mibService.performSnmpWalk(request, user);
    }

    /**
     * Test custom OID
     */
    public MibBrowserResponse testCustomOid(String targetIp, String community, String oid, User user) {
        log.info("Testing custom OID {} against target: {}", oid, targetIp);

        MibBrowserRequest request = MibBrowserRequest.builder()
                .targetIp(targetIp)
                .community(community)
                .oid(oid)
                .timeout(5000)
                .retries(3)
                .build();

        return mibService.performSnmpGet(request, user);
    }

    /**
     * Test SNMP connectivity
     */
    public boolean testConnectivity(String targetIp, String community) {
        log.info("Testing SNMP connectivity to target: {}", targetIp);

        MibBrowserRequest request = MibBrowserRequest.builder()
                .targetIp(targetIp)
                .community(community)
                .oid("1.3.6.1.2.1.1.1.0") // sysDescr - should always be available
                .timeout(3000)
                .retries(1)
                .build();

        try {
            MibBrowserResponse response = mibService.performSnmpGet(request, null);
            return response.isSuccess();
        } catch (Exception e) {
            log.error("Connectivity test failed: {}", e.getMessage());
            return false;
        }
    }
}
