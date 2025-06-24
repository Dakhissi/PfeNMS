package com.farukgenc.boilerplate.springboot.dto;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDto {

    private Long id;

    @NotBlank(message = "Device name is required")
    private String name;

    private String description;
    private String systemObjectId;
    private Long systemUptime;
    private String systemContact;
    private String systemName;
    private String systemLocation;
    private Integer systemServices;

    // Monitoring
    private LocalDateTime lastMonitored;
    private Boolean monitoringEnabled;

    @NotNull(message = "Device status is required")
    private Device.DeviceStatus status;

    @NotNull(message = "Device type is required")
    private Device.DeviceType type;

    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Device Configuration (includes network and SNMP settings)
    private DeviceConfigDto deviceConfig;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceConfigDto {
        private Long id;
        private String targetIp;
        private Integer snmpPort;
        private DeviceConfig.SnmpVersion snmpVersion;
        private String communityString;
        private Integer snmpTimeout;
        private Integer snmpRetries;
        private Integer pollInterval;
        private Boolean enabled;
        private LocalDateTime lastPollTime;
        private DeviceConfig.PollStatus lastPollStatus;
        private String errorMessage;
        private Integer consecutiveFailures;

        // SNMPv3 specific fields
        private String securityName;
        private DeviceConfig.AuthProtocol authProtocol;
        private String authPassphrase;
        private DeviceConfig.PrivProtocol privProtocol;
        private String privPassphrase;
        private String contextName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
