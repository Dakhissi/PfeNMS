package com.farukgenc.boilerplate.springboot.dto;

import com.farukgenc.boilerplate.springboot.model.MibFile;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for MIB File operations
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MibFileDto {

    private Long id;
    private String name;
    private String filename;
    private String filePath;
    private Long fileSize;
    private String fileHash;
    private String description;
    private String moduleName;
    private String moduleOid;
    private String version;
    private String organization;
    private String contactInfo;
    private MibFile.MibFileStatus status;
    private String loadErrorMessage;
    private Integer objectCount;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
