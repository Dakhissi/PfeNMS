package com.farukgenc.boilerplate.springboot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Device Configuration entity containing SNMP and network configuration
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "device_configs")
public class DeviceConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false, unique = true)
    private Device device;

    @Column(name = "target_ip", nullable = false)
    private String targetIp;

    @Column(name = "snmp_port")
    @Builder.Default
    private Integer snmpPort = 161;

    @Column(name = "snmp_version")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SnmpVersion snmpVersion = SnmpVersion.V2C;

    @Column(name = "community_string")
    @Builder.Default
    private String communityString = "public";

    @Column(name = "snmp_timeout")
    @Builder.Default
    private Integer snmpTimeout = 5000; // milliseconds

    @Column(name = "snmp_retries")
    @Builder.Default
    private Integer snmpRetries = 3;

    @Column(name = "poll_interval")
    @Builder.Default
    private Integer pollInterval = 300; // seconds

    @Column(name = "enabled")
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "last_poll_time")
    private LocalDateTime lastPollTime;

    @Column(name = "last_poll_status")
    @Enumerated(EnumType.STRING)
    private PollStatus lastPollStatus;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "consecutive_failures")
    @Builder.Default
    private Integer consecutiveFailures = 0;

    // SNMPv3 specific fields
    @Column(name = "security_name")
    private String securityName;

    @Column(name = "auth_protocol")
    @Enumerated(EnumType.STRING)
    private AuthProtocol authProtocol;

    @Column(name = "auth_passphrase")
    private String authPassphrase;

    @Column(name = "priv_protocol")
    @Enumerated(EnumType.STRING)
    private PrivProtocol privProtocol;

    @Column(name = "priv_passphrase")
    private String privPassphrase;

    @Column(name = "context_name")
    private String contextName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum SnmpVersion {
        V1, V2C, V3
    }

    public enum PollStatus {
        SUCCESS, FAILURE, TIMEOUT, UNREACHABLE, AUTHENTICATION_FAILED
    }

    public enum AuthProtocol {
        NONE, MD5, SHA, SHA224, SHA256, SHA384, SHA512
    }

    public enum PrivProtocol {
        NONE, DES, AES128, AES192, AES256
    }
}
