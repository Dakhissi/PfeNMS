import { BrowserRouter as Router, Routes, Route } from "react-router-dom"
import { QueryClient, QueryClientProvider } from "@tanstack/react-query"
import { ThemeProvider } from "@/lib/theme-provider"
import { AuthProvider } from "@/lib/auth-context"
import { StoreAuthProvider } from "@/lib/store-auth-context"
import { Navbar } from "@/components/navbar"
import { StoreLayout } from "@/components/store-layout"
import { ProtectedRoute, PublicRoute } from "@/components/protected-route"
import { AuthRedirect } from "@/components/auth-redirect"
import { HomePage } from "@/pages/home"
import { LoginPage } from "@/pages/login"
import { RegisterPage } from "@/pages/register"
import { DashboardPage } from "@/pages/dashboard"
import { DevicesPage } from "@/pages/devices"
import { DeviceDetailsPage } from "@/pages/device-details"
import { DeviceFormPage } from "@/pages/device-form"
import { DiscoveryDashboardPage } from "@/pages/discovery-dashboard"
import { DiscoveryStartPage } from "@/pages/discovery-start"
import { StorePage } from "@/pages/store"
import { StoreLoginPage } from "@/pages/store-login"
import { StoreRegisterPage } from "@/pages/store-register"
import { StoreAdminLoginPage } from "@/pages/store-admin-login"
import { StoreAdminPage } from "@/pages/store-admin"
import { MibBrowserPage } from "@/pages/mib-browser"
import { MibFileDetailsPage } from "@/pages/mib-file-details"

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
            <Routes>
              {/* Store Routes with Store Layout */}
              <Route path="/store" element={
                <StoreAuthProvider>
                  <StoreLayout>
                    <StorePage />
                  </StoreLayout>
                </StoreAuthProvider>
              } />
              <Route path="/store/login" element={
                <StoreAuthProvider>
                  <StoreLayout>
                    <StoreLoginPage />
                  </StoreLayout>
                </StoreAuthProvider>
              } />
              <Route path="/store/register" element={
                <StoreAuthProvider>
                  <StoreLayout>
                    <StoreRegisterPage />
                  </StoreLayout>
                </StoreAuthProvider>
              } />
              <Route path="/store/admin/login" element={
                <StoreAuthProvider>
                  <StoreLayout>
                    <StoreAdminLoginPage />
                  </StoreLayout>
                </StoreAuthProvider>
              } />
              <Route path="/store/admin" element={
                <StoreAuthProvider>
                  <StoreLayout>
                    <StoreAdminPage />
                  </StoreLayout>
                </StoreAuthProvider>
              } />

              {/* Main App Routes with Main Navbar */}
              <Route path="*" element={
                <div className="min-h-screen bg-background text-foreground">
                  <Navbar />
                  <Routes>
                    <Route 
                      path="/" 
                      element={
                        <AuthRedirect redirectTo="/dashboard">
                          <HomePage />
                        </AuthRedirect>
                      } 
                    />
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
                    {/* Device Management Routes */}
                    <Route 
                      path="/devices" 
                      element={
                        <ProtectedRoute>
                          <DevicesPage />
                        </ProtectedRoute>
                      } 
                    />
                    <Route 
                      path="/devices/add" 
                      element={
                        <ProtectedRoute>
                          <DeviceFormPage />
                        </ProtectedRoute>
                      } 
                    />
                    <Route 
                      path="/devices/:id" 
                      element={
                        <ProtectedRoute>
                          <DeviceDetailsPage />
                        </ProtectedRoute>
                      } 
                    />
                    <Route 
                      path="/devices/:id/edit" 
                      element={
                        <ProtectedRoute>
                          <DeviceFormPage />
                        </ProtectedRoute>
                      } 
                    />
                    {/* Discovery Routes */}
                    <Route 
                      path="/discovery" 
                      element={
                        <ProtectedRoute>
                          <DiscoveryDashboardPage />
                        </ProtectedRoute>
                      } 
                    />
                    <Route 
                      path="/discovery/start" 
                      element={
                        <ProtectedRoute>
                          <DiscoveryStartPage />
                        </ProtectedRoute>
                      } 
                    />
                    {/* MIB Browser Routes */}
                    <Route 
                      path="/mib" 
                      element={
                        <ProtectedRoute>
                          <MibBrowserPage />
                        </ProtectedRoute>
                      } 
                    />
                    <Route 
                      path="/mib/files/:id" 
                      element={
                        <ProtectedRoute>
                          <MibFileDetailsPage />
                        </ProtectedRoute>
                      } 
                    />
                  </Routes>
                </div>
              } />
            </Routes>
          </AuthProvider>
        </Router>
      </ThemeProvider>
    </QueryClientProvider>
  )
}

export default App
