package com.farukgenc.boilerplate.springboot.dto;

import com.farukgenc.boilerplate.springboot.model.Alert;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for Alert responses
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDto {

    private Long id;
    private Alert.AlertType type;
    private Alert.AlertSeverity severity;
    private Alert.AlertStatus status;
    private String title;
    private String description;
    private Long sourceId;
    private Alert.SourceType sourceType;
    private String sourceName;
    private String alertKey;
    private LocalDateTime firstOccurrence;
    private LocalDateTime lastOccurrence;
    private Integer occurrenceCount;
    private Boolean acknowledged;
    private String acknowledgedBy;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime resolvedAt;
    private String resolvedBy;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
