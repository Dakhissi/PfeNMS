package com.farukgenc.boilerplate.springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopologyResponseDto {
    private String discoveryId;
    private List<NetworkNodeDto> nodes = new ArrayList<>();
    private List<NetworkConnectionDto> connections = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private String status; // "COMPLETE", "PARTIAL", "FAILED"
    private long totalDevicesDiscovered;
    private long discoveryDurationMs;
}
