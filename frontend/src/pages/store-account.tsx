import { useState, useEffect, useContext } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { 
  User, 
  ShoppingBag, 
  Package, 
  Truck, 
  CheckCircle, 
  Clock, 
  AlertCircle,
  LogOut,
  ArrowLeft,
  Eye
} from "lucide-react"
import { StoreAuthContext } from "@/lib/store-auth-context-definition"
import { ProductDetailsModal } from "@/components/product-details-modal"

interface Order {
  id: string
  status: string
  total: number
  createdAt: string
  address?: string
  phone?: string
  deliveryMethod?: string
  notes?: string
  items: Array<{
    productId: string
    productName: string
    quantity: number
    price: number
  }>
}

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

export function StoreAccountPage() {
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)
  const [editingOrder, setEditingOrder] = useState<string | null>(null)
  const [editForm, setEditForm] = useState({
    address: "",
    phone: "",
    deliveryMethod: "standard",
    notes: ""
  })
  const [productModalOpen, setProductModalOpen] = useState(false)
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null)
  const authContext = useContext(StoreAuthContext)
  const user = authContext?.user

  const API_BASE_URL = "http://localhost:3001/api"

  useEffect(() => {
    const fetchOrders = async () => {
      if (!user) return

      try {
        const token = localStorage.getItem("store_token")
        console.log("Fetching orders for user:", user.id)
        
        const response = await fetch(`${API_BASE_URL}/orders/my-orders`, {
          headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
          }
        })
        
        if (response.ok) {
          const data = await response.json()
          console.log("Orders fetched successfully:", data)
          setOrders(data.orders || [])
        } else {
          console.error("Failed to fetch orders:", response.status, response.statusText)
          const errorData = await response.json().catch(() => ({}))
          console.error("Error details:", errorData)
        }
      } catch (error) {
        console.error("Failed to fetch orders:", error)
      } finally {
        setLoading(false)
      }
    }

    fetchOrders()
  }, [user])

  const handleLogout = () => {
    authContext?.logout()
  }

  const startEditOrder = (order: Order) => {
    setEditingOrder(order.id)
    setEditForm({
      address: order.address || "",
      phone: order.phone || "",
      deliveryMethod: order.deliveryMethod || "standard",
      notes: order.notes || ""
    })
  }

  const cancelEditOrder = () => {
    setEditingOrder(null)
    setEditForm({
      address: "",
      phone: "",
      deliveryMethod: "standard",
      notes: ""
    })
  }

  const updateOrder = async (orderId: string) => {
    try {
      const token = localStorage.getItem("store_token")
      console.log("Updating order:", orderId, "with data:", editForm)
      
      const response = await fetch(`${API_BASE_URL}/orders/my-orders/${orderId}/update`, {
        method: "PUT",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify(editForm)
      })

      console.log("Update response status:", response.status)
      
      if (response.ok) {
        const data = await response.json()
        console.log("Order updated successfully:", data)
        
        // Refresh orders
        const ordersResponse = await fetch(`${API_BASE_URL}/orders/my-orders`, {
          headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
          }
        })
        if (ordersResponse.ok) {
          const ordersData = await ordersResponse.json()
          setOrders(ordersData.orders || [])
        }
        setEditingOrder(null)
      } else {
        const errorData = await response.json().catch(() => ({}))
        console.error("Failed to update order:", response.status, errorData)
      }
    } catch (error) {
      console.error("Error updating order:", error)
    }
  }

  const cancelOrder = async (orderId: string) => {
    if (!confirm("Are you sure you want to cancel this order?")) return

    try {
      const token = localStorage.getItem("store_token")
      console.log("Cancelling order:", orderId)
      
      const response = await fetch(`${API_BASE_URL}/orders/my-orders/${orderId}/cancel`, {
        method: "PUT",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        }
      })

      console.log("Cancel response status:", response.status)
      
      if (response.ok) {
        const data = await response.json()
        console.log("Order cancelled successfully:", data)
        
        // Refresh orders
        const ordersResponse = await fetch(`${API_BASE_URL}/orders/my-orders`, {
          headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
          }
        })
        if (ordersResponse.ok) {
          const ordersData = await ordersResponse.json()
          setOrders(ordersData.orders || [])
        }
      } else {
        const errorData = await response.json().catch(() => ({}))
        console.error("Failed to cancel order:", response.status, errorData)
      }
    } catch (error) {
      console.error("Error cancelling order:", error)
    }
  }

  const viewProductDetails = async (productId: string) => {
    try {
      console.log("Fetching product details for ID:", productId)
      const response = await fetch(`${API_BASE_URL}/products/${productId}`)
      console.log("Product API response status:", response.status)
      
      if (response.ok) {
        const data = await response.json()
        console.log("Product API response data:", data)
        setSelectedProduct(data.product) // Extract product from { product }
        setProductModalOpen(true)
        console.log("Product modal opened with product:", data.product)
      } else {
        console.error("Failed to fetch product details")
        const errorData = await response.json().catch(() => ({}))
        console.error("Error details:", errorData)
      }
    } catch (error) {
      console.error("Error fetching product details:", error)
    }
  }

  const closeProductModal = () => {
    setProductModalOpen(false)
    setSelectedProduct(null)
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "PENDING":
        return <Clock className="h-4 w-4 text-yellow-500" />
      case "CONFIRMED":
        return <Package className="h-4 w-4 text-blue-500" />
      case "SHIPPED":
        return <Truck className="h-4 w-4 text-purple-500" />
      case "DELIVERED":
        return <CheckCircle className="h-4 w-4 text-green-500" />
      case "CANCELLED":
        return <AlertCircle className="h-4 w-4 text-red-500" />
      default:
        return <Clock className="h-4 w-4 text-gray-500" />
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case "PENDING":
        return "bg-yellow-100 text-yellow-800"
      case "CONFIRMED":
        return "bg-blue-100 text-blue-800"
      case "SHIPPED":
        return "bg-purple-100 text-purple-800"
      case "DELIVERED":
        return "bg-green-100 text-green-800"
      case "CANCELLED":
        return "bg-red-100 text-red-800"
      default:
        return "bg-gray-100 text-gray-800"
    }
  }

  if (!user) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold mb-2">Access Denied</h2>
          <p className="text-muted-foreground mb-4">Please log in to view your account.</p>
          <Button asChild>
            <a href="/store/login">Login</a>
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div className="flex flex-col sm:flex-row sm:items-center gap-4">
            <Button variant="outline" asChild className="w-fit">
              <a href="/store">
                <ArrowLeft className="h-4 w-4 mr-2" />
                Back to Store
              </a>
            </Button>
            <div>
              <h1 className="text-2xl sm:text-3xl font-bold mb-2">My Account</h1>
              <p className="text-muted-foreground text-sm sm:text-base">
                Manage your profile and view order history
              </p>
            </div>
          </div>
          <Button variant="outline" onClick={handleLogout} className="w-fit">
            <LogOut className="h-4 w-4 mr-2" />
            Logout
          </Button>
        </div>
      </div>

      <Tabs defaultValue="profile" className="space-y-6">
        <TabsList>
          <TabsTrigger value="profile">Profile</TabsTrigger>
          <TabsTrigger value="orders">Order History</TabsTrigger>
        </TabsList>

        {/* Profile Tab */}
        <TabsContent value="profile" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <User className="h-5 w-5" />
                Account Information
              </CardTitle>
              <CardDescription>
                Your personal information and account details
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium text-muted-foreground">Username</label>
                  <p className="text-lg">{user.username}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-muted-foreground">Email</label>
                  <p className="text-lg">{user.email}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-muted-foreground">Role</label>
                  <p className="text-lg capitalize">{user.role.toLowerCase()}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-muted-foreground">Member Since</label>
                  <p className="text-lg">
                    {new Date().toLocaleDateString()}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Orders Tab */}
        <TabsContent value="orders" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <ShoppingBag className="h-5 w-5" />
                Order History
              </CardTitle>
              <CardDescription>
                View all your past orders and their current status
              </CardDescription>
            </CardHeader>
            <CardContent>
              {loading ? (
                <div className="text-center py-8">
                  <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
                  <p>Loading orders...</p>
                </div>
              ) : orders.length === 0 ? (
                <div className="text-center py-8">
                  <ShoppingBag className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <h3 className="text-lg font-medium mb-2">No orders yet</h3>
                  <p className="text-muted-foreground mb-4">
                    Start shopping to see your order history here
                  </p>
                  <Button asChild>
                    <a href="/store">Browse Products</a>
                  </Button>
                </div>
              ) : (
                <div className="space-y-4">
                  {orders.map((order) => (
                    <div key={order.id} className="border rounded-lg p-4">
                      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-4">
                        <div className="flex items-center gap-3">
                          <div className="flex items-center gap-2">
                            {getStatusIcon(order.status)}
                            <Badge className={getStatusColor(order.status)}>
                              {order.status}
                            </Badge>
                          </div>
                          <div>
                            <p className="font-medium">Order #{order.id.slice(-8)}</p>
                            <p className="text-sm text-muted-foreground">
                              {new Date(order.createdAt).toLocaleDateString()}
                            </p>
                          </div>
                        </div>
                        <div className="text-left sm:text-right">
                          <p className="font-bold text-lg">${Number(order.total).toFixed(2)}</p>
                          <p className="text-sm text-muted-foreground">
                            {order.items.length} item{order.items.length !== 1 ? 's' : ''}
                          </p>
                        </div>
                      </div>

                      {/* Delivery Information */}
                      {(order.address || order.phone || order.deliveryMethod) && (
                        <div className="mb-4 p-3 bg-gray-50 rounded-lg">
                          <h4 className="font-medium text-sm mb-2">Delivery Information</h4>
                          {order.address && (
                            <p className="text-sm text-muted-foreground mb-1">
                              <span className="font-medium">Address:</span> {order.address}
                            </p>
                          )}
                          {order.phone && (
                            <p className="text-sm text-muted-foreground mb-1">
                              <span className="font-medium">Phone:</span> {order.phone}
                            </p>
                          )}
                          {order.deliveryMethod && (
                            <p className="text-sm text-muted-foreground mb-1">
                              <span className="font-medium">Delivery:</span> {order.deliveryMethod}
                            </p>
                          )}
                          {order.notes && (
                            <p className="text-sm text-muted-foreground">
                              <span className="font-medium">Notes:</span> {order.notes}
                            </p>
                          )}
                        </div>
                      )}

                      {/* Edit Form for Pending Orders */}
                      {editingOrder === order.id && order.status === "PENDING" && (
                        <div className="mb-4 p-4 border rounded-lg bg-blue-50">
                          <h4 className="font-medium mb-3">Edit Order Details</h4>
                          <div className="space-y-3">
                            <div>
                              <label className="text-sm font-medium">Address</label>
                              <input
                                type="text"
                                value={editForm.address}
                                onChange={(e) => setEditForm(prev => ({ ...prev, address: e.target.value }))}
                                className="w-full p-2 border rounded-md text-sm"
                                placeholder="Delivery address"
                              />
                            </div>
                            <div>
                              <label className="text-sm font-medium">Phone</label>
                              <input
                                type="tel"
                                value={editForm.phone}
                                onChange={(e) => setEditForm(prev => ({ ...prev, phone: e.target.value }))}
                                className="w-full p-2 border rounded-md text-sm"
                                placeholder="Phone number"
                              />
                            </div>
                            <div>
                              <label className="text-sm font-medium">Delivery Method</label>
                              <select
                                value={editForm.deliveryMethod}
                                onChange={(e) => setEditForm(prev => ({ ...prev, deliveryMethod: e.target.value }))}
                                className="w-full p-2 border rounded-md text-sm"
                              >
                                <option value="standard">Standard Delivery</option>
                                <option value="express">Express Delivery</option>
                              </select>
                            </div>
                            <div>
                              <label className="text-sm font-medium">Notes</label>
                              <textarea
                                value={editForm.notes}
                                onChange={(e) => setEditForm(prev => ({ ...prev, notes: e.target.value }))}
                                className="w-full p-2 border rounded-md text-sm"
                                rows={2}
                                placeholder="Additional notes"
                              />
                            </div>
                            <div className="flex flex-col sm:flex-row gap-2">
                              <Button size="sm" onClick={() => updateOrder(order.id)}>
                                Save Changes
                              </Button>
                              <Button size="sm" variant="outline" onClick={cancelEditOrder}>
                                Cancel
                              </Button>
                            </div>
                          </div>
                        </div>
                      )}
                      
                      <div className="space-y-2">
                        {order.items.map((item, index) => (
                          <div key={index} className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 text-sm">
                            <div className="flex-1">
                              <p className="font-medium">{item.productName}</p>
                              <p className="text-muted-foreground">
                                Quantity: {item.quantity} × ${Number(item.price).toFixed(2)}
                              </p>
                            </div>
                            <div className="flex items-center gap-2">
                              <p className="font-medium">
                                ${(item.quantity * Number(item.price)).toFixed(2)}
                              </p>
                              <Button
                                size="sm"
                                variant="outline"
                                onClick={() => viewProductDetails(item.productId)}
                              >
                                <Eye className="h-3 w-3" />
                              </Button>
                            </div>
                          </div>
                        ))}
                      </div>

                      {/* Action Buttons for Pending Orders */}
                      {order.status === "PENDING" && (
                        <div className="mt-4 flex flex-col sm:flex-row gap-2">
                          <Button size="sm" variant="outline" onClick={() => startEditOrder(order)}>
                            Edit Order
                          </Button>
                          <Button size="sm" variant="destructive" onClick={() => cancelOrder(order.id)}>
                            Cancel Order
                          </Button>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* Product Details Modal */}
      <ProductDetailsModal
        isOpen={productModalOpen}
        onClose={closeProductModal}
        product={selectedProduct}
      />
    </div>
  )
} 