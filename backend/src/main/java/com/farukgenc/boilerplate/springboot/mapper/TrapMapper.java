package com.farukgenc.boilerplate.springboot.mapper;

import com.farukgenc.boilerplate.springboot.dto.TrapEventDto;
import com.farukgenc.boilerplate.springboot.model.TrapEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * MapStruct mapper for TrapEvent entity and DTO conversion
 */
@Mapper(componentModel = "spring")
public abstract class TrapMapper {

    @Autowired
    private ObjectMapper objectMapper;

    @Mapping(target = "deviceId", source = "device.id")
    @Mapping(target = "deviceName", source = "device.name")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.username")
    @Mapping(target = "variableBindings", source = "variableBindings", qualifiedByName = "stringToMap")
    public abstract TrapEventDto toDto(TrapEvent trapEvent);

    @Mapping(target = "device", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "variableBindings", source = "variableBindings", qualifiedByName = "mapToString")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract TrapEvent toEntity(TrapEventDto trapEventDto);

    @Named("stringToMap")
    public Map<String, Object> stringToMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    @Named("mapToString")
    public String mapToString(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
