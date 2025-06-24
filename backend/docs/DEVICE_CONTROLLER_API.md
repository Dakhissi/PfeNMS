# Device Controller API Documentation

## Overview

The Device Controller provides RESTful endpoints for managing network devices in the system. It handles device creation, updating, deletion, and retrieval operations, all scoped to the authenticated user.

## Base URL
`/api/devices`

## Authentication
All endpoints require authentication. The authenticated user can only access and manage their own devices.

## Endpoints

### 1. Create Device
- **Method**: `POST`
- **Path**: `/api/devices`
- **Summary**: Create a new device
- **Description**: Creates a new network device for the authenticated user
- **Request Body**: `DeviceDto` (JSON)
- **Response**: `DeviceDto` with assigned ID and timestamps
- **Status Codes**:
  - `201 Created`: Device created successfully
  - `400 Bad Request`: Invalid input data
  - `401 Unauthorized`: User not authenticated
  - `409 Conflict`: Device name already exists

**Example Request**:
```json
{
  "name": "Router-001",
  "description": "Main office router",
  "ipAddress": "192.168.1.1",
  "snmpCommunity": "public",
  "snmpPort": 161,
  "snmpEnabled": true,
  "monitoringEnabled": true,
  "status": "ONLINE",
  "type": "ROUTER"
}
```

### 2. Update Device
- **Method**: `PUT`
- **Path**: `/api/devices/{id}`
- **Summary**: Update a device
- **Description**: Updates an existing device owned by the authenticated user
- **Path Parameters**: `id` (Long) - Device ID
- **Request Body**: `DeviceDto` (JSON)
- **Response**: Updated `DeviceDto`
- **Status Codes**:
  - `200 OK`: Device updated successfully
  - `400 Bad Request`: Invalid input data
  - `401 Unauthorized`: User not authenticated
  - `404 Not Found`: Device not found
  - `409 Conflict`: Device name already exists

### 3. Delete Device
- **Method**: `DELETE`
- **Path**: `/api/devices/{id}`
- **Summary**: Delete a device
- **Description**: Deletes a device owned by the authenticated user
- **Path Parameters**: `id` (Long) - Device ID
- **Response**: No content
- **Status Codes**:
  - `204 No Content`: Device deleted successfully
  - `401 Unauthorized`: User not authenticated
  - `404 Not Found`: Device not found

### 4. Get Device by ID
- **Method**: `GET`
- **Path**: `/api/devices/{id}`
- **Summary**: Get device by ID
- **Description**: Retrieves a specific device owned by the authenticated user
- **Path Parameters**: `id` (Long) - Device ID
- **Response**: `DeviceDto`
- **Status Codes**:
  - `200 OK`: Device found
  - `401 Unauthorized`: User not authenticated
  - `404 Not Found`: Device not found

### 5. Get All Devices (Paginated)
- **Method**: `GET`
- **Path**: `/api/devices`
- **Summary**: Get all devices with pagination
- **Description**: Retrieves all devices owned by the authenticated user with pagination support
- **Query Parameters**: Standard pagination parameters (`page`, `size`, `sort`)
- **Response**: `Page<DeviceDto>`
- **Status Codes**:
  - `200 OK`: Devices retrieved successfully

**Example Response**:
```json
{
  "content": [
    {
      "id": 1,
      "name": "Router-001",
      "description": "Main office router",
      "ipAddress": "192.168.1.1",
      "status": "ONLINE",
      "type": "ROUTER",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### 6. Get Devices by Status
- **Method**: `GET`
- **Path**: `/api/devices/by-status/{status}`
- **Summary**: Get devices by status
- **Description**: Retrieves devices filtered by status
- **Path Parameters**: `status` (DeviceStatus) - Device status (`ONLINE`, `OFFLINE`, `MAINTENANCE`, `ERROR`)
- **Response**: `List<DeviceDto>`
- **Status Codes**:
  - `200 OK`: Devices retrieved successfully

### 7. Get Devices by Type
- **Method**: `GET`
- **Path**: `/api/devices/by-type/{type}`
- **Summary**: Get devices by type
- **Description**: Retrieves devices filtered by type
- **Path Parameters**: `type` (DeviceType) - Device type (`ROUTER`, `SWITCH`, `FIREWALL`, `ACCESS_POINT`, `SERVER`, `WORKSTATION`, `PRINTER`, `OTHER`)
- **Response**: `List<DeviceDto>`
- **Status Codes**:
  - `200 OK`: Devices retrieved successfully

### 8. Search Devices by Name
- **Method**: `GET`
- **Path**: `/api/devices/search`
- **Summary**: Search devices by name
- **Description**: Searches devices by name pattern
- **Query Parameters**: `name` (String) - Search term
- **Response**: `List<DeviceDto>`
- **Status Codes**:
  - `200 OK`: Devices retrieved successfully

### 9. Trigger Device Monitoring
- **Method**: `POST`
- **Path**: `/api/devices/{id}/monitor`
- **Summary**: Trigger device monitoring
- **Description**: Manually triggers monitoring for a specific device
- **Path Parameters**: `id` (Long) - Device ID
- **Response**: String message
- **Status Codes**:
  - `200 OK`: Monitoring triggered successfully
  - `401 Unauthorized`: User not authenticated
  - `404 Not Found`: Device not found

## Data Models

### DeviceDto
```json
{
  "id": "Long (read-only)",
  "name": "String (required, not blank)",
  "description": "String (optional)",
  "systemObjectId": "String (optional)",
  "systemUptime": "Long (optional)",
  "systemContact": "String (optional)",
  "systemName": "String (optional)",
  "systemLocation": "String (optional)",
  "systemServices": "Integer (optional)",
  "ipAddress": "String (optional)",
  "macAddress": "String (optional)",
  "subnetMask": "String (optional)",
  "gateway": "String (optional)",
  "snmpEnabled": "Boolean (optional)",
  "snmpCommunity": "String (optional)",
  "snmpPort": "Integer (optional)",
  "lastMonitored": "LocalDateTime (read-only)",
  "monitoringEnabled": "Boolean (optional)",
  "status": "DeviceStatus (required)",
  "type": "DeviceType (required)",
  "userId": "Long (read-only)",
  "userName": "String (read-only)",
  "createdAt": "LocalDateTime (read-only)",
  "updatedAt": "LocalDateTime (read-only)"
}
```

### DeviceStatus Enum
- `ONLINE`: Device is reachable and functioning
- `OFFLINE`: Device is not reachable
- `MAINTENANCE`: Device is in maintenance mode
- `ERROR`: Device has errors

### DeviceType Enum
- `ROUTER`: Network router
- `SWITCH`: Network switch
- `FIREWALL`: Firewall device
- `ACCESS_POINT`: Wireless access point
- `SERVER`: Server device
- `WORKSTATION`: Client workstation
- `PRINTER`: Network printer
- `OTHER`: Other device type

## Changes Made

### Removed Endpoints
The following endpoints were removed as they were redundant:

1. **`POST /with-config`** - Removed because device configuration is now included directly in `DeviceDto`
2. **`GET /{id}/with-config`** - Removed because device configuration is now included in the standard `GET /{id}` endpoint
3. **`GET /with-config`** - Removed because device configuration is now included in the standard `GET /` endpoint
4. **`GET /paged`** - Removed because pagination is now the default behavior for `GET /`
5. **`GET /with-config/paged`** - Removed because it's redundant with the updated `GET /` endpoint
6. **`GET /count`** - Removed because count information is available in the paginated response
7. **`GET /exists/{name}`** - Removed because this check can be done client-side or during validation

### Simplified Design
- **Single DTO**: Uses only `DeviceDto` which now includes all device information including configuration
- **Pagination Default**: The main `GET /devices` endpoint now uses pagination by default
- **Cleaner API**: Reduced the number of endpoints from 11 to 9, making the API more focused and easier to use

## Usage Examples

### Create a new router
```bash
curl -X POST "http://localhost:8080/api/devices" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "name": "Main-Router",
    "description": "Primary office router",
    "ipAddress": "192.168.1.1",
    "snmpCommunity": "public",
    "snmpPort": 161,
    "snmpEnabled": true,
    "monitoringEnabled": true,
    "status": "ONLINE",
    "type": "ROUTER"
  }'
```

### Get all devices with pagination
```bash
curl -X GET "http://localhost:8080/api/devices?page=0&size=10&sort=name,asc" \
  -H "Authorization: Bearer <token>"
```

### Search devices by name
```bash
curl -X GET "http://localhost:8080/api/devices/search?name=router" \
  -H "Authorization: Bearer <token>"
```

### Trigger monitoring for a device
```bash
curl -X POST "http://localhost:8080/api/devices/1/monitor" \
  -H "Authorization: Bearer <token>"
```
