package com.farukgenc.boilerplate.springboot.service.device;

import com.farukgenc.boilerplate.springboot.dto.UdpProfileDto;
import com.farukgenc.boilerplate.springboot.mapper.UdpProfileMapper;
import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.UdpProfile;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.repository.DeviceRepository;
import com.farukgenc.boilerplate.springboot.repository.UdpProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UdpProfileServiceImpl implements UdpProfileService {

    private final UdpProfileRepository udpProfileRepository;
    private final DeviceRepository deviceRepository;
    private final UdpProfileMapper udpProfileMapper;

    @Override
    public UdpProfileDto createUdpProfile(UdpProfileDto udpProfileDto, User user) {
        log.info("Creating UDP profile for device ID: {} by user: {}", udpProfileDto.getDeviceId(), user.getUsername());

        Device device = deviceRepository.findByIdAndUser(udpProfileDto.getDeviceId(), user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        if (udpProfileDto.getUdpLocalAddress() != null && udpProfileDto.getUdpLocalPort() != null &&
            existsByDeviceAndLocalAddressAndPort(udpProfileDto.getDeviceId(), 
                udpProfileDto.getUdpLocalAddress(), udpProfileDto.getUdpLocalPort(), user)) {
            throw new IllegalArgumentException("UDP profile with address " + udpProfileDto.getUdpLocalAddress() + 
                ":" + udpProfileDto.getUdpLocalPort() + " already exists for this device");
        }

        UdpProfile udpProfile = udpProfileMapper.toEntity(udpProfileDto);
        udpProfile.setDevice(device);

        UdpProfile savedUdpProfile = udpProfileRepository.save(udpProfile);
        log.info("UDP profile created successfully with ID: {}", savedUdpProfile.getId());

        return udpProfileMapper.toDto(savedUdpProfile);
    }

    @Override
    public UdpProfileDto updateUdpProfile(Long id, UdpProfileDto udpProfileDto, User user) {
        log.info("Updating UDP profile ID: {} by user: {}", id, user.getUsername());

        UdpProfile existingUdpProfile = udpProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("UDP profile not found"));

        // Verify user owns the device
        if (!existingUdpProfile.getDevice().getUser().equals(user)) {
            throw new IllegalArgumentException("Access denied");
        }

        // Check if address/port is being changed and if it conflicts
        if (udpProfileDto.getUdpLocalAddress() != null && udpProfileDto.getUdpLocalPort() != null &&
            (!udpProfileDto.getUdpLocalAddress().equals(existingUdpProfile.getUdpLocalAddress()) ||
             !udpProfileDto.getUdpLocalPort().equals(existingUdpProfile.getUdpLocalPort())) &&
            existsByDeviceAndLocalAddressAndPort(existingUdpProfile.getDevice().getId(), 
                udpProfileDto.getUdpLocalAddress(), udpProfileDto.getUdpLocalPort(), user)) {
            throw new IllegalArgumentException("UDP profile with address " + udpProfileDto.getUdpLocalAddress() + 
                ":" + udpProfileDto.getUdpLocalPort() + " already exists for this device");
        }

        udpProfileMapper.updateEntity(existingUdpProfile, udpProfileDto);
        UdpProfile updatedUdpProfile = udpProfileRepository.save(existingUdpProfile);

        log.info("UDP profile updated successfully: {}", updatedUdpProfile.getId());
        return udpProfileMapper.toDto(updatedUdpProfile);
    }

    @Override
    public void deleteUdpProfile(Long id, User user) {
        log.info("Deleting UDP profile ID: {} by user: {}", id, user.getUsername());

        UdpProfile udpProfile = udpProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("UDP profile not found"));

        // Verify user owns the device
        if (!udpProfile.getDevice().getUser().equals(user)) {
            throw new IllegalArgumentException("Access denied");
        }

        udpProfileRepository.delete(udpProfile);
        log.info("UDP profile deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public UdpProfileDto getUdpProfileById(Long id, User user) {
        log.debug("Getting UDP profile ID: {} for user: {}", id, user.getUsername());

        UdpProfile udpProfile = udpProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("UDP profile not found"));

        // Verify user owns the device
        if (!udpProfile.getDevice().getUser().equals(user)) {
            throw new IllegalArgumentException("Access denied");
        }

        return udpProfileMapper.toDto(udpProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UdpProfileDto> getUdpProfilesByDevice(Long deviceId, User user) {
        log.debug("Getting UDP profiles for device ID: {} for user: {}", deviceId, user.getUsername());

        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        List<UdpProfile> udpProfiles = udpProfileRepository.findByDevice(device);
        return udpProfiles.stream()
                .map(udpProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UdpProfileDto> getUdpProfilesByDeviceAndStatus(Long deviceId, UdpProfile.UdpEntryStatus status, User user) {
        log.debug("Getting UDP profiles by status: {} for device ID: {} for user: {}", status, deviceId, user.getUsername());

        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        List<UdpProfile> udpProfiles = udpProfileRepository.findByDeviceAndUdpEntryStatus(device, status);
        return udpProfiles.stream()
                .map(udpProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UdpProfileDto> searchUdpProfilesByAddress(Long deviceId, String address, User user) {
        log.debug("Searching UDP profiles by address: {} for device ID: {} for user: {}", address, deviceId, user.getUsername());

        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        List<UdpProfile> udpProfiles = udpProfileRepository.findByDeviceAndUdpLocalAddress(device, address);
        return udpProfiles.stream()
                .map(udpProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUdpProfilesByDevice(Long deviceId, User user) {
        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        return udpProfileRepository.countByDevice(device);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDeviceAndLocalAddressAndPort(Long deviceId, String localAddress, Integer localPort, User user) {
        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        return udpProfileRepository.existsByDeviceAndUdpLocalAddressAndUdpLocalPort(device, localAddress, localPort);
    }
}
