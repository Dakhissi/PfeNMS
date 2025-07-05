package com.farukgenc.boilerplate.springboot.mapper;

import com.farukgenc.boilerplate.springboot.dto.DeviceDto;
import com.farukgenc.boilerplate.springboot.dto.DeviceCreateRequest;
import com.farukgenc.boilerplate.springboot.dto.DeviceResponse;
import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DeviceMapper {

    // Device to DeviceDto mapping (includes DeviceConfig)
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "deviceConfig", target = "deviceConfig")
    DeviceDto toDto(Device device);

    // DeviceConfig to DeviceConfigDto mapping
    DeviceDto.DeviceConfigDto toConfigDto(DeviceConfig deviceConfig);

    // DeviceDto to Device mapping (excludes config and relationships)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "interfaces", ignore = true)
    @Mapping(target = "systemUnits", ignore = true)
    @Mapping(target = "ipProfiles", ignore = true)
    @Mapping(target = "icmpProfiles", ignore = true)
    @Mapping(target = "udpProfiles", ignore = true)
    @Mapping(target = "deviceConfig", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Device toEntity(DeviceDto deviceDto);

    // Update entity from DTO (excludes config and relationships)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "interfaces", ignore = true)
    @Mapping(target = "systemUnits", ignore = true)
    @Mapping(target = "ipProfiles", ignore = true)
    @Mapping(target = "icmpProfiles", ignore = true)
    @Mapping(target = "udpProfiles", ignore = true)
    @Mapping(target = "deviceConfig", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Device device, DeviceDto deviceDto);

    // DeviceCreateRequest to Device mapping
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "interfaces", ignore = true)
    @Mapping(target = "systemUnits", ignore = true)
    @Mapping(target = "ipProfiles", ignore = true)
    @Mapping(target = "icmpProfiles", ignore = true)
    @Mapping(target = "udpProfiles", ignore = true)
    @Mapping(target = "deviceConfig", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "systemObjectId", ignore = true)
    @Mapping(target = "systemUptime", ignore = true)
    @Mapping(target = "systemServices", ignore = true)
    @Mapping(target = "lastMonitored", ignore = true)
    @Mapping(target = "monitoringEnabled", constant = "false")
    @Mapping(target = "status", constant = "INACTIVE")
    Device toEntity(DeviceCreateRequest request);

    // DeviceCreateRequest to DeviceConfig mapping
    @Mapping(source = "configuration.targetIp", target = "targetIp")
    @Mapping(source = "configuration.snmpPort", target = "snmpPort")
    @Mapping(source = "configuration.snmpVersion", target = "snmpVersion")
    @Mapping(source = "configuration.communityString", target = "communityString")
    @Mapping(source = "configuration.snmpTimeout", target = "snmpTimeout")
    @Mapping(source = "configuration.snmpRetries", target = "snmpRetries")
    @Mapping(source = "configuration.pollInterval", target = "pollInterval")
    @Mapping(source = "configuration.enabled", target = "enabled")
    @Mapping(source = "configuration.securityName", target = "securityName")
    @Mapping(source = "configuration.authProtocol", target = "authProtocol")
    @Mapping(source = "configuration.authPassphrase", target = "authPassphrase")
    @Mapping(source = "configuration.privProtocol", target = "privProtocol")
    @Mapping(source = "configuration.privPassphrase", target = "privPassphrase")
    @Mapping(source = "configuration.contextName", target = "contextName")
    @Mapping(target = "device", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastPollTime", ignore = true)
    @Mapping(target = "lastPollStatus", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "consecutiveFailures", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DeviceConfig toConfigEntity(DeviceCreateRequest request);

    // Device to DeviceResponse mapping
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "deviceConfig", target = "configuration")
    DeviceResponse toResponse(Device device);

    // DeviceConfig to DeviceConfigResponse mapping
    @Mapping(source = "lastPollTime", target = "lastPollTime")
    @Mapping(source = "lastPollStatus", target = "lastPollStatus")
    @Mapping(source = "errorMessage", target = "errorMessage")
    @Mapping(source = "consecutiveFailures", target = "consecutiveFailures")
    DeviceResponse.DeviceConfigResponse toConfigResponse(DeviceConfig config);
}
