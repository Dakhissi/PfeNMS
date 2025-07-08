import { createContext } from 'react'

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

export const StoreAuthContext = createContext<StoreAuthContextType | null>(null) 