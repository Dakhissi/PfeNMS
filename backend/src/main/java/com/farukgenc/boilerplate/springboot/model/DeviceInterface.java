package com.farukgenc.boilerplate.springboot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Device Interface entity representing network interfaces (MIB-2 ifTable)
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "device_interfaces")
public class DeviceInterface {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "if_index", nullable = false)
    private Integer ifIndex;

    @Column(name = "if_descr", length = 1000)
    private String ifDescr;

    @Enumerated(EnumType.STRING)
    @Column(name = "if_type")
    private InterfaceType ifType;

    @Column(name = "if_mtu")
    private Integer ifMtu;

    @Column(name = "if_speed")
    private Long ifSpeed;

    @Column(name = "if_phys_address", length = 500)
    private String ifPhysAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "if_admin_status")
    private InterfaceStatus ifAdminStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "if_oper_status")
    private InterfaceStatus ifOperStatus;

    @Column(name = "if_last_change")
    private Long ifLastChange;

    @Column(name = "if_in_octets")
    private Long ifInOctets;

    @Column(name = "if_in_ucast_pkts")
    private Long ifInUcastPkts;

    @Column(name = "if_in_nucast_pkts")
    private Long ifInNucastPkts;

    @Column(name = "if_in_discards")
    private Long ifInDiscards;

    @Column(name = "if_in_errors")
    private Long ifInErrors;

    @Column(name = "if_in_unknown_protos")
    private Long ifInUnknownProtos;

    @Column(name = "if_out_octets")
    private Long ifOutOctets;

    @Column(name = "if_out_ucast_pkts")
    private Long ifOutUcastPkts;

    @Column(name = "if_out_nucast_pkts")
    private Long ifOutNucastPkts;

    @Column(name = "if_out_discards")
    private Long ifOutDiscards;

    @Column(name = "if_out_errors")
    private Long ifOutErrors;

    @Column(name = "if_out_qlen")
    private Long ifOutQLen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum InterfaceType {
        OTHER, REGULAR1822, HDH1822, DDN_X25, RFC877_X25, ETHERNET_CSMACD,
        ISO88023_CSMACD, ISO88024_TOKENBUS, ISO88025_TOKENRING, ISO88026_MAN,
        STARLAN, PROTEON_10MBIT, PROTEON_80MBIT, HYPERCHANNEL, FDDI,
        LAPB, SDLC, DS1, E1, BASIC_ISDN, PRIMARY_ISDN, PROP_PTP_SERIAL,
        PPP, SOFTWARE_LOOPBACK, EON, ETHERNET_3MBIT, NSIP, SLIP,
        ULTRA, DS3, SIP, FRAME_RELAY
    }

    public enum InterfaceStatus {
        UP, DOWN, TESTING
    }
}
