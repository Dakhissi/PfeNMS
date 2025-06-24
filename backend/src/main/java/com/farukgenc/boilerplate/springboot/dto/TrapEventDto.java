package com.farukgenc.boilerplate.springboot.dto;

import com.farukgenc.boilerplate.springboot.model.TrapEvent;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for TrapEvent responses
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrapEventDto {

    private Long id;
    private String sourceIp;
    private Integer sourcePort;
    private String community;
    private String trapOid;
    private String enterpriseOid;
    private Integer genericTrap;
    private Integer specificTrap;
    private Long timestamp;
    private Long uptime;
    private TrapEvent.TrapType trapType;
    private TrapEvent.TrapSeverity severity;
    private String message;
    private String rawData;
    private Map<String, Object> variableBindings;
    private Boolean processed;
    private Boolean alertCreated;
    private Long alertId;
    private Integer duplicateCount;
    private LocalDateTime lastOccurrence;
    private String hashKey;
    private Long deviceId;
    private String deviceName;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
