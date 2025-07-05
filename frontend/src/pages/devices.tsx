import { useState } from "react"
import { Link } from "react-router-dom"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { 
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { 
  useDevicesList, 
  useDevicesByStatus, 
  useDevicesByType, 
  useSearchDevices,
  useDeleteDevice 
} from "@/lib/device-hooks"
import { 
  Plus, 
  Search, 
  Filter, 
  MoreHorizontal, 
  Edit, 
  Trash2, 
  Eye,
  ChevronLeft,
  ChevronRight,
  RefreshCw,
  Wifi,
  WifiOff,
  Settings,
  AlertCircle,
  Info,
  Server,
  Router,
  Network,
  Printer,
  Shield,
  Radio,
  Monitor
} from "lucide-react"

export function DevicesPage() {
  const [currentPage, setCurrentPage] = useState(0)
  const [pageSize] = useState(10)
  const [sortField, setSortField] = useState<string>('name')
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc')
  const [searchQuery, setSearchQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState<string>('')
  const [typeFilter, setTypeFilter] = useState<string>('')
  const [filterType, setFilterType] = useState<'all' | 'status' | 'type' | 'search'>('all')

  const deleteDeviceMutation = useDeleteDevice()

  // Determine which query to use based on filter type
  const devicesListQuery = useDevicesList(currentPage, pageSize, sortField, sortOrder)
  const devicesByStatusQuery = useDevicesByStatus(statusFilter, currentPage, pageSize)
  const devicesByTypeQuery = useDevicesByType(typeFilter, currentPage, pageSize)
  const searchDevicesQuery = useSearchDevices(searchQuery, currentPage, pageSize)

  // Select the appropriate query result
  const getActiveQuery = () => {
    switch (filterType) {
      case 'status':
        return devicesByStatusQuery
      case 'type':
        return devicesByTypeQuery
      case 'search':
        return searchDevicesQuery
      default:
        return devicesListQuery
    }
  }

  const { data: devicesData, isLoading, refetch } = getActiveQuery()

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

  const getTypeIcon = (type: string) => {
    switch (type) {
      case 'ROUTER': return <Router className="h-4 w-4" />
      case 'SWITCH': return <Network className="h-4 w-4" />
      case 'SERVER': return <Server className="h-4 w-4" />
      case 'WORKSTATION': return <Monitor className="h-4 w-4" />
      case 'PRINTER': return <Printer className="h-4 w-4" />
      case 'FIREWALL': return <Shield className="h-4 w-4" />
      case 'ACCESS_POINT': return <Radio className="h-4 w-4" />
      default: return <Server className="h-4 w-4" />
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString()
  }

  const handleSort = (field: string) => {
    if (sortField === field) {
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc')
    } else {
      setSortField(field)
      setSortOrder('asc')
    }
  }

  const handleSearch = (query: string) => {
    setSearchQuery(query)
    setFilterType(query.length >= 2 ? 'search' : 'all')
    setCurrentPage(0)
  }

  const handleStatusFilter = (status: string) => {
    setStatusFilter(status)
    setFilterType(status ? 'status' : 'all')
    setCurrentPage(0)
  }

  const handleTypeFilter = (type: string) => {
    setTypeFilter(type)
    setFilterType(type ? 'type' : 'all')
    setCurrentPage(0)
  }

  const handleDelete = (deviceId: number) => {
    if (confirm('Are you sure you want to delete this device?')) {
      deleteDeviceMutation.mutate(deviceId)
    }
  }

  const clearFilters = () => {
    setSearchQuery('')
    setStatusFilter('')
    setTypeFilter('')
    setFilterType('all')
    setCurrentPage(0)
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold tracking-tight">Device Management</h1>
              <p className="text-muted-foreground mt-2">
                Manage and monitor your network devices
              </p>
            </div>
            <Link to="/devices/add">
              <Button className="flex items-center gap-2">
                <Plus className="h-4 w-4" />
                Add Device
              </Button>
            </Link>
          </div>
        </div>

        {/* Filters and Search */}
        <Card className="mb-6">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Filter className="h-5 w-5" />
              Filters & Search
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid gap-4 md:grid-cols-4">
              {/* Search */}
              <div className="relative">
                <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Search devices..."
                  value={searchQuery}
                  onChange={(e) => handleSearch(e.target.value)}
                  className="pl-10"
                />
              </div>

              {/* Status Filter */}
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="outline" className="justify-between">
                    Status: {statusFilter || 'All'}
                    <Filter className="h-4 w-4" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent>
                  <DropdownMenuItem onClick={() => handleStatusFilter('')}>
                    All Statuses
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleStatusFilter('ACTIVE')}>
                    Active
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleStatusFilter('INACTIVE')}>
                    Inactive
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleStatusFilter('MAINTENANCE')}>
                    Maintenance
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleStatusFilter('ERROR')}>
                    Error
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>

              {/* Type Filter */}
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="outline" className="justify-between">
                    Type: {typeFilter || 'All'}
                    <Filter className="h-4 w-4" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent>
                  <DropdownMenuItem onClick={() => handleTypeFilter('')}>
                    All Types
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleTypeFilter('ROUTER')}>
                    Router
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleTypeFilter('SWITCH')}>
                    Switch
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleTypeFilter('SERVER')}>
                    Server
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleTypeFilter('WORKSTATION')}>
                    Workstation
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleTypeFilter('PRINTER')}>
                    Printer
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleTypeFilter('FIREWALL')}>
                    Firewall
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleTypeFilter('ACCESS_POINT')}>
                    Access Point
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleTypeFilter('OTHER')}>
                    Other
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>

              {/* Clear Filters */}
              <Button variant="outline" onClick={clearFilters}>
                Clear Filters
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Devices Table */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle>Devices</CardTitle>
              <Button variant="outline" size="sm" onClick={() => refetch()}>
                <RefreshCw className="h-4 w-4" />
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="flex items-center justify-center py-8">
                <RefreshCw className="h-6 w-6 animate-spin" />
              </div>
            ) : devicesData?.content && devicesData.content.length > 0 ? (
              <>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead 
                        className="cursor-pointer"
                        onClick={() => handleSort('name')}
                      >
                        Name
                        {sortField === 'name' && (
                          <span className="ml-1">{sortOrder === 'asc' ? '↑' : '↓'}</span>
                        )}
                      </TableHead>
                      <TableHead>Type</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead>SNMP Target</TableHead>
                      <TableHead>Monitoring</TableHead>
                      <TableHead 
                        className="cursor-pointer"
                        onClick={() => handleSort('lastMonitored')}
                      >
                        Last Monitored
                        {sortField === 'lastMonitored' && (
                          <span className="ml-1">{sortOrder === 'asc' ? '↑' : '↓'}</span>
                        )}
                      </TableHead>
                      <TableHead>Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {devicesData.content.map((device) => (
                      <TableRow key={device.id}>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            {getTypeIcon(device.type)}
                            <Link 
                              to={`/devices/${device.id}`}
                              className="font-medium hover:underline"
                            >
                              {device.name}
                            </Link>
                          </div>
                          {device.description && (
                            <p className="text-sm text-muted-foreground">
                              {device.description}
                            </p>
                          )}
                        </TableCell>
                        <TableCell>
                          <Badge variant="outline">
                            {device.type.replace('_', ' ')}
                          </Badge>
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
                          {device.configuration ? (
                            <div className="space-y-1">
                              <div className="font-mono text-sm">
                                {device.configuration.targetIp}:{device.configuration.snmpPort}
                              </div>
                              <div className="flex items-center gap-2">
                                <Badge variant="outline" className="text-xs">
                                  {device.configuration.snmpVersion}
                                </Badge>
                                {device.configuration.lastPollStatus && (
                                  <Badge 
                                    variant={
                                      device.configuration.lastPollStatus === 'SUCCESS' ? 'success' :
                                      device.configuration.lastPollStatus === 'FAILED' ? 'destructive' :
                                      'secondary'
                                    }
                                    className="text-xs"
                                  >
                                    {device.configuration.lastPollStatus}
                                  </Badge>
                                )}
                              </div>
                            </div>
                          ) : (
                            <span className="text-muted-foreground text-sm">Not configured</span>
                          )}
                        </TableCell>
                        <TableCell>
                          <Badge variant={device.monitoringEnabled ? 'success' : 'secondary'}>
                            {device.monitoringEnabled ? 'Enabled' : 'Disabled'}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          {device.lastMonitored 
                            ? formatDate(device.lastMonitored)
                            : 'Never'
                          }
                        </TableCell>
                        <TableCell>
                          <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                              <Button variant="ghost" size="sm">
                                <MoreHorizontal className="h-4 w-4" />
                              </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent>
                              <DropdownMenuItem asChild>
                                <Link to={`/devices/${device.id}`}>
                                  <Eye className="h-4 w-4 mr-2" />
                                  View Details
                                </Link>
                              </DropdownMenuItem>
                              <DropdownMenuItem asChild>
                                <Link to={`/devices/${device.id}/edit`}>
                                  <Edit className="h-4 w-4 mr-2" />
                                  Edit
                                </Link>
                              </DropdownMenuItem>
                              <DropdownMenuItem 
                                onClick={() => handleDelete(device.id)}
                                className="text-destructive"
                              >
                                <Trash2 className="h-4 w-4 mr-2" />
                                Delete
                              </DropdownMenuItem>
                            </DropdownMenuContent>
                          </DropdownMenu>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>

                {/* Pagination */}
                {devicesData.totalPages > 1 && (
                  <div className="flex items-center justify-between mt-4">
                    <div className="text-sm text-muted-foreground">
                      Showing {currentPage * pageSize + 1} to{' '}
                      {Math.min((currentPage + 1) * pageSize, devicesData.totalElements)} of{' '}
                      {devicesData.totalElements} devices
                    </div>
                    <div className="flex items-center gap-2">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setCurrentPage(currentPage - 1)}
                        disabled={currentPage === 0}
                      >
                        <ChevronLeft className="h-4 w-4" />
                        Previous
                      </Button>
                      <span className="text-sm">
                        Page {currentPage + 1} of {devicesData.totalPages}
                      </span>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setCurrentPage(currentPage + 1)}
                        disabled={currentPage === devicesData.totalPages - 1}
                      >
                        Next
                        <ChevronRight className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                )}
              </>
            ) : (
              <div className="text-center py-8">
                <Server className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium mb-2">No devices found</h3>
                <p className="text-muted-foreground mb-4">
                  {filterType !== 'all' 
                    ? 'Try adjusting your filters or search terms.'
                    : 'Get started by adding your first device.'
                  }
                </p>
                {filterType === 'all' && (
                  <Link to="/devices/add">
                    <Button>
                      <Plus className="h-4 w-4 mr-2" />
                      Add Device
                    </Button>
                  </Link>
                )}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
} 