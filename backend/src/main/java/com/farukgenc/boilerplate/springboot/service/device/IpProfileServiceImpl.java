package com.farukgenc.boilerplate.springboot.service.device;

import com.farukgenc.boilerplate.springboot.dto.IpProfileDto;
import com.farukgenc.boilerplate.springboot.mapper.IpProfileMapper;
import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.IpProfile;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.repository.DeviceRepository;
import com.farukgenc.boilerplate.springboot.repository.IpProfileRepository;
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
public class IpProfileServiceImpl implements IpProfileService {

    private final IpProfileRepository ipProfileRepository;
    private final DeviceRepository deviceRepository;
    private final IpProfileMapper ipProfileMapper;

    @Override
    public IpProfileDto createIpProfile(IpProfileDto ipProfileDto, User user) {
        log.info("Creating IP profile for device ID: {} by user: {}", ipProfileDto.getDeviceId(), user.getUsername());

        Device device = deviceRepository.findByIdAndUser(ipProfileDto.getDeviceId(), user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        if (ipProfileDto.getIpAddress() != null && 
            existsByDeviceAndIpAddress(ipProfileDto.getDeviceId(), ipProfileDto.getIpAddress(), user)) {
            throw new IllegalArgumentException("IP profile with address " + ipProfileDto.getIpAddress() + " already exists for this device");
        }

        IpProfile ipProfile = ipProfileMapper.toEntity(ipProfileDto);
        ipProfile.setDevice(device);

        IpProfile savedIpProfile = ipProfileRepository.save(ipProfile);
        log.info("IP profile created successfully with ID: {}", savedIpProfile.getId());

        return ipProfileMapper.toDto(savedIpProfile);
    }

    @Override
    public IpProfileDto updateIpProfile(Long id, IpProfileDto ipProfileDto, User user) {
        log.info("Updating IP profile ID: {} by user: {}", id, user.getUsername());

        IpProfile existingIpProfile = ipProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("IP profile not found"));

        // Verify user owns the device
        if (!existingIpProfile.getDevice().getUser().equals(user)) {
            throw new IllegalArgumentException("Access denied");
        }

        // Check if IP address is being changed and if it conflicts
        if (ipProfileDto.getIpAddress() != null && 
            !ipProfileDto.getIpAddress().equals(existingIpProfile.getIpAddress()) &&
            existsByDeviceAndIpAddress(existingIpProfile.getDevice().getId(), ipProfileDto.getIpAddress(), user)) {
            throw new IllegalArgumentException("IP profile with address " + ipProfileDto.getIpAddress() + " already exists for this device");
        }

        ipProfileMapper.updateEntity(existingIpProfile, ipProfileDto);
        IpProfile updatedIpProfile = ipProfileRepository.save(existingIpProfile);

        log.info("IP profile updated successfully: {}", updatedIpProfile.getId());
        return ipProfileMapper.toDto(updatedIpProfile);
    }

    @Override
    public void deleteIpProfile(Long id, User user) {
        log.info("Deleting IP profile ID: {} by user: {}", id, user.getUsername());

        IpProfile ipProfile = ipProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("IP profile not found"));

        // Verify user owns the device
        if (!ipProfile.getDevice().getUser().equals(user)) {
            throw new IllegalArgumentException("Access denied");
        }

        ipProfileRepository.delete(ipProfile);
        log.info("IP profile deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public IpProfileDto getIpProfileById(Long id, User user) {
        log.debug("Getting IP profile ID: {} for user: {}", id, user.getUsername());

        IpProfile ipProfile = ipProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("IP profile not found"));

        // Verify user owns the device
        if (!ipProfile.getDevice().getUser().equals(user)) {
            throw new IllegalArgumentException("Access denied");
        }

        return ipProfileMapper.toDto(ipProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IpProfileDto> getIpProfilesByDevice(Long deviceId, User user) {
        log.debug("Getting IP profiles for device ID: {} for user: {}", deviceId, user.getUsername());

        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        List<IpProfile> ipProfiles = ipProfileRepository.findByDevice(device);
        return ipProfiles.stream()
                .map(ipProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IpProfileDto> getIpProfilesByDeviceAndForwarding(Long deviceId, Boolean forwarding, User user) {
        log.debug("Getting IP profiles by forwarding: {} for device ID: {} for user: {}", forwarding, deviceId, user.getUsername());

        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        List<IpProfile> ipProfiles = ipProfileRepository.findByDeviceAndIpForwarding(device, forwarding);
        return ipProfiles.stream()
                .map(ipProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IpProfileDto> searchIpProfilesByAddress(Long deviceId, String address, User user) {
        log.debug("Searching IP profiles by address: {} for device ID: {} for user: {}", address, deviceId, user.getUsername());

        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        List<IpProfile> ipProfiles = ipProfileRepository.findByDeviceAndIpAddressContaining(device, address);
        return ipProfiles.stream()
                .map(ipProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long countIpProfilesByDevice(Long deviceId, User user) {
        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        return ipProfileRepository.countByDevice(device);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDeviceAndIpAddress(Long deviceId, String ipAddress, User user) {
        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        return ipProfileRepository.existsByDeviceAndIpAddress(device, ipAddress);
    }
}
