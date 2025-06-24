package com.farukgenc.boilerplate.springboot.mapper;

import com.farukgenc.boilerplate.springboot.dto.IcmpProfileDto;
import com.farukgenc.boilerplate.springboot.model.IcmpProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IcmpProfileMapper {

    @Mapping(source = "device.id", target = "deviceId")
    @Mapping(source = "device.name", target = "deviceName")
    IcmpProfileDto toDto(IcmpProfile icmpProfile);

    @Mapping(target = "device", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    IcmpProfile toEntity(IcmpProfileDto icmpProfileDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "device", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget IcmpProfile icmpProfile, IcmpProfileDto icmpProfileDto);
}
