package com.farukgenc.boilerplate.springboot.repository;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.DeviceInterface;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceInterfaceRepository extends JpaRepository<DeviceInterface, Long> {

    List<DeviceInterface> findByDevice(Device device);
    
    List<DeviceInterface> findByDeviceId(Long deviceId);

    Optional<DeviceInterface> findByDeviceAndIfIndex(Device device, Integer ifIndex);
    
    Optional<DeviceInterface> findByDeviceIdAndIfIndex(Long deviceId, Integer ifIndex);

    List<DeviceInterface> findByDeviceAndIfAdminStatus(Device device, DeviceInterface.InterfaceStatus status);

    List<DeviceInterface> findByDeviceAndIfOperStatus(Device device, DeviceInterface.InterfaceStatus status);

    @Query("SELECT di FROM DeviceInterface di WHERE di.device = :device AND di.ifDescr LIKE %:description%")
    List<DeviceInterface> findByDeviceAndIfDescrContaining(@Param("device") Device device, @Param("description") String description);

    @Query("SELECT COUNT(di) FROM DeviceInterface di WHERE di.device = :device")
    Long countByDevice(@Param("device") Device device);

    boolean existsByDeviceAndIfIndex(Device device, Integer ifIndex);
}
