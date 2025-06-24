package com.farukgenc.boilerplate.springboot.repository;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.IpProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IpProfileRepository extends JpaRepository<IpProfile, Long> {

    List<IpProfile> findByDevice(Device device);
    
    Optional<IpProfile> findByDeviceId(Long deviceId);

    Optional<IpProfile> findByDeviceAndIpAddress(Device device, String ipAddress);

    List<IpProfile> findByDeviceAndIpForwarding(Device device, Boolean ipForwarding);

    @Query("SELECT ip FROM IpProfile ip WHERE ip.device = :device AND ip.ipAddress LIKE %:address%")
    List<IpProfile> findByDeviceAndIpAddressContaining(@Param("device") Device device, @Param("address") String address);

    @Query("SELECT COUNT(ip) FROM IpProfile ip WHERE ip.device = :device")
    Long countByDevice(@Param("device") Device device);

    boolean existsByDeviceAndIpAddress(Device device, String ipAddress);
}
