# Authentication Controllers API Documentation

## Overview

The Authentication Controllers handle user authentication and registration in the system. They provide secure endpoints for user login and account creation with JWT token-based authentication.

## Controllers

### Login Controller
**Base URL**: `/login`

### Registration Controller  
**Base URL**: `/register`

## Endpoints

### 1. User Login
- **Method**: `POST`
- **Path**: `/login`
- **Summary**: Authenticate user and get JWT token
- **Description**: Authenticate with correct credentials to obtain JWT token for accessing protected endpoints
- **Request Body**: `LoginRequest` (JSON)
- **Response**: `LoginResponse` with JWT token
- **Status Codes**:
  - `200 OK`: Login successful, token returned
  - `400 Bad Request`: Invalid request format
  - `401 Unauthorized`: Invalid credentials

**Example Request**:
```json
{
  "username": "user@example.com",
  "password": "securepassword123"
}
```

**Example Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 600,
  "user": {
    "id": 1,
    "username": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "email": "user@example.com"
  }
}
```

### 2. User Registration
- **Method**: `POST`
- **Path**: `/register`
- **Summary**: Register a new user account
- **Description**: Create a new user account by providing required information in the appropriate format
- **Request Body**: `RegistrationRequest` (JSON)
- **Response**: `RegistrationResponse`
- **Status Codes**:
  - `201 Created`: User registered successfully
  - `400 Bad Request`: Invalid request format or validation errors
  - `409 Conflict`: Username or email already exists

**Example Request**:
```json
{
  "username": "newuser@example.com",
  "password": "securepassword123",
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "newuser@example.com"
}
```

**Example Response**:
```json
{
  "message": "User registered successfully",
  "userId": 2,
  "username": "newuser@example.com"
}
```

## Data Models

### LoginRequest
```json
{
  "username": "String (required) - Email or username",
  "password": "String (required) - User password"
}
```

### LoginResponse
```json
{
  "token": "String - JWT token",
  "tokenType": "String - Token type (Bearer)",
  "expiresIn": "Long - Token expiration time in seconds",
  "user": {
    "id": "Long - User ID",
    "username": "String - Username",
    "firstName": "String - First name",
    "lastName": "String - Last name",
    "email": "String - Email address"
  }
}
```

### RegistrationRequest
```json
{
  "username": "String (required) - Unique username",
  "password": "String (required) - Password (minimum requirements apply)",
  "firstName": "String (required) - First name",
  "lastName": "String (required) - Last name",
  "email": "String (required) - Valid email address"
}
```

### RegistrationResponse
```json
{
  "message": "String - Success message",
  "userId": "Long - Assigned user ID",
  "username": "String - Confirmed username"
}
```

## Authentication Flow

### Registration Process
1. User submits registration form with required information
2. System validates input data and checks for existing users
3. Password is securely hashed
4. User account is created in the database
5. Success response is returned

### Login Process
1. User submits credentials (username/email and password)
2. System validates credentials against stored user data
3. If valid, a JWT token is generated with user information
4. Token is returned to client for subsequent API requests
5. Client includes token in Authorization header for protected endpoints

## Security Features

### Password Requirements
- Minimum length requirements
- Password hashing using secure algorithms
- Protection against common password vulnerabilities

### JWT Token Security
- Configurable expiration time (default: 10 minutes)
- Secure token generation with secret key
- Token includes user claims for authorization

### Input Validation
- Server-side validation of all input fields
- Email format validation
- Username uniqueness checking
- Protection against injection attacks

## Usage Examples

### Register a new user
```bash
curl -X POST "http://localhost:8080/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe@company.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@company.com"
  }'
```

### Login with credentials
```bash
curl -X POST "http://localhost:8080/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe@company.com",
    "password": "SecurePass123!"
  }'
```

### Using the JWT token for authenticated requests
```bash
curl -X GET "http://localhost:8080/api/devices" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Error Handling

### Common Error Responses

#### 400 Bad Request
```json
{
  "error": "Validation failed",
  "message": "Invalid input data",
  "details": [
    {
      "field": "email",
      "message": "Invalid email format"
    }
  ]
}
```

#### 401 Unauthorized
```json
{
  "error": "Authentication failed",
  "message": "Invalid username or password"
}
```

#### 409 Conflict
```json
{
  "error": "User already exists",
  "message": "Username or email is already registered"
}
```

## Token Management

### Token Expiration
- Default expiration: 10 minutes (configurable)
- Clients should handle token refresh or re-authentication
- Expired tokens will receive 401 Unauthorized responses

### Token Usage
- Include in Authorization header: `Authorization: Bearer <token>`
- Required for all protected API endpoints
- Token contains user information for authorization decisions

## Configuration

### JWT Settings (application.yml)
```yaml
jwt:
  secretKey: secret          # Change in production
  issuer: www.farukgenc.com
  expirationMinute: 10       # Token expiration time
```

### Security Considerations
- Change the JWT secret key in production environments
- Use HTTPS in production to protect token transmission
- Implement proper session management on the client side
- Consider implementing refresh token mechanism for longer sessions

## Integration with Other APIs

All other API endpoints in the system require authentication:
- Device Management APIs
- Alert Management APIs  
- Discovery APIs
- Trap Management APIs
- MIB Browser APIs

The JWT token obtained from login must be included in the Authorization header for accessing these protected resources.
