package com.farukgenc.boilerplate.springboot.dto;

import lombok.*;

/**
 * DTO for MIB Browser response
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MibBrowserResponse {

    private String oid;
    private String name;
    private String value;
    private String type;
    private String syntax;
    private boolean success;
    private String errorMessage;
    private long responseTime; // in milliseconds
}
