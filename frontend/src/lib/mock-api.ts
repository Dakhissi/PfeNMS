// Mock API for development/testing purposes
// This simulates a backend server with authentication endpoints

interface MockUser {
  id: string
  username: string
  email: string
  password: string // In real app, this would be hashed
}

interface UserResponse {
  id: string
  username: string
  email: string
}

interface MockResponse {
  success: boolean
  message?: string
  token?: string
  user?: UserResponse
}

// Mock database
const users: MockUser[] = [
  {
    id: '1',
    username: 'admin',
    email: 'admin@example.com',
    password: 'password123'
  }
]

// Generate a simple JWT-like token
function generateToken(userId: string): string {
  const payload = {
    userId,
    exp: Date.now() + 24 * 60 * 60 * 1000, // 24 hours
  }
  return btoa(JSON.stringify(payload))
}

// Validate token
function validateToken(token: string): string | null {
  try {
    const payload = JSON.parse(atob(token))
    if (payload.exp > Date.now()) {
      return payload.userId
    }
    return null
  } catch {
    return null
  }
}

// Mock API endpoints
export const mockApi = {
  // Login endpoint
  login: async (username: string, password: string): Promise<MockResponse> => {
    // Simulate network delay
    await new Promise(resolve => setTimeout(resolve, 500))

    const user = users.find(u => u.username === username && u.password === password)
    
    if (!user) {
      return {
        success: false,
        message: 'Invalid username or password'
      }
    }

    const token = generateToken(user.id)
    
    return {
      success: true,
      token,
      user: {
        id: user.id,
        username: user.username,
        email: user.email
      }
    }
  },

  // Register endpoint
  register: async (username: string, email: string, password: string): Promise<MockResponse> => {
    // Simulate network delay
    await new Promise(resolve => setTimeout(resolve, 500))

    // Check if user already exists
    if (users.find(u => u.username === username)) {
      return {
        success: false,
        message: 'Username already exists'
      }
    }

    if (users.find(u => u.email === email)) {
      return {
        success: false,
        message: 'Email already exists'
      }
    }

    // Create new user
    const newUser: MockUser = {
      id: (users.length + 1).toString(),
      username,
      email,
      password
    }

    users.push(newUser)

    return {
      success: true,
      message: 'User registered successfully'
    }
  },

  // Get current user endpoint
  getCurrentUser: async (token: string): Promise<MockResponse> => {
    // Simulate network delay
    await new Promise(resolve => setTimeout(resolve, 300))

    const userId = validateToken(token)
    
    if (!userId) {
      return {
        success: false,
        message: 'Invalid or expired token'
      }
    }

    const user = users.find(u => u.id === userId)
    
    if (!user) {
      return {
        success: false,
        message: 'User not found'
      }
    }

    return {
      success: true,
      user: {
        id: user.id,
        username: user.username,
        email: user.email
      }
    }
  }
} 