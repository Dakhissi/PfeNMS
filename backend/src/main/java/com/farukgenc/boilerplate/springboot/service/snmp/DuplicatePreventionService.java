package com.farukgenc.boilerplate.springboot.service.snmp;

import com.farukgenc.boilerplate.springboot.model.*;
import com.farukgenc.boilerplate.springboot.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing duplicate entities during SNMP polling
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicatePreventionService {

    private final DeviceInterfaceRepository deviceInterfaceRepository;
    private final SystemUnitRepository systemUnitRepository;
    private final IpProfileRepository ipProfileRepository;
    private final IcmpProfileRepository icmpProfileRepository;
    private final UdpProfileRepository udpProfileRepository;

    /**
     * Clean up stale interfaces that are no longer present in SNMP data
     */
    @Transactional
    public void cleanupStaleInterfaces(Device device, Set<Integer> activeInterfaceIndices) {
        List<DeviceInterface> existingInterfaces = deviceInterfaceRepository.findByDeviceId(device.getId());
        List<DeviceInterface> staleInterfaces = existingInterfaces.stream()
            .filter(iface -> !activeInterfaceIndices.contains(iface.getIfIndex()))
            .collect(Collectors.toList());

        if (!staleInterfaces.isEmpty()) {
            deviceInterfaceRepository.deleteAll(staleInterfaces);
            log.info("Cleaned up {} stale interfaces for device: {}", staleInterfaces.size(), device.getName());
        }
    }

    /**
     * Get or create interface to avoid duplicates
     */
    @Transactional
    public DeviceInterface getOrCreateInterface(Device device, Integer ifIndex) {
        Optional<DeviceInterface> existing = deviceInterfaceRepository
            .findByDeviceIdAndIfIndex(device.getId(), ifIndex);

        return existing.orElse(
            DeviceInterface.builder()
                .device(device)
                .ifIndex(ifIndex)
                .build()
        );
    }

    /**
     * Clean up stale system units
     */
    @Transactional
    public void cleanupStaleSystemUnits(Device device, Set<Integer> activeUnitIndices) {
        List<SystemUnit> existingUnits = systemUnitRepository.findByDevice(device);
        List<SystemUnit> staleUnits = existingUnits.stream()
            .filter(unit -> !activeUnitIndices.contains(unit.getUnitIndex()))
            .collect(Collectors.toList());

        if (!staleUnits.isEmpty()) {
            systemUnitRepository.deleteAll(staleUnits);
            log.info("Cleaned up {} stale system units for device: {}", staleUnits.size(), device.getName());
        }
    }

    /**
     * Get or create system unit to avoid duplicates
     */
    @Transactional
    public SystemUnit getOrCreateSystemUnit(Device device, Integer unitIndex) {
        Optional<SystemUnit> existing = systemUnitRepository
            .findByDeviceAndUnitIndex(device, unitIndex);

        return existing.orElse(
            SystemUnit.builder()
                .device(device)
                .unitIndex(unitIndex)
                .build()
        );
    }

    /**
     * Get or create IP profile to avoid duplicates
     */
    @Transactional
    public IpProfile getOrCreateIpProfile(Device device, String ipAddress) {
        if (ipAddress == null) {
            return IpProfile.builder().device(device).build();
        }

        Optional<IpProfile> existing = ipProfileRepository
            .findByDeviceAndIpAddress(device, ipAddress);

        return existing.orElse(
            IpProfile.builder()
                .device(device)
                .ipAddress(ipAddress)
                .build()
        );
    }

    /**
     * Get or create ICMP profile to avoid duplicates
     */
    @Transactional
    public IcmpProfile getOrCreateIcmpProfile(Device device) {
        Optional<IcmpProfile> existing = icmpProfileRepository.findByDeviceId(device.getId());

        return existing.orElse(
            IcmpProfile.builder()
                .device(device)
                .build()
        );
    }

    /**
     * Get or create UDP profile to avoid duplicates
     */
    @Transactional
    public UdpProfile getOrCreateUdpProfile(Device device, String localAddress, Integer localPort) {
        if (localAddress == null || localPort == null) {
            return UdpProfile.builder().device(device).build();
        }

        Optional<UdpProfile> existing = udpProfileRepository
            .findByDeviceAndUdpLocalAddressAndUdpLocalPort(device, localAddress, localPort);

        return existing.orElse(
            UdpProfile.builder()
                .device(device)
                .udpLocalAddress(localAddress)
                .udpLocalPort(localPort)
                .build()
        );
    }

    /**
     * Clean up duplicate IP profiles (keep the most recent)
     */
    @Transactional
    public void deduplicateIpProfiles(Device device) {
        List<IpProfile> allProfiles = ipProfileRepository.findByDevice(device);
        
        // Group by IP address and keep only the most recent for each address
        allProfiles.stream()
            .collect(Collectors.groupingBy(IpProfile::getIpAddress))
            .forEach((ipAddress, profiles) -> {
                if (profiles.size() > 1) {
                    // Sort by creation time and keep the most recent
                    profiles.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
                    List<IpProfile> duplicates = profiles.subList(1, profiles.size());
                    
                    ipProfileRepository.deleteAll(duplicates);
                    log.info("Removed {} duplicate IP profiles for address {} on device {}", 
                        duplicates.size(), ipAddress, device.getName());
                }
            });
    }

    /**
     * Clean up duplicate UDP profiles (keep the most recent)
     */
    @Transactional
    public void deduplicateUdpProfiles(Device device) {
        List<UdpProfile> allProfiles = udpProfileRepository.findByDevice(device);
        
        // Group by local address and port combination
        allProfiles.stream()
            .collect(Collectors.groupingBy(profile -> 
                profile.getUdpLocalAddress() + ":" + profile.getUdpLocalPort()))
            .forEach((addressPort, profiles) -> {
                if (profiles.size() > 1) {
                    // Sort by creation time and keep the most recent
                    profiles.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
                    List<UdpProfile> duplicates = profiles.subList(1, profiles.size());
                    
                    udpProfileRepository.deleteAll(duplicates);
                    log.info("Removed {} duplicate UDP profiles for {} on device {}", 
                        duplicates.size(), addressPort, device.getName());
                }
            });
    }
}
