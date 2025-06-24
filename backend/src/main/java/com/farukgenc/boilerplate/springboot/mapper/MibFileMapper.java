package com.farukgenc.boilerplate.springboot.mapper;

import com.farukgenc.boilerplate.springboot.dto.MibFileDto;
import com.farukgenc.boilerplate.springboot.model.MibFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MibFileMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(target = "objectCount", expression = "java(mibFile.getMibObjects().size())")
    MibFileDto toDto(MibFile mibFile);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "mibObjects", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MibFile toEntity(MibFileDto mibFileDto);
}
