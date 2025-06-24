package com.farukgenc.boilerplate.springboot.service.device;

import com.farukgenc.boilerplate.springboot.dto.SystemUnitDto;
import com.farukgenc.boilerplate.springboot.model.User;

import java.util.List;

public interface SystemUnitService {

    SystemUnitDto createSystemUnit(SystemUnitDto systemUnitDto, User user);

    SystemUnitDto updateSystemUnit(Long id, SystemUnitDto systemUnitDto, User user);

    void deleteSystemUnit(Long id, User user);

    SystemUnitDto getSystemUnitById(Long id, User user);

    List<SystemUnitDto> getSystemUnitsByDevice(Long deviceId, User user);

    List<SystemUnitDto> searchSystemUnitsByName(Long deviceId, String name, User user);

    List<SystemUnitDto> getSystemUnitsByDeviceAndType(Long deviceId, String type, User user);

    Long countSystemUnitsByDevice(Long deviceId, User user);

    boolean existsByDeviceAndUnitIndex(Long deviceId, Integer unitIndex, User user);
}
