import { z } from 'zod'

// Validation schemas
export const loginSchema = z.object({
  username: z.string().min(1, 'Username is required'),
  password: z.string().min(1, 'Password is required'),
})

export const registerSchema = z.object({
  name: z.string().min(1, 'Name is required'),
  username: z.string().min(3, 'Username must be at least 3 characters'),
  email: z.string().email('Invalid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
  confirmPassword: z.string().min(1, 'Please confirm your password'),
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
})

export type LoginFormData = z.infer<typeof loginSchema>
export type RegisterFormData = z.infer<typeof registerSchema>

// API base URL - backend on port 8080
const API_BASE_URL = 'http://localhost:8080'

// JWT storage utilities
const JWT_KEY = 'auth_token'

export const authService = {
  // Store JWT token
  setToken: (token: string) => {
    localStorage.setItem(JWT_KEY, token)
  },

  // Get JWT token
  getToken: (): string | null => {
    return localStorage.getItem(JWT_KEY)
  },

  // Remove JWT token
  removeToken: () => {
    localStorage.removeItem(JWT_KEY)
  },

  // Check if user is authenticated
  isAuthenticated: (): boolean => {
    return !!localStorage.getItem(JWT_KEY)
  },

  // Login API call
  login: async (credentials: LoginFormData) => {
    // Trim whitespace from input data
    const trimmedCredentials = {
      username: credentials.username.trim(),
      password: credentials.password.trim(),
    }

    const response = await fetch(`${API_BASE_URL}/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(trimmedCredentials),
    })

    if (!response.ok) {
      const error = await response.json()
      throw new Error(error.message || 'Login failed')
    }

    const data = await response.json()
    return data
  },

  // Register API call
  register: async (userData: Omit<RegisterFormData, 'confirmPassword'>) => {
    // Trim whitespace from input data
    const trimmedUserData = {
      name: userData.name.trim(),
      username: userData.username.trim(),
      email: userData.email.trim(),
      password: userData.password.trim(),
    }

    const response = await fetch(`${API_BASE_URL}/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(trimmedUserData),
    })

    if (!response.ok) {
      const error = await response.json()
      throw new Error(error.message || 'Registration failed')
    }

    const data = await response.json()
    return data
  },

  // Get authenticated user data
  getCurrentUser: async () => {
    const token = authService.getToken()
    if (!token) {
      throw new Error('No authentication token')
    }
    // not yet working
    // const response = await fetch(`${API_BASE_URL}/auth/me`, {
    //   headers: {
    //     'Authorization': `Bearer ${token}`,
    //   },
    // })

    // if (!response.ok) {
    //   throw new Error('Failed to get user data')
    // }

    return token;
  },
} 