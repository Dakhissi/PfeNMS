import { useState, useContext, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { useToast } from "@/hooks/use-toast"
import { 
  ShoppingBag, 
  Truck, 
  ArrowLeft,
  CheckCircle,
  Package
} from "lucide-react"
import { StoreAuthContext } from "@/lib/store-auth-context-definition"

interface Product {
  id: string
  name: string
  price: number
  imageUrl: string
}

interface CartItem {
  product: Product
  quantity: number
}

interface CheckoutForm {
  address: string
  phone: string
  deliveryMethod: string
  notes: string
}

export function StoreCheckoutPage() {
  const [formData, setFormData] = useState<CheckoutForm>({
    address: "",
    phone: "",
    deliveryMethod: "standard",
    notes: ""
  })
  const [loading, setLoading] = useState(false)
  const [success, setSuccess] = useState(false)
  const [error, setError] = useState("")
  const [orderId, setOrderId] = useState("")
  const [cart, setCart] = useState<CartItem[]>([])
  
  const authContext = useContext(StoreAuthContext)
  const user = authContext?.user
  const { toast } = useToast()

  // Load cart from localStorage
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

  const getCartTotal = () => {
    return cart.reduce((total, item) => {
      const price = typeof item.product.price === 'string' ? parseFloat(item.product.price) : Number(item.product.price)
      return total + (price * item.quantity)
    }, 0)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    setLoading(true)

    try {
      const token = localStorage.getItem("store_token")
      const orderData = {
        items: cart.map(item => ({
          productId: item.product.id,
          quantity: item.quantity
        })),
        address: formData.address,
        phone: formData.phone,
        deliveryMethod: formData.deliveryMethod,
        notes: formData.notes
      }
      
      console.log("Submitting order:", orderData)
      
      const response = await fetch("http://localhost:3001/api/orders", {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify(orderData)
      })

      if (response.ok) {
        const data = await response.json()
        console.log("Order created successfully:", data)
        setOrderId(data.order.id)
        setSuccess(true)
        
        // Show success toast
        toast({
          title: "Order Placed Successfully!",
          description: `Order #${data.order.id.slice(-8)} has been placed. Total: $${getCartTotal().toFixed(2)}`,
          variant: "success",
        })
        
        // Clear cart after successful order
        localStorage.removeItem("store_cart")
        setCart([])
      } else {
        const errorData = await response.json()
        console.error("Order creation failed:", response.status, errorData)
        setError(errorData.error || "Failed to place order")
        
        // Show error toast
        toast({
          title: "Order Failed",
          description: errorData.error || "Failed to place order. Please try again.",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Network error during order creation:", error)
      setError("Network error. Please try again.")
      
      // Show error toast
      toast({
        title: "Network Error",
        description: "Network error. Please try again.",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const handleInputChange = (field: keyof CheckoutForm, value: string) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }))
  }

  if (!user) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold mb-2">Access Denied</h2>
          <p className="text-muted-foreground mb-4">Please log in to checkout.</p>
          <Button asChild>
            <a href="/store/login">Login</a>
          </Button>
        </div>
      </div>
    )
  }

  if (cart.length === 0) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-2xl mx-auto text-center">
          <ShoppingBag className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
          <h2 className="text-2xl font-bold mb-2">Your cart is empty</h2>
          <p className="text-muted-foreground mb-6">
            Add some products to your cart before checking out.
          </p>
          <Button asChild>
            <a href="/store">Continue Shopping</a>
          </Button>
        </div>
      </div>
    )
  }

  if (success) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-2xl mx-auto">
          <Card>
            <CardHeader className="text-center">
              <div className="flex justify-center mb-4">
                <CheckCircle className="h-16 w-16 text-green-500" />
              </div>
              <CardTitle className="text-2xl font-bold">Order Placed Successfully!</CardTitle>
              <CardDescription>
                Thank you for your order. We'll process it and contact you soon.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="text-center">
                <p className="text-lg font-medium">Order #{orderId.slice(-8)}</p>
                <p className="text-muted-foreground">Total: ${getCartTotal().toFixed(2)}</p>
              </div>
              
              <div className="bg-green-50 p-4 rounded-lg">
                <h3 className="font-medium mb-2">What happens next?</h3>
                <ul className="text-sm space-y-1 text-muted-foreground">
                  <li>• We'll confirm your order within 24 hours</li>
                  <li>• You'll receive updates on your order status</li>
                  <li>• Payment will be collected on delivery</li>
                  <li>• Delivery typically takes 3-5 business days</li>
                </ul>
              </div>

              <div className="flex gap-4">
                <Button className="flex-1" asChild>
                  <a href="/store/account">View Order History</a>
                </Button>
                <Button variant="outline" className="flex-1" asChild>
                  <a href="/store">Continue Shopping</a>
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center gap-4 mb-4">
            <Button variant="outline" asChild>
              <a href="/store">
                <ArrowLeft className="h-4 w-4 mr-2" />
                Back to Store
              </a>
            </Button>
            <div>
              <h1 className="text-3xl font-bold mb-2">Checkout</h1>
              <p className="text-muted-foreground">
                Complete your order with delivery information
              </p>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Order Summary */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <ShoppingBag className="h-5 w-5" />
                Order Summary
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {cart.map((item) => (
                <div key={item.product.id} className="flex items-center gap-4 p-4 border rounded-lg">
                  <img
                    src={item.product.imageUrl || "https://via.placeholder.com/100x100?text=Product"}
                    alt={item.product.name}
                    className="w-16 h-16 object-cover rounded"
                    onError={(e) => {
                      const target = e.target as HTMLImageElement
                      target.src = "https://via.placeholder.com/100x100?text=Product"
                    }}
                  />
                  <div className="flex-1">
                    <h3 className="font-medium">{item.product.name}</h3>
                    <p className="text-sm text-muted-foreground">
                      Quantity: {item.quantity}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="font-medium">${(Number(item.product.price) * item.quantity).toFixed(2)}</p>
                    <p className="text-sm text-muted-foreground">
                      ${Number(item.product.price).toFixed(2)} each
                    </p>
                  </div>
                </div>
              ))}
              
              <div className="border-t pt-4">
                <div className="flex justify-between items-center">
                  <span className="text-lg font-medium">Total:</span>
                  <span className="text-2xl font-bold">${getCartTotal().toFixed(2)}</span>
                </div>
                <p className="text-sm text-muted-foreground mt-2">
                  Payment will be collected on delivery
                </p>
              </div>
            </CardContent>
          </Card>

          {/* Checkout Form */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Truck className="h-5 w-5" />
                Delivery Information
              </CardTitle>
              <CardDescription>
                Provide your delivery details for order processing
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-4">
                {error && (
                  <Alert className="border-red-200 bg-red-50 text-red-800">
                    <AlertDescription>{error}</AlertDescription>
                  </Alert>
                )}

                <div className="space-y-2">
                  <Label htmlFor="address">Delivery Address</Label>
                  <Textarea
                    id="address"
                    placeholder="Enter your full delivery address"
                    value={formData.address}
                    onChange={(e) => handleInputChange('address', e.target.value)}
                    required
                    disabled={loading}
                    rows={3}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="phone">Phone Number</Label>
                  <Input
                    id="phone"
                    type="tel"
                    placeholder="Enter your phone number"
                    value={formData.phone}
                    onChange={(e) => handleInputChange('phone', e.target.value)}
                    required
                    disabled={loading}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="deliveryMethod">Delivery Method</Label>
                  <select
                    id="deliveryMethod"
                    value={formData.deliveryMethod}
                    onChange={(e) => handleInputChange('deliveryMethod', e.target.value)}
                    disabled={loading}
                    className="w-full p-2 border rounded-md"
                  >
                    <option value="standard">Standard Delivery (3-5 days)</option>
                    <option value="express">Express Delivery (1-2 days)</option>
                  </select>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="notes">Order Notes (Optional)</Label>
                  <Textarea
                    id="notes"
                    placeholder="Any special instructions or notes for your order"
                    value={formData.notes}
                    onChange={(e) => handleInputChange('notes', e.target.value)}
                    disabled={loading}
                    rows={2}
                  />
                </div>

                <div className="bg-blue-50 p-4 rounded-lg">
                  <h3 className="font-medium mb-2 flex items-center gap-2">
                    <Package className="h-4 w-4" />
                    Cash on Delivery
                  </h3>
                  <p className="text-sm text-muted-foreground">
                    Payment will be collected when your order is delivered. 
                    No upfront payment required.
                  </p>
                </div>

                <Button type="submit" className="w-full" disabled={loading}>
                  {loading ? "Placing Order..." : "Place Order"}
                </Button>
              </form>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
} 