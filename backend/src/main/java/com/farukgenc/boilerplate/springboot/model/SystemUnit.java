package com.farukgenc.boilerplate.springboot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * System Unit entity representing system units/modules of a device
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "system_units")
public class SystemUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unit_index", nullable = false)
    private Integer unitIndex;

    @Column(name = "unit_name")
    private String unitName;

    @Column(name = "unit_description")
    private String unitDescription;

    @Column(name = "unit_type")
    private String unitType;

    @Column(name = "unit_hw_version")
    private String unitHwVersion;

    @Column(name = "unit_fw_version")
    private String unitFwVersion;

    @Column(name = "unit_sw_version")
    private String unitSwVersion;

    @Column(name = "unit_serial_number")
    private String unitSerialNumber;

    @Column(name = "unit_mfg_name")
    private String unitMfgName;

    @Column(name = "unit_model_name")
    private String unitModelName;

    @Column(name = "unit_alias")
    private String unitAlias;

    @Column(name = "unit_asset_id")
    private String unitAssetId;    @Column(name = "unit_is_fru")
    private Boolean unitIsFru;

    @Column(name = "unit_mfg_date")
    private LocalDateTime unitMfgDate;

    @Column(name = "unit_uris")
    private String unitUris;

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
