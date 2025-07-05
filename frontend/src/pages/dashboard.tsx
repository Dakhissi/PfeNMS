import { useAuth } from "@/lib/auth-hooks"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { 
  useAlertStatistics, 
  useDevices, 
  useRecentAlerts, 
  useDeviceStatusSummary 
} from "@/lib/dashboard-hooks"
import { 
  LogOut, 
  User, 
  Mail, 
  AlertTriangle, 
  CheckCircle, 
  Clock, 
  Server, 
  ChevronLeft,
  ChevronRight,
  RefreshCw,
  Wifi,
  WifiOff,
  Settings,
  AlertCircle,
  Info
} from "lucide-react"
import { useState } from "react"

export function DashboardPage() {
  const { user, logout } = useAuth()
  const [currentPage, setCurrentPage] = useState(0)
  const [pageSize] = useState(10)

  // Data hooks
  const { data: alertStats, isLoading: statsLoading } = useAlertStatistics()
  const { data: devicesData, isLoading: devicesLoading } = useDevices(currentPage, pageSize)
  const { data: recentAlerts, isLoading: alertsLoading } = useRecentAlerts(5)
  const deviceStatusSummary = useDeviceStatusSummary()

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'CRITICAL': return 'destructive'
      case 'WARNING': return 'warning'
      case 'INFO': return 'info'
      default: return 'secondary'
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE': return 'success'
      case 'INACTIVE': return 'secondary'
      case 'MAINTENANCE': return 'warning'
      case 'ERROR': return 'destructive'
      default: return 'secondary'
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'ACTIVE': return <Wifi className="h-4 w-4 text-green-500" />
      case 'INACTIVE': return <WifiOff className="h-4 w-4 text-gray-500" />
      case 'MAINTENANCE': return <Settings className="h-4 w-4 text-yellow-500" />
      case 'ERROR': return <AlertCircle className="h-4 w-4 text-red-500" />
      default: return <Info className="h-4 w-4 text-gray-500" />
    }
  }

  const formatUptime = (uptime?: number) => {
    if (!uptime) return 'Unknown'
    const days = Math.floor(uptime / (24 * 60 * 60 * 100))
    const hours = Math.floor((uptime % (24 * 60 * 60 * 100)) / (60 * 60 * 100))
    const minutes = Math.floor((uptime % (60 * 60 * 100)) / (60 * 100))
    return `${days}d ${hours}h ${minutes}m`
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString()
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
          <p className="text-muted-foreground mt-2">
            Welcome to your Network Manager dashboard
          </p>
        </div>

        <div className="grid gap-6">
          {/* Welcome Card */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <User className="h-5 w-5" />
                Welcome, {user?.username}!
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-muted-foreground mb-4">
                You are now logged into your Network Manager account. Monitor your network devices and alerts in real-time.
              </p>
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Mail className="h-4 w-4" />
                <span>{user?.email}</span>
              </div>
            </CardContent>
          </Card>

          {/* Statistics Overview */}
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            {/* Alert Statistics */}
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Total Alerts</CardTitle>
                <AlertTriangle className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">
                  {statsLoading ? '...' : alertStats?.total || 0}
                </div>
                <p className="text-xs text-muted-foreground">
                  {alertStats?.critical || 0} critical, {alertStats?.warning || 0} warnings
                </p>
              </CardContent>
            </Card>

            {/* Device Status */}
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Active Devices</CardTitle>
                <Server className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">
                  {deviceStatusSummary.active}
                </div>
                <p className="text-xs text-muted-foreground">
                  of {deviceStatusSummary.total} total devices
                </p>
              </CardContent>
            </Card>

            {/* System Health */}
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">System Health</CardTitle>
                <Info className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">
                  {deviceStatusSummary.total > 0 
                    ? Math.round((deviceStatusSummary.active / deviceStatusSummary.total) * 100)
                    : 0}%
                </div>
                <p className="text-xs text-muted-foreground">
                  {deviceStatusSummary.error} devices in error
                </p>
              </CardContent>
            </Card>

            {/* Recent Activity */}
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Recent Alerts</CardTitle>
                <Clock className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">
                  {recentAlerts?.length || 0}
                </div>
                <p className="text-xs text-muted-foreground">
                  recent alerts
                </p>
              </CardContent>
            </Card>
          </div>

          {/* Main Content Grid */}
          <div className="grid gap-6 lg:grid-cols-2">
            {/* Recent Alerts */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <AlertTriangle className="h-5 w-5" />
                  Recent Alerts
                </CardTitle>
              </CardHeader>
              <CardContent>
                {alertsLoading ? (
                  <div className="flex items-center justify-center py-8">
                    <RefreshCw className="h-6 w-6 animate-spin" />
                  </div>
                ) : recentAlerts && recentAlerts.length > 0 ? (
                  <div className="space-y-3">
                    {recentAlerts.map((alert) => (
                      <div key={alert.id} className="flex items-start gap-3 p-3 rounded-lg border">
                        <div className="flex-shrink-0">
                          <Badge variant={getSeverityColor(alert.severity)}>
                            {alert.severity}
                          </Badge>
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-medium">{alert.title}</p>
                          {alert.description && (
                            <p className="text-xs text-muted-foreground mt-1">
                              {alert.description}
                            </p>
                          )}
                          <p className="text-xs text-muted-foreground mt-1">
                            {formatDate(alert.createdAt)}
                          </p>
                        </div>
                        <Badge variant={alert.status === 'RESOLVED' ? 'success' : 'secondary'}>
                          {alert.status}
                        </Badge>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8 text-muted-foreground">
                    <CheckCircle className="h-8 w-8 mx-auto mb-2" />
                    <p>No recent alerts</p>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Device List */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Server className="h-5 w-5" />
                  Network Devices
                </CardTitle>
              </CardHeader>
              <CardContent>
                {devicesLoading ? (
                  <div className="flex items-center justify-center py-8">
                    <RefreshCw className="h-6 w-6 animate-spin" />
                  </div>
                ) : devicesData && devicesData.content.length > 0 ? (
                  <>
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Name</TableHead>
                          <TableHead>Type</TableHead>
                          <TableHead>Status</TableHead>
                          <TableHead>Uptime</TableHead>
                          <TableHead>Last Monitored</TableHead>
                          <TableHead>Monitoring</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {devicesData.content.map((device) => (
                          <TableRow key={device.id}>
                            <TableCell className="font-medium">
                              <div>
                                <p>{device.name}</p>
                                {device.description && (
                                  <p className="text-xs text-muted-foreground">
                                    {device.description}
                                  </p>
                                )}
                              </div>
                            </TableCell>
                            <TableCell>
                              <Badge variant="outline">{device.type}</Badge>
                            </TableCell>
                            <TableCell>
                              <div className="flex items-center gap-2">
                                {getStatusIcon(device.status)}
                                <Badge variant={getStatusColor(device.status)}>
                                  {device.status}
                                </Badge>
                              </div>
                            </TableCell>
                            <TableCell>
                              {formatUptime(device.systemUptime)}
                            </TableCell>
                            <TableCell>
                              {device.lastMonitored 
                                ? formatDate(device.lastMonitored)
                                : 'Never'
                              }
                            </TableCell>
                            <TableCell>
                              <Badge variant={device.monitoringEnabled ? 'success' : 'secondary'}>
                                {device.monitoringEnabled ? 'Enabled' : 'Disabled'}
                              </Badge>
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>

                    {/* Pagination */}
                    <div className="flex items-center justify-between mt-4">
                      <div className="text-sm text-muted-foreground">
                        Showing {devicesData.number * devicesData.size + 1} to{' '}
                        {Math.min((devicesData.number + 1) * devicesData.size, devicesData.totalElements)} of{' '}
                        {devicesData.totalElements} devices
                      </div>
                      <div className="flex items-center gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                          disabled={devicesData.first}
                        >
                          <ChevronLeft className="h-4 w-4" />
                          Previous
                        </Button>
                        <span className="text-sm">
                          Page {devicesData.number + 1} of {devicesData.totalPages}
                        </span>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => setCurrentPage(currentPage + 1)}
                          disabled={devicesData.last}
                        >
                          Next
                          <ChevronRight className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  </>
                ) : (
                  <div className="text-center py-8 text-muted-foreground">
                    <Server className="h-8 w-8 mx-auto mb-2" />
                    <p>No devices found</p>
                    <p className="text-xs">Add devices to start monitoring your network</p>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Logout Section */}
            <Card>
              <CardHeader>
                <CardTitle>Account</CardTitle>
              </CardHeader>
              <CardContent>
                <Button 
                  variant="destructive" 
                  onClick={logout}
                  className="flex items-center gap-2"
                >
                  <LogOut className="h-4 w-4" />
                  Logout
                </Button>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  )
} 