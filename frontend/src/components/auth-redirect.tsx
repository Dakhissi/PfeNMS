import { useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { useAuth } from "@/lib/auth-hooks"

interface AuthRedirectProps {
  children: React.ReactNode
  redirectTo: string
}

export function AuthRedirect({ children, redirectTo }: AuthRedirectProps) {
  const { isAuthenticated } = useAuth()
  const navigate = useNavigate()

  useEffect(() => {
    if (isAuthenticated) {
      navigate(redirectTo, { replace: true })
    }
  }, [isAuthenticated, navigate, redirectTo])

  // Don't render children if user is authenticated (will redirect)
  if (isAuthenticated) {
    return null
  }

  return <>{children}</>
} 