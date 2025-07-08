import { useEffect, useState, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { authService } from './auth'
import { AuthContext } from './auth-context-definition'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<{ id: string; username: string; email: string } | null>(null)
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  // Check if user is authenticated on mount
  useEffect(() => {
    const token = authService.getToken()
    if (token) {
      setIsAuthenticated(true)
      // Try to get user data, but don't automatically logout on failure
      setIsLoading(true)
      authService.getCurrentUser()
        .then(() => {
          // Currently getCurrentUser just returns the token string
          // In the future, it should return user data
          // For now, we'll keep the user authenticated but without user details
          console.log('Token exists, user authenticated')
        })
        .catch((error) => {
          // Only logout if the token is completely invalid or missing
          // For network errors or temporary API issues, keep the user logged in
          console.warn('Failed to get user data:', error)
          if (error.message === 'No authentication token') {
            // Token is missing, clear auth state
            logout()
          }
          // For other errors, keep the user authenticated but without user details
        })
        .finally(() => {
          setIsLoading(false)
        })
    }
  }, [])

  const login = async (username: string, password: string) => {
    const response = await authService.login({ username, password })
    authService.setToken(response.token)
    setIsAuthenticated(true)
    setUser(response.user)
    queryClient.invalidateQueries({ queryKey: ['user'] })
    navigate('/dashboard')
  }

  const register = async (name: string, username: string, email: string, password: string) => {
    await authService.register({ name, username, email, password })
    navigate('/login')
  }

  const logout = () => {
    authService.removeToken()
    setUser(null)
    setIsAuthenticated(false)
    queryClient.clear()
    navigate('/')
  }

  const value = {
    user,
    isLoading,
    login,
    register,
    logout,
    isAuthenticated,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
} 