package com.farukgenc.boilerplate.springboot.service.device;

import com.farukgenc.boilerplate.springboot.dto.DeviceDto;
import com.farukgenc.boilerplate.springboot.dto.DeviceCreateRequest;
import com.farukgenc.boilerplate.springboot.dto.DeviceResponse;
import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DeviceService {

    // Legacy methods for existing DeviceDto
    DeviceDto createDevice(DeviceDto deviceDto, User user);

    DeviceDto updateDevice(Long id, DeviceDto deviceDto, User user);

    void deleteDevice(Long id, User user);

    DeviceDto getDeviceById(Long id, User user);

    List<DeviceDto> getDevicesByUser(User user);

    Page<DeviceDto> getDevicesByUser(User user, Pageable pageable);

    List<DeviceDto> getDevicesByUserAndStatus(User user, Device.DeviceStatus status);

    List<DeviceDto> getDevicesByUserAndType(User user, Device.DeviceType type);

    List<DeviceDto> searchDevicesByName(User user, String name);

    Long countDevicesByUser(User user);

    boolean existsByNameAndUser(String name, User user);

    // New methods for configuration-based device creation
    DeviceResponse createDeviceWithConfig(DeviceCreateRequest request, User user);

    DeviceResponse getDeviceWithConfigById(Long id, User user);

    List<DeviceResponse> getDevicesWithConfigByUser(User user);

    Page<DeviceResponse> getDevicesWithConfigByUser(User user, Pageable pageable);

    // Monitoring methods
    void triggerMonitoring(Long deviceId, User user);
}
