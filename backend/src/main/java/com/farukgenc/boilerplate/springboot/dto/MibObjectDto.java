package com.farukgenc.boilerplate.springboot.dto;

import com.farukgenc.boilerplate.springboot.model.MibObject;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for MIB Object responses including tree structure
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MibObjectDto {

    private Long id;
    private String name;
    private String oid;
    private String description;
    private MibObject.MibType type;
    private MibObject.MibAccess access;
    private MibObject.MibStatus status;
    private String syntaxType;
    private String maxAccess;
    private String units;
    private String reference;
    private String indexObjects;
    private String augments;

    // Tree structure
    private Long parentId;
    private String parentName;
    private List<MibObjectDto> children;

    // MIB file info
    private Long mibFileId;
    private String mibFileName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
