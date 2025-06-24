package com.farukgenc.boilerplate.springboot.repository;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.IcmpProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IcmpProfileRepository extends JpaRepository<IcmpProfile, Long> {

    List<IcmpProfile> findByDevice(Device device);
    
    Optional<IcmpProfile> findByDeviceId(Long deviceId);

    @Query("SELECT COUNT(ip) FROM IcmpProfile ip WHERE ip.device = :device")
    Long countByDevice(@Param("device") Device device);
}
