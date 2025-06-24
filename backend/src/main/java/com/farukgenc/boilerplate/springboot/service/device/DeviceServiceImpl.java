package com.farukgenc.boilerplate.springboot.service.device;

import com.farukgenc.boilerplate.springboot.dto.DeviceDto;
import com.farukgenc.boilerplate.springboot.dto.DeviceCreateRequest;
import com.farukgenc.boilerplate.springboot.dto.DeviceResponse;
import com.farukgenc.boilerplate.springboot.mapper.DeviceMapper;
import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceConfig;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;
    // Note: DeviceMonitoringService would be injected here in a real implementation
    // but to avoid circular dependency issues in this demo, we'll keep it simple

    @Override
    public DeviceDto createDevice(DeviceDto deviceDto, User user) {
        log.info("Creating device: {} for user: {}", deviceDto.getName(), user.getUsername());

        if (existsByNameAndUser(deviceDto.getName(), user)) {
            throw new IllegalArgumentException("Device with name '" + deviceDto.getName() + "' already exists for this user");
        }

        Device device = deviceMapper.toEntity(deviceDto);
        device.setUser(user);

        Device savedDevice = deviceRepository.save(device);
        log.info("Device created successfully with ID: {}", savedDevice.getId());

        return deviceMapper.toDto(savedDevice);
    }

    @Override
    public DeviceDto updateDevice(Long id, DeviceDto deviceDto, User user) {
        log.info("Updating device ID: {} for user: {}", id, user.getUsername());

        Device existingDevice = deviceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        // Check if name is being changed and if it conflicts with existing devices
        if (!existingDevice.getName().equals(deviceDto.getName()) && 
            existsByNameAndUser(deviceDto.getName(), user)) {
            throw new IllegalArgumentException("Device with name '" + deviceDto.getName() + "' already exists for this user");
        }

        deviceMapper.updateEntity(existingDevice, deviceDto);
        Device updatedDevice = deviceRepository.save(existingDevice);

        log.info("Device updated successfully: {}", updatedDevice.getId());
        return deviceMapper.toDto(updatedDevice);
    }

    @Override
    public void deleteDevice(Long id, User user) {
        log.info("Deleting device ID: {} for user: {}", id, user.getUsername());

        Device device = deviceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        deviceRepository.delete(device);
        log.info("Device deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceDto getDeviceById(Long id, User user) {
        log.debug("Getting device ID: {} for user: {}", id, user.getUsername());

        Device device = deviceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        return deviceMapper.toDto(device);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceDto> getDevicesByUser(User user) {
        log.debug("Getting all devices for user: {}", user.getUsername());

        List<Device> devices = deviceRepository.findByUser(user);
        return devices.stream()
                .map(deviceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeviceDto> getDevicesByUser(User user, Pageable pageable) {
        log.debug("Getting devices page for user: {} with pageable: {}", user.getUsername(), pageable);

        Page<Device> devices = deviceRepository.findByUser(user, pageable);
        return devices.map(deviceMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceDto> getDevicesByUserAndStatus(User user, Device.DeviceStatus status) {
        log.debug("Getting devices by status: {} for user: {}", status, user.getUsername());

        List<Device> devices = deviceRepository.findByUserAndStatus(user, status);
        return devices.stream()
                .map(deviceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceDto> getDevicesByUserAndType(User user, Device.DeviceType type) {
        log.debug("Getting devices by type: {} for user: {}", type, user.getUsername());

        List<Device> devices = deviceRepository.findByUserAndType(user, type);
        return devices.stream()
                .map(deviceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceDto> searchDevicesByName(User user, String name) {
        log.debug("Searching devices by name: {} for user: {}", name, user.getUsername());

        List<Device> devices = deviceRepository.findByUserAndNameContaining(user, name);
        return devices.stream()
                .map(deviceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long countDevicesByUser(User user) {
        return deviceRepository.countByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndUser(String name, User user) {
        return deviceRepository.existsByNameAndUser(name, user);
    }

    // New configuration-based methods
    @Override
    public DeviceResponse createDeviceWithConfig(DeviceCreateRequest request, User user) {
        // Add null check for user parameter
        if (user == null) {
            throw new IllegalArgumentException("User is not authenticated. Please log in first.");
        }

        log.info("Creating device with config: {} for user: {}", request.getName(), user.getUsername());

        if (existsByNameAndUser(request.getName(), user)) {
            throw new IllegalArgumentException("Device with name '" + request.getName() + "' already exists for this user");
        }

        // Create device entity
        Device device = deviceMapper.toEntity(request);
        device.setUser(user);
        device.setStatus(Device.DeviceStatus.INACTIVE); // Initial status

        // Create device config entity
        DeviceConfig config = deviceMapper.toConfigEntity(request);
        config.setDevice(device);
        config.setConsecutiveFailures(0);

        // Set the config on device (bidirectional relationship)
        device.setDeviceConfig(config);

        Device savedDevice = deviceRepository.save(device);
        log.info("Device with config created successfully with ID: {}", savedDevice.getId());

        return deviceMapper.toResponse(savedDevice);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceResponse getDeviceWithConfigById(Long id, User user) {
        log.debug("Getting device with config ID: {} for user: {}", id, user.getUsername());

        Device device = deviceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        return deviceMapper.toResponse(device);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponse> getDevicesWithConfigByUser(User user) {
        log.debug("Getting all devices with config for user: {}", user.getUsername());

        List<Device> devices = deviceRepository.findByUser(user);
        return devices.stream()
                .map(deviceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeviceResponse> getDevicesWithConfigByUser(User user, Pageable pageable) {
        log.debug("Getting devices with config page for user: {} with pageable: {}", user.getUsername(), pageable);

        Page<Device> devices = deviceRepository.findByUser(user, pageable);
        return devices.map(deviceMapper::toResponse);
    }

    @Override
    public void triggerMonitoring(Long deviceId, User user) {
        log.info("Triggering monitoring for device ID: {} by user: {}", deviceId, user.getUsername());
        
        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));
        
        // This would typically call the monitoring service
        // For now, we'll just log the action
        log.info("Monitoring triggered for device: {} ({})", device.getName(), 
                device.getDeviceConfig() != null ? device.getDeviceConfig().getTargetIp() : "N/A");
    }
}
