package com.farukgenc.boilerplate.springboot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IcmpProfileDto {

    private Long id;

    private Long icmpInMsgs;
    private Long icmpInErrors;
    private Long icmpInDestUnreachs;
    private Long icmpInTimeExcds;
    private Long icmpInParmProbs;
    private Long icmpInSrcQuenchs;
    private Long icmpInRedirects;
    private Long icmpInEchos;
    private Long icmpInEchoReps;
    private Long icmpInTimestamps;
    private Long icmpInTimestampReps;
    private Long icmpInAddrMasks;
    private Long icmpInAddrMaskReps;
    private Long icmpOutMsgs;
    private Long icmpOutErrors;
    private Long icmpOutDestUnreachs;
    private Long icmpOutTimeExcds;
    private Long icmpOutParmProbs;
    private Long icmpOutSrcQuenchs;
    private Long icmpOutRedirects;
    private Long icmpOutEchos;
    private Long icmpOutEchoReps;
    private Long icmpOutTimestamps;
    private Long icmpOutTimestampReps;
    private Long icmpOutAddrMasks;
    private Long icmpOutAddrMaskReps;

    @NotNull(message = "Device ID is required")
    private Long deviceId;
    private String deviceName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
