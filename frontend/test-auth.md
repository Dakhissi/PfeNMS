# Authentication Test Guide

This guide helps you verify that the dashboard authentication is working correctly with your backend.

## Prerequisites

1. **Backend Running**: Ensure your Spring Boot backend is running on `http://localhost:8080`
2. **Frontend Running**: Start the frontend development server
3. **Valid User**: Have a test user account in your backend database

## Test Steps

### 1. Test Login Flow

1. **Navigate to Login Page**

   ```
   http://localhost:5173/login
   ```

2. **Login with Valid Credentials**

   - Username: `admin` (or your test user)
   - Password: `password123` (or your test user password)

3. **Verify Token Storage**
   - Open Browser DevTools → Application → Local Storage
   - Check that `auth_token` is stored with a JWT value
   - Token should look like: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`

### 2. Test Dashboard Access

1. **Navigate to Dashboard**

   ```
   http://localhost:5173/dashboard
   ```

2. **Check Network Requests**

   - Open Browser DevTools → Network tab
   - Refresh the dashboard page
   - Verify that all API requests include:
     ```
     Authorization: Bearer <your_jwt_token>
     ```

3. **Expected API Calls**
   - `GET /api/alerts/statistics`
   - `GET /api/devices?page=0&size=10`
   - `GET /api/alerts/recent?limit=5`
   - `WebSocket /topic/alerts?token=<jwt_token>`

### 3. Test Authentication Errors

1. **Test Invalid Token**

   - In DevTools → Application → Local Storage
   - Modify the `auth_token` value to be invalid
   - Refresh the dashboard
   - Should redirect to login page

2. **Test Missing Token**

   - In DevTools → Application → Local Storage
   - Delete the `auth_token` entry
   - Refresh the dashboard
   - Should redirect to login page

3. **Test Expired Token**
   - Wait for your JWT token to expire (if configured)
   - Refresh the dashboard
   - Should redirect to login page

### 4. Test WebSocket Authentication

1. **Check WebSocket Connection**

   - In DevTools → Network tab
   - Filter by "WS" (WebSocket)
   - Verify the connection URL includes the token:
     ```
     ws://localhost:8080/topic/alerts?token=<jwt_token>
     ```

2. **Test WebSocket Auth Failure**
   - Modify the token in localStorage
   - Refresh the page
   - Check console for WebSocket authentication errors

## Expected Behavior

### ✅ Success Cases

- Dashboard loads with real data from backend
- All API requests include Bearer token
- WebSocket connects successfully
- Real-time updates work
- No authentication errors in console

### ❌ Error Cases

- 401 Unauthorized → Redirect to login
- 403 Forbidden → Show access denied
- Missing token → Redirect to login
- Invalid token → Redirect to login

## Debugging

### Check Console Logs

```javascript
// Look for these messages:
"Connected to alert WebSocket"; // WebSocket connected
"Authentication failed. Please log in again."; // Auth error
```

### Check Network Tab

- All requests should have `Authorization` header
- Response status codes should be 200 (not 401/403)
- WebSocket connection should be established

### Check Local Storage

```javascript
// In browser console:
localStorage.getItem("auth_token"); // Should return JWT token
```

## Common Issues

### 1. CORS Errors

**Problem**: `Access to fetch at 'http://localhost:8080/api/...' from origin 'http://localhost:5173' has been blocked by CORS policy`

**Solution**: Configure CORS in your Spring Boot backend:

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### 2. JWT Token Issues

**Problem**: Token not being sent or invalid

**Solution**: Check token format and backend JWT configuration:

```java
// Ensure your JWT secret matches between frontend and backend
@Value("${jwt.secret}")
private String jwtSecret;
```

### 3. WebSocket Connection Issues

**Problem**: WebSocket fails to connect

**Solution**: Check WebSocket configuration in backend:

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
}
```

### 4. Backend Not Running

**Problem**: Dashboard shows loading states indefinitely

**Solution**:

1. Ensure Spring Boot backend is running on port 8080
2. Check backend logs for any startup errors
3. Verify the API endpoints are properly implemented
4. Test API endpoints directly with tools like Postman

### 5. Database Connection Issues

**Problem**: Backend running but no data returned

**Solution**:

1. Check database connection in backend logs
2. Verify database schema and tables exist
3. Ensure test data is present in database
4. Check backend API endpoint implementations

## Production Considerations

1. **HTTPS**: Use HTTPS in production
2. **Token Security**: Consider httpOnly cookies instead of localStorage
3. **Token Refresh**: Implement automatic token refresh
4. **Error Logging**: Add proper error logging and monitoring
5. **Rate Limiting**: Implement rate limiting on backend
6. **Database Optimization**: Ensure database queries are optimized
7. **Load Balancing**: Consider load balancing for high traffic
