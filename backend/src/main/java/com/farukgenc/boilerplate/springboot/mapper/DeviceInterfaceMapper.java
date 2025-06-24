package com.farukgenc.boilerplate.springboot.mapper;

import com.farukgenc.boilerplate.springboot.dto.DeviceInterfaceDto;
import com.farukgenc.boilerplate.springboot.model.DeviceInterface;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DeviceInterfaceMapper {

    @Mapping(source = "device.id", target = "deviceId")
    @Mapping(source = "device.name", target = "deviceName")
    DeviceInterfaceDto toDto(DeviceInterface deviceInterface);

    @Mapping(target = "device", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DeviceInterface toEntity(DeviceInterfaceDto deviceInterfaceDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "device", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget DeviceInterface deviceInterface, DeviceInterfaceDto deviceInterfaceDto);
}
