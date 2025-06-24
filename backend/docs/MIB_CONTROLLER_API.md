# MIB Controller API Documentation

## Overview

The MIB Controller provides RESTful endpoints for managing SNMP MIB (Management Information Base) files and objects. It supports MIB file upload, parsing, browsing, and SNMP operations for testing OIDs against network devices.

## Base URL
`/api/mib`

## Authentication
All endpoints require authentication. MIB files and operations are scoped to the authenticated user.

## Endpoints

### MIB File Management

#### 1. Upload MIB File
- **Method**: `POST`
- **Path**: `/api/mib/files/upload`
- **Summary**: Upload MIB file
- **Description**: Upload and parse a MIB file for use in SNMP operations
- **Request**: Multipart form data with file
- **Response**: `MibFileDto`
- **Status Codes**:
  - `201 Created`: MIB file uploaded successfully
  - `400 Bad Request`: Invalid file or parsing error
  - `401 Unauthorized`: User not authenticated
  - `409 Conflict`: File already exists

**Example Request**:
```bash
curl -X POST "http://localhost:8080/api/mib/files/upload" \
  -H "Authorization: Bearer <token>" \
  -F "file=@RFC1213-MIB.txt"
```

**Example Response**:
```json
{
  "id": 1,
  "filename": "RFC1213-MIB.txt",
  "originalFilename": "RFC1213-MIB.txt",
  "mibName": "RFC1213-MIB",
  "version": "1.0",
  "description": "MIB objects for network management",
  "objectCount": 156,
  "uploadDate": "2024-01-01T10:00:00",
  "fileSize": 25600,
  "status": "PARSED"
}
```

#### 2. Get MIB Files
- **Method**: `GET`
- **Path**: `/api/mib/files`
- **Summary**: Get MIB files
- **Description**: Get all MIB files for the authenticated user
- **Response**: `List<MibFileDto>`
- **Status Codes**:
  - `200 OK`: MIB files retrieved successfully

#### 3. Get MIB File by ID
- **Method**: `GET`
- **Path**: `/api/mib/files/{id}`
- **Summary**: Get MIB file by ID
- **Description**: Get a specific MIB file by its ID
- **Path Parameters**: `id` (Long) - MIB file ID
- **Response**: `MibFileDto`
- **Status Codes**:
  - `200 OK`: MIB file found
  - `404 Not Found`: MIB file not found

#### 4. Delete MIB File
- **Method**: `DELETE`
- **Path**: `/api/mib/files/{id}`
- **Summary**: Delete MIB file
- **Description**: Delete a MIB file and all its associated objects
- **Path Parameters**: `id` (Long) - MIB file ID
- **Response**: No content
- **Status Codes**:
  - `204 No Content`: MIB file deleted successfully
  - `404 Not Found`: MIB file not found

### MIB Object Navigation

#### 5. Get MIB Tree
- **Method**: `GET`
- **Path**: `/api/mib/tree`
- **Summary**: Get MIB tree
- **Description**: Get the complete MIB object tree for the user (all loaded MIB files)
- **Response**: `List<MibObjectDto>`
- **Status Codes**:
  - `200 OK`: MIB tree retrieved successfully

#### 6. Get MIB Tree by File
- **Method**: `GET`
- **Path**: `/api/mib/tree/file/{fileId}`
- **Summary**: Get MIB tree by file
- **Description**: Get MIB object tree for a specific MIB file
- **Path Parameters**: `fileId` (Long) - MIB file ID
- **Response**: `List<MibObjectDto>`
- **Status Codes**:
  - `200 OK`: MIB tree retrieved successfully
  - `404 Not Found`: MIB file not found

#### 7. Get MIB Object by OID
- **Method**: `GET`
- **Path**: `/api/mib/objects/oid/{oid}`
- **Summary**: Get MIB object by OID
- **Description**: Get a specific MIB object by its Object Identifier (OID)
- **Path Parameters**: `oid` (String) - Object Identifier (e.g., "1.3.6.1.2.1.1.1")
- **Response**: `MibObjectDto`
- **Status Codes**:
  - `200 OK`: MIB object found
  - `404 Not Found`: MIB object not found

#### 8. Search MIB Objects
- **Method**: `GET`
- **Path**: `/api/mib/objects/search`
- **Summary**: Search MIB objects
- **Description**: Search MIB objects by name or OID pattern
- **Query Parameters**: `query` (String) - Search term
- **Response**: `List<MibObjectDto>`
- **Status Codes**:
  - `200 OK`: Search completed successfully

### SNMP Operations

#### 9. Browse SNMP OID
- **Method**: `POST`
- **Path**: `/api/mib/browse`
- **Summary**: Browse SNMP OID
- **Description**: Test a specific OID against a target device using SNMP GET
- **Request Body**: `MibBrowserRequest` (JSON)
- **Response**: `MibBrowserResponse`
- **Status Codes**:
  - `200 OK`: SNMP query completed
  - `400 Bad Request`: Invalid request parameters

**Example Request**:
```json
{
  "targetIp": "192.168.1.1",
  "community": "public",
  "oid": "1.3.6.1.2.1.1.1.0",
  "snmpPort": 161,
  "timeout": 5000,
  "retries": 3
}
```

**Example Response**:
```json
{
  "oid": "1.3.6.1.2.1.1.1.0",
  "name": "sysDescr",
  "value": "Cisco IOS Software, C4500 Software",
  "type": "OCTET_STRING",
  "description": "A textual description of the entity",
  "success": true,
  "errorMessage": null,
  "responseTime": 45
}
```

#### 10. Walk SNMP OID Tree
- **Method**: `POST`
- **Path**: `/api/mib/walk`
- **Summary**: Walk SNMP OID tree
- **Description**: Walk the OID tree starting from the specified OID using SNMP GETNEXT
- **Request Body**: `MibBrowserRequest` (JSON)
- **Response**: `List<MibBrowserResponse>`
- **Status Codes**:
  - `200 OK`: SNMP walk completed
  - `400 Bad Request`: Invalid request parameters

## Data Models

### MibFileDto
```json
{
  "id": "Long (read-only)",
  "filename": "String",
  "originalFilename": "String",
  "mibName": "String",
  "version": "String",
  "description": "String",
  "objectCount": "Integer",
  "uploadDate": "LocalDateTime (read-only)",
  "fileSize": "Long (bytes)",
  "status": "String (UPLOADED, PARSING, PARSED, ERROR)"
}
```

### MibObjectDto
```json
{
  "id": "Long",
  "oid": "String (Object Identifier)",
  "name": "String (object name)",
  "description": "String",
  "syntax": "String (SNMP data type)",
  "access": "String (read-only, read-write, etc.)",
  "status": "String (mandatory, optional, etc.)",
  "parent": "String (parent OID)",
  "children": "List<MibObjectDto> (child objects)",
  "mibFileName": "String"
}
```

### MibBrowserRequest
```json
{
  "targetIp": "String (required) - Target device IP",
  "community": "String (required) - SNMP community",
  "oid": "String (required) - Object Identifier",
  "snmpPort": "Integer (optional, default: 161)",
  "timeout": "Integer (optional, default: 5000) - milliseconds",
  "retries": "Integer (optional, default: 3)"
}
```

### MibBrowserResponse
```json
{
  "oid": "String - Object Identifier",
  "name": "String - Object name (if known)",
  "value": "String - Retrieved value",
  "type": "String - SNMP data type",
  "description": "String - Object description",
  "success": "Boolean - Operation success",
  "errorMessage": "String (optional) - Error details",
  "responseTime": "Long - Response time in milliseconds"
}
```

## Usage Examples

### Upload a MIB file
```bash
curl -X POST "http://localhost:8080/api/mib/files/upload" \
  -H "Authorization: Bearer <token>" \
  -F "file=@SNMPv2-MIB.txt"
```

### Browse the MIB tree
```bash
curl -X GET "http://localhost:8080/api/mib/tree" \
  -H "Authorization: Bearer <token>"
```

### Search for MIB objects
```bash
curl -X GET "http://localhost:8080/api/mib/objects/search?query=sysName" \
  -H "Authorization: Bearer <token>"
```

### Test an OID against a device
```bash
curl -X POST "http://localhost:8080/api/mib/browse" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "targetIp": "192.168.1.1",
    "community": "public",
    "oid": "1.3.6.1.2.1.1.5.0"
  }'
```

### Walk an OID tree
```bash
curl -X POST "http://localhost:8080/api/mib/walk" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "targetIp": "192.168.1.1",
    "community": "public",
    "oid": "1.3.6.1.2.1.2.2.1"
  }'
```

## MIB File Format Support

### Supported Formats
- Standard SMI (Structure of Management Information) format
- RFC-compliant MIB files
- Text-based MIB definitions

### Common MIB Files
- **RFC1213-MIB**: Basic MIB-II objects
- **SNMPv2-MIB**: SNMPv2 specific objects  
- **IF-MIB**: Interface objects
- **HOST-RESOURCES-MIB**: Host system resources
- **Vendor-specific MIBs**: Cisco, HP, Juniper, etc.

## OID Tree Navigation

### Standard OID Branches
- **1.3.6.1.1**: Experimental
- **1.3.6.1.2.1**: MIB-II (standard objects)
- **1.3.6.1.4.1**: Enterprise (vendor-specific)
- **1.3.6.1.6**: SNMPv2

### Common System Objects
- **1.3.6.1.2.1.1.1.0**: sysDescr (system description)
- **1.3.6.1.2.1.1.3.0**: sysUpTime (system uptime)
- **1.3.6.1.2.1.1.5.0**: sysName (system name)
- **1.3.6.1.2.1.1.6.0**: sysLocation (system location)

## Error Handling

### File Upload Errors
- **Invalid file format**: MIB parsing failed
- **Duplicate file**: File already exists
- **Large file size**: File exceeds size limits

### SNMP Operation Errors
- **Timeout**: Device not responding
- **Invalid community**: Authentication failed
- **No such object**: OID not supported by device
- **Network unreachable**: Connectivity issues

### Common Error Response
```json
{
  "error": "SNMP_ERROR",
  "message": "Timeout waiting for response",
  "details": {
    "targetIp": "192.168.1.1",
    "oid": "1.3.6.1.2.1.1.1.0",
    "timeout": 5000
  }
}
```

## Integration with Device Management

### MIB Browser Benefits
- **Device Discovery**: Use standard MIBs to discover device capabilities
- **Monitoring Setup**: Identify available OIDs for monitoring
- **Troubleshooting**: Test SNMP connectivity and object availability
- **Custom Metrics**: Define custom monitoring based on enterprise MIBs

### Workflow Integration
1. Upload relevant MIB files for your network devices
2. Browse the MIB tree to understand available objects
3. Test OIDs against actual devices using the browser
4. Configure monitoring and alerting based on verified OIDs
5. Use discovered information in device management workflows
