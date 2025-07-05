import { ThemeToggle } from "@/components/ui/theme-toggle"
import { Network, LogOut, Server, Search, ShoppingCart, Database } from "lucide-react"
import { Link, useLocation } from "react-router-dom"
import { useAuth } from "@/lib/auth-hooks"
import { Button } from "@/components/ui/button"

export function Navbar() {
  const location = useLocation()
  const { isAuthenticated, logout, user } = useAuth()
  
  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container flex h-16 items-center">
        <div className="mr-8">
          <Link to={isAuthenticated ? "/dashboard" : "/"} className="flex items-center space-x-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary">
              <Network className="h-5 w-5 text-primary-foreground" />
            </div>
            <span className="font-bold">NetworkManager</span>
          </Link>
        </div>
        
        <nav className="flex items-center space-x-6 text-sm font-medium">
          {isAuthenticated ? (
            <>
              <Link
                to="/dashboard"
                className={`transition-colors hover:text-foreground/80 ${
                  location.pathname === "/dashboard" ? "text-foreground" : "text-foreground/60"
                }`}
              >
                Dashboard
              </Link>
              <Link
                to="/devices"
                className={`transition-colors hover:text-foreground/80 ${
                  location.pathname.startsWith("/devices") ? "text-foreground" : "text-foreground/60"
                }`}
              >
                <div className="flex items-center gap-2">
                  <Server className="h-4 w-4" />
                  Devices
                </div>
              </Link>
              <Link
                to="/discovery"
                className={`transition-colors hover:text-foreground/80 ${
                  location.pathname.startsWith("/discovery") ? "text-foreground" : "text-foreground/60"
                }`}
              >
                <div className="flex items-center gap-2">
                  <Search className="h-4 w-4" />
                  Discovery
                </div>
              </Link>
              <Link
                to="/mib"
                className={`transition-colors hover:text-foreground/80 ${
                  location.pathname.startsWith("/mib") ? "text-foreground" : "text-foreground/60"
                }`}
              >
                <div className="flex items-center gap-2">
                  <Database className="h-4 w-4" />
                  MIB Browser
                </div>
              </Link>
            </>
          ) : (
            <>
              <Link
                to="/"
                className={`transition-colors hover:text-foreground/80 ${
                  location.pathname === "/" ? "text-foreground" : "text-foreground/60"
                }`}
              >
                Home
              </Link>
              <Link
                to="/login"
                className={`transition-colors hover:text-foreground/80 ${
                  location.pathname === "/login" ? "text-foreground" : "text-foreground/60"
                }`}
              >
                Login
              </Link>
              <Link
                to="/register"
                className={`transition-colors hover:text-foreground/80 ${
                  location.pathname === "/register" ? "text-foreground" : "text-foreground/60"
                }`}
              >
                Register
              </Link>
              
              {/* Store link - visible to anonymous users only */}
              <Link
                to="/store"
                target="_blank"
                rel="noopener noreferrer"
                className={`transition-colors hover:text-foreground/80 ${
                  location.pathname.startsWith("/store") ? "text-foreground" : "text-foreground/60"
                }`}
              >
                <div className="flex items-center gap-2">
                  <ShoppingCart className="h-4 w-4" />
                  Check our store
                </div>
              </Link>
            </>
          )}
        </nav>
        
        <div className="ml-auto flex items-center space-x-4">
          {isAuthenticated && (
            <div className="flex items-center space-x-3">
              <span className="text-sm text-muted-foreground">
                Welcome, {user?.username}
              </span>
              <Button
                variant="ghost"
                size="sm"
                onClick={logout}
                className="flex items-center gap-2"
              >
                <LogOut className="h-4 w-4" />
                Logout
              </Button>
            </div>
          )}
          <ThemeToggle />
        </div>
      </div>
    </header>
  )
} 