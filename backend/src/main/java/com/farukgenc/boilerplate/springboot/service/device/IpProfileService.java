package com.farukgenc.boilerplate.springboot.service.device;

import com.farukgenc.boilerplate.springboot.dto.IpProfileDto;
import com.farukgenc.boilerplate.springboot.model.User;

import java.util.List;

public interface IpProfileService {

    IpProfileDto createIpProfile(IpProfileDto ipProfileDto, User user);

    IpProfileDto updateIpProfile(Long id, IpProfileDto ipProfileDto, User user);

    void deleteIpProfile(Long id, User user);

    IpProfileDto getIpProfileById(Long id, User user);

    List<IpProfileDto> getIpProfilesByDevice(Long deviceId, User user);

    List<IpProfileDto> getIpProfilesByDeviceAndForwarding(Long deviceId, Boolean forwarding, User user);

    List<IpProfileDto> searchIpProfilesByAddress(Long deviceId, String address, User user);

    Long countIpProfilesByDevice(Long deviceId, User user);

    boolean existsByDeviceAndIpAddress(Long deviceId, String ipAddress, User user);
}
