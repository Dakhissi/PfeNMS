package com.farukgenc.boilerplate.springboot.configuration;

import com.farukgenc.boilerplate.springboot.model.*;
import com.farukgenc.boilerplate.springboot.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Data initializer for development and testing
 */
@Slf4j
@Component
@Profile({"dev", "test"})
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceInterfaceRepository interfaceRepository;
    private final SystemUnitRepository systemUnitRepository;
    private final IpProfileRepository ipProfileRepository;
    private final IcmpProfileRepository icmpProfileRepository;
    private final UdpProfileRepository udpProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            initializeData();
        }
    }

    private void initializeData() {
        log.info("Initializing sample data...");

        // Create admin user
        User adminUser = User.builder()
                .username("admin")
                .email("admin@example.com")
                .name("System Administrator")
                .password(passwordEncoder.encode("admin123"))
                .userRole(UserRole.ADMIN)
                .build();
        adminUser = userRepository.save(adminUser);

        // Create regular user
        User regularUser = User.builder()
                .username("user")
                .email("user@example.com")
                .name("Regular User")
                .password(passwordEncoder.encode("user123"))
                .userRole(UserRole.USER)
                .build();
        regularUser = userRepository.save(regularUser);

        // Create sample devices for admin
        Device router1 = Device.builder()
                .name("Core Router 1")
                .description("Main core router for network backbone")
                .systemObjectId("1.3.6.1.4.1.9.1.1")
                .systemName("core-router-01")
                .systemLocation("Data Center Rack A1")
                .systemContact("admin@company.com")
                .systemServices(6)
                .status(Device.DeviceStatus.ACTIVE)
                .type(Device.DeviceType.ROUTER)
                .user(adminUser)
                .build();
        router1 = deviceRepository.save(router1);

        Device switch1 = Device.builder()
                .name("Access Switch 1")
                .description("Access switch for floor 1")
                .systemObjectId("1.3.6.1.4.1.9.1.2")
                .systemName("access-switch-01")
                .systemLocation("Floor 1 IDF")
                .systemContact("admin@company.com")
                .systemServices(2)
                .status(Device.DeviceStatus.ACTIVE)
                .type(Device.DeviceType.SWITCH)
                .user(adminUser)
                .build();
        switch1 = deviceRepository.save(switch1);

        // Create sample device for regular user
        Device printer1 = Device.builder()
                .name("Office Printer")
                .description("Network printer for office use")
                .systemObjectId("1.3.6.1.4.1.11.2.3.9.4.2.1")
                .systemName("printer-01")
                .systemLocation("Office Floor 2")
                .systemContact("user@company.com")
                .systemServices(1)
                .status(Device.DeviceStatus.ACTIVE)
                .type(Device.DeviceType.PRINTER)
                .user(regularUser)
                .build();
        printer1 = deviceRepository.save(printer1);

        // Create sample interfaces for router
        DeviceInterface eth0 = DeviceInterface.builder()
                .ifIndex(1)
                .ifDescr("Ethernet0/0")
                .ifType(DeviceInterface.InterfaceType.ETHERNET_CSMACD)
                .ifMtu(1500)
                .ifSpeed(1000000000L)
                .ifPhysAddress("00:1A:2B:3C:4D:5E")
                .ifAdminStatus(DeviceInterface.InterfaceStatus.UP)
                .ifOperStatus(DeviceInterface.InterfaceStatus.UP)
                .device(router1)
                .build();
        interfaceRepository.save(eth0);

        DeviceInterface eth1 = DeviceInterface.builder()
                .ifIndex(2)
                .ifDescr("Ethernet0/1")
                .ifType(DeviceInterface.InterfaceType.ETHERNET_CSMACD)
                .ifMtu(1500)
                .ifSpeed(1000000000L)
                .ifPhysAddress("00:1A:2B:3C:4D:5F")
                .ifAdminStatus(DeviceInterface.InterfaceStatus.UP)
                .ifOperStatus(DeviceInterface.InterfaceStatus.DOWN)
                .device(router1)
                .build();
        interfaceRepository.save(eth1);

        // Create sample system units
        SystemUnit cpu = SystemUnit.builder()
                .unitIndex(1)
                .unitName("CPU Module")
                .unitDescription("Main CPU processing unit")
                .unitType("CPU")
                .unitHwVersion("1.0")
                .unitFwVersion("15.4.3")
                .unitSwVersion("IOS XE")
                .unitSerialNumber("ABC123456")
                .unitMfgName("Cisco Systems")
                .unitModelName("ISR4431")
                .unitMfgDate(LocalDateTime.now().minusYears(2))
                .device(router1)
                .build();
        systemUnitRepository.save(cpu);

        // Create sample IP profile
        IpProfile ipProfile = IpProfile.builder()
                .ipForwarding(true)
                .ipDefaultTTL(64)
                .ipAddress("192.168.1.1")
                .ipSubnetMask("255.255.255.0")
                .ipBroadcastAddr("192.168.1.255")
                .device(router1)
                .build();
        ipProfileRepository.save(ipProfile);

        // Create sample ICMP profile
        IcmpProfile icmpProfile = IcmpProfile.builder()
                .icmpInMsgs(1000L)
                .icmpInErrors(5L)
                .icmpOutMsgs(800L)
                .icmpOutErrors(2L)
                .device(router1)
                .build();
        icmpProfileRepository.save(icmpProfile);

        // Create sample UDP profile
        UdpProfile udpProfile = UdpProfile.builder()
                .udpInDatagrams(5000L)
                .udpOutDatagrams(4500L)
                .udpLocalAddress("192.168.1.1")
                .udpLocalPort(161)
                .udpEntryStatus(UdpProfile.UdpEntryStatus.VALID)
                .device(router1)
                .build();
        udpProfileRepository.save(udpProfile);

        log.info("Sample data initialization completed successfully!");
        log.info("Admin user: username=admin, password=admin123");
        log.info("Regular user: username=user, password=user123");
    }
}
