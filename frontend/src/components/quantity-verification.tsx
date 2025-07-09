import { useState, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Package, RefreshCw } from "lucide-react"

interface Product {
  id: string
  name: string
  price: number
  stock: number
  category: string
  isActive: boolean
}

export function QuantityVerification() {
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(false)

  const fetchProducts = async () => {
    setLoading(true)
    try {
      const response = await fetch("http://localhost:3001/api/products")
      if (response.ok) {
        const data = await response.json()
        setProducts(data.products || [])
      }
    } catch (error) {
      console.error("Failed to fetch products:", error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchProducts()
  }, [])

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Package className="h-5 w-5" />
          Product Stock Verification
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div className="mb-4">
          <Button onClick={fetchProducts} disabled={loading}>
            <RefreshCw className={`h-4 w-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
            Refresh Stock Levels
          </Button>
        </div>
        
        <div className="space-y-2">
          {products.map((product) => (
            <div key={product.id} className="flex items-center justify-between p-3 border rounded-lg">
              <div>
                <p className="font-medium">{product.name}</p>
                <p className="text-sm text-muted-foreground">{product.category}</p>
              </div>
              <div className="flex items-center gap-2">
                <Badge variant={product.stock > 0 ? "default" : "secondary"}>
                  Stock: {product.stock}
                </Badge>
                <p className="font-medium">${product.price}</p>
              </div>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  )
} 