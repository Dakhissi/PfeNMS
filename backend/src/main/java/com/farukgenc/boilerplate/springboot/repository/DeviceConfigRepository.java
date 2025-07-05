package com.farukgenc.boilerplate.springboot.repository;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceConfigRepository extends JpaRepository<DeviceConfig, Long> {

    Optional<DeviceConfig> findByDevice(Device device);
    
    Optional<DeviceConfig> findByDeviceId(Long deviceId);
    
    List<DeviceConfig> findByEnabledTrue();

    @Query("SELECT dc FROM DeviceConfig dc WHERE dc.enabled = true")
    List<DeviceConfig> findAllEnabled();

    @Query("SELECT dc FROM DeviceConfig dc WHERE dc.enabled = true AND " +
           "(dc.lastPollTime IS NULL OR dc.lastPollTime < :cutoffTime)")
    List<DeviceConfig> findEnabledDevicesDueForPolling(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT dc FROM DeviceConfig dc WHERE dc.device.user.id = :userId")
    List<DeviceConfig> findByUserId(@Param("userId") Long userId);

    @Query("SELECT dc FROM DeviceConfig dc WHERE dc.targetIp = :targetIp")
    List<DeviceConfig> findByTargetIp(@Param("targetIp") String targetIp);

    @Query("SELECT dc FROM DeviceConfig dc WHERE dc.lastPollStatus = :status")
    List<DeviceConfig> findByLastPollStatus(@Param("status") DeviceConfig.PollStatus status);

    @Query("SELECT dc FROM DeviceConfig dc WHERE dc.consecutiveFailures >= :threshold")
    List<DeviceConfig> findDevicesWithFailures(@Param("threshold") Integer threshold);

    @Query("SELECT dc FROM DeviceConfig dc LEFT JOIN FETCH dc.device LEFT JOIN FETCH dc.device.user WHERE dc.enabled = true")
    List<DeviceConfig> findByEnabledTrueWithDevice();
}
