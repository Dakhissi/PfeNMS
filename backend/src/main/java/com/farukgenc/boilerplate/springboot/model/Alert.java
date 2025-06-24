package com.farukgenc.boilerplate.springboot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Alert entity for monitoring device/interface status changes
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AlertStatus status = AlertStatus.ACTIVE;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "source_id")
    private Long sourceId; // ID of the device, interface, etc.

    @Column(name = "source_type")
    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    @Column(name = "source_name")
    private String sourceName;

    @Column(name = "alert_key")
    private String alertKey; // Unique key to prevent duplicates

    @Column(name = "first_occurrence")
    private LocalDateTime firstOccurrence;

    @Column(name = "last_occurrence")
    private LocalDateTime lastOccurrence;

    @Column(name = "occurrence_count")
    @Builder.Default
    private Integer occurrenceCount = 1;

    @Column(name = "acknowledged")
    @Builder.Default
    private Boolean acknowledged = false;

    @Column(name = "acknowledged_by")
    private String acknowledgedBy;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by")
    private String resolvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;    public enum AlertType {
        DEVICE_DOWN, DEVICE_UP, INTERFACE_DOWN, INTERFACE_UP, 
        SYSTEM_DOWN, SYSTEM_UP, HIGH_CPU, HIGH_MEMORY, 
        HIGH_BANDWIDTH, SNMP_TIMEOUT, CONNECTIVITY_LOST, 
        CONFIGURATION_CHANGED, PERFORMANCE, CONNECTIVITY
    }

    public enum AlertSeverity {
        CRITICAL, MAJOR, MINOR, WARNING, INFO
    }

    public enum AlertStatus {
        ACTIVE, ACKNOWLEDGED, RESOLVED, CLEARED
    }

    public enum SourceType {
        DEVICE, INTERFACE, SYSTEM_UNIT, IP_PROFILE, ICMP_PROFILE, UDP_PROFILE
    }
}
