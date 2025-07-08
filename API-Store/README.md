# API Store

A comprehensive e-commerce API for network management equipment and tools.

## Features

- **User Management**: Registration, authentication, and role-based access control
- **Product Management**: CRUD operations for products with inventory tracking
- **Order Management**: Complete order lifecycle with status tracking
- **Admin Dashboard**: Analytics, user management, and data export
- **Real-time Chat**: Support chat system with WebSocket integration
- **Security**: JWT authentication, rate limiting, and input validation

## Quick Start

### Prerequisites

- Docker and Docker Compose
- Node.js 18+ (for local development)

### Running with Docker

1. **Clone and navigate to the API-Store directory:**

   ```bash
   cd API-Store
   ```

2. **Start the services:**

   ```bash
   docker-compose up -d
   ```

3. **Access the API:**
   - API Server: http://localhost:3001
   - Health Check: http://localhost:3001/health
   - Prisma Studio: http://localhost:5555 (optional)

### Default Admin Account

After running docker-compose, the following admin account is automatically created:

- **Username**: `admin`
- **Password**: `admin`
- **Email**: `admin@admin.com`
- **Role**: `ADMIN`

### API Endpoints

#### Authentication

- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/profile` - Get user profile (protected)
- `PUT /api/auth/profile` - Update user profile (protected)

#### Products

- `GET /api/products` - Get all products (public)
- `GET /api/products/categories` - Get product categories (public)
- `GET /api/products/:id` - Get specific product (public)
- `POST /api/products` - Create new product (admin only)
- `PUT /api/products/:id` - Update product (admin only)
- `DELETE /api/products/:id` - Delete product (admin only)

#### Orders

- `POST /api/orders` - Create new order (authenticated users)
- `GET /api/orders/my-orders` - Get user's orders (authenticated users)
- `GET /api/orders/my-orders/:id` - Get specific order (authenticated users)
- `PUT /api/orders/my-orders/:id/cancel` - Cancel order (authenticated users)
- `GET /api/orders/all` - Get all orders (admin only)
- `PUT /api/orders/:id/status` - Update order status (admin only)

#### Admin

- `GET /api/admin/dashboard` - Get dashboard analytics
- `GET /api/admin/analytics/orders` - Get order analytics
- `GET /api/admin/analytics/products` - Get product analytics
- `GET /api/admin/users` - Get all users (admin only)
- `PUT /api/admin/users/:id/role` - Update user role (admin only)
- `PUT /api/admin/products/:id/stock` - Update product stock (admin only)
- `PUT /api/admin/products/bulk-stock` - Bulk update product stock (admin only)
- `GET /api/admin/export` - Export data (admin only)

#### Chat

- `GET /api/chat/rooms` - Get user's chat rooms (authenticated users)
- `GET /api/chat/rooms/:roomId/messages` - Get room messages (authenticated users)
- `POST /api/chat/rooms/:roomId/messages` - Send message (authenticated users)
- `GET /api/chat/direct/:userId` - Get or create direct room (authenticated users)
- `PUT /api/chat/status` - Update online status (authenticated users)
- `POST /api/chat/support` - Create support room (admin only)
- `GET /api/chat/support` - Get support rooms (admin only)

## Frontend Integration

The frontend is configured to connect to the API-Store at `http://localhost:3001/api`.

### Store Admin Route

Access the admin dashboard at: `/store/admin`

Features:

- Dashboard analytics
- User management
- Product management
- Order management
- Data export

## Development

### Local Development

1. **Install dependencies:**

   ```bash
   npm install
   ```

2. **Set up environment variables:**

   ```bash
   cp env.example .env
   ```

3. **Run database migrations:**

   ```bash
   npx prisma migrate dev
   ```

4. **Seed the database:**

   ```bash
   node prisma/seed.js
   ```

5. **Start the development server:**
   ```bash
   npm run dev
   ```

### Database Schema

The application uses PostgreSQL with Prisma ORM. Key models:

- **User**: Authentication and user management
- **Product**: Product catalog with inventory
- **Order**: Order management with status tracking
- **ChatRoom/Message**: Real-time chat system

### Environment Variables

```env
DATABASE_URL=postgresql://postgres:postgres123@localhost:5432/api_store_db
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production
JWT_EXPIRES_IN=24h
SOCKET_CORS_ORIGIN=http://localhost:3000
RATE_LIMIT_WINDOW_MS=900000
RATE_LIMIT_MAX_REQUESTS=100
```

## Security Features

- JWT-based authentication
- Role-based access control (USER, ADMIN, MODERATOR, SUPPORT)
- Rate limiting to prevent abuse
- Input validation and sanitization
- CORS configuration
- Helmet.js security headers

## Monitoring

- Health check endpoint: `/health`
- Socket status: `/api/socket/status`
- Request logging with Morgan
- Error handling and logging

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.
