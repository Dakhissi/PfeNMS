package com.farukgenc.boilerplate.springboot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * IP Profile entity representing IP network statistics and configuration
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ip_profiles")
public class IpProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_forwarding")
    private Boolean ipForwarding;

    @Column(name = "ip_default_ttl")
    private Integer ipDefaultTTL;

    @Column(name = "ip_in_receives")
    private Long ipInReceives;

    @Column(name = "ip_in_hdr_errors")
    private Long ipInHdrErrors;

    @Column(name = "ip_in_addr_errors")
    private Long ipInAddrErrors;

    @Column(name = "ip_forw_datagrams")
    private Long ipForwDatagrams;

    @Column(name = "ip_in_unknown_protos")
    private Long ipInUnknownProtos;

    @Column(name = "ip_in_discards")
    private Long ipInDiscards;

    @Column(name = "ip_in_delivers")
    private Long ipInDelivers;

    @Column(name = "ip_out_requests")
    private Long ipOutRequests;

    @Column(name = "ip_out_discards")
    private Long ipOutDiscards;

    @Column(name = "ip_out_no_routes")
    private Long ipOutNoRoutes;

    @Column(name = "ip_reasm_timeout")
    private Integer ipReasmTimeout;

    @Column(name = "ip_reasm_reqds")
    private Long ipReasmReqds;

    @Column(name = "ip_reasm_oks")
    private Long ipReasmOKs;

    @Column(name = "ip_reasm_fails")
    private Long ipReasmFails;

    @Column(name = "ip_frag_oks")
    private Long ipFragOKs;

    @Column(name = "ip_frag_fails")
    private Long ipFragFails;

    @Column(name = "ip_frag_creates")
    private Long ipFragCreates;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "ip_subnet_mask")
    private String ipSubnetMask;

    @Column(name = "ip_broadcast_addr")
    private String ipBroadcastAddr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
