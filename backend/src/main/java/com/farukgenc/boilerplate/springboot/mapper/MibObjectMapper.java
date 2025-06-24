package com.farukgenc.boilerplate.springboot.mapper;

import com.farukgenc.boilerplate.springboot.dto.MibObjectDto;
import com.farukgenc.boilerplate.springboot.model.MibObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MibObjectMapper {

    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "parent.name", target = "parentName")
    @Mapping(source = "mibFile.id", target = "mibFileId")
    @Mapping(source = "mibFile.name", target = "mibFileName")
    @Mapping(target = "children", ignore = true) // Will be set manually in service
    MibObjectDto toDto(MibObject mibObject);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "mibFile", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MibObject toEntity(MibObjectDto mibObjectDto);
}
