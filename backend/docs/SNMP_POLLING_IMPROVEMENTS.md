# SNMP Polling Logic Improvements

## Overview
This document describes the comprehensive improvements made to the SNMP polling logic to address the following requirements:

1. **30-second polling interval** with parallel threads
2. **Enhanced hex parsing** for SNMP data
3. **Duplicate prevention** for interfaces, system units, and profiles
4. **Alert filtering** (only CRITICAL, SEVERE, WARNING - no INFO)
5. **Device IP updating** based on SNMP target IP
6. **Improved error handling** and logging

## Key Improvements

### 1. Polling Interval and Parallelization

**Changed from 5 minutes to 30 seconds:**
- `SnmpPollingService`: Updated `@Scheduled(fixedRate = 30000)` 
- `DeviceMonitoringService`: Updated `@Scheduled(fixedRate = 30000)`

**Enhanced parallel execution:**
```java
// Added proper parallel processing with timeout
List<CompletableFuture<Void>> futures = new ArrayList<>();
for (DeviceConfig config : enabledConfigs) {
    CompletableFuture<Void> future = pollDeviceAsync(config);
    futures.add(future);
}

CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
allFutures.get(120, TimeUnit.SECONDS); // 2-minute timeout
```

### 2. Enhanced Hex Parsing

**Created `SnmpDataParser` utility class:**
- `parseHexToString()`: Converts hex values to readable strings
- `formatMacAddress()`: Enhanced MAC address formatting
- `parseHexToIpAddress()`: Converts hex IP addresses
- `parseTimeTicks()`: Converts SNMP TimeTicks to readable duration
- `isHexFormat()`: Detects hex format data

**Integrated into polling services:**
```java
// Example usage in SystemInfoPollService
String descr = sysDescr.toString();
if (snmpDataParser.isHexFormat(descr)) {
    descr = snmpDataParser.parseHexToString(descr);
}
systemInfo.setSysDescr(descr);
```

### 3. Duplicate Prevention

**Created `DuplicatePreventionService`:**
- `cleanupStaleInterfaces()`: Removes interfaces no longer present
- `getOrCreateInterface()`: Prevents duplicate interface creation
- `cleanupStaleSystemUnits()`: Manages system unit duplicates
- `deduplicateIpProfiles()`: Removes duplicate IP profiles
- `deduplicateUdpProfiles()`: Removes duplicate UDP profiles

**Integration example:**
```java
// In InterfacePollService
DeviceInterface deviceInterface = duplicatePreventionService.getOrCreateInterface(device, ifIndex);
// ... update data ...
duplicatePreventionService.cleanupStaleInterfaces(device, activeInterfaceIndices);
```

### 4. Alert Filtering

**Enhanced `AlertServiceImpl` to filter by severity:**
```java
// Only create alerts for CRITICAL, SEVERE, and WARNING levels - skip INFO
if (severity == Alert.AlertSeverity.INFO) {
    log.debug("Skipping INFO level alert: {} for user: {}", title, user.getUsername());
    return null;
}
```

**Improved duplicate alert prevention:**
- Time-based deduplication (5-minute window)
- Enhanced alert key generation including severity
- Occurrence count tracking

### 5. Device IP Address Updates

**Added to `SystemInfoPollService`:**
```java
private void updateDeviceIpIfNeeded(Device device, DeviceConfig config) {
    if (!device.getIpAddress().equals(config.getTargetIp())) {
        log.info("Updating device {} IP address from {} to {}", 
            device.getName(), device.getIpAddress(), config.getTargetIp());
        
        device.setIpAddress(config.getTargetIp());
        deviceRepository.save(device);
    }
}
```

### 6. Enhanced Error Handling

**Improved exception handling throughout:**
- Better logging for debugging
- Graceful degradation on individual component failures
- Timeout management for parallel operations
- Detailed error messages for troubleshooting

## Files Modified

### Core Polling Services
- `SnmpPollingService.java` - Main orchestrator with 30s interval and parallel execution
- `InterfacePollService.java` - Enhanced with duplicate prevention and hex parsing
- `SystemInfoPollService.java` - Added device IP updates and enhanced parsing
- `DeviceMonitoringService.java` - Updated interval and alert triggering

### Alert Management
- `AlertServiceImpl.java` - Added severity filtering and improved deduplication

### New Utility Services
- `SnmpDataParser.java` - Comprehensive hex and SNMP data parsing
- `DuplicatePreventionService.java` - Manages entity duplicates across all profiles

### REST Controllers
- `UdpProfileController.java` - New controller for UDP profile management
- `IcmpProfileController.java` - New controller for ICMP profile management

### Service Layers
- `UdpProfileService.java` & `UdpProfileServiceImpl.java` - Complete CRUD for UDP profiles
- `IcmpProfileService.java` & `IcmpProfileServiceImpl.java` - Complete CRUD for ICMP profiles

### Configuration
- `application.yml` - Updated Swagger groupings to include new profile endpoints

## Performance Considerations

### Threading and Concurrency
- Parallel device polling with proper thread management
- Timeout protection to prevent hanging operations
- Async execution with `@Async` annotation

### Database Efficiency
- Bulk operations where possible (`saveAll`, `deleteAll`)
- Optimized queries with proper indexing
- Transaction boundaries to ensure data consistency

### Memory Management
- Proper cleanup of stale entities
- Efficient data structures for processing
- Limited result sets where appropriate

## Monitoring and Alerting

### Alert Severity Levels
- **CRITICAL**: Device down, major system failures
- **SEVERE**: Interface down, SNMP unreachable  
- **WARNING**: Slow response, minor issues
- **INFO**: Skipped (not created)

### Alert Deduplication
- 5-minute suppression window for identical alerts
- Occurrence counting for repeated issues
- Intelligent alert key generation

### Logging Levels
- **INFO**: Successful operations, important state changes
- **DEBUG**: Detailed polling information
- **WARN**: Recoverable errors, parsing issues
- **ERROR**: Critical failures requiring attention

## Configuration Parameters

### Polling Intervals
```yaml
# In application.yml or as environment variables
app:
  monitoring:
    enabled: true
    interval: 30000 # 30 seconds in milliseconds
    snmp-timeout: 5000
    ping-timeout: 5000
```

### Thread Pool Configuration
```java
// Recommended thread pool settings for @Async
@Bean(name = "snmpTaskExecutor")
public TaskExecutor snmpTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(50);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("snmp-poll-");
    executor.initialize();
    return executor;
}
```

## Testing Recommendations

### Unit Tests
- Test hex parsing with various input formats
- Verify duplicate prevention logic
- Test alert filtering by severity
- Validate device IP update logic

### Integration Tests
- End-to-end polling workflows
- Concurrent polling scenarios
- Error recovery testing
- Performance under load

### Monitoring
- Track polling success rates
- Monitor alert generation patterns
- Watch for memory leaks or performance degradation
- Verify data consistency across polling cycles

## Future Enhancements

### Potential Improvements
1. **Dynamic polling intervals** based on device priority
2. **SNMP v3 support** for enhanced security
3. **Custom MIB loading** for vendor-specific data
4. **Historical data trending** for performance analysis
5. **Predictive alerting** based on trends
6. **Dashboard integration** for real-time monitoring

### Scalability Considerations
1. **Database partitioning** for large device counts
2. **Distributed polling** across multiple instances
3. **Caching strategies** for frequently accessed data
4. **Queue-based processing** for high-volume environments
