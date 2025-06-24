package com.farukgenc.boilerplate.springboot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * System Information entity representing system-level information from SNMP MIB-2
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "system_info")
public class SystemInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false, unique = true)
    private Device device;

    // System MIB-2 fields
    @Column(name = "sys_descr", length = 1000)
    private String sysDescr;

    @Column(name = "sys_object_id")
    private String sysObjectId;

    @Column(name = "sys_up_time")
    private Long sysUpTime;

    @Column(name = "sys_contact")
    private String sysContact;

    @Column(name = "sys_name")
    private String sysName;

    @Column(name = "sys_location")
    private String sysLocation;

    @Column(name = "sys_services")
    private Integer sysServices;

    // Host Resources MIB fields
    @Column(name = "hr_system_uptime")
    private Long hrSystemUptime;

    @Column(name = "hr_system_date")
    private LocalDateTime hrSystemDate;

    @Column(name = "hr_system_initial_load_device")
    private Integer hrSystemInitialLoadDevice;

    @Column(name = "hr_system_initial_load_parameters")
    private String hrSystemInitialLoadParameters;

    @Column(name = "hr_system_num_users")
    private Integer hrSystemNumUsers;

    @Column(name = "hr_system_processes")
    private Integer hrSystemProcesses;

    @Column(name = "hr_system_max_processes")
    private Integer hrSystemMaxProcesses;

    @Column(name = "last_polled")
    private LocalDateTime lastPolled;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
