# Dashboard/Overview Page

This document describes the enhanced Dashboard/Overview page that provides comprehensive network monitoring and alert management capabilities.

## Features

### 1. Statistics Overview

- **Total Alerts**: Shows the total number of alerts with breakdown by severity
- **Active Devices**: Displays count of active devices vs total devices
- **System Health**: Shows overall system health percentage and error count
- **Recent Alerts**: Real-time notification count

### 2. Recent Alerts Section

- Displays the 5 most recent alerts from the system
- Shows alert severity, title, description, and timestamp
- Color-coded badges for different alert statuses (NEW, ACKNOWLEDGED, RESOLVED)
- Auto-refreshes every 15 seconds

### 3. Real-time Notifications

- WebSocket-based real-time alert notifications
- Displays alerts as they occur in the system
- Shows severity, title, description, and timestamp
- Maintains last 10 real-time notifications

### 4. Network Devices Table

- Comprehensive device list with pagination
- Shows device name, type, status, uptime, last monitored time, and monitoring status
- Status indicators with icons and color-coded badges
- Pagination controls for large device lists
- Auto-refreshes every minute

## Authentication

**All dashboard API endpoints require authentication.** The dashboard automatically:

- ✅ **Includes Bearer Token**: All API requests include the JWT token from localStorage
- ✅ **Handles Auth Errors**: Automatically redirects to login on authentication failures
- ✅ **WebSocket Auth**: Includes token in WebSocket connection URL
- ✅ **Token Validation**: Checks for token existence before making requests
- ✅ **Error Recovery**: Clears invalid tokens and redirects to login

### Authentication Flow

1. User logs in and receives JWT token
2. Token is stored in localStorage as `auth_token`
3. All API requests include `Authorization: Bearer <token>` header
4. WebSocket connections include token as URL parameter
5. On authentication errors (401/403), user is redirected to login

## API Endpoints

The dashboard integrates with the following backend API endpoints:

### 1. Alert Statistics

```
GET /api/alerts/statistics
Authorization: Bearer <jwt_token>
```

**Response:**

```json
{
  "total": 24,
  "critical": 3,
  "warning": 8,
  "info": 13,
  "resolved": 18,
  "acknowledged": 4
}
```

### 2. Device List with Pagination

```
GET /api/devices?page=0&size=10
Authorization: Bearer <jwt_token>
```

**Response:**

```json
{
  "content": [
    {
      "id": 1,
      "name": "Core Router - HQ",
      "description": "Main core router for headquarters",
      "systemObjectId": "1.3.6.1.4.1.9.1.1234",
      "systemUptime": 86400000,
      "systemContact": "admin@company.com",
      "systemName": "HQ-CORE-01",
      "systemLocation": "Server Room A",
      "systemServices": 6,
      "lastMonitored": "2024-01-15T10:00:00Z",
      "monitoringEnabled": true,
      "status": "ACTIVE",
      "type": "ROUTER",
      "createdAt": "2024-01-15T10:00:00Z",
      "updatedAt": "2024-01-15T10:00:00Z"
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "size": 10,
  "number": 0,
  "first": true,
  "last": true
}
```

### 3. Recent Alerts

```
GET /api/alerts/recent?limit=5
Authorization: Bearer <jwt_token>
```

**Response:**

```json
[
  {
    "id": 1,
    "type": "DEVICE_DOWN",
    "severity": "CRITICAL",
    "status": "NEW",
    "title": "Web Server - Production is down",
    "description": "The production web server has stopped responding to SNMP queries",
    "sourceId": 3,
    "sourceType": "DEVICE",
    "sourceDescription": "Web Server - Production",
    "details": "SNMP timeout after 3 retries",
    "createdAt": "2024-01-15T09:55:00Z",
    "updatedAt": "2024-01-15T09:55:00Z"
  }
]
```

### 4. WebSocket Real-time Alerts

```
WebSocket: /topic/alerts?token=<jwt_token>
```

**Message Format:**

```json
{
  "type": "ALERT",
  "payload": {
    "id": 6,
    "type": "MEMORY_USAGE_HIGH",
    "severity": "WARNING",
    "title": "High memory usage on database server",
    "description": "Memory usage has exceeded 90% threshold",
    "sourceId": 7,
    "sourceType": "DEVICE",
    "createdAt": "2024-01-15T09:59:30Z"
  },
  "timestamp": "2024-01-15T09:59:30Z"
}
```

## Components and Hooks

### UI Components

- **Table**: For displaying device lists with pagination
- **Badge**: For status and severity indicators
- **Card**: For organizing dashboard sections
- **Button**: For pagination and actions

### Custom Hooks

- `useAlertStatistics()`: Fetches and caches alert statistics
- `useDevices(page, size)`: Fetches device list with pagination
- `useRecentAlerts(limit)`: Fetches recent alerts
- `useRealTimeAlerts()`: Manages real-time WebSocket notifications
- `useDeviceStatusSummary()`: Calculates device status summary

### API Utilities

- `api.getAlertStatistics()`: Fetches alert statistics
- `api.getDevices(page, size)`: Fetches device list
- `api.getRecentAlerts(limit)`: Fetches recent alerts
- `connectToAlerts()`: Connects to WebSocket for real-time alerts

## Error Handling

### Authentication Errors

- **401 Unauthorized**: Token invalid/expired → Redirect to login
- **403 Forbidden**: Insufficient permissions → Show access denied message
- **Missing Token**: No token found → Redirect to login

### Network Errors

- **Connection Failed**: Show error message to user
- **WebSocket Disconnect**: Automatic reconnection with exponential backoff
- **API Timeout**: Retry with exponential backoff (max 3 attempts)

### User Experience

- **Loading States**: Spinner indicators during data fetching
- **Empty States**: Helpful messages when no data is available
- **Error Messages**: Clear, actionable error messages

## Environment Configuration

### Environment Variables

```bash
# API Configuration
VITE_API_BASE_URL=http://localhost:8080/api
VITE_WS_BASE_URL=ws://localhost:8080
```

### Backend Requirements

- Spring Boot backend running on `http://localhost:8080`
- JWT authentication configured
- CORS enabled for frontend origin
- WebSocket support for real-time alerts

## Styling and Theming

The dashboard uses:

- **Tailwind CSS** for styling
- **shadcn/ui** components for consistent design
- **Lucide React** icons for visual elements
- **CSS Variables** for theme support (light/dark mode)

## Performance Optimizations

- **React Query**: Caching and background refetching
- **Pagination**: Efficient loading of large device lists
- **Debounced Updates**: Prevents excessive re-renders
- **Memoization**: Optimized component rendering
- **Token Caching**: JWT token stored in localStorage for persistence

## Security Considerations

- **Token Storage**: JWT tokens stored in localStorage (consider httpOnly cookies for production)
- **Token Validation**: Automatic validation and cleanup of invalid tokens
- **HTTPS**: All API calls should use HTTPS in production
- **CORS**: Backend should be configured with appropriate CORS policies
- **Token Expiry**: Automatic logout on token expiration

## Future Enhancements

Potential improvements for the dashboard:

1. **Filtering and Search**: Add filters for devices and alerts
2. **Export Functionality**: Export device lists and alert reports
3. **Customizable Layout**: Allow users to rearrange dashboard sections
4. **Alert Actions**: Acknowledge and resolve alerts directly from dashboard
5. **Device Details**: Click to view detailed device information
6. **Charts and Graphs**: Visual representation of statistics
7. **Notification Settings**: Configure alert notification preferences
8. **Token Refresh**: Automatic token refresh before expiration
9. **Role-based Access**: Different dashboard views based on user roles
10. **Audit Logging**: Track user actions and system events
