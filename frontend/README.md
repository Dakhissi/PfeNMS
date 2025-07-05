# Network Manager Frontend

A modern React application for network management with authentication system.

## Features

- 🔐 **Authentication System**: Login and registration with JWT tokens
- 📱 **Responsive Design**: Built with Tailwind CSS and shadcn/ui
- 🎨 **Theme Support**: Light/dark mode with system preference detection
- 🛡️ **Protected Routes**: Dashboard access requires authentication
- ✅ **Form Validation**: Zod schema validation with error messages
- 🔄 **State Management**: React Query for server state management
- 📦 **TypeScript**: Full type safety throughout the application
- 🔗 **Backend Integration**: Connected to Node.js backend on port 8080

## Authentication System

### Features

- **Login**: Username/password authentication with JWT token storage
- **Registration**: User registration with email validation
- **Protected Routes**: Dashboard is only accessible to authenticated users
- **Auto-logout**: Automatic logout on token expiration
- **Persistent Login**: JWT tokens stored in localStorage

### Backend API

The application connects to a Node.js backend server:

- **API Base URL**: `http://localhost:8080`
- **Authentication**: JWT-based authentication
- **CORS**: Configured for cross-origin requests

### API Endpoints

- `POST /login` - User login
- `POST /register` - User registration

## Getting Started

### Prerequisites

- Node.js 18+
- pnpm (recommended) or npm
- Backend server running on port 8080

### Installation

1. Install dependencies:

```bash
pnpm install
```

2. Start the development server:

```bash
pnpm dev
```

3. Open [http://localhost:5173](http://localhost:5173) in your browser

### Backend Setup

Make sure your backend server is running on port 8080 with the following endpoints:

```javascript
// Example backend routes
POST /auth/login
POST /auth/register
GET /auth/me (with Authorization: Bearer <token>)
```

## Project Structure

```
src/
├── components/
│   ├── ui/           # shadcn/ui components
│   ├── navbar.tsx    # Navigation bar
│   └── protected-route.tsx  # Route protection
├── lib/
│   ├── auth.ts       # Authentication service
│   ├── auth-context.tsx  # Auth context provider
│   ├── auth-context-definition.tsx  # Auth context definition
│   ├── auth-hooks.ts # Auth hooks
│   └── utils.ts      # Utility functions
├── pages/
│   ├── home.tsx      # Home page
│   ├── login.tsx     # Login page
│   ├── register.tsx  # Registration page
│   └── dashboard.tsx # Dashboard page
└── App.tsx           # Main app component
```

## Key Components

### AuthProvider

Manages authentication state and provides login/logout functions to the entire app.

### ProtectedRoute

Wraps routes that require authentication. Redirects to login if user is not authenticated.

### Form Validation

Uses Zod schemas for validation:

- Username: Required, min 3 characters
- Email: Required, valid email format
- Password: Required, min 6 characters
- Confirm Password: Must match password

## API Configuration

The frontend is configured to connect to the backend on `http://localhost:8080`. To change the API URL, update the `API_BASE_URL` in `src/lib/auth.ts`:

```typescript
const API_BASE_URL = "http://localhost:8080"; // Change this to your backend URL
```

## Customization

### Styling

The app uses Tailwind CSS with shadcn/ui components. Customize the theme in `tailwind.config.js`.

## Available Scripts

- `pnpm dev` - Start development server
- `pnpm build` - Build for production
- `pnpm preview` - Preview production build
- `pnpm lint` - Run ESLint

## Technologies Used

- **React 19** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool
- **Tailwind CSS** - Styling
- **shadcn/ui** - Component library
- **React Router** - Routing
- **React Query** - Server state management
- **React Hook Form** - Form handling
- **Zod** - Schema validation
- **Lucide React** - Icons
