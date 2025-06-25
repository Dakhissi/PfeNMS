import { createContext } from 'react'

interface User {
  id: string
  username: string
  email: string
}

interface AuthContextType {
  user: User | null
  isLoading: boolean
  login: (username: string, password: string) => Promise<void>
  register: (name: string, username: string, email: string, password: string) => Promise<void>
  logout: () => void
  isAuthenticated: boolean
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined) 