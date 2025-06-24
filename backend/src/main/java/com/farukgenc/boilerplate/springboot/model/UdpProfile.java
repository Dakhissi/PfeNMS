package com.farukgenc.boilerplate.springboot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * UDP Profile entity representing UDP statistics and configuration
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "udp_profiles")
public class UdpProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "udp_in_datagrams")
    private Long udpInDatagrams;

    @Column(name = "udp_no_ports")
    private Long udpNoPorts;

    @Column(name = "udp_in_errors")
    private Long udpInErrors;

    @Column(name = "udp_out_datagrams")
    private Long udpOutDatagrams;

    @Column(name = "udp_local_address")
    private String udpLocalAddress;

    @Column(name = "udp_local_port")
    private Integer udpLocalPort;

    @Column(name = "udp_remote_address")
    private String udpRemoteAddress;

    @Column(name = "udp_remote_port")
    private Integer udpRemotePort;

    @Enumerated(EnumType.STRING)
    @Column(name = "udp_entry_status")
    private UdpEntryStatus udpEntryStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum UdpEntryStatus {
        OTHER, INVALID, VALID
    }
}
