package com.farukgenc.boilerplate.springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkNodeDto {
    private String id;
    private String name;
    private String ipAddress;
    private String macAddress;
    private DeviceType deviceType;
    private String systemDescription;
    private String vendor;
    private String model;
    private String osVersion;
    private boolean reachable;
    private Map<String, String> attributes = new HashMap<>();
    private List<String> services = new ArrayList<>(); // Discovered services (HTTP, SSH, etc.)
    private List<InterfaceDto> interfaces = new ArrayList<>();

    public enum DeviceType {
        ROUTER,
        SWITCH,
        FIREWALL,
        SERVER,
        WORKSTATION,
        PRINTER,
        WIRELESS_ACCESS_POINT,
        UNKNOWN
    }
}
