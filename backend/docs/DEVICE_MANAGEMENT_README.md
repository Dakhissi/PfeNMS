# Device Management System API

## Overview

This is a comprehensive device management system built with Spring Boot that provides SNMP MIB-2 style network device monitoring and management capabilities. The system includes authentication-based device ownership, where each user can manage their own set of network devices.

## Features

### 🔐 Authentication & Authorization
- JWT-based authentication
- User-specific device ownership
- Role-based access control (USER, ADMIN)

### 📟 Device Management
- **Devices**: Core network devices (routers, switches, servers, etc.)
- **Device Interfaces**: Network interface management with MIB-2 compliance
- **System Units**: Hardware components and modules
- **IP Profiles**: IP layer statistics and configuration
- **ICMP Profiles**: ICMP protocol statistics
- **UDP Profiles**: UDP protocol statistics and configuration

### 🛠️ API Features
- RESTful API design
- Comprehensive Swagger/OpenAPI documentation
- Data validation and error handling
- Pagination support
- Search and filtering capabilities
- Full CRUD operations for all entities

## Technology Stack

- **Framework**: Spring Boot 3.5.0
- **Security**: Spring Security + JWT
- **Database**: PostgreSQL (production), H2 (testing)
- **ORM**: Spring Data JPA + Hibernate
- **Documentation**: Swagger/OpenAPI 3
- **Mapping**: MapStruct
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Build Tool**: Maven

## Project Structure

```
src/main/java/com/farukgenc/boilerplate/springboot/
├── configuration/          # Configuration classes
│   ├── DataInitializer     # Sample data for dev/test
│   ├── SecurityConfiguration
│   └── SwaggerConfiguration
├── controller/             # REST controllers
│   ├── DeviceController
│   ├── DeviceInterfaceController
│   ├── SystemUnitController
│   └── IpProfileController
├── dto/                    # Data Transfer Objects
├── mapper/                 # MapStruct mappers
├── model/                  # JPA entities
│   ├── Device
│   ├── DeviceInterface
│   ├── SystemUnit
│   ├── IpProfile
│   ├── IcmpProfile
│   ├── UdpProfile
│   └── User
├── repository/             # JPA repositories
├── service/                # Business logic services
└── security/               # Security components
```

## API Endpoints

### Authentication
- `POST /login` - User authentication
- `POST /register` - User registration

### Device Management
- `GET /api/devices` - Get all user devices
- `POST /api/devices` - Create new device
- `GET /api/devices/{id}` - Get device by ID
- `PUT /api/devices/{id}` - Update device
- `DELETE /api/devices/{id}` - Delete device
- `GET /api/devices/by-status/{status}` - Filter by status
- `GET /api/devices/by-type/{type}` - Filter by type
- `GET /api/devices/search?name={name}` - Search by name

### Device Interfaces
- `GET /api/device-interfaces/device/{deviceId}` - Get device interfaces
- `POST /api/device-interfaces` - Create new interface
- `PUT /api/device-interfaces/{id}` - Update interface
- `DELETE /api/device-interfaces/{id}` - Delete interface

### System Units
- `GET /api/system-units/device/{deviceId}` - Get device system units
- `POST /api/system-units` - Create new system unit
- `PUT /api/system-units/{id}` - Update system unit
- `DELETE /api/system-units/{id}` - Delete system unit

### IP Profiles
- `GET /api/ip-profiles/device/{deviceId}` - Get device IP profiles
- `POST /api/ip-profiles` - Create new IP profile
- `PUT /api/ip-profiles/{id}` - Update IP profile
- `DELETE /api/ip-profiles/{id}` - Delete IP profile

## Getting Started

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- PostgreSQL 12+ (for production)
- Docker (optional)

### Running with Docker

```bash
# Start PostgreSQL and the application
docker-compose up -d

# The API will be available at http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

### Running Locally

1. **Clone the repository**
```bash
git clone <repository-url>
cd backend
```

2. **Configure database**
Update `src/main/resources/application.yml` with your PostgreSQL credentials.

3. **Run the application**
```bash
mvn spring-boot:run
```

4. **Access the API**
- API Base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Console (test profile): `http://localhost:8080/h2-console`

### Sample Data

When running with `dev` or `test` profiles, sample data is automatically created:

**Admin User:**
- Username: `admin`
- Password: `admin123`

**Regular User:**
- Username: `user`
- Password: `user123`

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=DeviceServiceImplTest

# Run integration tests
mvn test -Dtest=DeviceIntegrationTest
```

### Test Coverage

The project includes:
- Unit tests for services and controllers
- Integration tests for API endpoints
- Repository tests with H2 database
- Security tests for authentication

## API Usage Examples

### Authentication

```bash
# Login
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'

# Response
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "userRole": "ADMIN"
}
```

### Device Management

```bash
# Create a device (include JWT token in Authorization header)
curl -X POST http://localhost:8080/api/devices \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Core Router",
    "description": "Main network router",
    "status": "ACTIVE",
    "type": "ROUTER",
    "systemName": "core-router-01"
  }'

# Get all devices
curl -X GET http://localhost:8080/api/devices \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get device by ID
curl -X GET http://localhost:8080/api/devices/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Device Interface Management

```bash
# Create device interface
curl -X POST http://localhost:8080/api/device-interfaces \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": 1,
    "ifIndex": 1,
    "ifDescr": "Ethernet0/0",
    "ifType": "ETHERNET_CSMACD",
    "ifMtu": 1500,
    "ifSpeed": 1000000000,
    "ifAdminStatus": "UP",
    "ifOperStatus": "UP"
  }'
```

## Database Schema

### Key Entities

- **Users**: System users with device ownership
- **Devices**: Network devices with MIB-2 system information
- **Device_Interfaces**: Network interfaces (ifTable equivalent)
- **System_Units**: Hardware components and modules
- **IP_Profiles**: IP layer statistics and configuration
- **ICMP_Profiles**: ICMP protocol statistics
- **UDP_Profiles**: UDP protocol statistics

### Relationships

- User → Devices (One-to-Many)
- Device → Interfaces (One-to-Many)
- Device → System Units (One-to-Many)
- Device → Profiles (One-to-Many)

## Security

- JWT-based stateless authentication
- User isolation (users can only access their own devices)
- Role-based authorization
- Input validation and sanitization
- SQL injection prevention through JPA

## Monitoring & Observability

- Spring Boot Actuator endpoints
- Application health checks
- Custom metrics for device counts
- Comprehensive logging with SLF4J

## Development

### Adding New Features

1. Create entity in `model/` package
2. Add repository in `repository/` package
3. Create DTO in `dto/` package
4. Add mapper in `mapper/` package
5. Implement service in `service/` package
6. Create controller in `controller/` package
7. Add tests for all layers

### Best Practices

- Follow REST conventions
- Use proper HTTP status codes
- Implement comprehensive error handling
- Add Swagger documentation
- Write unit and integration tests
- Use MapStruct for object mapping
- Follow Spring Boot conventions

## Contributing

1. Fork the repository
2. Create a feature branch
3. Implement changes with tests
4. Update documentation
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For questions and support, please contact the development team or create an issue in the repository.
