package com.farukgenc.boilerplate.springboot.repository;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.UdpProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UdpProfileRepository extends JpaRepository<UdpProfile, Long> {

    List<UdpProfile> findByDevice(Device device);
    
    Optional<UdpProfile> findByDeviceId(Long deviceId);

    Optional<UdpProfile> findByDeviceAndUdpLocalAddressAndUdpLocalPort(Device device, String localAddress, Integer localPort);

    List<UdpProfile> findByDeviceAndUdpEntryStatus(Device device, UdpProfile.UdpEntryStatus status);

    @Query("SELECT up FROM UdpProfile up WHERE up.device = :device AND up.udpLocalAddress = :address")
    List<UdpProfile> findByDeviceAndUdpLocalAddress(@Param("device") Device device, @Param("address") String address);

    @Query("SELECT COUNT(up) FROM UdpProfile up WHERE up.device = :device")
    Long countByDevice(@Param("device") Device device);

    boolean existsByDeviceAndUdpLocalAddressAndUdpLocalPort(Device device, String localAddress, Integer localPort);
}
