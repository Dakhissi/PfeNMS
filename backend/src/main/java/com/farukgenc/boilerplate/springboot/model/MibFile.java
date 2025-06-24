package com.farukgenc.boilerplate.springboot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MIB File entity representing loaded MIB files
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mib_files")
public class MibFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String filename;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_hash")
    private String fileHash;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "module_name")
    private String moduleName;

    @Column(name = "module_oid")
    private String moduleOid;

    @Column(name = "version")
    private String version;

    @Column(name = "organization")
    private String organization;

    @Column(name = "contact_info")
    private String contactInfo;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MibFileStatus status = MibFileStatus.PENDING;

    @Column(name = "load_error_message")
    private String loadErrorMessage;

    @OneToMany(mappedBy = "mibFile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MibObject> mibObjects = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum MibFileStatus {
        PENDING, LOADING, LOADED, ERROR
    }
}
