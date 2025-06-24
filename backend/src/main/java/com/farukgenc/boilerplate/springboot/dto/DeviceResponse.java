package com.farukgenc.boilerplate.springboot.dto;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceConfig;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for device responses that includes both device info and runtime/poll details
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponse {

    private Long id;
    private String name;
    private String description;
    private String systemObjectId;
    private Long systemUptime;
    private String systemContact;
    private String systemName;
    private String systemLocation;
    private Integer systemServices;
    private Device.DeviceStatus status;
    private Device.DeviceType type;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Configuration details
    private DeviceConfigResponse configuration;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceConfigResponse {
        private Long id;
        private String targetIp;
        private Integer snmpPort;
        private DeviceConfig.SnmpVersion snmpVersion;
        private String communityString;
        private Integer snmpTimeout;
        private Integer snmpRetries;
        private Integer pollInterval;
        private Boolean enabled;

        // Runtime/Poll details
        private LocalDateTime lastPollTime;
        private DeviceConfig.PollStatus lastPollStatus;
        private String errorMessage;
        private Integer consecutiveFailures;

        // SNMPv3 specific fields
        private String securityName;
        private DeviceConfig.AuthProtocol authProtocol;
        private DeviceConfig.PrivProtocol privProtocol;
        private String contextName;
        
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
