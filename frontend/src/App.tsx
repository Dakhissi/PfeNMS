import { BrowserRouter as Router, Routes, Route } from "react-router-dom"
import { QueryClient, QueryClientProvider } from "@tanstack/react-query"
import { ThemeProvider } from "@/lib/theme-provider"
import { AuthProvider } from "@/lib/auth-context"
import { Navbar } from "@/components/navbar"
import { ProtectedRoute, PublicRoute } from "@/components/protected-route"
import { HomePage } from "@/pages/home"
import { LoginPage } from "@/pages/login"
import { RegisterPage } from "@/pages/register"
import { DashboardPage } from "@/pages/dashboard"

// Create a client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      retry: 1,
    },
  },
})

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider defaultTheme="system" storageKey="network-manager-theme">
        <Router>
          <AuthProvider>
            <div className="min-h-screen bg-background text-foreground">
              <Navbar />
              <Routes>
                <Route path="/" element={<HomePage />} />
                <Route 
                  path="/login" 
                  element={
                    <PublicRoute>
                      <LoginPage />
                    </PublicRoute>
                  } 
                />
                <Route 
                  path="/register" 
                  element={
                    <PublicRoute>
                      <RegisterPage />
                    </PublicRoute>
                  } 
                />
                <Route 
                  path="/dashboard" 
                  element={
                    <ProtectedRoute>
                      <DashboardPage />
                    </ProtectedRoute>
                  } 
                />
              </Routes>
            </div>
          </AuthProvider>
        </Router>
      </ThemeProvider>
    </QueryClientProvider>
  )
}

export default App
