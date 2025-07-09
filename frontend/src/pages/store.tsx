import { useState, useEffect, useContext } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Search, ShoppingCart, Star, Package, Truck, User, LogOut, ShoppingBag } from "lucide-react"
import { StoreAuthContext } from "@/lib/store-auth-context-definition"

interface Product {
  id: string
  name: string
  description: string
  price: number
  category: string
  imageUrl: string
  brand: string
  model: string
  stock: number
  isActive: boolean
}

interface CartItem {
  product: Product
  quantity: number
}

export function StorePage() {
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState("")
  const [selectedCategory, setSelectedCategory] = useState<string>("all")
  const [cart, setCart] = useState<CartItem[]>([])
  const [showCart, setShowCart] = useState(false)
  
  const authContext = useContext(StoreAuthContext)
  const user = authContext?.user

  const API_BASE_URL = "http://localhost:3001/api"

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const response = await fetch(`${API_BASE_URL}/products`)
        const data = await response.json()
        
        if (data.products) {
          // Ensure prices are numbers and convert from Decimal if needed
          const productsWithNumberPrices = data.products.map((product: Product) => ({
            ...product,
            price: typeof product.price === 'string' ? parseFloat(product.price) : Number(product.price)
          }))
          setProducts(productsWithNumberPrices)
        } else {
          setProducts([])
        }
        setLoading(false)
      } catch (error) {
        console.error("Failed to fetch products:", error)
        setProducts([])
        setLoading(false)
      }
    }

    fetchProducts()
  }, [])

  // Load cart from localStorage on initial load
  useEffect(() => {
    const savedCart = localStorage.getItem("store_cart")
    if (savedCart) {
      try {
        setCart(JSON.parse(savedCart))
      } catch (error) {
        console.error("Failed to parse cart data:", error)
        setCart([])
      }
    }
  }, [])

  const filteredProducts = products.filter(product => {
    const matchesSearch = product.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         product.description.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesCategory = selectedCategory === "all" || product.category === selectedCategory
    return matchesSearch && matchesCategory && product.isActive
  })

  const categories = ["all", "Network Switches", "Network Routers", "Wireless Access Points", "Cables", "Software"]

  // Cart functions
  const addToCart = (product: Product) => {
    setCart(prevCart => {
      const existingItem = prevCart.find(item => item.product.id === product.id)
      if (existingItem) {
        return prevCart.map(item =>
          item.product.id === product.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        )
      }
      return [...prevCart, { product, quantity: 1 }]
    })
  }

  const removeFromCart = (productId: string) => {
    setCart(prevCart => prevCart.filter(item => item.product.id !== productId))
  }

  const updateCartQuantity = (productId: string, quantity: number) => {
    if (quantity <= 0) {
      removeFromCart(productId)
      return
    }
    setCart(prevCart =>
      prevCart.map(item =>
        item.product.id === productId
          ? { ...item, quantity }
          : item
      )
    )
  }

  const getCartTotal = () => {
    return cart.reduce((total, item) => {
      const price = typeof item.product.price === 'string' ? parseFloat(item.product.price) : Number(item.product.price)
      return total + (price * item.quantity)
    }, 0)
  }

  const getCartItemCount = () => {
    return cart.reduce((total, item) => total + item.quantity, 0)
  }

  // Save cart to localStorage whenever it changes
  useEffect(() => {
    localStorage.setItem("store_cart", JSON.stringify(cart))
  }, [cart])

  const handleLogout = () => {
    authContext?.logout()
  }

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="flex items-center justify-center h-64">
          <div className="text-lg">Loading store...</div>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header with User Auth */}
      <div className="mb-8">
        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 mb-4">
          <div className="flex-1">
            <h1 className="text-2xl lg:text-3xl font-bold mb-2">Network Manager Store</h1>
            <p className="text-muted-foreground text-sm lg:text-base">
              Discover premium networking equipment and tools for your infrastructure. 
              All orders are cash on delivery for your convenience.
            </p>
          </div>
          
          {/* User Menu */}
          <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2 sm:gap-4">
            {/* Cart Button */}
            <Button
              variant="outline"
              onClick={() => setShowCart(!showCart)}
              className="relative"
            >
              <ShoppingBag className="h-4 w-4 mr-2" />
              Cart
              {getCartItemCount() > 0 && (
                <Badge className="absolute -top-2 -right-2 h-5 w-5 rounded-full p-0 flex items-center justify-center text-xs">
                  {getCartItemCount()}
                </Badge>
              )}
            </Button>

            {user ? (
              <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2">
                <Button variant="outline" asChild className="justify-center">
                  <a href="/store/account">
                    <User className="h-4 w-4 mr-2" />
                    <span className="hidden sm:inline">{user.username}</span>
                    <span className="sm:hidden">Account</span>
                  </a>
                </Button>
                <Button variant="outline" onClick={handleLogout} className="justify-center">
                  <LogOut className="h-4 w-4 mr-2" />
                  Logout
                </Button>
              </div>
            ) : (
              <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2">
                <Button variant="outline" asChild className="justify-center">
                  <a href="/store/login">
                    <User className="h-4 w-4 mr-2" />
                    Login
                  </a>
                </Button>
                <Button asChild className="justify-center">
                  <a href="/store/register">Register</a>
                </Button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Cart Sidebar */}
      {showCart && (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex justify-end">
          <div className="bg-white w-full sm:w-96 h-full overflow-y-auto p-4 sm:p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-bold">Shopping Cart</h2>
              <Button variant="ghost" onClick={() => setShowCart(false)}>
                ×
              </Button>
            </div>
            
            {cart.length === 0 ? (
              <div className="text-center py-8">
                <ShoppingBag className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <p className="text-muted-foreground">Your cart is empty</p>
              </div>
            ) : (
              <div className="space-y-4">
                {cart.map((item) => (
                  <div key={item.product.id} className="flex items-center gap-3 p-3 sm:p-4 border rounded-lg">
                    <img
                      src={item.product.imageUrl}
                      alt={item.product.name}
                      className="w-12 h-12 sm:w-16 sm:h-16 object-cover rounded"
                    />
                    <div className="flex-1 min-w-0">
                      <h3 className="font-medium text-sm sm:text-base truncate">{item.product.name}</h3>
                      <p className="text-sm text-muted-foreground">${Number(item.product.price).toFixed(2)}</p>
                    </div>
                    <div className="flex items-center gap-2">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => updateCartQuantity(item.product.id, item.quantity - 1)}
                        className="h-8 w-8 p-0"
                      >
                        -
                      </Button>
                      <span className="w-6 sm:w-8 text-center text-sm">{item.quantity}</span>
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => updateCartQuantity(item.product.id, item.quantity + 1)}
                        className="h-8 w-8 p-0"
                      >
                        +
                      </Button>
                    </div>
                  </div>
                ))}
                
                <div className="border-t pt-4">
                  <div className="flex justify-between mb-4">
                    <span className="font-medium">Total:</span>
                    <span className="font-bold">${getCartTotal().toFixed(2)}</span>
                  </div>
                  
                  {user ? (
                    <Button className="w-full" asChild>
                      <a href="/store/checkout">Proceed to Checkout</a>
                    </Button>
                  ) : (
                    <div className="space-y-2">
                      <Button className="w-full" asChild>
                        <a href="/store/login">Login to Checkout</a>
                      </Button>
                      <Button variant="outline" className="w-full" asChild>
                        <a href="/store/register">Register</a>
                      </Button>
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Search and Filters */}
      <div className="mb-6 space-y-4">
        <div className="flex flex-col lg:flex-row gap-4">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
            <Input
              placeholder="Search products..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
          <div className="flex flex-wrap gap-2">
            {categories.map((category) => (
              <Button
                key={category}
                variant={selectedCategory === category ? "default" : "outline"}
                size="sm"
                onClick={() => setSelectedCategory(category)}
                className="text-xs sm:text-sm"
              >
                {category === "all" ? "All" : category}
              </Button>
            ))}
          </div>
        </div>
      </div>

      {/* Features */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-8">
        <Card>
          <CardContent className="flex items-center space-x-3 p-4">
            <Truck className="h-5 w-5 text-primary" />
            <div>
              <p className="font-medium">Free Shipping</p>
              <p className="text-sm text-muted-foreground">On orders over $100</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center space-x-3 p-4">
            <Package className="h-5 w-5 text-primary" />
            <div>
              <p className="font-medium">Cash on Delivery</p>
              <p className="text-sm text-muted-foreground">Pay when you receive</p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Products Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
        {filteredProducts.map((product) => (
          <Card key={product.id} className="overflow-hidden">
            <div className="aspect-video bg-muted">
              <img
                src={product.imageUrl || "https://via.placeholder.com/300x200?text=Product"}
                alt={product.name}
                className="w-full h-full object-cover"
              />
            </div>
            <CardHeader className="pb-3">
              <div className="flex items-start justify-between">
                <CardTitle className="text-lg">{product.name}</CardTitle>
                <Badge variant={product.stock > 0 ? "default" : "secondary"}>
                  {product.stock > 0 ? "In Stock" : "Out of Stock"}
                </Badge>
              </div>
              <CardDescription className="line-clamp-2">
                {product.description}
              </CardDescription>
              <div className="text-sm text-muted-foreground">
                {product.brand} {product.model}
              </div>
            </CardHeader>
            <CardContent className="pt-0">
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center space-x-1">
                  <Star className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                  <span className="text-sm">4.5</span>
                </div>
                <span className="text-lg font-bold">${Number(product.price).toFixed(2)}</span>
              </div>
              <Button 
                className="w-full" 
                disabled={product.stock === 0}
                onClick={() => addToCart(product)}
              >
                <ShoppingCart className="h-4 w-4 mr-2" />
                {product.stock > 0 ? "Add to Cart" : "Out of Stock"}
              </Button>
            </CardContent>
          </Card>
        ))}
      </div>

      {filteredProducts.length === 0 && (
        <div className="text-center py-12">
          <Package className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
          <h3 className="text-lg font-medium mb-2">No products found</h3>
          <p className="text-muted-foreground">
            Try adjusting your search or filter criteria
          </p>
        </div>
      )}
    </div>
  )
} 