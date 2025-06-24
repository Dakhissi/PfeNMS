package com.farukgenc.boilerplate.springboot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemUnitDto {

    private Long id;

    @NotNull(message = "Unit index is required")
    private Integer unitIndex;

    private String unitName;
    private String unitDescription;
    private String unitType;
    private String unitHwVersion;
    private String unitFwVersion;
    private String unitSwVersion;
    private String unitSerialNumber;
    private String unitMfgName;
    private String unitModelName;
    private String unitAlias;
    private String unitAssetId;
    private Boolean unitIsFru;
    private LocalDateTime unitMfgDate;
    private String unitUris;

    @NotNull(message = "Device ID is required")
    private Long deviceId;
    private String deviceName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
