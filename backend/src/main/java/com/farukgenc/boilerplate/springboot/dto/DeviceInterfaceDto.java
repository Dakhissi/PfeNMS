package com.farukgenc.boilerplate.springboot.dto;

import com.farukgenc.boilerplate.springboot.model.DeviceInterface;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInterfaceDto {

    private Long id;

    @NotNull(message = "Interface index is required")
    private Integer ifIndex;

    private String ifDescr;
    private DeviceInterface.InterfaceType ifType;
    private Integer ifMtu;
    private Long ifSpeed;
    private String ifPhysAddress;
    private DeviceInterface.InterfaceStatus ifAdminStatus;
    private DeviceInterface.InterfaceStatus ifOperStatus;
    private Long ifLastChange;
    private Long ifInOctets;
    private Long ifInUcastPkts;
    private Long ifInNucastPkts;
    private Long ifInDiscards;
    private Long ifInErrors;
    private Long ifInUnknownProtos;
    private Long ifOutOctets;
    private Long ifOutUcastPkts;
    private Long ifOutNucastPkts;
    private Long ifOutDiscards;
    private Long ifOutErrors;
    private Long ifOutQLen;

    @NotNull(message = "Device ID is required")
    private Long deviceId;
    private String deviceName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
