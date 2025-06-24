# UDP Profile Controller API Documentation

## Overview
The UDP Profile Controller provides REST endpoints for managing UDP profiles associated with network devices. Each device can have multiple UDP profiles representing different UDP connections and statistics.

## Base URL
`/api/udp-profiles`

## Swagger Group
Device Sub-Components API

## Endpoints

### 1. Create UDP Profile
**POST** `/api/udp-profiles`

Creates a new UDP profile for a device.

**Request Body:**
```json
{
  "deviceId": 1,
  "udpInDatagrams": 1000,
  "udpNoPorts": 5,
  "udpInErrors": 2,
  "udpOutDatagrams": 800,
  "udpLocalAddress": "192.168.1.10",
  "udpLocalPort": 161,
  "udpRemoteAddress": "192.168.1.100",
  "udpRemotePort": 2048,
  "udpEntryStatus": "VALID"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "deviceId": 1,
  "deviceName": "Router-01",
  "udpInDatagrams": 1000,
  "udpNoPorts": 5,
  "udpInErrors": 2,
  "udpOutDatagrams": 800,
  "udpLocalAddress": "192.168.1.10",
  "udpLocalPort": 161,
  "udpRemoteAddress": "192.168.1.100",
  "udpRemotePort": 2048,
  "udpEntryStatus": "VALID",
  "createdAt": "2025-06-24T10:30:00",
  "updatedAt": "2025-06-24T10:30:00"
}
```

### 2. Update UDP Profile
**PUT** `/api/udp-profiles/{id}`

Updates an existing UDP profile.

**Parameters:**
- `id` (path): UDP profile ID

**Request Body:** Same as create (partial updates supported)

**Response:** `200 OK` with updated UDP profile data

### 3. Delete UDP Profile
**DELETE** `/api/udp-profiles/{id}`

Deletes a UDP profile.

**Parameters:**
- `id` (path): UDP profile ID

**Response:** `204 No Content`

### 4. Get UDP Profile by ID
**GET** `/api/udp-profiles/{id}`

Retrieves a specific UDP profile by ID.

**Parameters:**
- `id` (path): UDP profile ID

**Response:** `200 OK` with UDP profile data

### 5. Get UDP Profiles by Device
**GET** `/api/udp-profiles/device/{deviceId}`

Retrieves all UDP profiles for a specific device.

**Parameters:**
- `deviceId` (path): Device ID

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "deviceId": 1,
    "deviceName": "Router-01",
    "udpLocalAddress": "192.168.1.10",
    "udpLocalPort": 161,
    // ... other fields
  },
  {
    "id": 2,
    "deviceId": 1,
    "deviceName": "Router-01",
    "udpLocalAddress": "192.168.1.10", 
    "udpLocalPort": 162,
    // ... other fields
  }
]
```

### 6. Get UDP Profiles by Status
**GET** `/api/udp-profiles/device/{deviceId}/status/{status}`

Retrieves UDP profiles filtered by entry status.

**Parameters:**
- `deviceId` (path): Device ID
- `status` (path): UDP entry status (OTHER, INVALID, VALID)

**Response:** `200 OK` with filtered UDP profiles

### 7. Search UDP Profiles by Address
**GET** `/api/udp-profiles/device/{deviceId}/search?address={searchTerm}`

Searches UDP profiles by local address pattern.

**Parameters:**
- `deviceId` (path): Device ID
- `address` (query): Search term for local address

**Response:** `200 OK` with matching UDP profiles

### 8. Get UDP Profile Count
**GET** `/api/udp-profiles/device/{deviceId}/count`

Returns the count of UDP profiles for a device.

**Parameters:**
- `deviceId` (path): Device ID

**Response:** `200 OK`
```json
5
```

### 9. Check UDP Address/Port Existence
**GET** `/api/udp-profiles/device/{deviceId}/exists?localAddress={address}&localPort={port}`

Checks if a UDP local address and port combination exists.

**Parameters:**
- `deviceId` (path): Device ID
- `localAddress` (query): Local IP address
- `localPort` (query): Local port number

**Response:** `200 OK`
```json
true
```

## Data Model

### UDP Profile Fields
- `id`: Unique identifier
- `deviceId`: Associated device ID (required)
- `deviceName`: Device name (read-only)
- `udpInDatagrams`: Number of UDP datagrams received
- `udpNoPorts`: UDP datagrams with no matching port
- `udpInErrors`: UDP input errors
- `udpOutDatagrams`: Number of UDP datagrams sent
- `udpLocalAddress`: Local IP address
- `udpLocalPort`: Local port number
- `udpRemoteAddress`: Remote IP address
- `udpRemotePort`: Remote port number
- `udpEntryStatus`: Entry status (OTHER, INVALID, VALID)
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp

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
  "message": "UDP profile not found"
}
```

### 409 Conflict
```json
{
  "message": "UDP profile with address 192.168.1.10:161 already exists for this device"
}
```

## Security
- All endpoints require authentication
- Users can only access UDP profiles for devices they own
- JWT token required in Authorization header

## Usage Examples

### Create a UDP Profile
```bash
curl -X POST /api/udp-profiles \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": 1,
    "udpLocalAddress": "192.168.1.10",
    "udpLocalPort": 161,
    "udpEntryStatus": "VALID"
  }'
```

### Get All UDP Profiles for a Device
```bash
curl -X GET /api/udp-profiles/device/1 \
  -H "Authorization: Bearer your-jwt-token"
```

### Search UDP Profiles by Address
```bash
curl -X GET "/api/udp-profiles/device/1/search?address=192.168.1" \
  -H "Authorization: Bearer your-jwt-token"
```
