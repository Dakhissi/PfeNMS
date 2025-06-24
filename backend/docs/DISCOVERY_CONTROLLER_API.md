# Discovery Controller API Documentation

## Overview

The Discovery Controller provides RESTful endpoints for network topology discovery operations. It supports asynchronous network discovery using SNMP, Nmap, and ICMP ping, along with utilities for testing connectivity and performing SNMP operations.

## Base URL
`/api/discovery`

## Authentication
All endpoints require authentication. Discovery operations are scoped to the authenticated user.

## Endpoints

### 1. Start Network Discovery
- **Method**: `POST`
- **Path**: `/api/discovery/start`
- **Summary**: Start network discovery
- **Description**: Starts an asynchronous network discovery process using the specified configuration
- **Request Body**: `DiscoveryRequest` (JSON)
- **Response**: `DiscoveryStatus`
- **Status Codes**:
  - `202 Accepted`: Discovery started successfully
  - `400 Bad Request`: Invalid discovery request
  - `401 Unauthorized`: User not authenticated

**Example Request**:
```json
{
  "name": "Office Network Discovery",
  "networkRange": "192.168.1.0/24",
  "enablePing": true,
  "enableSnmp": true,
  "enablePortScan": false,
  "discoverInterfaces": true,
  "discoverRoutes": true,
  "snmpCommunity": "public",
  "snmpPort": 161,
  "snmpTimeout": 5000,
  "snmpRetries": 3,
  "maxThreads": 10
}
```

**Example Response**:
```json
{
  "discoveryId": "disc_12345",
  "status": "PENDING",
  "phase": "INITIALIZATION",
  "message": "Discovery request accepted and queued for processing",
  "websocketEndpoint": "/topic/discovery/disc_12345/progress"
}
```

### 2. Get Discovery Status
- **Method**: `GET`
- **Path**: `/api/discovery/status/{discoveryId}`
- **Summary**: Get discovery status and result
- **Description**: Gets the current status and result of a discovery process (both ongoing and completed)
- **Path Parameters**: `discoveryId` (String) - Discovery ID
- **Response**: `TopologyResponseDto`
- **Status Codes**:
  - `200 OK`: Status retrieved successfully
  - `404 Not Found`: Discovery not found
  - `401 Unauthorized`: User not authenticated

**Example Response**:
```json
{
  "discoveryId": "disc_12345",
  "status": "COMPLETED",
  "phase": "FINISHED",
  "progress": 100,
  "message": "Discovery completed successfully",
  "startTime": "2024-01-01T10:00:00",
  "endTime": "2024-01-01T10:05:30",
  "devicesFound": 15,
  "topology": {
    "nodes": [
      {
        "id": "192.168.1.1",
        "type": "ROUTER",
        "ipAddress": "192.168.1.1",
        "hostname": "gateway.local",
        "manufacturer": "Cisco",
        "model": "ISR4321"
      }
    ],
    "links": [
      {
        "source": "192.168.1.1",
        "target": "192.168.1.2",
        "type": "ETHERNET"
      }
    ]
  }
}
```

### 3. Cancel Discovery
- **Method**: `POST`
- **Path**: `/api/discovery/cancel/{discoveryId}`
- **Summary**: Cancel discovery
- **Description**: Cancels an ongoing discovery process
- **Path Parameters**: `discoveryId` (String) - Discovery ID
- **Response**: String message
- **Status Codes**:
  - `200 OK`: Discovery cancelled successfully
  - `400 Bad Request`: Discovery cannot be cancelled
  - `404 Not Found`: Discovery not found
  - `401 Unauthorized`: User not authenticated

### 4. Ping Host
- **Method**: `POST`
- **Path**: `/api/discovery/ping`
- **Summary**: Ping a host
- **Description**: Performs a simple ping test to check if a host is reachable
- **Query Parameters**: 
  - `ipAddress` (String, required) - IP address to ping
  - `timeout` (Integer, optional, default: 5000) - Timeout in milliseconds
- **Response**: `PingResult`
- **Status Codes**:
  - `200 OK`: Ping completed (regardless of success/failure)
  - `400 Bad Request`: Invalid IP address
  - `401 Unauthorized`: User not authenticated

**Example Response**:
```json
{
  "ipAddress": "192.168.1.1",
  "reachable": true,
  "responseTime": 15,
  "hostname": "gateway.local",
  "packetLoss": 0,
  "errorMessage": null
}
```

### 5. SNMP Walk
- **Method**: `POST`
- **Path**: `/api/discovery/snmp-walk`
- **Summary**: Perform SNMP walk
- **Description**: Performs an SNMP walk operation on a device
- **Query Parameters**:
  - `ipAddress` (String, required) - IP address of the device
  - `community` (String, optional, default: "public") - SNMP community string
  - `oid` (String, optional, default: "1.3.6.1.2.1.1") - Starting OID
- **Response**: `List<MibBrowserResponse>`
- **Status Codes**:
  - `200 OK`: SNMP walk completed
  - `400 Bad Request`: Invalid parameters or SNMP error
  - `401 Unauthorized`: User not authenticated

**Example Response**:
```json
[
  {
    "oid": "1.3.6.1.2.1.1.1.0",
    "name": "sysDescr",
    "value": "Cisco IOS Software",
    "type": "OCTET_STRING",
    "description": "System description"
  },
  {
    "oid": "1.3.6.1.2.1.1.2.0",
    "name": "sysObjectID",
    "value": "1.3.6.1.4.1.9.1.1",
    "type": "OBJECT_IDENTIFIER",
    "description": "System object identifier"
  }
]
```

## Data Models

### DiscoveryRequest
```json
{
  "name": "String (discovery name)",
  "networkRange": "String (CIDR notation, e.g., '192.168.1.0/24')",
  "enablePing": "Boolean",
  "enableSnmp": "Boolean",
  "enablePortScan": "Boolean",
  "discoverInterfaces": "Boolean",
  "discoverRoutes": "Boolean",
  "snmpCommunity": "String (default: 'public')",
  "snmpPort": "Integer (default: 161)",
  "snmpTimeout": "Integer (default: 5000)",
  "snmpRetries": "Integer (default: 3)",
  "maxThreads": "Integer (default: 10)"
}
```

### DiscoveryStatus
```json
{
  "discoveryId": "String",
  "status": "String (PENDING, IN_PROGRESS, COMPLETED, CANCELLED, ERROR)",
  "phase": "String (current phase description)",
  "message": "String (status message)",
  "websocketEndpoint": "String (WebSocket endpoint for real-time updates)"
}
```

### TopologyResponseDto
```json
{
  "discoveryId": "String",
  "status": "String",
  "phase": "String",
  "progress": "Integer (0-100)",
  "message": "String",
  "startTime": "LocalDateTime",
  "endTime": "LocalDateTime (optional)",
  "devicesFound": "Integer",
  "topology": {
    "nodes": "Array of network nodes",
    "links": "Array of network connections"
  },
  "errors": "Array of error messages (optional)"
}
```

### PingResult
```json
{
  "ipAddress": "String",
  "reachable": "Boolean",
  "responseTime": "Long (milliseconds, -1 if unreachable)",
  "hostname": "String (optional)",
  "packetLoss": "Integer (percentage)",
  "errorMessage": "String (optional)"
}
```

### MibBrowserResponse
```json
{
  "oid": "String (object identifier)",
  "name": "String (MIB object name)",
  "value": "String (object value)",
  "type": "String (SNMP data type)",
  "description": "String (object description)"
}
```

## Changes Made

### Removed Endpoints
The following endpoint was consolidated:

1. **`GET /result/{discoveryId}`** - Removed because it was identical to `/status/{discoveryId}`. The status endpoint now provides both status and results.

### Simplified Design
- **Single Status Endpoint**: Combined status and result retrieval into one endpoint
- **Utility Functions**: Separate endpoints for ping and SNMP operations
- **Real-time Updates**: WebSocket integration for live discovery progress

## Discovery Process Flow

1. **Initiation**: User submits a discovery request with network range and options
2. **Queuing**: Request is queued and assigned a unique discovery ID
3. **Processing**: Discovery engine performs:
   - Network range analysis
   - ICMP ping scanning (if enabled)
   - Port scanning (if enabled)
   - SNMP discovery (if enabled)
   - Device fingerprinting
   - Topology mapping
4. **Progress Updates**: Real-time updates via WebSocket
5. **Completion**: Final topology result is available via status endpoint

## Real-time Updates

Discovery progress can be monitored in real-time using WebSocket:

```javascript
const socket = new SockJS('/websocket');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function() {
    stompClient.subscribe('/topic/discovery/' + discoveryId + '/progress', function(message) {
        const status = JSON.parse(message.body);
        console.log('Discovery progress:', status.progress + '%');
    });
});
```

## Usage Examples

### Start a comprehensive network discovery
```bash
curl -X POST "http://localhost:8080/api/discovery/start" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "name": "Main Office Network",
    "networkRange": "192.168.1.0/24",
    "enablePing": true,
    "enableSnmp": true,
    "enablePortScan": false,
    "discoverInterfaces": true,
    "discoverRoutes": true,
    "snmpCommunity": "public",
    "maxThreads": 10
  }'
```

### Check discovery status
```bash
curl -X GET "http://localhost:8080/api/discovery/status/disc_12345" \
  -H "Authorization: Bearer <token>"
```

### Test connectivity to a device
```bash
curl -X POST "http://localhost:8080/api/discovery/ping?ipAddress=192.168.1.1&timeout=3000" \
  -H "Authorization: Bearer <token>"
```

### Perform SNMP walk
```bash
curl -X POST "http://localhost:8080/api/discovery/snmp-walk?ipAddress=192.168.1.1&community=public&oid=1.3.6.1.2.1.2" \
  -H "Authorization: Bearer <token>"
```

### Cancel an ongoing discovery
```bash
curl -X POST "http://localhost:8080/api/discovery/cancel/disc_12345" \
  -H "Authorization: Bearer <token>"
```

## Integration with Device Management

Discovered devices can be automatically:
- Added to the user's device inventory
- Configured for ongoing monitoring
- Associated with appropriate device types
- Scheduled for regular SNMP polling

The discovery results provide sufficient information to populate device records with network configuration, SNMP settings, and initial status information.
