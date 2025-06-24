package com.farukgenc.boilerplate.springboot.repository;

import com.farukgenc.boilerplate.springboot.model.Device;
import com.farukgenc.boilerplate.springboot.model.SystemUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemUnitRepository extends JpaRepository<SystemUnit, Long> {

    List<SystemUnit> findByDevice(Device device);

    Optional<SystemUnit> findByDeviceAndUnitIndex(Device device, Integer unitIndex);

    @Query("SELECT su FROM SystemUnit su WHERE su.device = :device AND su.unitName LIKE %:name%")
    List<SystemUnit> findByDeviceAndUnitNameContaining(@Param("device") Device device, @Param("name") String name);

    @Query("SELECT su FROM SystemUnit su WHERE su.device = :device AND su.unitType = :type")
    List<SystemUnit> findByDeviceAndUnitType(@Param("device") Device device, @Param("type") String type);

    @Query("SELECT COUNT(su) FROM SystemUnit su WHERE su.device = :device")
    Long countByDevice(@Param("device") Device device);

    boolean existsByDeviceAndUnitIndex(Device device, Integer unitIndex);
}
