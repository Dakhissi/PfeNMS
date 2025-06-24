package com.farukgenc.boilerplate.springboot.repository;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    List<Device> findByUser(User user);

    Page<Device> findByUser(User user, Pageable pageable);

    Optional<Device> findByIdAndUser(Long id, User user);

    List<Device> findByUserAndStatus(User user, Device.DeviceStatus status);

    List<Device> findByUserAndType(User user, Device.DeviceType type);

    @Query("SELECT d FROM Device d WHERE d.user = :user AND d.name LIKE %:name%")
    List<Device> findByUserAndNameContaining(@Param("user") User user, @Param("name") String name);

    @Query("SELECT COUNT(d) FROM Device d WHERE d.user = :user")
    Long countByUser(@Param("user") User user);

    boolean existsByNameAndUser(String name, User user);

    @Query("SELECT d FROM Device d WHERE d.deviceConfig.targetIp = :targetIp")
    Optional<Device> findByTargetIp(@Param("targetIp") String targetIp);
}
