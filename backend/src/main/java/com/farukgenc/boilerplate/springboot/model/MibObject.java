package com.farukgenc.boilerplate.springboot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MIB Object entity representing SNMP MIB objects in a tree structure
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mib_objects", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"oid", "mib_file_id"})
       })
public class MibObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String oid;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private MibType type;

    @Enumerated(EnumType.STRING)
    private MibAccess access;

    @Enumerated(EnumType.STRING)
    private MibStatus status;

    @Column(name = "syntax_type")
    private String syntaxType;

    @Column(name = "max_access")
    private String maxAccess;

    @Column(name = "units")
    private String units;

    @Column(name = "reference")
    private String reference;

    @Column(name = "index_objects")
    private String indexObjects;

    @Column(name = "augments")
    private String augments;

    // Tree structure
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private MibObject parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MibObject> children = new ArrayList<>();

    // MIB file information
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mib_file_id")
    private MibFile mibFile;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum MibType {
        OBJECT_TYPE, OBJECT_IDENTITY, MODULE_IDENTITY, NOTIFICATION_TYPE, 
        TEXTUAL_CONVENTION, OBJECT_GROUP, NOTIFICATION_GROUP, MODULE_COMPLIANCE,
        AGENT_CAPABILITIES, TRAP_TYPE, SEQUENCE, INTEGER, OCTET_STRING, 
        OBJECT_IDENTIFIER, NULL_TYPE, BIT_STRING, COUNTER, GAUGE, TIME_TICKS,
        OPAQUE, COUNTER64, UNSIGNED32
    }

    public enum MibAccess {
        READ_ONLY, READ_WRITE, READ_CREATE, NOT_ACCESSIBLE, ACCESSIBLE_FOR_NOTIFY
    }

    public enum MibStatus {
        CURRENT, DEPRECATED, OBSOLETE
    }
}
