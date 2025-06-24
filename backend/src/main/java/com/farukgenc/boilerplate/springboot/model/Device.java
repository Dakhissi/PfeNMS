package com.farukgenc.boilerplate.springboot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Device entity representing network devices in SNMP MIB-2 style
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "system_object_id")
    private String systemObjectId;

    @Column(name = "system_uptime")
    private Long systemUptime;

    @Column(name = "system_contact")
    private String systemContact;

    @Column(name = "system_name")
    private String systemName;

    @Column(name = "system_location")
    private String systemLocation;

    @Column(name = "system_services")
    private Integer systemServices;

    // Monitoring
    @Column(name = "last_monitored")
    private LocalDateTime lastMonitored;

    @Column(name = "monitoring_enabled")
    private Boolean monitoringEnabled;

    @Enumerated(EnumType.STRING)
    private DeviceStatus status;

    @Enumerated(EnumType.STRING)
    private DeviceType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeviceInterface> interfaces = new ArrayList<>();

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SystemUnit> systemUnits = new ArrayList<>();

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IpProfile> ipProfiles = new ArrayList<>();

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IcmpProfile> icmpProfiles = new ArrayList<>();    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UdpProfile> udpProfiles = new ArrayList<>();

    @OneToOne(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private DeviceConfig deviceConfig;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum DeviceStatus {
        ACTIVE, INACTIVE, MAINTENANCE, ERROR
    }

    public enum DeviceType {
        ROUTER, SWITCH, SERVER, WORKSTATION, PRINTER, OTHER
    }
}
