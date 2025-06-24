package com.farukgenc.boilerplate.springboot.dto;

import com.farukgenc.boilerplate.springboot.model.UdpProfile;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UdpProfileDto {

    private Long id;

    private Long udpInDatagrams;
    private Long udpNoPorts;
    private Long udpInErrors;
    private Long udpOutDatagrams;
    private String udpLocalAddress;
    private Integer udpLocalPort;
    private String udpRemoteAddress;
    private Integer udpRemotePort;
    private UdpProfile.UdpEntryStatus udpEntryStatus;

    @NotNull(message = "Device ID is required")
    private Long deviceId;
    private String deviceName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
