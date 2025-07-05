# API Store - Express.js Backend

A complete Express.js API with PostgreSQL, Prisma ORM, Socket.io for real-time communication, and Docker Compose for easy deployment.

## 🚀 Features

- **Express.js** - Fast, unopinionated web framework
- **PostgreSQL** - Robust relational database
- **Prisma ORM** - Type-safe database client
- **Socket.io** - Real-time bidirectional communication
- **Redis** - Caching and session storage
- **Docker Compose** - Easy containerized deployment
- **JWT Authentication** - Secure user authentication
- **Role-based Access Control** - User, Admin, Moderator, and Support roles
- **Real-time Chat** - Direct messages and support chat rooms
- **Admin Dashboard** - Analytics, user management, and product management
- **Rate Limiting** - API protection against abuse
- **Input Validation** - Request validation with express-validator
- **Security Middleware** - Helmet, CORS, and other security features

## 📋 Prerequisites

- Docker and Docker Compose
- Node.js (v16 or higher) - for local development
- PostgreSQL database - for local development

## 🛠️ Installation

### Option 1: Docker Compose (Recommended)

1. **Clone and navigate to the project**

   ```bash
   cd API-Store
   ```

2. **Create environment file**

   ```bash
   # Copy the example environment file
   cp env.example .env
   ```

3. **Start all services with Docker Compose**

   ```bash
   npm run docker:up
   ```

4. **Run database migrations and seed data**

   ```bash
   # Access the API container
   docker exec -it api_store_server sh

   # Inside the container, run:
   npm run prisma:generate
   npm run prisma:migrate
   npm run prisma:seed
   ```

5. **Access the services**
   - API: http://localhost:3001
   - Prisma Studio: http://localhost:5555
   - PostgreSQL: localhost:5434

### Option 2: Local Development

1. **Install dependencies**

   ```bash
   npm install
   ```

2. **Set up environment variables**

   ```bash
   # Copy the example environment file
   cp env.example .env
   ```

3. **Set up the database**

   ```bash
   # Generate Prisma client
   npm run prisma:generate

   # Run database migrations
   npm run prisma:migrate

   # Seed the database with sample data
   npm run prisma:seed
   ```

4. **Start the development server**
   ```bash
   npm run dev
   ```

## 📊 Database Schema

### Users

- `id` - Unique identifier
- `email` - User email (unique)
- `username` - Username (unique)
- `password` - Hashed password
- `role` - USER, ADMIN, MODERATOR, or SUPPORT
- `avatar` - User avatar URL
- `isOnline` - Online status
- `lastSeen` - Last seen timestamp
- `createdAt` - Account creation timestamp
- `updatedAt` - Last update timestamp

### Products

- `id` - Unique identifier
- `name` - Product name
- `description` - Product description
- `price` - Product price (decimal)
- `stock` - Available stock quantity
- `category` - Product category
- `brand` - Product brand
- `model` - Product model
- `imageUrl` - Product image URL
- `specifications` - Product specifications (JSON)
- `isActive` - Product availability status
- `createdAt` - Product creation timestamp
- `updatedAt` - Last update timestamp

### Orders

- `id` - Unique identifier
- `userId` - User who placed the order
- `status` - Order status (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED, REFUNDED)
- `total` - Order total amount
- `shippingAddress` - Shipping address (JSON)
- `notes` - Order notes
- `createdAt` - Order creation timestamp
- `updatedAt` - Last update timestamp

### Chat System

- `ChatRoom` - Chat rooms (DIRECT, GROUP, SUPPORT)
- `ChatRoomMember` - Room membership and roles
- `Message` - Chat messages with read status

## 🔌 API Endpoints

### Authentication

- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `GET /api/auth/profile` - Get user profile (protected)
- `PUT /api/auth/profile` - Update user profile (protected)

### Products

- `GET /api/products` - Get all products (with pagination and filtering)
- `GET /api/products/categories` - Get product categories
- `GET /api/products/:id` - Get single product
- `POST /api/products` - Create product (admin only)
- `PUT /api/products/:id` - Update product (admin only)
- `DELETE /api/products/:id` - Delete product (admin only)

### Orders

- `POST /api/orders` - Create new order (protected)
- `GET /api/orders/my-orders` - Get user orders (protected)
- `GET /api/orders/my-orders/:id` - Get single order (protected)
- `PUT /api/orders/my-orders/:id/cancel` - Cancel order (protected)
- `GET /api/orders/all` - Get all orders (admin only)
- `PUT /api/orders/:id/status` - Update order status (admin only)

### Chat

- `GET /api/chat/rooms` - Get user's chat rooms (protected)
- `GET /api/chat/rooms/:roomId/messages` - Get room messages (protected)
- `POST /api/chat/rooms/:roomId/messages` - Send message (protected)
- `GET /api/chat/direct/:userId` - Get or create direct chat (protected)
- `PUT /api/chat/status` - Update online status (protected)
- `POST /api/chat/support` - Create support room (admin only)
- `GET /api/chat/support` - Get support rooms (admin only)

### Admin

- `GET /api/admin/dashboard` - Get dashboard analytics (admin only)
- `GET /api/admin/analytics/orders` - Get order analytics (admin only)
- `GET /api/admin/analytics/products` - Get product analytics (admin only)
- `GET /api/admin/users` - Get all users (admin only)
- `PUT /api/admin/users/:id/role` - Update user role (admin only)
- `PUT /api/admin/products/:id/stock` - Update product stock (admin only)
- `PUT /api/admin/products/bulk-stock` - Bulk update stock (admin only)
- `GET /api/admin/export` - Export data (admin only)

### System

- `GET /health` - Health check
- `GET /api/socket/status` - Socket connection status

## 🔌 Socket.io Events

### Client to Server

- `join-chat-room` - Join a chat room
- `leave-chat-room` - Leave a chat room
- `chat-message` - Send chat message
- `typing-start` - User started typing
- `typing-stop` - User stopped typing
- `update-status` - Update user online status
- `order-status-update` - Update order status (admin only)
- `stock-update` - Update product stock (admin only)

### Server to Client

- `new-message` - New chat message received
- `user-online` - User came online
- `user-offline` - User went offline
- `user-status-changed` - User status changed
- `user-typing` - User typing indicator
- `user-stopped-typing` - User stopped typing indicator
- `order-updated` - Order status updated
- `order-status-changed` - Order status changed (admin notification)
- `stock-updated` - Product stock updated
- `new-order` - New order notification (admin only)
- `low-stock-alert` - Low stock alert (admin only)
- `new-message-notification` - New message notification for offline users

## 🔐 Authentication

The API uses JWT tokens for authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## 👥 User Roles

- **USER** - Regular customers, can place orders and chat
- **ADMIN** - Full access to all features and admin dashboard
- **MODERATOR** - Can manage orders, products, and support chat
- **SUPPORT** - Can handle support chat and basic order management

## 📝 Sample Data

After running the seed script, you'll have:

**Users:**

- Admin: `admin@store.com` / `admin123`
- Moderator: `moderator@store.com` / `moderator123`
- Support: `support@store.com` / `support123`
- User: `user@store.com` / `user123`

**Products:** 12 networking products (switches, routers, access points, firewalls)
**Orders:** 2 sample orders with shipping addresses
**Chat Rooms:** 2 sample rooms (support and direct)
**Messages:** 2 sample messages

## 🛡️ Security Features

- **JWT Authentication** - Secure token-based authentication
- **Password Hashing** - bcrypt for password security
- **Rate Limiting** - Protection against API abuse
- **Input Validation** - Request validation and sanitization
- **CORS Protection** - Cross-origin resource sharing control
- **Helmet** - Security headers
- **SQL Injection Protection** - Prisma ORM prevents SQL injection
- **Role-based Access Control** - Granular permissions

## 🚀 Docker Commands

```bash
# Start all services
npm run docker:up

# Stop all services
npm run docker:down

# View logs
npm run docker:logs

# Restart services
npm run docker:restart

# Access API container
docker exec -it api_store_server sh

# Access PostgreSQL
docker exec -it api_store_postgres psql -U postgres -d api_store_db
```

## 📚 Scripts

- `npm start` - Start production server
- `npm run dev` - Start development server with nodemon
- `npm run prisma:generate` - Generate Prisma client
- `npm run prisma:migrate` - Run database migrations
- `npm run prisma:studio` - Open Prisma Studio
- `npm run prisma:seed` - Seed database with sample data
- `npm run docker:up` - Start Docker services
- `npm run docker:down` - Stop Docker services
- `npm run docker:logs` - View Docker logs
- `npm run docker:restart` - Restart Docker services

## 🔧 Configuration

### Environment Variables

- `PORT` - Server port (default: 3001)
- `NODE_ENV` - Environment (development/production)
- `DATABASE_URL` - PostgreSQL connection string
- `JWT_SECRET` - JWT signing secret
- `JWT_EXPIRES_IN` - JWT token expiration
- `SOCKET_CORS_ORIGIN` - Socket.io CORS origin
- `RATE_LIMIT_WINDOW_MS` - Rate limiting window
- `RATE_LIMIT_MAX_REQUESTS` - Rate limiting max requests

## 🎯 Admin Features

### Dashboard Analytics

- Total users, products, orders, and revenue
- Recent orders with user details
- Low stock product alerts
- Online user count
- Monthly revenue trends

### User Management

- View all users with pagination and filtering
- Update user roles (USER, ADMIN, MODERATOR, SUPPORT)
- User activity tracking (online status, last seen)

### Product Management

- Update individual product stock
- Bulk stock updates
- Product analytics and top sellers
- Category distribution

### Order Management

- View all orders with filtering
- Update order status
- Order analytics and revenue tracking
- Export order data

### Chat Support

- Create support chat rooms
- Monitor support conversations
- Real-time notifications

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For support, please open an issue in the repository or contact the development team.
