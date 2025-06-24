package com.farukgenc.boilerplate.springboot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IpProfileDto {

    private Long id;

    private Boolean ipForwarding;
    private Integer ipDefaultTTL;
    private Long ipInReceives;
    private Long ipInHdrErrors;
    private Long ipInAddrErrors;
    private Long ipForwDatagrams;
    private Long ipInUnknownProtos;
    private Long ipInDiscards;
    private Long ipInDelivers;
    private Long ipOutRequests;
    private Long ipOutDiscards;
    private Long ipOutNoRoutes;
    private Integer ipReasmTimeout;
    private Long ipReasmReqds;
    private Long ipReasmOKs;
    private Long ipReasmFails;
    private Long ipFragOKs;
    private Long ipFragFails;
    private Long ipFragCreates;
    private String ipAddress;
    private String ipSubnetMask;
    private String ipBroadcastAddr;

    @NotNull(message = "Device ID is required")
    private Long deviceId;
    private String deviceName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
