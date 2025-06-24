package com.farukgenc.boilerplate.springboot.dto;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

/**
 * DTO for creating a new device with its configuration
 * Contains only the configuration part, not the runtime/poll details
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCreateRequest {

    // Basic device information
    @NotBlank(message = "Device name is required")
    private String name;

    private String description;
    private String systemContact;
    private String systemName;
    private String systemLocation;

    @NotNull(message = "Device type is required")
    private Device.DeviceType type;

    // Device configuration (SNMP settings)
    @Valid
    @NotNull(message = "Device configuration is required")
    private DeviceConfigDto configuration;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceConfigDto {

        @NotBlank(message = "Target IP is required")
        private String targetIp;

        @Min(value = 1, message = "SNMP port must be between 1 and 65535")
        @Max(value = 65535, message = "SNMP port must be between 1 and 65535")
        @Builder.Default
        private Integer snmpPort = 161;

        @NotNull(message = "SNMP version is required")
        @Builder.Default
        private DeviceConfig.SnmpVersion snmpVersion = DeviceConfig.SnmpVersion.V2C;

        @Builder.Default
        private String communityString = "public";

        @Min(value = 1000, message = "SNMP timeout must be at least 1000ms")
        @Builder.Default
        private Integer snmpTimeout = 5000; // milliseconds

        @Min(value = 1, message = "SNMP retries must be at least 1")
        @Max(value = 10, message = "SNMP retries must not exceed 10")
        @Builder.Default
        private Integer snmpRetries = 3;

        @Min(value = 30, message = "Poll interval must be at least 30 seconds")
        @Builder.Default
        private Integer pollInterval = 300; // seconds

        @Builder.Default
        private Boolean enabled = true;

        // SNMPv3 specific fields (optional)
        private String securityName;
        private DeviceConfig.AuthProtocol authProtocol;
        private String authPassphrase;
        private DeviceConfig.PrivProtocol privProtocol;
        private String privPassphrase;
        private String contextName;
    }
}

