package com.farukgenc.boilerplate.springboot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing SNMP trap events received from network devices
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trap_events")
public class TrapEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_ip", nullable = false)
    private String sourceIp;

    @Column(name = "source_port")
    private Integer sourcePort;

    @Column(name = "community")
    private String community;

    @Column(name = "trap_oid", nullable = false)
    private String trapOid;

    @Column(name = "enterprise_oid")
    private String enterpriseOid;

    @Column(name = "generic_trap")
    private Integer genericTrap;

    @Column(name = "specific_trap")
    private Integer specificTrap;

    @Column(name = "timestamp")
    private Long timestamp;

    @Column(name = "uptime")
    private Long uptime;

    @Enumerated(EnumType.STRING)
    @Column(name = "trap_type", nullable = false)
    private TrapType trapType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    @Builder.Default
    private TrapSeverity severity = TrapSeverity.INFO;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "raw_data", columnDefinition = "TEXT")
    private String rawData;

    @Column(name = "variable_bindings", columnDefinition = "TEXT")
    private String variableBindings; // JSON format

    @Column(name = "processed")
    @Builder.Default
    private Boolean processed = false;

    @Column(name = "alert_created")
    @Builder.Default
    private Boolean alertCreated = false;

    @Column(name = "alert_id")
    private Long alertId;

    @Column(name = "duplicate_count")
    @Builder.Default
    private Integer duplicateCount = 1;

    @Column(name = "last_occurrence")
    private LocalDateTime lastOccurrence;

    @Column(name = "hash_key", unique = true)
    private String hashKey; // For duplicate detection

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device; // Linked device if found

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Owner of the device

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TrapType {
        COLD_START,
        WARM_START,
        LINK_DOWN,
        LINK_UP,
        AUTHENTICATION_FAILURE,
        EGP_NEIGHBOR_LOSS,
        ENTERPRISE_SPECIFIC,
        DEVICE_DOWN,
        DEVICE_UP,
        INTERFACE_DOWN,
        INTERFACE_UP,
        CONFIGURATION_CHANGE,
        THRESHOLD_EXCEEDED,
        SYSTEM_RESTART,
        POWER_FAILURE,
        TEMPERATURE_ALARM,
        FAN_FAILURE,
        DISK_FULL,
        MEMORY_LOW,
        CPU_HIGH,
        UNKNOWN
    }

    public enum TrapSeverity {
        CRITICAL,
        MAJOR,
        MINOR,
        WARNING,
        INFO,
        CLEARED
    }
}
