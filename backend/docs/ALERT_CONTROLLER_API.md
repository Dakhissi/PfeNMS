# Alert Controller API Documentation

## Overview

The Alert Controller provides RESTful endpoints for managing system alerts and notifications. It handles alert retrieval, acknowledgment, resolution, and provides various filtering options, all scoped to the authenticated user.

## Base URL
`/api/alerts`

## Authentication
All endpoints require authentication. The authenticated user can only access and manage their own alerts.

## Endpoints

### 1. Get All Alerts (Paginated)
- **Method**: `GET`
- **Path**: `/api/alerts`
- **Summary**: Get alerts with pagination
- **Description**: Get all alerts for the authenticated user with pagination support
- **Query Parameters**: Standard pagination parameters (`page`, `size`, `sort`)
- **Response**: `Page<AlertDto>`
- **Status Codes**:
  - `200 OK`: Alerts retrieved successfully

**Example Response**:
```json
{
  "content": [
    {
      "id": 1,
      "type": "DEVICE_DOWN",
      "severity": "CRITICAL",
      "title": "Device Offline",
      "message": "Router-001 is not responding",
      "status": "ACTIVE",
      "deviceId": 123,
      "sourceType": "DEVICE",
      "sourceIdentifier": "192.168.1.1",
      "createdAt": "2024-01-01T10:00:00",
      "acknowledgedAt": null,
      "resolvedAt": null
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

### 2. Get Alert by ID
- **Method**: `GET`
- **Path**: `/api/alerts/{id}`
- **Summary**: Get alert by ID
- **Description**: Get a specific alert by its ID
- **Path Parameters**: `id` (Long) - Alert ID
- **Response**: `AlertDto`
- **Status Codes**:
  - `200 OK`: Alert found
  - `404 Not Found`: Alert not found

### 3. Get Alerts by Status
- **Method**: `GET`
- **Path**: `/api/alerts/status/{status}`
- **Summary**: Get alerts by status
- **Description**: Get alerts filtered by status
- **Path Parameters**: `status` (AlertStatus) - Alert status (`ACTIVE`, `ACKNOWLEDGED`, `RESOLVED`, `CLEARED`)
- **Response**: `List<AlertDto>`
- **Status Codes**:
  - `200 OK`: Alerts retrieved successfully

### 4. Get Alerts by Severity
- **Method**: `GET`
- **Path**: `/api/alerts/severity/{severity}`
- **Summary**: Get alerts by severity
- **Description**: Get alerts filtered by severity
- **Path Parameters**: `severity` (AlertSeverity) - Alert severity (`CRITICAL`, `MAJOR`, `MINOR`, `WARNING`, `INFO`)
- **Response**: `List<AlertDto>`
- **Status Codes**:
  - `200 OK`: Alerts retrieved successfully

### 5. Get Unacknowledged Alerts
- **Method**: `GET`
- **Path**: `/api/alerts/unacknowledged`
- **Summary**: Get unacknowledged alerts
- **Description**: Get all unacknowledged alerts
- **Response**: `List<AlertDto>`
- **Status Codes**:
  - `200 OK`: Unacknowledged alerts retrieved successfully

### 6. Get Recent Alerts
- **Method**: `GET`
- **Path**: `/api/alerts/recent`
- **Summary**: Get recent alerts
- **Description**: Get recent alerts since specified time (defaults to last 24 hours)
- **Query Parameters**: `since` (LocalDateTime, optional) - Since timestamp
- **Response**: `List<AlertDto>`
- **Status Codes**:
  - `200 OK`: Recent alerts retrieved successfully

### 7. Acknowledge Alert
- **Method**: `POST`
- **Path**: `/api/alerts/{id}/acknowledge`
- **Summary**: Acknowledge alert
- **Description**: Acknowledge an alert with optional notes
- **Path Parameters**: `id` (Long) - Alert ID
- **Request Body**: `AlertAcknowledgeRequest` (JSON)
- **Response**: Updated `AlertDto`
- **Status Codes**:
  - `200 OK`: Alert acknowledged successfully
  - `404 Not Found`: Alert not found

**Example Request**:
```json
{
  "notes": "Investigating the issue"
}
```

### 8. Resolve Alert
- **Method**: `POST`
- **Path**: `/api/alerts/{id}/resolve`
- **Summary**: Resolve alert
- **Description**: Mark an alert as resolved
- **Path Parameters**: `id` (Long) - Alert ID
- **Response**: Updated `AlertDto`
- **Status Codes**:
  - `200 OK`: Alert resolved successfully
  - `404 Not Found`: Alert not found

### 9. Clear Alert
- **Method**: `DELETE`
- **Path**: `/api/alerts/{id}`
- **Summary**: Clear alert
- **Description**: Clear an alert (mark as cleared)
- **Path Parameters**: `id` (Long) - Alert ID
- **Response**: No content
- **Status Codes**:
  - `204 No Content`: Alert cleared successfully
  - `404 Not Found`: Alert not found

### 10. Get Alert Statistics
- **Method**: `GET`
- **Path**: `/api/alerts/statistics`
- **Summary**: Get alert statistics
- **Description**: Get alert statistics and counts
- **Response**: Statistics object (JSON)
- **Status Codes**:
  - `200 OK`: Alert statistics retrieved successfully

**Example Response**:
```json
{
  "activeCount": 5,
  "criticalCount": 2,
  "unacknowledgedCount": 3
}
```

## Data Models

### AlertDto
```json
{
  "id": "Long (read-only)",
  "type": "AlertType (enum)",
  "severity": "AlertSeverity (enum)",
  "title": "String",
  "message": "String",
  "status": "AlertStatus (enum)",
  "deviceId": "Long (optional)",
  "sourceType": "SourceType (enum)",
  "sourceIdentifier": "String",
  "userId": "Long (read-only)",
  "createdAt": "LocalDateTime (read-only)",
  "acknowledgedAt": "LocalDateTime (optional)",
  "acknowledgedBy": "String (optional)",
  "acknowledgmentNotes": "String (optional)",
  "resolvedAt": "LocalDateTime (optional)",
  "resolvedBy": "String (optional)",
  "clearedAt": "LocalDateTime (optional)"
}
```

### AlertType Enum
- `DEVICE_DOWN`: Device is offline or unreachable
- `DEVICE_UP`: Device is back online
- `INTERFACE_DOWN`: Network interface is down
- `INTERFACE_UP`: Network interface is back up
- `SYSTEM_DOWN`: System or service is down
- `SYSTEM_UP`: System or service is back up
- `PERFORMANCE`: Performance-related alert
- `CONNECTIVITY`: Connectivity issues
- `CONFIGURATION_CHANGED`: Configuration changes detected

### AlertSeverity Enum
- `CRITICAL`: Immediate attention required
- `MAJOR`: Significant impact on operations
- `MINOR`: Minor impact, monitor situation
- `WARNING`: Potential issue, informational
- `INFO`: General information

### AlertStatus Enum
- `ACTIVE`: Alert is active and requires attention
- `ACKNOWLEDGED`: Alert has been acknowledged by a user
- `RESOLVED`: Alert has been resolved
- `CLEARED`: Alert has been cleared

### SourceType Enum
- `DEVICE`: Alert originated from a device
- `SYSTEM`: Alert originated from the system
- `USER`: Alert originated from user action

### AlertAcknowledgeRequest
```json
{
  "notes": "String (optional)"
}
```

## Changes Made

### Removed Endpoints
The following endpoint was consolidated:

1. **`GET /paged`** - Removed because pagination is now the default behavior for `GET /`

### Simplified Design
- **Pagination Default**: The main `GET /alerts` endpoint now uses pagination by default
- **Consistent Filtering**: All filtering endpoints use consistent patterns
- **Clear Actions**: Separate endpoints for acknowledge, resolve, and clear actions

## Usage Examples

### Get all alerts with pagination
```bash
curl -X GET "http://localhost:8080/api/alerts?page=0&size=10&sort=createdAt,desc" \
  -H "Authorization: Bearer <token>"
```

### Get critical alerts only
```bash
curl -X GET "http://localhost:8080/api/alerts/severity/CRITICAL" \
  -H "Authorization: Bearer <token>"
```

### Get unacknowledged alerts
```bash
curl -X GET "http://localhost:8080/api/alerts/unacknowledged" \
  -H "Authorization: Bearer <token>"
```

### Acknowledge an alert
```bash
curl -X POST "http://localhost:8080/api/alerts/1/acknowledge" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "notes": "Investigating the network connectivity issue"
  }'
```

### Resolve an alert
```bash
curl -X POST "http://localhost:8080/api/alerts/1/resolve" \
  -H "Authorization: Bearer <token>"
```

### Get alert statistics
```bash
curl -X GET "http://localhost:8080/api/alerts/statistics" \
  -H "Authorization: Bearer <token>"
```

## Alert Workflow

1. **Creation**: Alerts are automatically created by the monitoring system when issues are detected
2. **Notification**: Users receive real-time notifications via WebSocket
3. **Acknowledgment**: Users can acknowledge alerts to indicate they are aware of the issue
4. **Investigation**: Users can add notes during acknowledgment
5. **Resolution**: Once the issue is fixed, alerts can be marked as resolved
6. **Clearing**: Old or irrelevant alerts can be cleared from the system

## Integration with Device Monitoring

Alerts are automatically generated when:
- Devices go offline or come back online
- Network interfaces change status
- Performance thresholds are exceeded
- SNMP traps are received (via TrapService)
- Configuration changes are detected
