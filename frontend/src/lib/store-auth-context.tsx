import { useEffect, useState, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { StoreAuthContext } from './store-auth-context-definition'

interface StoreUser {
  id: string
  username: string
  email: string
  role: string
}

interface StoreAuthContextType {
  user: StoreUser | null
  isLoading: boolean
  login: (email: string, password: string) => Promise<void>
  register: (username: string, email: string, password: string) => Promise<void>
  logout: () => void
  isAuthenticated: boolean
}

const API_BASE_URL = "http://localhost:3001/api"

export function StoreAuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<StoreUser | null>(null)
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  // Check if user is authenticated on mount
  useEffect(() => {
    const checkAuth = async () => {
      const token = localStorage.getItem("store_token")
      if (token) {
        setIsAuthenticated(true)
        setIsLoading(true)
        try {
          const userData = await getCurrentUser()
          setUser(userData)
        } catch (error) {
          console.warn('Failed to get store user data:', error)
          // Only logout if token is invalid, not on network errors
          const errorMessage = error instanceof Error ? error.message : 'Unknown error'
          if (errorMessage === 'No authentication token' || errorMessage === 'Failed to get user data' || errorMessage === 'Invalid token') {
            logout()
          }
        } finally {
          setIsLoading(false)
        }
      }
    }
    
    checkAuth()
  }, [])

  const getCurrentUser = async (): Promise<StoreUser> => {
    const token = localStorage.getItem("store_token")
    if (!token) {
      throw new Error('No authentication token')
    }

    const response = await fetch(`${API_BASE_URL}/auth/profile`, {
      headers: {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
      }
    })

    if (!response.ok) {
      if (response.status === 401) {
        // Token is invalid, clear it
        localStorage.removeItem("store_token")
        throw new Error('Invalid token')
      }
      throw new Error('Failed to get user data')
    }

    const userData = await response.json()
    return userData.user || userData
  }

  const login = async (email: string, password: string) => {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ email, password })
      })

      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.message || 'Login failed')
      }

      const data = await response.json()
      localStorage.setItem("store_token", data.token)
      setIsAuthenticated(true)
      setUser(data.user)
      queryClient.invalidateQueries({ queryKey: ['store_user'] })
      
      // Redirect based on role
      if (data.user.role === "ADMIN") {
        navigate('/store/admin')
      } else {
        navigate('/store')
      }
    } catch (error) {
      console.error('Store login failed:', error)
      throw error
    }
  }

  const register = async (username: string, email: string, password: string) => {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/register`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ username, email, password })
      })

      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.message || 'Registration failed')
      }

      // After successful registration, redirect to login
      navigate('/store/login')
    } catch (error) {
      console.error('Store registration failed:', error)
      throw error
    }
  }

  const logout = () => {
    localStorage.removeItem("store_token")
    setUser(null)
    setIsAuthenticated(false)
    queryClient.clear()
    navigate('/store')
  }

  const value: StoreAuthContextType = {
    user,
    isLoading,
    login,
    register,
    logout,
    isAuthenticated,
  }

  return <StoreAuthContext.Provider value={value}>{children}</StoreAuthContext.Provider>
} 