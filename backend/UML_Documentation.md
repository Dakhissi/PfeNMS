# Network Management System - UML Documentation

This directory contains comprehensive UML diagrams for the Network Management System, a Spring Boot application designed for monitoring and managing network devices using SNMP protocols.

## Diagram Files

### 1. Class Diagram (`class_diagram.puml`)
**Purpose**: Shows the complete object-oriented structure of the system including all entities, enums, and their relationships.

**Key Features**:
- **User Management**: User entity with role-based access
- **Device Management**: Device, DeviceConfig, and DeviceInterface entities with comprehensive SNMP support
- **Monitoring Profiles**: IpProfile, IcmpProfile, and UdpProfile for different monitoring types
- **Alert System**: Alert entity with severity levels and status tracking
- **SNMP Trap Management**: TrapEvent and TrapVariable for handling SNMP traps
- **MIB Management**: MibFile and MibObject for MIB browser functionality

**Relationships**:
- One-to-many relationships between User and Devices
- One-to-one relationship between Device and DeviceConfig
- One-to-many relationships for device components (interfaces, system units, profiles)
- Proper separation of concerns with dedicated configuration entities

### 2. Use Case Diagram (`use_case_diagram.puml`)
**Purpose**: Illustrates the functional requirements and interactions between different user types and the system.

**Actors**:
- **System Administrator**: Full system access and configuration
- **Network Operator**: Device monitoring and alert management
- **Network Device**: External devices that send traps and respond to polls
- **SNMP Agent**: Software agents running on network devices
- **Monitoring System**: Internal automated monitoring components

**Use Case Categories**:
- User Management (registration, authentication, profile management)
- Device Management (CRUD operations, configuration, monitoring)
- Network Discovery (device discovery, auto-configuration)
- SNMP Monitoring (polling, data collection, status tracking)
- Profile Management (ICMP, UDP, IP profile configuration)
- Alert Management (generation, acknowledgment, resolution)
- SNMP Trap Management (receiving, processing, alert correlation)
- MIB Management (file upload, browsing, object querying)
- Reporting & Analytics (dashboards, reports, data export)
- System Configuration (SNMP settings, polling intervals, backup/restore)

### 3. Architecture Diagram (`architecture_diagram.puml`)
**Purpose**: Shows the high-level system architecture including layers, components, and external integrations.

**Architecture Layers**:
- **Client Layer**: Web browsers, mobile apps, API clients
- **Presentation Layer**: REST controllers for different functional areas
- **Security Layer**: JWT authentication, authorization, user management
- **Service Layer**: Business logic organized into core, SNMP, and monitoring services
- **Data Access Layer**: JPA repositories for database operations
- **Database Layer**: PostgreSQL with properly normalized tables

**External Integrations**:
- Network devices via SNMP protocols
- Email server for alert notifications
- File system for MIB file storage
- Scheduled tasks for automated polling

### 4. SNMP Polling Sequence Diagram (`snmp_polling_sequence.puml`)
**Purpose**: Details the complete SNMP polling and monitoring workflow.

**Key Processes**:
- **Scheduled Polling**: 30-second intervals for device monitoring
- **Device Reachability**: SNMP connectivity testing
- **System Information Polling**: Collection of system MIB data
- **Interface Polling**: Interface statistics and status monitoring
- **Status Management**: Device status updates and alert generation
- **Trap Processing**: Asynchronous SNMP trap handling

**Error Handling**:
- Device unreachability detection
- Automatic status transitions
- Alert generation for status changes
- Duplicate prevention mechanisms

### 5. Network Topology Discovery Sequence Diagram (`topology_discovery_sequence.puml`)
**Purpose**: Shows the complete network topology discovery process with real-time WebSocket updates.

**Discovery Phases**:
- **Phase 1: Initial Network Scan**: ICMP ping and Nmap port scanning to discover live hosts
- **Phase 2: SNMP Discovery**: Multi-hop SNMP scanning to gather device details and discover neighbors
- **Phase 3: Topology Building**: Analysis of connections and creation of network topology graph

**Key Features**:
- **Asynchronous Processing**: Discovery runs in background with real-time progress updates
- **WebSocket Communication**: Real-time progress updates to frontend via `/topic/discovery/{id}/progress`
- **Multi-threaded Scanning**: Parallel device discovery with configurable thread count
- **Progressive Discovery**: Hop-by-hop neighbor discovery using LLDP/CDP tables
- **Comprehensive SNMP Queries**: System info, interface tables, and neighbor discovery
- **Error Resilience**: Graceful handling of SNMP timeouts and unreachable devices
- **Cancellation Support**: User can cancel ongoing discovery via WebSocket

**Frontend Integration**:
- Axios HTTP requests for discovery initiation and result retrieval
- WebSocket connection for real-time progress updates
- Support for discovery cancellation and error handling

### 6. Database ERD (`database_erd.puml`)
**Purpose**: Shows the complete database schema with tables, relationships, and constraints.

**Key Tables**:
- **users**: User authentication and profile information
- **devices**: Core device information and system properties
- **device_configs**: SNMP configuration separated from device data
- **device_interfaces**: Interface statistics following MIB-2 ifTable structure
- **alerts**: Comprehensive alert management with status tracking
- **trap_events**: SNMP trap storage with variable bindings
- **mib_files/mib_objects**: MIB browser functionality

**Database Features**:
- Proper normalization to 3NF
- Foreign key relationships with cascading
- Indexes for performance optimization
- String length considerations for SNMP data
- Timestamp tracking for audit trails

### 7. Component Diagram (`component_diagram.puml`)
**Purpose**: Illustrates the modular architecture and component interactions.

**Component Categories**:
- **Web Layer**: REST controllers, security filters, exception handling
- **Service Layer**: Organized into core, SNMP, discovery, and monitoring services
- **Data Access Layer**: JPA repositories, entity mappers, validation
- **Infrastructure Layer**: Database, security, scheduling, events, file management
- **External Interfaces**: SNMP protocol, email, file system, network interfaces

**Cross-cutting Concerns**:
- Logging with SLF4J and Logback
- Metrics collection with Micrometer
- Caching with Spring Cache
- Transaction management
- Comprehensive error handling and validation

## How to View the Diagrams

### Option 1: PlantUML Online Server
1. Copy the content of any `.puml` file
2. Go to [PlantUML Online Server](http://www.plantuml.com/plantuml/uml/)
3. Paste the content and view the rendered diagram

### Option 2: VS Code with PlantUML Extension
1. Install the "PlantUML" extension in VS Code
2. Open any `.puml` file
3. Use `Ctrl+Shift+P` and select "PlantUML: Preview Current Diagram"

### Option 3: Local PlantUML Installation
1. Install Java and PlantUML
2. Run: `java -jar plantuml.jar diagram_name.puml`
3. This generates PNG/SVG files

## System Overview

The Network Management System is a comprehensive SNMP-based network monitoring solution built with Spring Boot. It provides:

### Core Functionality
- **Device Discovery**: Automatic network device discovery using SNMP and network scanning
- **Real-time Monitoring**: Continuous SNMP polling for device status and performance metrics
- **Alert Management**: Intelligent alert generation, notification, and resolution tracking
- **SNMP Trap Handling**: Asynchronous trap reception and processing
- **MIB Browser**: Complete MIB file management and object browsing capabilities

### Technical Features
- **Multi-tenancy**: User-based device isolation and access control
- **Scalable Architecture**: Modular design supporting horizontal scaling
- **Performance Optimization**: Efficient polling with duplicate prevention
- **Robust Error Handling**: Comprehensive error detection and recovery
- **Security**: JWT-based authentication with role-based authorization
- **Database Design**: Normalized schema with proper indexing for performance

### Monitoring Capabilities
- **Device Status**: Continuous availability monitoring
- **Interface Statistics**: Detailed network interface metrics following MIB-2 standards
- **System Information**: Complete system inventory and configuration tracking
- **Custom Profiles**: ICMP ping, UDP port testing, and IP connectivity monitoring
- **Historical Data**: Complete audit trail and historical performance data

This documentation provides a complete view of the system architecture, making it easy for developers to understand the codebase structure, database design, and operational workflows.
