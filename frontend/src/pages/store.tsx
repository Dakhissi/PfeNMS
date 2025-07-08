import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Search, ShoppingCart, Star, Package, Truck } from "lucide-react"

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

export function StorePage() {
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState("")
  const [selectedCategory, setSelectedCategory] = useState<string>("all")

  const API_BASE_URL = "http://localhost:3001/api"

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const response = await fetch(`${API_BASE_URL}/products`)
        const data = await response.json()
        
        if (data.products) {
          setProducts(data.products)
        } else {
          // Fallback to mock data if API is not available
          const mockProducts: Product[] = [
            {
              id: "1",
              name: "Network Switch Pro",
              description: "High-performance network switch with 24 ports",
              price: 299.99,
              category: "Network Switches",
              imageUrl: "https://via.placeholder.com/300x200?text=Network+Switch",
              brand: "Cisco",
              model: "WS-C2960X-24LPS-L",
              stock: 15,
              isActive: true
            },
            {
              id: "2",
              name: "Wireless Router",
              description: "Dual-band wireless router with advanced security",
              price: 149.99,
              category: "Network Routers",
              imageUrl: "https://via.placeholder.com/300x200?text=Wireless+Router",
              brand: "Juniper",
              model: "SRX300",
              stock: 10,
              isActive: true
            },
            {
              id: "3",
              name: "Network Cable Set",
              description: "Premium CAT6 ethernet cables, 10-pack",
              price: 29.99,
              category: "Cables",
              imageUrl: "https://via.placeholder.com/300x200?text=Network+Cables",
              brand: "Generic",
              model: "CAT6-10PK",
              stock: 50,
              isActive: true
            },
            {
              id: "4",
              name: "Network Monitoring Tool",
              description: "Professional network monitoring software license",
              price: 199.99,
              category: "Software",
              imageUrl: "https://via.placeholder.com/300x200?text=Monitoring+Tool",
              brand: "Monitoring Pro",
              model: "NMS-2024",
              stock: 0,
              isActive: false
            }
          ]
          setProducts(mockProducts)
        }
        setLoading(false)
      } catch (error) {
        console.error("Failed to fetch products:", error)
        setLoading(false)
      }
    }

    fetchProducts()
  }, [])

  const filteredProducts = products.filter(product => {
    const matchesSearch = product.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         product.description.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesCategory = selectedCategory === "all" || product.category === selectedCategory
    return matchesSearch && matchesCategory && product.isActive
  })

  const categories = ["all", "Network Switches", "Network Routers", "Wireless Access Points", "Cables", "Software"]

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
      {/* Page Title */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">Network Manager Store</h1>
        <p className="text-muted-foreground">
          Discover premium networking equipment and tools for your infrastructure. 
          All orders are cash on delivery for your convenience.
        </p>
      </div>

      {/* Search and Filters */}
      <div className="mb-6 space-y-4">
        <div className="flex gap-4">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
            <Input
              placeholder="Search products..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
          <div className="flex gap-2">
            {categories.map((category) => (
              <Button
                key={category}
                variant={selectedCategory === category ? "default" : "outline"}
                size="sm"
                onClick={() => setSelectedCategory(category)}
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
                <span className="text-lg font-bold">${product.price}</span>
              </div>
              <Button 
                className="w-full" 
                disabled={product.stock === 0}
                onClick={() => {
                  // Add to cart functionality
                  console.log(`Added ${product.name} to cart`)
                }}
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