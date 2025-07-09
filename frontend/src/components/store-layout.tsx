import { type ReactNode, useState } from "react"
import { Link, useLocation } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { ThemeToggle } from "@/components/ui/theme-toggle"
import { ShoppingCart, User, LogOut, Shield, Settings, Menu, X } from "lucide-react"
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
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)

  return (
    <div className="min-h-screen bg-background text-foreground">
      {/* Store-specific navbar */}
      <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container flex h-16 items-center justify-between">
          {/* Logo */}
          <div className="flex items-center">
            <Link to="/store" className="flex items-center space-x-2">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-blue-600">
                <ShoppingCart className="h-5 w-5 text-white" />
              </div>
              <span className="font-bold hidden sm:inline">Network Store</span>
              <span className="font-bold sm:hidden">Store</span>
            </Link>
          </div>
          
          {/* Desktop Navigation */}
          <nav className="hidden md:flex items-center space-x-6 text-sm font-medium">
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
                      <span className="hidden lg:inline">Admin Panel</span>
                      <span className="lg:hidden">Admin</span>
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
          
          {/* Desktop Actions */}
          <div className="hidden md:flex items-center space-x-4">
            {isStoreAuthenticated && storeUser && (
              <div className="flex items-center space-x-3">
                <span className="text-sm text-muted-foreground hidden lg:inline">
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
                  <span className="hidden lg:inline">Logout</span>
                </Button>
              </div>
            )}
            
            <ThemeToggle />
            
            {/* Link back to main app */}
            <Button asChild variant="outline" size="sm">
              <Link to="/">
                <Settings className="h-4 w-4 mr-2" />
                <span className="hidden lg:inline">Network Manager</span>
                <span className="lg:hidden">Manager</span>
              </Link>
            </Button>
          </div>

          {/* Mobile Menu Button */}
          <div className="md:hidden flex items-center space-x-2">
            <ThemeToggle />
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-2"
            >
              {mobileMenuOpen ? (
                <X className="h-5 w-5" />
              ) : (
                <Menu className="h-5 w-5" />
              )}
            </Button>
          </div>
        </div>

        {/* Mobile Menu */}
        {mobileMenuOpen && (
          <div className="md:hidden border-t bg-background/95 backdrop-blur">
            <div className="container py-4 space-y-4">
              {/* Mobile Navigation */}
              <nav className="space-y-2">
                <Link
                  to="/store"
                  className={`block px-4 py-2 rounded-md transition-colors hover:bg-muted ${
                    location.pathname === "/store" ? "bg-muted" : ""
                  }`}
                  onClick={() => setMobileMenuOpen(false)}
                >
                  Products
                </Link>
                
                {isStoreAuthenticated ? (
                  <>
                    {storeUser?.role === "ADMIN" && (
                      <Link
                        to="/store/admin"
                        className={`block px-4 py-2 rounded-md transition-colors hover:bg-muted ${
                          location.pathname.startsWith("/store/admin") ? "bg-muted" : ""
                        }`}
                        onClick={() => setMobileMenuOpen(false)}
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
                      className={`block px-4 py-2 rounded-md transition-colors hover:bg-muted ${
                        location.pathname === "/store/login" ? "bg-muted" : ""
                      }`}
                      onClick={() => setMobileMenuOpen(false)}
                    >
                      Login
                    </Link>
                    <Link
                      to="/store/register"
                      className={`block px-4 py-2 rounded-md transition-colors hover:bg-muted ${
                        location.pathname === "/store/register" ? "bg-muted" : ""
                      }`}
                      onClick={() => setMobileMenuOpen(false)}
                    >
                      Register
                    </Link>
                  </>
                )}
              </nav>

              {/* Mobile User Info */}
              {isStoreAuthenticated && storeUser && (
                <div className="border-t pt-4 space-y-2">
                  <div className="px-4 py-2 text-sm text-muted-foreground">
                    <User className="h-4 w-4 inline mr-2" />
                    {storeUser.username}
                    {storeUser.role === "ADMIN" && (
                      <span className="ml-2 px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded">
                        Admin
                      </span>
                    )}
                  </div>
                  <div className="px-4">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => {
                        storeAuthContext?.logout()
                        setMobileMenuOpen(false)
                      }}
                      className="w-full justify-start"
                    >
                      <LogOut className="h-4 w-4 mr-2" />
                      Logout
                    </Button>
                  </div>
                </div>
              )}

              {/* Mobile Actions */}
              <div className="border-t pt-4 px-4">
                <Button asChild variant="outline" size="sm" className="w-full">
                  <Link to="/" onClick={() => setMobileMenuOpen(false)}>
                    <Settings className="h-4 w-4 mr-2" />
                    Network Manager
                  </Link>
                </Button>
              </div>
            </div>
          </div>
        )}
      </header>
      
      {children}
    </div>
  )
} 