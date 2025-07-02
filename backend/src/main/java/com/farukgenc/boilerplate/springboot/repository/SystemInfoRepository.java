package com.farukgenc.boilerplate.springboot.repository;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.SystemInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemInfoRepository extends JpaRepository<SystemInfo, Long> {

    Optional<SystemInfo> findByDevice(Device device);
    
    Optional<SystemInfo> findByDeviceId(Long deviceId);

    boolean existsByDevice(Device device);
    
    void deleteByDevice(Device device);
}
