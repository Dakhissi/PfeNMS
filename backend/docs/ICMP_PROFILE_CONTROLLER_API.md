# ICMP Profile Controller API Documentation

## Overview
The ICMP Profile Controller provides REST endpoints for managing ICMP profiles associated with network devices. Each device typically has one ICMP profile containing comprehensive ICMP statistics for monitoring network layer performance.

## Base URL
`/api/icmp-profiles`

## Swagger Group
Device Sub-Components API

## Endpoints

### 1. Create ICMP Profile
**POST** `/api/icmp-profiles`

Creates a new ICMP profile for a device.

**Request Body:**
```json
{
  "deviceId": 1,
  "icmpInMsgs": 1500,
  "icmpInErrors": 3,
  "icmpInDestUnreachs": 5,
  "icmpInTimeExcds": 2,
  "icmpInParmProbs": 0,
  "icmpInSrcQuenchs": 1,
  "icmpInRedirects": 0,
  "icmpInEchos": 100,
  "icmpInEchoReps": 95,
  "icmpInTimestamps": 0,
  "icmpInTimestampReps": 0,
  "icmpInAddrMasks": 0,
  "icmpInAddrMaskReps": 0,
  "icmpOutMsgs": 1200,
  "icmpOutErrors": 2,
  "icmpOutDestUnreachs": 3,
  "icmpOutTimeExcds": 1,
  "icmpOutParmProbs": 0,
  "icmpOutSrcQuenchs": 0,
  "icmpOutRedirects": 0,
  "icmpOutEchos": 95,
  "icmpOutEchoReps": 100,
  "icmpOutTimestamps": 0,
  "icmpOutTimestampReps": 0,
  "icmpOutAddrMasks": 0,
  "icmpOutAddrMaskReps": 0
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "deviceId": 1,
  "deviceName": "Router-01",
  "icmpInMsgs": 1500,
  "icmpInErrors": 3,
  "icmpInDestUnreachs": 5,
  "icmpInTimeExcds": 2,
  "icmpInParmProbs": 0,
  "icmpInSrcQuenchs": 1,
  "icmpInRedirects": 0,
  "icmpInEchos": 100,
  "icmpInEchoReps": 95,
  "icmpInTimestamps": 0,
  "icmpInTimestampReps": 0,
  "icmpInAddrMasks": 0,
  "icmpInAddrMaskReps": 0,
  "icmpOutMsgs": 1200,
  "icmpOutErrors": 2,
  "icmpOutDestUnreachs": 3,
  "icmpOutTimeExcds": 1,
  "icmpOutParmProbs": 0,
  "icmpOutSrcQuenchs": 0,
  "icmpOutRedirects": 0,
  "icmpOutEchos": 95,
  "icmpOutEchoReps": 100,
  "icmpOutTimestamps": 0,
  "icmpOutTimestampReps": 0,
  "icmpOutAddrMasks": 0,
  "icmpOutAddrMaskReps": 0,
  "createdAt": "2025-06-24T10:30:00",
  "updatedAt": "2025-06-24T10:30:00"
}
```

### 2. Update ICMP Profile
**PUT** `/api/icmp-profiles/{id}`

Updates an existing ICMP profile.

**Parameters:**
- `id` (path): ICMP profile ID

**Request Body:** Same as create (partial updates supported)

**Response:** `200 OK` with updated ICMP profile data

### 3. Delete ICMP Profile
**DELETE** `/api/icmp-profiles/{id}`

Deletes an ICMP profile.

**Parameters:**
- `id` (path): ICMP profile ID

**Response:** `204 No Content`

### 4. Get ICMP Profile by ID
**GET** `/api/icmp-profiles/{id}`

Retrieves a specific ICMP profile by ID.

**Parameters:**
- `id` (path): ICMP profile ID

**Response:** `200 OK` with ICMP profile data

### 5. Get ICMP Profiles by Device
**GET** `/api/icmp-profiles/device/{deviceId}`

Retrieves all ICMP profiles for a specific device.

**Parameters:**
- `deviceId` (path): Device ID

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "deviceId": 1,
    "deviceName": "Router-01",
    "icmpInMsgs": 1500,
    "icmpOutMsgs": 1200,
    "icmpInErrors": 3,
    "icmpOutErrors": 2,
    // ... other fields
    "createdAt": "2025-06-24T10:30:00",
    "updatedAt": "2025-06-24T10:35:00"
  }
]
```

### 6. Get ICMP Profile Count
**GET** `/api/icmp-profiles/device/{deviceId}/count`

Returns the count of ICMP profiles for a device.

**Parameters:**
- `deviceId` (path): Device ID

**Response:** `200 OK`
```json
1
```

## Data Model

### ICMP Profile Fields

#### Incoming ICMP Statistics
- `icmpInMsgs`: Total ICMP messages received
- `icmpInErrors`: ICMP messages with errors
- `icmpInDestUnreachs`: Destination unreachable messages received
- `icmpInTimeExcds`: Time exceeded messages received
- `icmpInParmProbs`: Parameter problem messages received
- `icmpInSrcQuenchs`: Source quench messages received
- `icmpInRedirects`: Redirect messages received
- `icmpInEchos`: Echo request messages received
- `icmpInEchoReps`: Echo reply messages received
- `icmpInTimestamps`: Timestamp request messages received
- `icmpInTimestampReps`: Timestamp reply messages received
- `icmpInAddrMasks`: Address mask request messages received
- `icmpInAddrMaskReps`: Address mask reply messages received

#### Outgoing ICMP Statistics
- `icmpOutMsgs`: Total ICMP messages sent
- `icmpOutErrors`: ICMP messages with errors sent
- `icmpOutDestUnreachs`: Destination unreachable messages sent
- `icmpOutTimeExcds`: Time exceeded messages sent
- `icmpOutParmProbs`: Parameter problem messages sent
- `icmpOutSrcQuenchs`: Source quench messages sent
- `icmpOutRedirects`: Redirect messages sent
- `icmpOutEchos`: Echo request messages sent
- `icmpOutEchoReps`: Echo reply messages sent
- `icmpOutTimestamps`: Timestamp request messages sent
- `icmpOutTimestampReps`: Timestamp reply messages sent
- `icmpOutAddrMasks`: Address mask request messages sent
- `icmpOutAddrMaskReps`: Address mask reply messages sent

#### Metadata
- `id`: Unique identifier
- `deviceId`: Associated device ID (required)
- `deviceName`: Device name (read-only)
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp

## ICMP Message Types Reference

| Type | Description | In Field | Out Field |
|------|-------------|----------|-----------|
| 0 | Echo Reply | icmpInEchoReps | icmpOutEchoReps |
| 3 | Destination Unreachable | icmpInDestUnreachs | icmpOutDestUnreachs |
| 4 | Source Quench | icmpInSrcQuenchs | icmpOutSrcQuenchs |
| 5 | Redirect | icmpInRedirects | icmpOutRedirects |
| 8 | Echo Request | icmpInEchos | icmpOutEchos |
| 11 | Time Exceeded | icmpInTimeExcds | icmpOutTimeExcds |
| 12 | Parameter Problem | icmpInParmProbs | icmpOutParmProbs |
| 13 | Timestamp Request | icmpInTimestamps | icmpOutTimestamps |
| 14 | Timestamp Reply | icmpInTimestampReps | icmpOutTimestampReps |
| 17 | Address Mask Request | icmpInAddrMasks | icmpOutAddrMasks |
| 18 | Address Mask Reply | icmpInAddrMaskReps | icmpOutAddrMaskReps |

## Error Responses

### 400 Bad Request
```json
{
  "message": "Invalid input data",
  "details": ["Device ID is required"]
}
```

### 404 Not Found
```json
{
  "message": "ICMP profile not found"
}
```

## Security
- All endpoints require authentication
- Users can only access ICMP profiles for devices they own
- JWT token required in Authorization header

## Usage Examples

### Create an ICMP Profile
```bash
curl -X POST /api/icmp-profiles \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": 1,
    "icmpInMsgs": 1500,
    "icmpOutMsgs": 1200,
    "icmpInErrors": 3,
    "icmpOutErrors": 2
  }'
```

### Get ICMP Profile for a Device
```bash
curl -X GET /api/icmp-profiles/device/1 \
  -H "Authorization: Bearer your-jwt-token"
```

### Update ICMP Profile
```bash
curl -X PUT /api/icmp-profiles/1 \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "icmpInMsgs": 1600,
    "icmpOutMsgs": 1300
  }'
```

## Monitoring Use Cases

### Network Connectivity Analysis
- Monitor `icmpInEchos` and `icmpOutEchoReps` for ping response patterns
- Track `icmpInDestUnreachs` for unreachable destinations
- Analyze `icmpInTimeExcds` for routing loop detection

### Error Rate Monitoring
- Calculate error rates: `icmpInErrors / icmpInMsgs * 100`
- Compare incoming vs outgoing message ratios
- Identify abnormal ICMP traffic patterns

### Performance Metrics
- Echo request/reply latency analysis
- Network reachability statistics
- Protocol-level health indicators

## Integration with SNMP Polling

The ICMP profiles are automatically populated by the SNMP polling service from the following MIB objects:
- **RFC 1213 MIB**: `1.3.6.1.2.1.5.*` (icmp group)
- **Updates**: Every 30 seconds during device polling
- **Duplicate Prevention**: Automatic deduplication ensures single profile per device
