package com.farukgenc.boilerplate.springboot.service.device;

import com.farukgenc.boilerplate.springboot.dto.DeviceInterfaceDto;
import com.farukgenc.boilerplate.springboot.mapper.DeviceInterfaceMapper;
import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceInterface;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.repository.DeviceInterfaceRepository;
import com.farukgenc.boilerplate.springboot.repository.DeviceRepository;
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
public class DeviceInterfaceServiceImpl implements DeviceInterfaceService {

    private final DeviceInterfaceRepository interfaceRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceInterfaceMapper interfaceMapper;

    @Override
    public DeviceInterfaceDto createInterface(DeviceInterfaceDto interfaceDto, User user) {
        log.info("Creating interface for device ID: {} by user: {}", interfaceDto.getDeviceId(), user.getUsername());

        Device device = deviceRepository.findByIdAndUser(interfaceDto.getDeviceId(), user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        if (existsByDeviceAndIfIndex(interfaceDto.getDeviceId(), interfaceDto.getIfIndex(), user)) {
            throw new IllegalArgumentException("Interface with index " + interfaceDto.getIfIndex() + " already exists for this device");
        }

        DeviceInterface deviceInterface = interfaceMapper.toEntity(interfaceDto);
        deviceInterface.setDevice(device);

        DeviceInterface savedInterface = interfaceRepository.save(deviceInterface);
        log.info("Interface created successfully with ID: {}", savedInterface.getId());

        return interfaceMapper.toDto(savedInterface);
    }

    @Override
    public DeviceInterfaceDto updateInterface(Long id, DeviceInterfaceDto interfaceDto, User user) {
        log.info("Updating interface ID: {} by user: {}", id, user.getUsername());

        DeviceInterface existingInterface = interfaceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Interface not found"));

        // Verify user owns the device
        if (!existingInterface.getDevice().getUser().equals(user)) {
            throw new IllegalArgumentException("Access denied");
        }

        // Check if ifIndex is being changed and if it conflicts
        if (!existingInterface.getIfIndex().equals(interfaceDto.getIfIndex()) &&
            existsByDeviceAndIfIndex(existingInterface.getDevice().getId(), interfaceDto.getIfIndex(), user)) {
            throw new IllegalArgumentException("Interface with index " + interfaceDto.getIfIndex() + " already exists for this device");
        }

        interfaceMapper.updateEntity(existingInterface, interfaceDto);
        DeviceInterface updatedInterface = interfaceRepository.save(existingInterface);

        log.info("Interface updated successfully: {}", updatedInterface.getId());
        return interfaceMapper.toDto(updatedInterface);
    }

    @Override
    public void deleteInterface(Long id, User user) {
        log.info("Deleting interface ID: {} by user: {}", id, user.getUsername());

        DeviceInterface deviceInterface = interfaceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Interface not found"));

        // Verify user owns the device
        if (!deviceInterface.getDevice().getUser().equals(user)) {
            throw new IllegalArgumentException("Access denied");
        }

        interfaceRepository.delete(deviceInterface);
        log.info("Interface deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceInterfaceDto getInterfaceById(Long id, User user) {
        log.debug("Getting interface ID: {} for user: {}", id, user.getUsername());

        DeviceInterface deviceInterface = interfaceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Interface not found"));

        // Verify user owns the device
        if (!deviceInterface.getDevice().getUser().equals(user)) {
            throw new IllegalArgumentException("Access denied");
        }

        return interfaceMapper.toDto(deviceInterface);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceInterfaceDto> getInterfacesByDevice(Long deviceId, User user) {
        log.debug("Getting interfaces for device ID: {} for user: {}", deviceId, user.getUsername());

        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        List<DeviceInterface> interfaces = interfaceRepository.findByDevice(device);
        return interfaces.stream()
                .map(interfaceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceInterfaceDto> getInterfacesByDeviceAndStatus(Long deviceId, DeviceInterface.InterfaceStatus status, User user) {
        log.debug("Getting interfaces by status: {} for device ID: {} for user: {}", status, deviceId, user.getUsername());

        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        List<DeviceInterface> interfaces = interfaceRepository.findByDeviceAndIfAdminStatus(device, status);
        return interfaces.stream()
                .map(interfaceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceInterfaceDto> searchInterfacesByDescription(Long deviceId, String description, User user) {
        log.debug("Searching interfaces by description: {} for device ID: {} for user: {}", description, deviceId, user.getUsername());

        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        List<DeviceInterface> interfaces = interfaceRepository.findByDeviceAndIfDescrContaining(device, description);
        return interfaces.stream()
                .map(interfaceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long countInterfacesByDevice(Long deviceId, User user) {
        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        return interfaceRepository.countByDevice(device);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDeviceAndIfIndex(Long deviceId, Integer ifIndex, User user) {
        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        return interfaceRepository.existsByDeviceAndIfIndex(device, ifIndex);
    }
}
