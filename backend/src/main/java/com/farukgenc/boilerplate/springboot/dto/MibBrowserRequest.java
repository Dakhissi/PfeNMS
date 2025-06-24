package com.farukgenc.boilerplate.springboot.dto;

import lombok.*;

/**
 * DTO for MIB Browser operations - testing OID values against target
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MibBrowserRequest {    private String targetIp;
    @Builder.Default
    private Integer snmpPort = 161;
    @Builder.Default
    private String community = "public";
    private String oid;
    @Builder.Default
    private Integer timeout = 5000;
    @Builder.Default
    private Integer retries = 3;
}
