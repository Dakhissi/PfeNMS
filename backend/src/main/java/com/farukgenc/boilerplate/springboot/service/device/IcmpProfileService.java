package com.farukgenc.boilerplate.springboot.service.device;

import com.farukgenc.boilerplate.springboot.dto.IcmpProfileDto;
import com.farukgenc.boilerplate.springboot.model.User;

import java.util.List;

public interface IcmpProfileService {

    IcmpProfileDto createIcmpProfile(IcmpProfileDto icmpProfileDto, User user);

    IcmpProfileDto updateIcmpProfile(Long id, IcmpProfileDto icmpProfileDto, User user);

    void deleteIcmpProfile(Long id, User user);

    IcmpProfileDto getIcmpProfileById(Long id, User user);

    List<IcmpProfileDto> getIcmpProfilesByDevice(Long deviceId, User user);

    Long countIcmpProfilesByDevice(Long deviceId, User user);
}
