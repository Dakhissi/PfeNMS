package com.farukgenc.boilerplate.springboot.service.device;

import com.farukgenc.boilerplate.springboot.dto.UdpProfileDto;
import com.farukgenc.boilerplate.springboot.model.UdpProfile;
import com.farukgenc.boilerplate.springboot.model.User;

import java.util.List;

public interface UdpProfileService {

    UdpProfileDto createUdpProfile(UdpProfileDto udpProfileDto, User user);

    UdpProfileDto updateUdpProfile(Long id, UdpProfileDto udpProfileDto, User user);

    void deleteUdpProfile(Long id, User user);

    UdpProfileDto getUdpProfileById(Long id, User user);

    List<UdpProfileDto> getUdpProfilesByDevice(Long deviceId, User user);

    List<UdpProfileDto> getUdpProfilesByDeviceAndStatus(Long deviceId, UdpProfile.UdpEntryStatus status, User user);

    List<UdpProfileDto> searchUdpProfilesByAddress(Long deviceId, String address, User user);

    Long countUdpProfilesByDevice(Long deviceId, User user);

    boolean existsByDeviceAndLocalAddressAndPort(Long deviceId, String localAddress, Integer localPort, User user);
}
