import { type ReactNode } from "react"
import { Link, useLocation } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { ThemeToggle } from "@/components/ui/theme-toggle"
import { ShoppingCart, User, LogOut, Shield, Settings } from "lucide-react"
import { useContext } from "react"
import { StoreAuthContext } from "@/lib/store-auth-context-definition"

interface StoreLayoutProps {
  children: ReactNode
}

export function StoreLayout({ children }: StoreLayoutProps) {
  const location = useLocation()
  const storeAuthContext = useContext(StoreAuthContext)
  const isStoreAuthenticated = storeAuthContext?.isAuthenticated
  const storeUser = storeAuthContext?.user

  return (
    <div className="min-h-screen bg-background text-foreground">
      {/* Store-specific navbar */}
      <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container flex h-16 items-center">
          <div className="mr-8">
            <Link to="/store" className="flex items-center space-x-2">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-blue-600">
                <ShoppingCart className="h-5 w-5 text-white" />
              </div>
              <span className="font-bold">Network Store</span>
            </Link>
          </div>
          
          <nav className="flex items-center space-x-6 text-sm font-medium">
            <Link
              to="/store"
              className={`transition-colors hover:text-foreground/80 ${
                location.pathname === "/store" ? "text-foreground" : "text-foreground/60"
              }`}
            >
              Products
            </Link>
            
            {isStoreAuthenticated ? (
              <>
                {storeUser?.role === "ADMIN" && (
                  <Link
                    to="/store/admin"
                    className={`transition-colors hover:text-foreground/80 ${
                      location.pathname.startsWith("/store/admin") ? "text-foreground" : "text-foreground/60"
                    }`}
                  >
                    <div className="flex items-center gap-2">
                      <Shield className="h-4 w-4" />
                      Admin Panel
                    </div>
                  </Link>
                )}
              </>
            ) : (
              <>
                <Link
                  to="/store/login"
                  className={`transition-colors hover:text-foreground/80 ${
                    location.pathname === "/store/login" ? "text-foreground" : "text-foreground/60"
                  }`}
                >
                  Login
                </Link>
                <Link
                  to="/store/register"
                  className={`transition-colors hover:text-foreground/80 ${
                    location.pathname === "/store/register" ? "text-foreground" : "text-foreground/60"
                  }`}
                >
                  Register
                </Link>
              </>
            )}
          </nav>
          
          <div className="ml-auto flex items-center space-x-4">
            {isStoreAuthenticated && storeUser && (
              <div className="flex items-center space-x-3">
                <span className="text-sm text-muted-foreground">
                  <User className="h-4 w-4 inline mr-1" />
                  {storeUser.username}
                  {storeUser.role === "ADMIN" && (
                    <span className="ml-2 px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded">
                      Admin
                    </span>
                  )}
                </span>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => storeAuthContext?.logout()}
                  className="flex items-center gap-2"
                >
                  <LogOut className="h-4 w-4" />
                  Logout
                </Button>
              </div>
            )}
            
            <ThemeToggle />
            
            {/* Link back to main app */}
            <Button asChild variant="outline" size="sm">
              <Link to="/">
                <Settings className="h-4 w-4 mr-2" />
                Network Manager
              </Link>
            </Button>
          </div>
        </div>
      </header>
      
      {children}
    </div>
  )
} 