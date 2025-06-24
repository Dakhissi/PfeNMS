package com.farukgenc.boilerplate.springboot.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * DTO for SNMP walk operation results
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnmpWalkResult {
    
    private String ipAddress;
    private String community;
    private String startOid;
    private boolean success;
    private String errorMessage;
    private List<SnmpVariable> variables;
    private Map<String, String> systemInfo; // sysName, sysDescr, etc.
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SnmpVariable {
        private String oid;
        private String value;
        private String type;
    }
}
