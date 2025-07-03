package com.farukgenc.boilerplate.springboot.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for MIB Browser operations - testing OID values against target
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MibBrowserRequest {

    @NotBlank(message = "Target IP address is required")
    private String targetIp;

    @Builder.Default
    @Min(value = 1, message = "SNMP port must be between 1 and 65535")
    @Max(value = 65535, message = "SNMP port must be between 1 and 65535")
    private Integer snmpPort = 161;

    @Builder.Default
    @NotBlank(message = "Community string is required")
    private String community = "public";

    @NotBlank(message = "OID is required")
    private String oid;

    @Builder.Default
    @Min(value = 1000, message = "Timeout must be at least 1000ms")
    @Max(value = 30000, message = "Timeout must be at most 30000ms")
    private Integer timeout = 5000;

    @Builder.Default
    @Min(value = 0, message = "Retries must be at least 0")
    @Max(value = 10, message = "Retries must be at most 10")
    private Integer retries = 3;
}
