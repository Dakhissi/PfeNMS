package com.farukgenc.boilerplate.springboot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ICMP Profile entity representing ICMP statistics and configuration
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "icmp_profiles")
public class IcmpProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "icmp_in_msgs")
    private Long icmpInMsgs;

    @Column(name = "icmp_in_errors")
    private Long icmpInErrors;

    @Column(name = "icmp_in_dest_unreachs")
    private Long icmpInDestUnreachs;

    @Column(name = "icmp_in_time_excds")
    private Long icmpInTimeExcds;

    @Column(name = "icmp_in_parm_probs")
    private Long icmpInParmProbs;

    @Column(name = "icmp_in_src_quenchs")
    private Long icmpInSrcQuenchs;

    @Column(name = "icmp_in_redirects")
    private Long icmpInRedirects;

    @Column(name = "icmp_in_echos")
    private Long icmpInEchos;

    @Column(name = "icmp_in_echo_reps")
    private Long icmpInEchoReps;

    @Column(name = "icmp_in_timestamps")
    private Long icmpInTimestamps;

    @Column(name = "icmp_in_timestamp_reps")
    private Long icmpInTimestampReps;

    @Column(name = "icmp_in_addr_masks")
    private Long icmpInAddrMasks;

    @Column(name = "icmp_in_addr_mask_reps")
    private Long icmpInAddrMaskReps;

    @Column(name = "icmp_out_msgs")
    private Long icmpOutMsgs;

    @Column(name = "icmp_out_errors")
    private Long icmpOutErrors;

    @Column(name = "icmp_out_dest_unreachs")
    private Long icmpOutDestUnreachs;

    @Column(name = "icmp_out_time_excds")
    private Long icmpOutTimeExcds;

    @Column(name = "icmp_out_parm_probs")
    private Long icmpOutParmProbs;

    @Column(name = "icmp_out_src_quenchs")
    private Long icmpOutSrcQuenchs;

    @Column(name = "icmp_out_redirects")
    private Long icmpOutRedirects;

    @Column(name = "icmp_out_echos")
    private Long icmpOutEchos;

    @Column(name = "icmp_out_echo_reps")
    private Long icmpOutEchoReps;

    @Column(name = "icmp_out_timestamps")
    private Long icmpOutTimestamps;

    @Column(name = "icmp_out_timestamp_reps")
    private Long icmpOutTimestampReps;

    @Column(name = "icmp_out_addr_masks")
    private Long icmpOutAddrMasks;

    @Column(name = "icmp_out_addr_mask_reps")
    private Long icmpOutAddrMaskReps;

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
