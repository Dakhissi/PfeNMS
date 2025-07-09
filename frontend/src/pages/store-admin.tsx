import { useState, useEffect, useContext } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { 
  Users, 
  Package, 
  ShoppingCart, 
  DollarSign,
  Eye,
  Edit,
  Trash2,
  Plus
} from "lucide-react"
import { StoreAuthContext } from "@/lib/store-auth-context-definition"
import { ProductModal } from "@/components/product-modal"
import { ProductViewModal } from "@/components/product-view-modal"
import { OrderViewModal } from "@/components/order-view-modal"
import { OrderFilters } from "@/components/order-filters"
import { QuantityVerification } from "@/components/quantity-verification"

interface DashboardStats {
  totalUsers: number
  totalProducts: number
  totalOrders: number
  totalRevenue: number
  recentOrders: Order[]
  topProducts: Product[]
}

interface User {
  id: string
  email: string
  username: string
  role: string
  createdAt: string
  isOnline: boolean
}

interface Product {
  id: string
  name: string
  description: string
  price: number
  stock: number
  category: string
  isActive: boolean
  imageUrl?: string
  createdAt?: string
  updatedAt?: string
}

interface Order {
  id: string
  userId: string
  status: string
  total: number
  createdAt: string
  user: {
    username: string
    email: string
  }
  address?: string
  phone?: string
  deliveryMethod?: string
  items?: Array<{
    productId: string
    productName: string
    quantity: number
    price: number
  }>
}

interface OrderFilters {
  status: string
  dateFrom: string
  dateTo: string
  searchTerm: string
  minAmount: string
  maxAmount: string
}

export function StoreAdminPage() {
  const authContext = useContext(StoreAuthContext)
  const user = authContext?.user
  const [activeTab, setActiveTab] = useState("dashboard")
  const [loading, setLoading] = useState(true)
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [users, setUsers] = useState<User[]>([])
  const [products, setProducts] = useState<Product[]>([])
  const [orders, setOrders] = useState<Order[]>([])
  const [filteredOrders, setFilteredOrders] = useState<Order[]>([])
  
  // Product modal states
  const [productModalOpen, setProductModalOpen] = useState(false)
  const [productViewModalOpen, setProductViewModalOpen] = useState(false)
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null)
  const [productModalMode, setProductModalMode] = useState<'add' | 'edit'>('add')
  
  // Order modal states
  const [orderViewModalOpen, setOrderViewModalOpen] = useState(false)
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null)
  
  // Order filters
  const [orderFilters, setOrderFilters] = useState<OrderFilters>({
    status: "",
    dateFrom: "",
    dateTo: "",
    searchTerm: "",
    minAmount: "",
    maxAmount: ""
  })

  const API_BASE_URL = "http://localhost:3001/api"

  useEffect(() => {
    const checkAuthAndLoadData = async () => {
      // Wait for auth context to be ready
      if (authContext?.isLoading) {
        return
      }

      // Check if we have a token
      const token = localStorage.getItem("store_token")
      if (!token) {
        window.location.href = "/store/admin/login"
        return
      }

      // If we have a token but no user yet, wait a bit more
      if (!user && token) {
        return
      }

      if (!user) {
        // If no user, redirect to login
        window.location.href = "/store/admin/login"
        return
      }

      if (user.role !== "ADMIN") {
        // Redirect non-admin users
        window.location.href = "/store"
        return
      }

      // User is authenticated and is admin, load dashboard data
      await fetchDashboardData()
    }

    checkAuthAndLoadData()
  }, [user, authContext?.isLoading])

  // Apply filters to orders
  useEffect(() => {
    let filtered = [...orders]

    // Status filter
    if (orderFilters.status && orderFilters.status !== "ALL") {
      filtered = filtered.filter(order => order.status === orderFilters.status)
    }

    // Search filter
    if (orderFilters.searchTerm) {
      const searchTerm = orderFilters.searchTerm.toLowerCase()
      filtered = filtered.filter(order => 
        order.id.toLowerCase().includes(searchTerm) ||
        order.user.username.toLowerCase().includes(searchTerm) ||
        order.user.email.toLowerCase().includes(searchTerm)
      )
    }

    // Date range filter
    if (orderFilters.dateFrom) {
      const fromDate = new Date(orderFilters.dateFrom)
      filtered = filtered.filter(order => new Date(order.createdAt) >= fromDate)
    }

    if (orderFilters.dateTo) {
      const toDate = new Date(orderFilters.dateTo)
      toDate.setHours(23, 59, 59, 999) // End of day
      filtered = filtered.filter(order => new Date(order.createdAt) <= toDate)
    }

    // Amount range filter
    if (orderFilters.minAmount) {
      const minAmount = parseFloat(orderFilters.minAmount)
      filtered = filtered.filter(order => order.total >= minAmount)
    }

    if (orderFilters.maxAmount) {
      const maxAmount = parseFloat(orderFilters.maxAmount)
      filtered = filtered.filter(order => order.total <= maxAmount)
    }

    setFilteredOrders(filtered)
  }, [orders, orderFilters])

  const fetchDashboardData = async () => {
    try {
      const token = localStorage.getItem("store_token")
      if (!token) {
        window.location.href = "/store/admin/login"
        return
      }

      const headers = {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
      }

      // Fetch dashboard analytics
      const dashboardResponse = await fetch(`${API_BASE_URL}/admin/dashboard`, { headers })
      if (!dashboardResponse.ok) {
        if (dashboardResponse.status === 401) {
          // Token expired, redirect to login
          window.location.href = "/store/admin/login"
          return
        }
        throw new Error('Failed to fetch dashboard data')
      }
      const dashboardData = await dashboardResponse.json()
      console.log('Dashboard data:', dashboardData)

      // Fetch users
      const usersResponse = await fetch(`${API_BASE_URL}/admin/users`, { headers })
      const usersData = await usersResponse.json()
      console.log('Users data:', usersData)

      // Fetch products
      const productsResponse = await fetch(`${API_BASE_URL}/products`, { headers })
      const productsData = await productsResponse.json()
      console.log('Products data:', productsData)

      // Fetch orders
      const ordersResponse = await fetch(`${API_BASE_URL}/orders/all`, { headers })
      const ordersData = await ordersResponse.json()
      console.log('Orders data:', ordersData)

      // Set stats with fallback values
      setStats({
        totalUsers: dashboardData.totalUsers || usersData.users?.length || 0,
        totalProducts: dashboardData.totalProducts || productsData.products?.length || 0,
        totalOrders: dashboardData.totalOrders || ordersData.orders?.length || 0,
        totalRevenue: dashboardData.totalRevenue || 0,
        recentOrders: dashboardData.recentOrders || ordersData.orders?.slice(0, 5) || [],
        topProducts: dashboardData.topProducts || productsData.products?.slice(0, 5) || []
      })
      setUsers(usersData.users || [])
      setProducts(productsData.products || [])
      setOrders(ordersData.orders || [])
      setLoading(false)
    } catch (error) {
      console.error("Failed to fetch admin data:", error)
      setLoading(false)
    }
  }

  const updateUserRole = async (userId: string, newRole: string) => {
    try {
      const token = localStorage.getItem("store_token")
      const response = await fetch(`${API_BASE_URL}/admin/users/${userId}/role`, {
        method: "PUT",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ role: newRole })
      })

      if (response.ok) {
        fetchDashboardData() // Refresh data
      }
    } catch (error) {
      console.error("Failed to update user role:", error)
    }
  }

  const updateProductStock = async (productId: string, newStock: number) => {
    try {
      const token = localStorage.getItem("store_token")
      const response = await fetch(`${API_BASE_URL}/admin/products/${productId}/stock`, {
        method: "PUT",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ stock: newStock })
      })

      if (response.ok) {
        fetchDashboardData() // Refresh data
      }
    } catch (error) {
      console.error("Failed to update product stock:", error)
    }
  }

  const updateOrderStatus = async (orderId: string, newStatus: string) => {
    try {
      const token = localStorage.getItem("store_token")
      const response = await fetch(`${API_BASE_URL}/orders/${orderId}/status`, {
        method: "PUT",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ status: newStatus })
      })

      if (response.ok) {
        fetchDashboardData() // Refresh data
      }
    } catch (error) {
      console.error("Failed to update order status:", error)
    }
  }

  const saveProduct = async (product: Product) => {
    try {
      const token = localStorage.getItem("store_token")
      const headers = {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
      }

      let response
      if (productModalMode === 'add') {
        // Add new product
        response = await fetch(`${API_BASE_URL}/products`, {
          method: "POST",
          headers,
          body: JSON.stringify(product)
        })
      } else {
        // Update existing product
        response = await fetch(`${API_BASE_URL}/products/${product.id}`, {
          method: "PUT",
          headers,
          body: JSON.stringify(product)
        })
      }

      if (response.ok) {
        fetchDashboardData() // Refresh data
      } else {
        throw new Error('Failed to save product')
      }
    } catch (error) {
      console.error("Failed to save product:", error)
      throw error
    }
  }

  const deleteProduct = async (productId: string) => {
    if (!confirm("Are you sure you want to delete this product?")) {
      return
    }

    try {
      const token = localStorage.getItem("store_token")
      const response = await fetch(`${API_BASE_URL}/products/${productId}`, {
        method: "DELETE",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        }
      })

      if (response.ok) {
        fetchDashboardData() // Refresh data
      }
    } catch (error) {
      console.error("Failed to delete product:", error)
    }
  }

  const openAddProductModal = () => {
    setProductModalMode('add')
    setSelectedProduct(null)
    setProductModalOpen(true)
  }

  const openEditProductModal = (product: Product) => {
    setProductModalMode('edit')
    setSelectedProduct(product)
    setProductModalOpen(true)
  }

  const openViewProductModal = (product: Product) => {
    setSelectedProduct(product)
    setProductViewModalOpen(true)
  }

  const handleEditFromView = () => {
    setProductViewModalOpen(false)
    setProductModalMode('edit')
    setProductModalOpen(true)
  }

  const openViewOrderModal = (order: Order) => {
    setSelectedOrder(order)
    setOrderViewModalOpen(true)
  }

  const handleEditOrderFromView = () => {
    setOrderViewModalOpen(false)
    // TODO: Implement order edit functionality
    console.log('Edit order:', selectedOrder)
  }

  const clearOrderFilters = () => {
    setOrderFilters({
      status: "",
      dateFrom: "",
      dateTo: "",
      searchTerm: "",
      minAmount: "",
      maxAmount: ""
    })
  }

  // Show loading while checking authentication
  if (authContext?.isLoading || (!user && localStorage.getItem("store_token"))) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
          <p>Loading admin panel...</p>
        </div>
      </div>
    )
  }

  // Show access denied for non-admin users
  if (user && user.role !== "ADMIN") {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold mb-2">Access Denied</h2>
          <p className="text-muted-foreground mb-4">You don't have permission to access the admin panel.</p>
          <Button onClick={() => window.location.href = "/store"}>
            Back to Store
          </Button>
        </div>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="flex items-center justify-center h-64">
          <div className="text-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
            <div className="text-lg">Loading admin dashboard...</div>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">Store Admin Dashboard</h1>
        <p className="text-muted-foreground">
          Manage your store, users, products, and orders
        </p>
      </div>

      {/* Stats Cards */}
      {stats && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Total Users</CardTitle>
              <Users className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stats.totalUsers}</div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Total Products</CardTitle>
              <Package className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stats.totalProducts}</div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Total Orders</CardTitle>
              <ShoppingCart className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stats.totalOrders}</div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Total Revenue</CardTitle>
              <DollarSign className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">${stats.totalRevenue?.toFixed(2) || "0.00"}</div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="dashboard">Dashboard</TabsTrigger>
          <TabsTrigger value="users">Users</TabsTrigger>
          <TabsTrigger value="products">Products</TabsTrigger>
          <TabsTrigger value="orders">Orders</TabsTrigger>
        </TabsList>

        {/* Dashboard Tab */}
        <TabsContent value="dashboard" className="space-y-6">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Recent Orders */}
            <Card>
              <CardHeader>
                <CardTitle>Recent Orders</CardTitle>
                <CardDescription>Latest orders from customers</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {stats?.recentOrders?.slice(0, 5).map((order: Order) => (
                    <div key={order.id} className="flex items-center justify-between">
                      <div>
                        <p className="font-medium">Order #{order.id.slice(-8)}</p>
                        <p className="text-sm text-muted-foreground">${order.total}</p>
                      </div>
                      <Badge variant={order.status === "DELIVERED" ? "default" : "secondary"}>
                        {order.status}
                      </Badge>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            {/* Top Products */}
            <Card>
              <CardHeader>
                <CardTitle>Top Products</CardTitle>
                <CardDescription>Best selling products</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {stats?.topProducts?.slice(0, 5).map((product: Product) => (
                    <div key={product.id} className="flex items-center justify-between">
                      <div>
                        <p className="font-medium">{product.name}</p>
                        <p className="text-sm text-muted-foreground">Stock: {product.stock}</p>
                      </div>
                      <p className="font-medium">${product.price}</p>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        {/* Users Tab */}
        <TabsContent value="users" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>User Management</CardTitle>
              <CardDescription>Manage user accounts and roles</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {users.map((user) => (
                  <div key={user.id} className="flex items-center justify-between p-4 border rounded-lg">
                    <div className="flex items-center space-x-4">
                      <div>
                        <p className="font-medium">{user.username}</p>
                        <p className="text-sm text-muted-foreground">{user.email}</p>
                      </div>
                      <Badge variant={user.isOnline ? "default" : "secondary"}>
                        {user.isOnline ? "Online" : "Offline"}
                      </Badge>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Select
                        value={user.role}
                        onValueChange={(value) => updateUserRole(user.id, value)}
                      >
                        <SelectTrigger className="w-32">
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="USER">User</SelectItem>
                          <SelectItem value="ADMIN">Admin</SelectItem>
                          <SelectItem value="MODERATOR">Moderator</SelectItem>
                          <SelectItem value="SUPPORT">Support</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Products Tab */}
        <TabsContent value="products" className="space-y-6">
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle>Product Management</CardTitle>
                  <CardDescription>Manage product inventory and details</CardDescription>
                </div>
                <Button onClick={openAddProductModal}>
                  <Plus className="h-4 w-4 mr-2" />
                  Add Product
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {products.map((product) => (
                  <div key={product.id} className="flex items-center justify-between p-4 border rounded-lg">
                    <div>
                      <p className="font-medium">{product.name}</p>
                      <p className="text-sm text-muted-foreground">{product.category}</p>
                    </div>
                    <div className="flex items-center space-x-4">
                      <div className="text-right">
                        <p className="font-medium">${product.price}</p>
                        <p className="text-sm text-muted-foreground">Stock: {product.stock}</p>
                      </div>
                      <div className="flex items-center space-x-2">
                        <Input
                          type="number"
                          value={product.stock}
                          onChange={(e) => updateProductStock(product.id, parseInt(e.target.value))}
                          className="w-20"
                        />
                        <Button 
                          size="sm" 
                          variant="outline"
                          onClick={() => openViewProductModal(product)}
                        >
                          <Eye className="h-4 w-4" />
                        </Button>
                        <Button 
                          size="sm" 
                          variant="outline"
                          onClick={() => openEditProductModal(product)}
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button 
                          size="sm" 
                          variant="outline"
                          onClick={() => deleteProduct(product.id)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
          
          {/* Stock Verification */}
          <QuantityVerification />
        </TabsContent>

        {/* Orders Tab */}
        <TabsContent value="orders" className="space-y-6">
          {/* Order Filters */}
          <OrderFilters
            filters={orderFilters}
            onFiltersChange={setOrderFilters}
            onClearFilters={clearOrderFilters}
          />

          <Card>
            <CardHeader>
              <CardTitle>Order Management</CardTitle>
              <CardDescription>
                Manage customer orders and status ({filteredOrders.length} orders)
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {filteredOrders.map((order) => (
                  <div key={order.id} className="flex items-center justify-between p-4 border rounded-lg">
                    <div>
                      <p className="font-medium">Order #{order.id.slice(-8)}</p>
                      <p className="text-sm text-muted-foreground">{order.user?.username} - {order.user?.email}</p>
                      <p className="text-sm text-muted-foreground">
                        {new Date(order.createdAt).toLocaleDateString()}
                      </p>
                    </div>
                    <div className="flex items-center space-x-4">
                      <div className="text-right">
                        <p className="font-medium">${order.total}</p>
                        <Select
                          value={order.status}
                          onValueChange={(value) => updateOrderStatus(order.id, value)}
                        >
                          <SelectTrigger className="w-32">
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="PENDING">Pending</SelectItem>
                            <SelectItem value="CONFIRMED">Confirmed</SelectItem>
                            <SelectItem value="SHIPPED">Shipped</SelectItem>
                            <SelectItem value="DELIVERED">Delivered</SelectItem>
                            <SelectItem value="CANCELLED">Cancelled</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                      <Button size="sm" variant="outline" onClick={() => openViewOrderModal(order)}>
                        <Eye className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* Product Modal */}
      <ProductModal
        isOpen={productModalOpen}
        onClose={() => setProductModalOpen(false)}
        product={selectedProduct}
        onSave={saveProduct}
        mode={productModalMode}
      />

      {/* Product View Modal */}
      <ProductViewModal
        isOpen={productViewModalOpen}
        onClose={() => setProductViewModalOpen(false)}
        product={selectedProduct}
        onEdit={handleEditFromView}
      />

      {/* Order View Modal */}
      <OrderViewModal
        isOpen={orderViewModalOpen}
        onClose={() => setOrderViewModalOpen(false)}
        order={selectedOrder}
        onEdit={handleEditOrderFromView}
      />
    </div>
  )
} 