package com.farukgenc.boilerplate.springboot.service.device;

import com.farukgenc.boilerplate.springboot.dto.DeviceInterfaceDto;
import com.farukgenc.boilerplate.springboot.model.DeviceInterface;
import com.farukgenc.boilerplate.springboot.model.User;

import java.util.List;

public interface DeviceInterfaceService {

    DeviceInterfaceDto createInterface(DeviceInterfaceDto interfaceDto, User user);

    DeviceInterfaceDto updateInterface(Long id, DeviceInterfaceDto interfaceDto, User user);

    void deleteInterface(Long id, User user);

    DeviceInterfaceDto getInterfaceById(Long id, User user);

    List<DeviceInterfaceDto> getInterfacesByDevice(Long deviceId, User user);

    List<DeviceInterfaceDto> getInterfacesByDeviceAndStatus(Long deviceId, DeviceInterface.InterfaceStatus status, User user);

    List<DeviceInterfaceDto> searchInterfacesByDescription(Long deviceId, String description, User user);

    Long countInterfacesByDevice(Long deviceId, User user);

    boolean existsByDeviceAndIfIndex(Long deviceId, Integer ifIndex, User user);
}
