package com.farukgenc.boilerplate.springboot.service.device;

import com.farukgenc.boilerplate.springboot.dto.SystemUnitDto;
import com.farukgenc.boilerplate.springboot.mapper.SystemUnitMapper;
import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.SystemUnit;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.repository.DeviceRepository;
import com.farukgenc.boilerplate.springboot.repository.SystemUnitRepository;
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
public class SystemUnitServiceImpl implements SystemUnitService {

    private final SystemUnitRepository systemUnitRepository;
    private final DeviceRepository deviceRepository;
    private final SystemUnitMapper systemUnitMapper;

    @Override
    public SystemUnitDto createSystemUnit(SystemUnitDto systemUnitDto, User user) {
        log.info("Creating system unit for device ID: {} by user: {}", systemUnitDto.getDeviceId(), user.getUsername());

        Device device = deviceRepository.findByIdAndUser(systemUnitDto.getDeviceId(), user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        if (existsByDeviceAndUnitIndex(systemUnitDto.getDeviceId(), systemUnitDto.getUnitIndex(), user)) {
            throw new IllegalArgumentException("System unit with index " + systemUnitDto.getUnitIndex() + " already exists for this device");
        }

        SystemUnit systemUnit = systemUnitMapper.toEntity(systemUnitDto);
        systemUnit.setDevice(device);

        SystemUnit savedSystemUnit = systemUnitRepository.save(systemUnit);
        log.info("System unit created successfully with ID: {}", savedSystemUnit.getId());

        return systemUnitMapper.toDto(savedSystemUnit);
    }

    @Override
    public SystemUnitDto updateSystemUnit(Long id, SystemUnitDto systemUnitDto, User user) {
        log.info("Updating system unit ID: {} by user: {}", id, user.getUsername());

        SystemUnit existingSystemUnit = systemUnitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("System unit not found"));

        // Verify user owns the device
        if (!existingSystemUnit.getDevice().getUser().equals(user)) {
            throw new IllegalArgumentException("Access denied");
        }

        // Check if unitIndex is being changed and if it conflicts
        if (!existingSystemUnit.getUnitIndex().equals(systemUnitDto.getUnitIndex()) &&
            existsByDeviceAndUnitIndex(existingSystemUnit.getDevice().getId(), systemUnitDto.getUnitIndex(), user)) {
            throw new IllegalArgumentException("System unit with index " + systemUnitDto.getUnitIndex() + " already exists for this device");
        }

        systemUnitMapper.updateEntity(existingSystemUnit, systemUnitDto);
        SystemUnit updatedSystemUnit = systemUnitRepository.save(existingSystemUnit);

        log.info("System unit updated successfully: {}", updatedSystemUnit.getId());
        return systemUnitMapper.toDto(updatedSystemUnit);
    }

    @Override
    public void deleteSystemUnit(Long id, User user) {
        log.info("Deleting system unit ID: {} by user: {}", id, user.getUsername());

        SystemUnit systemUnit = systemUnitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("System unit not found"));

        // Verify user owns the device
        if (!systemUnit.getDevice().getUser().equals(user)) {
            throw new IllegalArgumentException("Access denied");
        }

        systemUnitRepository.delete(systemUnit);
        log.info("System unit deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public SystemUnitDto getSystemUnitById(Long id, User user) {
        log.debug("Getting system unit ID: {} for user: {}", id, user.getUsername());

        SystemUnit systemUnit = systemUnitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("System unit not found"));

        // Verify user owns the device
        if (!systemUnit.getDevice().getUser().equals(user)) {
            throw new IllegalArgumentException("Access denied");
        }

        return systemUnitMapper.toDto(systemUnit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemUnitDto> getSystemUnitsByDevice(Long deviceId, User user) {
        log.debug("Getting system units for device ID: {} for user: {}", deviceId, user.getUsername());

        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        List<SystemUnit> systemUnits = systemUnitRepository.findByDevice(device);
        return systemUnits.stream()
                .map(systemUnitMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemUnitDto> searchSystemUnitsByName(Long deviceId, String name, User user) {
        log.debug("Searching system units by name: {} for device ID: {} for user: {}", name, deviceId, user.getUsername());

        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        List<SystemUnit> systemUnits = systemUnitRepository.findByDeviceAndUnitNameContaining(device, name);
        return systemUnits.stream()
                .map(systemUnitMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemUnitDto> getSystemUnitsByDeviceAndType(Long deviceId, String type, User user) {
        log.debug("Getting system units by type: {} for device ID: {} for user: {}", type, deviceId, user.getUsername());

        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        List<SystemUnit> systemUnits = systemUnitRepository.findByDeviceAndUnitType(device, type);
        return systemUnits.stream()
                .map(systemUnitMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long countSystemUnitsByDevice(Long deviceId, User user) {
        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        return systemUnitRepository.countByDevice(device);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDeviceAndUnitIndex(Long deviceId, Integer unitIndex, User user) {
        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or access denied"));

        return systemUnitRepository.existsByDeviceAndUnitIndex(device, unitIndex);
    }
}
