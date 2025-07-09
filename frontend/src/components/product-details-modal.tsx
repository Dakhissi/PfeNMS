import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { 
  Package, 
  DollarSign, 
  Hash, 
  Calendar,
  Star,
  ShoppingCart
} from "lucide-react"

interface Product {
  id: string
  name: string
  description: string
  price: number
  stock: number
  category: string
  brand?: string
  model?: string
  imageUrl?: string
  specifications?: Record<string, unknown>
  isActive: boolean
  createdAt?: string
  updatedAt?: string
}

interface ProductDetailsModalProps {
  isOpen: boolean
  onClose: () => void
  product: Product | null
}

export function ProductDetailsModal({ isOpen, onClose, product }: ProductDetailsModalProps) {
  if (!product) return null

  // Ensure price is a number
  const price = typeof product.price === 'number' ? product.price : parseFloat(product.price) || 0

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[600px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Package className="h-5 w-5" />
            Product Details
          </DialogTitle>
          <DialogDescription>
            View detailed information about this product
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-6">
          {/* Product Image */}
          {product.imageUrl && (
            <div className="flex justify-center">
              <img
                src={product.imageUrl}
                alt={product.name}
                className="w-48 h-48 object-cover rounded-lg border"
                onError={(e) => {
                  e.currentTarget.style.display = 'none'
                }}
              />
            </div>
          )}

          {/* Product Info */}
          <div className="space-y-4">
            <div>
              <h3 className="text-xl font-semibold">{product.name}</h3>
              <div className="flex items-center gap-2 mt-2">
                <Badge variant={product.isActive ? "default" : "secondary"}>
                  {product.isActive ? "Active" : "Inactive"}
                </Badge>
                <Badge variant="outline">{product.category}</Badge>
                {product.brand && (
                  <Badge variant="outline">{product.brand}</Badge>
                )}
              </div>
              {product.model && (
                <p className="text-sm text-muted-foreground mt-1">
                  Model: {product.model}
                </p>
              )}
            </div>

            <div>
              <h4 className="font-medium mb-2">Description</h4>
              <p className="text-muted-foreground">{product.description}</p>
            </div>

            {/* Product Stats */}
            <div className="grid grid-cols-2 gap-4">
              <div className="flex items-center gap-2 p-3 border rounded-lg">
                <DollarSign className="h-4 w-4 text-green-600" />
                <div>
                  <p className="text-sm text-muted-foreground">Price</p>
                  <p className="font-semibold">${price.toFixed(2)}</p>
                </div>
              </div>

              <div className="flex items-center gap-2 p-3 border rounded-lg">
                <Hash className="h-4 w-4 text-blue-600" />
                <div>
                  <p className="text-sm text-muted-foreground">Stock</p>
                  <p className="font-semibold">{product.stock} units</p>
                </div>
              </div>
            </div>

            {/* Rating */}
            <div className="flex items-center gap-2 p-3 border rounded-lg">
              <Star className="h-4 w-4 text-yellow-500" />
              <div>
                <p className="text-sm text-muted-foreground">Rating</p>
                <div className="flex items-center gap-1">
                  <span className="font-semibold">4.5</span>
                  <span className="text-sm text-muted-foreground">/ 5.0</span>
                </div>
              </div>
            </div>

            {/* Timestamps */}
            {(product.createdAt || product.updatedAt) && (
              <div className="space-y-2">
                <h4 className="font-medium">Timestamps</h4>
                <div className="grid grid-cols-2 gap-4 text-sm">
                  {product.createdAt && (
                    <div className="flex items-center gap-2">
                      <Calendar className="h-4 w-4 text-muted-foreground" />
                      <div>
                        <p className="text-muted-foreground">Created</p>
                        <p>{new Date(product.createdAt).toLocaleDateString()}</p>
                      </div>
                    </div>
                  )}
                  {product.updatedAt && (
                    <div className="flex items-center gap-2">
                      <Calendar className="h-4 w-4 text-muted-foreground" />
                      <div>
                        <p className="text-muted-foreground">Updated</p>
                        <p>{new Date(product.updatedAt).toLocaleDateString()}</p>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>

          {/* Actions */}
          <div className="flex justify-end gap-2 pt-4 border-t">
            <Button variant="outline" onClick={onClose}>
              Close
            </Button>
            <Button asChild>
              <a href="/store">
                <ShoppingCart className="h-4 w-4 mr-2" />
                View in Store
              </a>
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
} 