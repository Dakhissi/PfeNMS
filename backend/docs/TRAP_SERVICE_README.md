# SNMP Trap Service Documentation

## Overview

The SNMP Trap Service is a comprehensive solution for receiving, processing, and managing SNMP trap events in the Spring Boot Network Management System. It provides real-time trap reception, duplicate detection, alert creation, and notification capabilities.

## Features

### 1. Trap Reception
- **SNMP v1/v2c/v3 Support**: Receives traps from various SNMP versions
- **Configurable Port**: Default port 162 (standard SNMP trap port)
- **Community String Validation**: Configurable community string checking
- **Auto-start**: Automatically starts when the application boots

### 2. Duplicate Detection
- **Hash-based Detection**: Uses MD5 hashing of source IP, trap OID, and time window
- **Time Window**: 5-minute windows for duplicate suppression
- **Duplicate Counting**: Tracks how many times a duplicate trap is received

### 3. Alert Integration
- **Automatic Alert Creation**: Creates alerts for critical, major, and minor severity traps
- **Severity Mapping**: Maps trap severity to alert severity levels
- **Type Classification**: Categorizes traps into standard types (link up/down, device status, etc.)
- **User Notifications**: Sends real-time notifications via WebSocket

### 4. Data Management
- **Persistent Storage**: Stores trap events in PostgreSQL database
- **User Association**: Links traps to device owners when possible
- **Device Linking**: Associates traps with registered devices by IP address
- **Automatic Cleanup**: Scheduled cleanup of old trap events

## Configuration

### Application Properties (application.yml)

```yaml
app:
  trap-receiver:
    enabled: true                    # Enable/disable trap receiver
    port: 162                       # SNMP trap port
    community: public               # Expected community string
    auto-start: true                # Start automatically on boot
    cleanup-enabled: true           # Enable scheduled cleanup
    cleanup-interval-hours: 24      # Cleanup interval
    cleanup-retention-days: 30      # Days to retain trap events
```

## API Endpoints

### Base URL: `/api/v1/traps`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get paginated trap events |
| GET | `/{id}` | Get specific trap event |
| GET | `/device/{deviceId}` | Get traps for specific device |
| GET | `/source/{sourceIp}` | Get traps from specific IP |
| GET | `/recent` | Get recent trap events |
| GET | `/unprocessed/count` | Get count of unprocessed traps |
| PUT | `/{id}/process` | Mark trap as processed |
| GET | `/statistics` | Get trap statistics |
| GET | `/receiver/status` | Get receiver status |
| DELETE | `/cleanup` | Trigger manual cleanup |

## Component Architecture

### 1. TrapEvent Entity
- Stores trap information in database
- Includes source IP, trap type, severity, variable bindings
- Tracks processing status and duplicate count

### 2. TrapReceiver
- Listens for incoming SNMP traps
- Uses SNMP4J library for trap reception
- Handles SNMPv1, v2c, and v3 traps

### 3. TrapProcessor
- Processes received traps
- Performs duplicate detection
- Creates alerts when necessary
- Associates traps with devices and users

### 4. TrapService/TrapServiceImpl
- Main service interface for trap management
- Provides CRUD operations for trap events
- Manages trap receiver lifecycle
- Generates statistics and reports

### 5. TrapController
- REST API endpoints for trap management
- Supports pagination and filtering
- Returns DTOs mapped via TrapMapper

### 6. TrapMapper
- MapStruct mapper for Entity ↔ DTO conversion
- Handles complex mapping scenarios
- Ensures data consistency

## Trap Types Supported

### Standard SNMP Traps (SNMPv1 Generic)
- **Cold Start** (0): Device restarted
- **Warm Start** (1): Device restarted without configuration change
- **Link Down** (2): Network interface went down
- **Link Up** (3): Network interface came up
- **Authentication Failure** (4): SNMP authentication failed
- **EGP Neighbor Loss** (5): EGP neighbor lost

### Enterprise-Specific Traps
- **Temperature Alarm**: Device temperature exceeds threshold
- **Fan Failure**: Cooling fan malfunction
- **Power Failure**: Power supply issues
- **CPU High**: High CPU utilization
- **Memory Low**: Low memory condition
- **Configuration Change**: Device configuration modified

## Severity Levels

1. **CRITICAL**: Immediate attention required
2. **MAJOR**: Significant impact on operations
3. **MINOR**: Minor impact, monitor situation
4. **WARNING**: Potential issue, informational
5. **INFO**: General information
6. **CLEARED**: Problem resolved

## Usage Examples

### Starting the Trap Receiver
The trap receiver starts automatically when the application boots. You can also control it programmatically:

```java
@Autowired
private TrapService trapService;

// Start receiver
trapService.startTrapReceiver();

// Check status
boolean isRunning = trapService.isTrapReceiverRunning();

// Stop receiver
trapService.stopTrapReceiver();
```

### Getting Trap Events via API
```bash
# Get recent traps
curl -X GET "http://localhost:8080/api/v1/traps?page=0&size=20"

# Get traps for specific device
curl -X GET "http://localhost:8080/api/v1/traps/device/123"

# Get trap statistics
curl -X GET "http://localhost:8080/api/v1/traps/statistics"
```

### Processing Workflow

1. **Trap Reception**: TrapReceiver listens on configured port
2. **Initial Processing**: Extract trap details and variable bindings
3. **Duplicate Check**: Generate hash and check for recent duplicates
4. **Device Association**: Find device by source IP if registered
5. **User Association**: Link to device owner or system user
6. **Severity Assessment**: Determine trap severity based on type and content
7. **Alert Creation**: Create alert if severity warrants it
8. **Notification**: Send WebSocket notification to user
9. **Storage**: Save trap event to database
10. **Cleanup**: Periodic cleanup of old trap events

## Security Considerations

- **Community String Validation**: Verifies expected community string
- **User Authorization**: API endpoints require authentication
- **Rate Limiting**: Built-in duplicate detection prevents spam
- **Data Isolation**: Users only see their own device traps

## Monitoring and Troubleshooting

### Logging
The service uses SLF4J logging with the following levels:
- **INFO**: Trap reception and processing events
- **DEBUG**: Detailed processing information
- **WARN**: Non-critical issues (e.g., unknown trap types)
- **ERROR**: Processing failures and exceptions

### Health Checks
- **Receiver Status**: Check if trap receiver is running
- **Statistics**: Monitor trap processing rates
- **Database Health**: Verify trap event storage

### Common Issues
1. **Port 162 Permission**: May require root/admin privileges
2. **Firewall**: Ensure port 162 is open for UDP traffic
3. **Community Mismatch**: Verify community string configuration
4. **Database Connection**: Check PostgreSQL connectivity
5. **Memory Usage**: Monitor for large numbers of trap events

## Future Enhancements

- **SNMP v3 Security**: Enhanced authentication and encryption
- **Advanced Correlation**: Cross-device alert correlation
- **Custom Rules**: User-definable trap processing rules
- **Metrics Export**: Prometheus/Grafana integration
- **Email Notifications**: Email alerts for critical traps
- **Trap Forwarding**: Forward traps to other management systems
