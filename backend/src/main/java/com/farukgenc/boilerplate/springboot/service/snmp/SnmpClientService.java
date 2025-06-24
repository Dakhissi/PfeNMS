package com.farukgenc.boilerplate.springboot.service.snmp;

import com.farukgenc.boilerplate.springboot.model.DeviceConfig;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * Core SNMP client service for executing SNMP operations
 */
@Slf4j
@Service
public class SnmpClientService {

    private static final Map<String, Snmp> snmpSessions = new HashMap<>();    /**
     * Performs SNMP GET operation for a single OID
     */
    public Optional<Variable> snmpGet(DeviceConfig config, String oid) {
        try {
            Snmp snmp = getSnmpSession(config);
            Target<Address> target = createTarget(config);
            
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid)));
            pdu.setType(PDU.GET);
            
            ResponseEvent<Address> response = snmp.send(pdu, target);
            if (response != null && response.getResponse() != null) {
                PDU responsePDU = response.getResponse();
                if (responsePDU.getErrorStatus() == PDU.noError) {
                    VariableBinding vb = responsePDU.get(0);
                    return Optional.of(vb.getVariable());
                }
            }
        } catch (Exception e) {
            log.error("SNMP GET failed for OID {} on device {}: {}", oid, config.getTargetIp(), e.getMessage());
        }
        return Optional.empty();
    }    /**
     * Performs SNMP GET-BULK operation for table walking
     */
    public Map<String, Variable> snmpWalk(DeviceConfig config, String baseOid, int maxRepetitions) {
        Map<String, Variable> results = new HashMap<>();
        
        try {
            Snmp snmp = getSnmpSession(config);
            Target<Address> target = createTarget(config);
            
            OID rootOID = new OID(baseOid);
            OID currentOID = rootOID;
            
            while (currentOID.startsWith(rootOID)) {
                PDU pdu = new PDU();
                pdu.add(new VariableBinding(currentOID));
                pdu.setType(PDU.GETNEXT);
                
                ResponseEvent<Address> response = snmp.send(pdu, target);
                if (response == null || response.getResponse() == null) {
                    break;
                }
                
                PDU responsePDU = response.getResponse();
                if (responsePDU.getErrorStatus() != PDU.noError) {
                    break;
                }
                
                VariableBinding vb = responsePDU.get(0);
                OID nextOID = vb.getOid();
                
                if (!nextOID.startsWith(rootOID)) {
                    break;
                }
                
                results.put(nextOID.toString(), vb.getVariable());
                currentOID = nextOID;
                
                if (results.size() >= maxRepetitions) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("SNMP WALK failed for OID {} on device {}: {}", baseOid, config.getTargetIp(), e.getMessage());
        }
        
        return results;
    }    /**
     * Performs SNMP GET for multiple OIDs
     */
    public Map<String, Variable> snmpGetMultiple(DeviceConfig config, List<String> oids) {
        Map<String, Variable> results = new HashMap<>();
        
        try {
            Snmp snmp = getSnmpSession(config);
            Target<Address> target = createTarget(config);
            
            PDU pdu = new PDU();
            for (String oid : oids) {
                pdu.add(new VariableBinding(new OID(oid)));
            }
            pdu.setType(PDU.GET);
            
            ResponseEvent<Address> response = snmp.send(pdu, target);
            if (response != null && response.getResponse() != null) {
                PDU responsePDU = response.getResponse();
                if (responsePDU.getErrorStatus() == PDU.noError) {
                    for (int i = 0; i < responsePDU.size(); i++) {
                        VariableBinding vb = responsePDU.get(i);
                        results.put(vb.getOid().toString(), vb.getVariable());
                    }
                }
            }
        } catch (Exception e) {
            log.error("SNMP GET multiple failed for device {}: {}", config.getTargetIp(), e.getMessage());
        }
        
        return results;
    }

    /**
     * Tests SNMP connectivity to a device
     */
    public boolean testConnection(DeviceConfig config) {
        Optional<Variable> sysName = snmpGet(config, "1.3.6.1.2.1.1.5.0"); // sysName
        return sysName.isPresent();
    }

    /**
     * Gets or creates SNMP session for a device configuration
     */
    private Snmp getSnmpSession(DeviceConfig config) throws IOException {
        String sessionKey = generateSessionKey(config);
        
        if (!snmpSessions.containsKey(sessionKey)) {
            TransportMapping<?> transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);
            
            if (config.getSnmpVersion() == DeviceConfig.SnmpVersion.V3) {
                setupSnmpV3(snmp, config);
            }
            
            transport.listen();
            snmpSessions.put(sessionKey, snmp);
        }
        
        return snmpSessions.get(sessionKey);
    }    /**
     * Creates target for SNMP operations
     */
    private Target<Address> createTarget(DeviceConfig config) {
        Address targetAddress = GenericAddress.parse("udp:" + config.getTargetIp() + "/" + config.getSnmpPort());
        
        if (config.getSnmpVersion() == DeviceConfig.SnmpVersion.V3) {
            UserTarget<Address> target = new UserTarget<>();
            target.setAddress(targetAddress);
            target.setRetries(config.getSnmpRetries());
            target.setTimeout(config.getSnmpTimeout());
            target.setVersion(SnmpConstants.version3);
            target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
            target.setSecurityName(new OctetString(config.getSecurityName()));
            return target;
        } else {
            CommunityTarget<Address> target = new CommunityTarget<>();
            target.setCommunity(new OctetString(config.getCommunityString()));
            target.setAddress(targetAddress);
            target.setRetries(config.getSnmpRetries());
            target.setTimeout(config.getSnmpTimeout());
            target.setVersion(config.getSnmpVersion() == DeviceConfig.SnmpVersion.V1 ? 
                SnmpConstants.version1 : SnmpConstants.version2c);
            return target;
        }
    }

    /**
     * Sets up SNMPv3 security parameters
     */
    private void setupSnmpV3(Snmp snmp, DeviceConfig config) {
        USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
        SecurityModels.getInstance().addSecurityModel(usm);

        // Add user for SNMPv3
        UsmUser user = new UsmUser(
            new OctetString(config.getSecurityName()),
            getAuthProtocol(config.getAuthProtocol()),
            new OctetString(config.getAuthPassphrase() != null ? config.getAuthPassphrase() : ""),
            getPrivProtocol(config.getPrivProtocol()),
            new OctetString(config.getPrivPassphrase() != null ? config.getPrivPassphrase() : "")
        );
        
        snmp.getUSM().addUser(user);
    }    /**
     * Maps auth protocol enum to SNMP4J auth protocol
     */
    private OID getAuthProtocol(DeviceConfig.AuthProtocol authProtocol) {
        if (authProtocol == null) return null;
        
        return switch (authProtocol) {
            case MD5 -> AuthMD5.ID;
            case SHA, SHA224, SHA256, SHA384, SHA512 -> AuthSHA.ID; // Use SHA for all SHA variants
            default -> null;
        };
    }

    /**
     * Maps privacy protocol enum to SNMP4J privacy protocol
     */
    private OID getPrivProtocol(DeviceConfig.PrivProtocol privProtocol) {
        if (privProtocol == null) return null;
        
        return switch (privProtocol) {
            case DES -> PrivDES.ID;
            case AES128 -> PrivAES128.ID;
            case AES192 -> PrivAES192.ID;
            case AES256 -> PrivAES256.ID;
            default -> null;
        };
    }

    /**
     * Generates session key for caching SNMP sessions
     */
    private String generateSessionKey(DeviceConfig config) {
        return String.format("%s:%d:%s:%s", 
            config.getTargetIp(), 
            config.getSnmpPort(),
            config.getSnmpVersion(),
            config.getCommunityString() != null ? config.getCommunityString() : config.getSecurityName()
        );
    }

    /**
     * Closes all SNMP sessions (for cleanup)
     */
    public void closeAllSessions() {
        for (Snmp snmp : snmpSessions.values()) {
            try {
                snmp.close();
            } catch (IOException e) {
                log.warn("Failed to close SNMP session: {}", e.getMessage());
            }
        }
        snmpSessions.clear();
    }
}
