package com.farukgenc.boilerplate.springboot.service.device;

import com.farukgenc.boilerplate.springboot.dto.IcmpProfileDto;
import com.farukgenc.boilerplate.springboot.mapper.IcmpProfileMapper;
import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.IcmpProfile;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.repository.DeviceRepository;
import com.farukgenc.boilerplate.springboot.repository.IcmpProfileRepository;
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
public class IcmpProfileServiceImpl implements IcmpProfileService {

    private final IcmpProfileRepository icmpProfileRepository;
    private final DeviceRepository deviceRepository;
    private final IcmpProfileMapper icmpProfileMapper;

    @Override
    public IcmpProfileDto createIcmpProfile(IcmpProfileDto icmpProfileDto, User user) {
        log.info("Creating ICMP profile for device ID: {} by user: {}", icmpProfileDto.getDeviceId(), user.getUsername());

        Device device = deviceRepository.findByIdAndUser(icmpProfileDto.getDeviceId(), user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        IcmpProfile icmpProfile = icmpProfileMapper.toEntity(icmpProfileDto);
        icmpProfile.setDevice(device);

        IcmpProfile savedIcmpProfile = icmpProfileRepository.save(icmpProfile);
        log.info("ICMP profile created successfully with ID: {}", savedIcmpProfile.getId());

        return icmpProfileMapper.toDto(savedIcmpProfile);
    }

    @Override
    public IcmpProfileDto updateIcmpProfile(Long id, IcmpProfileDto icmpProfileDto, User user) {
        log.info("Updating ICMP profile ID: {} by user: {}", id, user.getUsername());

        IcmpProfile existingIcmpProfile = icmpProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ICMP profile not found"));

        // Verify user owns the device
        if (!existingIcmpProfile.getDevice().getUser().equals(user)) {
            throw new IllegalArgumentException("Access denied");
        }

        icmpProfileMapper.updateEntity(existingIcmpProfile, icmpProfileDto);
        IcmpProfile updatedIcmpProfile = icmpProfileRepository.save(existingIcmpProfile);

        log.info("ICMP profile updated successfully: {}", updatedIcmpProfile.getId());
        return icmpProfileMapper.toDto(updatedIcmpProfile);
    }

    @Override
    public void deleteIcmpProfile(Long id, User user) {
        log.info("Deleting ICMP profile ID: {} by user: {}", id, user.getUsername());

        IcmpProfile icmpProfile = icmpProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ICMP profile not found"));

        // Verify user owns the device
        if (!icmpProfile.getDevice().getUser().equals(user)) {
            throw new IllegalArgumentException("Access denied");
        }

        icmpProfileRepository.delete(icmpProfile);
        log.info("ICMP profile deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public IcmpProfileDto getIcmpProfileById(Long id, User user) {
        log.debug("Getting ICMP profile ID: {} for user: {}", id, user.getUsername());

        IcmpProfile icmpProfile = icmpProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ICMP profile not found"));

        // Verify user owns the device
        if (!icmpProfile.getDevice().getUser().equals(user)) {
            throw new IllegalArgumentException("Access denied");
        }

        return icmpProfileMapper.toDto(icmpProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IcmpProfileDto> getIcmpProfilesByDevice(Long deviceId, User user) {
        log.debug("Getting ICMP profiles for device ID: {} for user: {}", deviceId, user.getUsername());

        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        List<IcmpProfile> icmpProfiles = icmpProfileRepository.findByDevice(device);
        return icmpProfiles.stream()
                .map(icmpProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long countIcmpProfilesByDevice(Long deviceId, User user) {
        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        return icmpProfileRepository.countByDevice(device);
    }
}
