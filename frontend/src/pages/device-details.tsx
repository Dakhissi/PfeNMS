import { useParams, Link } from "react-router-dom"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Drawer } from "@/components/ui/drawer"
import { 
  useDevice, 
  useDeviceInterfaces, 
  useDeviceAlerts, 
  useTriggerMonitoring,
  useDeleteDevice 
} from "@/lib/device-hooks"
import { 
  useIcmpProfiles,
  useSystemUnits,
  useIpProfiles,
  useUdpProfiles
} from "@/lib/profile-hooks"
import { 
  ArrowLeft, 
  Edit, 
  Trash2, 
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
  Monitor,
  Activity,
  AlertTriangle,
  CheckCircle,
  Calendar,
  Gauge,
  Network as NetworkIcon,
  HardDrive,
  Wifi as WifiIcon,
  Cpu,
  Globe,
  Database
} from "lucide-react"
import { useState } from "react"

export function DeviceDetailsPage() {
  const { id } = useParams<{ id: string }>()
  const deviceId = parseInt(id || '0')

  const { data: device, isLoading: deviceLoading } = useDevice(deviceId)
  const { data: interfaces, isLoading: interfacesLoading } = useDeviceInterfaces(deviceId)
  const { data: alerts, isLoading: alertsLoading } = useDeviceAlerts(deviceId)
  
  // Profile data hooks
  const { data: icmpProfiles, isLoading: icmpLoading } = useIcmpProfiles(deviceId)
  const { data: systemUnits, isLoading: systemUnitsLoading } = useSystemUnits(deviceId)
  const { data: ipProfiles, isLoading: ipLoading } = useIpProfiles(deviceId)
  const { data: udpProfiles, isLoading: udpLoading } = useUdpProfiles(deviceId)

  const triggerMonitoringMutation = useTriggerMonitoring()
  const deleteDeviceMutation = useDeleteDevice()

  // Drawer state
  const [activeDrawer, setActiveDrawer] = useState<'icmp' | 'system' | 'ip' | 'udp' | null>(null)

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

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'CRITICAL': return 'destructive'
      case 'WARNING': return 'warning'
      case 'INFO': return 'info'
      default: return 'secondary'
    }
  }

  const getSeverityIcon = (severity: string) => {
    switch (severity) {
      case 'CRITICAL': return <AlertTriangle className="h-4 w-4 text-red-500" />
      case 'WARNING': return <AlertTriangle className="h-4 w-4 text-yellow-500" />
      case 'INFO': return <Info className="h-4 w-4 text-blue-500" />
      default: return <Info className="h-4 w-4 text-gray-500" />
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString()
  }

  const formatUptime = (uptime?: number) => {
    if (!uptime) return 'Unknown'
    const days = Math.floor(uptime / (24 * 60 * 60 * 100))
    const hours = Math.floor((uptime % (24 * 60 * 60 * 100)) / (60 * 60 * 100))
    const minutes = Math.floor((uptime % (60 * 60 * 100)) / (60 * 100))
    return `${days}d ${hours}h ${minutes}m`
  }

  const formatSpeed = (speed: number) => {
    if (speed === 0) return 'Unknown'
    if (speed >= 1000000000) return `${(speed / 1000000000).toFixed(1)} Gbps`
    if (speed >= 1000000) return `${(speed / 1000000).toFixed(1)} Mbps`
    if (speed >= 1000) return `${(speed / 1000).toFixed(1)} Kbps`
    return `${speed} bps`
  }

  const formatBytes = (bytes: number) => {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  const handleTriggerMonitoring = () => {
    triggerMonitoringMutation.mutate(deviceId)
  }

  const handleDelete = () => {
    if (confirm('Are you sure you want to delete this device? This action cannot be undone.')) {
      deleteDeviceMutation.mutate(deviceId)
    }
  }

  const openDrawer = (type: 'icmp' | 'system' | 'ip' | 'udp') => {
    setActiveDrawer(type)
  }

  const closeDrawer = () => {
    setActiveDrawer(null)
  }

  if (deviceLoading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="flex items-center justify-center py-8">
          <RefreshCw className="h-6 w-6 animate-spin" />
        </div>
      </div>
    )
  }

  if (!device) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-8">
          <Server className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
          <h3 className="text-lg font-medium mb-2">Device not found</h3>
          <p className="text-muted-foreground mb-4">
            The device you're looking for doesn't exist or has been removed.
          </p>
          <Link to="/devices">
            <Button>
              <ArrowLeft className="h-4 w-4 mr-2" />
              Back to Devices
            </Button>
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Link to="/devices">
                <Button variant="outline" size="sm">
                  <ArrowLeft className="h-4 w-4 mr-2" />
                  Back to Devices
                </Button>
              </Link>
              <div>
                <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
                  {getTypeIcon(device.type)}
                  {device.name}
                </h1>
                <p className="text-muted-foreground mt-2">
                  Device Details & Monitoring
                </p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <Button 
                variant="outline" 
                onClick={handleTriggerMonitoring}
                disabled={triggerMonitoringMutation.isPending}
              >
                {triggerMonitoringMutation.isPending ? (
                  <RefreshCw className="h-4 w-4 animate-spin" />
                ) : (
                  <Activity className="h-4 w-4" />
                )}
                Trigger Monitoring
              </Button>
              <Link to={`/devices/${device.id}/edit`}>
                <Button variant="outline">
                  <Edit className="h-4 w-4 mr-2" />
                  Edit
                </Button>
              </Link>
              <Button 
                variant="destructive" 
                onClick={handleDelete}
                disabled={deleteDeviceMutation.isPending}
              >
                <Trash2 className="h-4 w-4 mr-2" />
                Delete
              </Button>
            </div>
          </div>
        </div>

        <div className="grid gap-6 lg:grid-cols-4">
          {/* Device Information */}
          <div className="lg:col-span-3">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Info className="h-5 w-5" />
                  Device Information
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid gap-4 md:grid-cols-2">
                  <div>
                    <h4 className="font-medium mb-2">Basic Information</h4>
                    <dl className="space-y-2">
                      <div className="flex justify-between">
                        <dt className="text-muted-foreground">Name:</dt>
                        <dd className="font-medium">{device.name}</dd>
                      </div>
                      <div className="flex justify-between">
                        <dt className="text-muted-foreground">Type:</dt>
                        <dd>
                          <Badge variant="outline">
                            {device.type.replace('_', ' ')}
                          </Badge>
                        </dd>
                      </div>
                      <div className="flex justify-between">
                        <dt className="text-muted-foreground">Status:</dt>
                        <dd>
                          <div className="flex items-center gap-2">
                            {getStatusIcon(device.status)}
                            <Badge variant={getStatusColor(device.status)}>
                              {device.status}
                            </Badge>
                          </div>
                        </dd>
                      </div>
                      <div className="flex justify-between">
                        <dt className="text-muted-foreground">Monitoring:</dt>
                        <dd>
                          <Badge variant={device.monitoringEnabled ? 'success' : 'secondary'}>
                            {device.monitoringEnabled ? 'Enabled' : 'Disabled'}
                          </Badge>
                        </dd>
                      </div>
                    </dl>
                  </div>
                  <div>
                    <h4 className="font-medium mb-2">System Information</h4>
                    <dl className="space-y-2">
                      {device.systemName && (
                        <div className="flex justify-between">
                          <dt className="text-muted-foreground">System Name:</dt>
                          <dd className="font-medium">{device.systemName}</dd>
                        </div>
                      )}
                      {device.systemLocation && (
                        <div className="flex justify-between">
                          <dt className="text-muted-foreground">Location:</dt>
                          <dd className="font-medium">{device.systemLocation}</dd>
                        </div>
                      )}
                      {device.systemContact && (
                        <div className="flex justify-between">
                          <dt className="text-muted-foreground">Contact:</dt>
                          <dd className="font-medium">{device.systemContact}</dd>
                        </div>
                      )}
                      {device.systemUptime && (
                        <div className="flex justify-between">
                          <dt className="text-muted-foreground">Uptime:</dt>
                          <dd className="font-medium">{formatUptime(device.systemUptime)}</dd>
                        </div>
                      )}
                    </dl>
                  </div>
                </div>
                {device.description && (
                  <div className="mt-4">
                    <h4 className="font-medium mb-2">Description</h4>
                    <p className="text-muted-foreground">{device.description}</p>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Device Interfaces */}
            <Card className="mt-6">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <NetworkIcon className="h-5 w-5" />
                  Network Interfaces
                </CardTitle>
              </CardHeader>
              <CardContent>
                {interfacesLoading ? (
                  <div className="flex items-center justify-center py-8">
                    <RefreshCw className="h-6 w-6 animate-spin" />
                  </div>
                ) : interfaces && interfaces.length > 0 ? (
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Index</TableHead>
                        <TableHead>Description</TableHead>
                        <TableHead>Type</TableHead>
                        <TableHead>Admin Status</TableHead>
                        <TableHead>Operational Status</TableHead>
                        <TableHead>MAC Address</TableHead>
                        <TableHead>Speed</TableHead>
                        <TableHead>MTU</TableHead>
                        <TableHead>In Traffic</TableHead>
                        <TableHead>Out Traffic</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {interfaces.map((iface) => (
                        <TableRow key={iface.id}>
                          <TableCell className="font-medium">{iface.ifIndex}</TableCell>
                          <TableCell>{iface.ifDescr}</TableCell>
                          <TableCell>{iface.ifType}</TableCell>
                          <TableCell>
                            <Badge variant={iface.ifAdminStatus === 'UP' ? 'success' : 'secondary'}>
                              {iface.ifAdminStatus}
                            </Badge>
                          </TableCell>
                          <TableCell>
                            <Badge variant={iface.ifOperStatus === 'UP' ? 'success' : 'secondary'}>
                              {iface.ifOperStatus}
                            </Badge>
                          </TableCell>
                          <TableCell className="font-mono text-sm">{iface.ifPhysAddress || '-'}</TableCell>
                          <TableCell>
                            {formatSpeed(iface.ifSpeed)}
                          </TableCell>
                          <TableCell>{iface.ifMtu || '-'}</TableCell>
                          <TableCell className="text-xs">
                            <div>{formatBytes(iface.ifInOctets)}</div>
                            <div className="text-muted-foreground">{iface.ifInUcastPkts} pkts</div>
                          </TableCell>
                          <TableCell className="text-xs">
                            <div>{formatBytes(iface.ifOutOctets)}</div>
                            <div className="text-muted-foreground">{iface.ifOutUcastPkts} pkts</div>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                ) : (
                  <div className="text-center py-8">
                    <NetworkIcon className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                    <h3 className="text-lg font-medium mb-2">No interfaces found</h3>
                    <p className="text-muted-foreground">
                      This device doesn't have any network interfaces configured.
                    </p>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Device Alerts */}
            <Card className="mt-6">
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
                ) : alerts && alerts.length > 0 ? (
                  <div className="space-y-4">
                    {alerts.slice(0, 10).map((alert) => (
                      <div key={alert.id} className="flex items-start gap-4 p-4 border rounded-lg">
                        <div className="flex-shrink-0">
                          {getSeverityIcon(alert.severity)}
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 mb-1">
                            <h4 className="font-medium">{alert.title}</h4>
                            <Badge variant={getSeverityColor(alert.severity)}>
                              {alert.severity}
                            </Badge>
                            <Badge variant="outline">
                              {alert.status}
                            </Badge>
                          </div>
                          {alert.description && (
                            <p className="text-sm text-muted-foreground mb-2">
                              {alert.description}
                            </p>
                          )}
                          <div className="flex items-center gap-4 text-xs text-muted-foreground">
                            <span className="flex items-center gap-1">
                              <Calendar className="h-3 w-3" />
                              {formatDate(alert.createdAt)}
                            </span>
                            {alert.acknowledgedAt && (
                              <span className="flex items-center gap-1">
                                <CheckCircle className="h-3 w-3" />
                                Acknowledged: {formatDate(alert.acknowledgedAt)}
                              </span>
                            )}
                            {alert.resolvedAt && (
                              <span className="flex items-center gap-1">
                                <CheckCircle className="h-3 w-3" />
                                Resolved: {formatDate(alert.resolvedAt)}
                              </span>
                            )}
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8">
                    <CheckCircle className="h-12 w-12 text-green-500 mx-auto mb-4" />
                    <h3 className="text-lg font-medium mb-2">No alerts</h3>
                    <p className="text-muted-foreground">
                      This device has no recent alerts.
                    </p>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* SNMP Configuration */}
            {device.configuration && (
              <Card className="mt-6">
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Wifi className="h-5 w-5" />
                    SNMP Configuration
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid gap-6 md:grid-cols-2">
                    {/* Basic SNMP Settings */}
                    <div>
                      <h4 className="font-medium mb-3">Connection Settings</h4>
                      <dl className="space-y-2">
                        <div className="flex justify-between">
                          <dt className="text-muted-foreground">Target IP:</dt>
                          <dd className="font-medium">{device.configuration.targetIp}</dd>
                        </div>
                        <div className="flex justify-between">
                          <dt className="text-muted-foreground">SNMP Port:</dt>
                          <dd className="font-medium">{device.configuration.snmpPort}</dd>
                        </div>
                        <div className="flex justify-between">
                          <dt className="text-muted-foreground">SNMP Version:</dt>
                          <dd className="font-medium">{device.configuration.snmpVersion}</dd>
                        </div>
                        <div className="flex justify-between">
                          <dt className="text-muted-foreground">Community String:</dt>
                          <dd className="font-medium font-mono text-sm">{device.configuration.communityString}</dd>
                        </div>
                      </dl>
                    </div>

                    {/* Polling Settings */}
                    <div>
                      <h4 className="font-medium mb-3">Polling Settings</h4>
                      <dl className="space-y-2">
                        <div className="flex justify-between">
                          <dt className="text-muted-foreground">Timeout:</dt>
                          <dd className="font-medium">{device.configuration.snmpTimeout}ms</dd>
                        </div>
                        <div className="flex justify-between">
                          <dt className="text-muted-foreground">Retries:</dt>
                          <dd className="font-medium">{device.configuration.snmpRetries}</dd>
                        </div>
                        <div className="flex justify-between">
                          <dt className="text-muted-foreground">Poll Interval:</dt>
                          <dd className="font-medium">{device.configuration.pollInterval}s</dd>
                        </div>
                        <div className="flex justify-between">
                          <dt className="text-muted-foreground">Enabled:</dt>
                          <dd>
                            <Badge variant={device.configuration.enabled ? 'success' : 'secondary'}>
                              {device.configuration.enabled ? 'Yes' : 'No'}
                            </Badge>
                          </dd>
                        </div>
                      </dl>
                    </div>
                  </div>

                  {/* SNMPv3 Security Settings */}
                  {device.configuration.snmpVersion === 'V3' && (
                    <div className="mt-6 pt-6 border-t">
                      <h4 className="font-medium mb-3">Security Settings (SNMPv3)</h4>
                      <div className="grid gap-6 md:grid-cols-2">
                        <dl className="space-y-2">
                          {device.configuration.securityName && (
                            <div className="flex justify-between">
                              <dt className="text-muted-foreground">Security Name:</dt>
                              <dd className="font-medium">{device.configuration.securityName}</dd>
                            </div>
                          )}
                          <div className="flex justify-between">
                            <dt className="text-muted-foreground">Auth Protocol:</dt>
                            <dd className="font-medium">{device.configuration.authProtocol}</dd>
                          </div>
                          <div className="flex justify-between">
                            <dt className="text-muted-foreground">Privacy Protocol:</dt>
                            <dd className="font-medium">{device.configuration.privProtocol}</dd>
                          </div>
                        </dl>
                        <dl className="space-y-2">
                          {device.configuration.contextName && (
                            <div className="flex justify-between">
                              <dt className="text-muted-foreground">Context Name:</dt>
                              <dd className="font-medium">{device.configuration.contextName}</dd>
                            </div>
                          )}
                        </dl>
                      </div>
                    </div>
                  )}

                  {/* Monitoring Status */}
                  <div className="mt-6 pt-6 border-t">
                    <h4 className="font-medium mb-3">Monitoring Status</h4>
                    <div className="grid gap-4 md:grid-cols-3">
                      <div className="flex items-center justify-between">
                        <span className="text-muted-foreground">Last Poll Time:</span>
                        <span className="font-medium text-sm">
                          {device.configuration.lastPollTime 
                            ? formatDate(device.configuration.lastPollTime)
                            : 'Never'
                          }
                        </span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-muted-foreground">Last Poll Status:</span>
                        <Badge 
                          variant={
                            device.configuration.lastPollStatus === 'SUCCESS' ? 'success' :
                            device.configuration.lastPollStatus === 'FAILED' ? 'destructive' :
                            'secondary'
                          }
                        >
                          {device.configuration.lastPollStatus || 'Unknown'}
                        </Badge>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-muted-foreground">Consecutive Failures:</span>
                        <span className="font-medium">{device.configuration.consecutiveFailures}</span>
                      </div>
                    </div>
                    {device.configuration.errorMessage && (
                      <div className="mt-4 p-3 bg-destructive/10 border border-destructive/20 rounded-lg">
                        <h5 className="font-medium text-destructive mb-1">Last Error</h5>
                        <p className="text-sm text-muted-foreground">{device.configuration.errorMessage}</p>
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>
            )}
          </div>

          {/* Right Sidebar */}
          <div className="space-y-6">
            {/* Quick Stats */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Gauge className="h-5 w-5" />
                  Quick Stats
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-muted-foreground">Interfaces</span>
                    <span className="font-medium">{interfaces?.length || 0}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-muted-foreground">Active Alerts</span>
                    <span className="font-medium">
                      {alerts?.filter(a => a.status === 'NEW' || a.status === 'ACKNOWLEDGED').length || 0}
                    </span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-muted-foreground">Last Monitored</span>
                    <span className="font-medium text-sm">
                      {device.lastMonitored 
                        ? formatDate(device.lastMonitored)
                        : 'Never'
                      }
                    </span>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Profile Management */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Settings className="h-5 w-5" />
                  Profile Management
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <Button 
                    variant="outline" 
                    className="w-full justify-start"
                    onClick={() => openDrawer('icmp')}
                  >
                    <WifiIcon className="h-4 w-4 mr-2" />
                    ICMP Profile
                    {icmpProfiles && icmpProfiles.length > 0 && (
                      <Badge variant="secondary" className="ml-auto">
                        {icmpProfiles.length}
                      </Badge>
                    )}
                  </Button>
                  
                  <Button 
                    variant="outline" 
                    className="w-full justify-start"
                    onClick={() => openDrawer('system')}
                  >
                    <Cpu className="h-4 w-4 mr-2" />
                    System Units
                    {systemUnits && systemUnits.length > 0 && (
                      <Badge variant="secondary" className="ml-auto">
                        {systemUnits.length}
                      </Badge>
                    )}
                  </Button>
                  
                  <Button 
                    variant="outline" 
                    className="w-full justify-start"
                    onClick={() => openDrawer('ip')}
                  >
                    <Globe className="h-4 w-4 mr-2" />
                    IP Profiles
                    {ipProfiles && ipProfiles.length > 0 && (
                      <Badge variant="secondary" className="ml-auto">
                        {ipProfiles.length}
                      </Badge>
                    )}
                  </Button>
                  
                  <Button 
                    variant="outline" 
                    className="w-full justify-start"
                    onClick={() => openDrawer('udp')}
                  >
                    <Database className="h-4 w-4 mr-2" />
                    UDP Profiles
                    {udpProfiles && udpProfiles.length > 0 && (
                      <Badge variant="secondary" className="ml-auto">
                        {udpProfiles.length}
                      </Badge>
                    )}
                  </Button>
                </div>
              </CardContent>
            </Card>

            {/* System Details */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <HardDrive className="h-5 w-5" />
                  System Details
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {device.systemObjectId && (
                    <div>
                      <h4 className="font-medium text-sm mb-1">System Object ID</h4>
                      <p className="text-xs text-muted-foreground break-all">
                        {device.systemObjectId}
                      </p>
                    </div>
                  )}
                  {device.systemServices && (
                    <div>
                      <h4 className="font-medium text-sm mb-1">System Services</h4>
                      <p className="text-xs text-muted-foreground">
                        {device.systemServices} services
                      </p>
                    </div>
                  )}
                  <div>
                    <h4 className="font-medium text-sm mb-1">Created</h4>
                    <p className="text-xs text-muted-foreground">
                      {formatDate(device.createdAt)}
                    </p>
                  </div>
                  <div>
                    <h4 className="font-medium text-sm mb-1">Last Updated</h4>
                    <p className="text-xs text-muted-foreground">
                      {formatDate(device.updatedAt)}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Actions */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Settings className="h-5 w-5" />
                  Quick Actions
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex flex-col space-y-2">
                  <Button 
                    className="w-full justify-start" 
                    variant="outline"
                    onClick={handleTriggerMonitoring}
                    disabled={triggerMonitoringMutation.isPending}
                  >
                    {triggerMonitoringMutation.isPending ? (
                      <RefreshCw className="h-4 w-4 animate-spin mr-2" />
                    ) : (
                      <Activity className="h-4 w-4 mr-2" />
                    )}
                    Trigger Monitoring
                  </Button>
                  <Link to={`/devices/${device.id}/edit`} className="w-full">
                    <Button className="w-full justify-start" variant="outline">
                      <Edit className="h-4 w-4 mr-2" />
                      Edit Device
                    </Button>
                  </Link>
                  <Button 
                    className="w-full justify-start" 
                    variant="destructive"
                    onClick={handleDelete}
                    disabled={deleteDeviceMutation.isPending}
                  >
                    <Trash2 className="h-4 w-4 mr-2" />
                    Delete Device
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>

        {/* Profile Management Drawers */}
        
        {/* ICMP Profile Drawer */}
        <Drawer
          isOpen={activeDrawer === 'icmp'}
          onClose={closeDrawer}
          title="ICMP Profile Management"
        >
          {icmpLoading ? (
            <div className="flex items-center justify-center py-8">
              <RefreshCw className="h-6 w-6 animate-spin" />
              <span className="ml-2">Loading ICMP profiles...</span>
            </div>
          ) : icmpProfiles && icmpProfiles.length > 0 ? (
            <div className="space-y-4">
              {icmpProfiles.map((profile) => (
                <Card key={profile.id}>
                  <CardHeader>
                    <CardTitle className="text-sm">ICMP Statistics</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3">
                      <div className="grid grid-cols-2 gap-2 text-sm">
                        <div>
                          <span className="text-muted-foreground">In Messages:</span>
                          <div className="font-medium">{profile.icmpInMsgs?.toLocaleString() || 0}</div>
                        </div>
                        <div>
                          <span className="text-muted-foreground">Out Messages:</span>
                          <div className="font-medium">{profile.icmpOutMsgs?.toLocaleString() || 0}</div>
                        </div>
                        <div>
                          <span className="text-muted-foreground">In Errors:</span>
                          <div className="font-medium">{profile.icmpInErrors?.toLocaleString() || 0}</div>
                        </div>
                        <div>
                          <span className="text-muted-foreground">Out Errors:</span>
                          <div className="font-medium">{profile.icmpOutErrors?.toLocaleString() || 0}</div>
                        </div>
                        <div>
                          <span className="text-muted-foreground">In Echos:</span>
                          <div className="font-medium">{profile.icmpInEchos?.toLocaleString() || 0}</div>
                        </div>
                        <div>
                          <span className="text-muted-foreground">Out Echos:</span>
                          <div className="font-medium">{profile.icmpOutEchos?.toLocaleString() || 0}</div>
                        </div>
                      </div>
                      <div className="text-xs text-muted-foreground">
                        Updated: {formatDate(profile.updatedAt)}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : (
            <div className="text-center py-8">
              <WifiIcon className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <h3 className="text-lg font-medium mb-2">No ICMP profiles</h3>
              <p className="text-muted-foreground">
                No ICMP profile data available for this device.
              </p>
            </div>
          )}
        </Drawer>

        {/* System Units Drawer */}
        <Drawer
          isOpen={activeDrawer === 'system'}
          onClose={closeDrawer}
          title="System Unit Management"
        >
          {systemUnitsLoading ? (
            <div className="flex items-center justify-center py-8">
              <RefreshCw className="h-6 w-6 animate-spin" />
              <span className="ml-2">Loading system units...</span>
            </div>
          ) : systemUnits && systemUnits.length > 0 ? (
            <div className="space-y-4">
              {systemUnits.map((unit) => (
                <Card key={unit.id}>
                  <CardHeader>
                    <CardTitle className="text-sm">{unit.name || `Unit ${unit.index}`}</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-2 text-sm">
                      <div>
                        <span className="text-muted-foreground">Description:</span>
                        <div className="font-medium">{unit.descr || 'N/A'}</div>
                      </div>
                      <div>
                        <span className="text-muted-foreground">Object ID:</span>
                        <div className="font-mono text-xs">{unit.objectId || 'N/A'}</div>
                      </div>
                      <div>
                        <span className="text-muted-foreground">Uptime:</span>
                        <div className="font-medium">{formatUptime(unit.upTime)}</div>
                      </div>
                      <div>
                        <span className="text-muted-foreground">Services:</span>
                        <div className="font-medium">{unit.services || 0}</div>
                      </div>
                      {unit.contact && (
                        <div>
                          <span className="text-muted-foreground">Contact:</span>
                          <div className="font-medium">{unit.contact}</div>
                        </div>
                      )}
                      {unit.location && (
                        <div>
                          <span className="text-muted-foreground">Location:</span>
                          <div className="font-medium">{unit.location}</div>
                        </div>
                      )}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : (
            <div className="text-center py-8">
              <Cpu className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <h3 className="text-lg font-medium mb-2">No system units</h3>
              <p className="text-muted-foreground">
                No system unit data available for this device.
              </p>
            </div>
          )}
        </Drawer>

        {/* IP Profile Drawer */}
        <Drawer
          isOpen={activeDrawer === 'ip'}
          onClose={closeDrawer}
          title="IP Profile Management"
        >
          {ipLoading ? (
            <div className="flex items-center justify-center py-8">
              <RefreshCw className="h-6 w-6 animate-spin" />
              <span className="ml-2">Loading IP profiles...</span>
            </div>
          ) : ipProfiles && ipProfiles.length > 0 ? (
            <div className="space-y-4">
              {ipProfiles.map((profile) => (
                <Card key={profile.id}>
                  <CardHeader>
                    <CardTitle className="text-sm">{profile.name || 'IP Profile'}</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-2 text-sm">
                      <div>
                        <span className="text-muted-foreground">IP Address:</span>
                        <div className="font-mono font-medium">{profile.ipAddress || 'N/A'}</div>
                      </div>
                      <div>
                        <span className="text-muted-foreground">Timeout:</span>
                        <div className="font-medium">{profile.timeout || 0}ms</div>
                      </div>
                      <div>
                        <span className="text-muted-foreground">Interval:</span>
                        <div className="font-medium">{profile.interval || 0}s</div>
                      </div>
                      <div>
                        <span className="text-muted-foreground">Status:</span>
                        <Badge variant={profile.enabled ? 'success' : 'secondary'}>
                          {profile.enabled ? 'Enabled' : 'Disabled'}
                        </Badge>
                      </div>
                      {profile.description && (
                        <div>
                          <span className="text-muted-foreground">Description:</span>
                          <div className="font-medium">{profile.description}</div>
                        </div>
                      )}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : (
            <div className="text-center py-8">
              <Globe className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <h3 className="text-lg font-medium mb-2">No IP profiles</h3>
              <p className="text-muted-foreground">
                No IP profile data available for this device.
              </p>
            </div>
          )}
        </Drawer>

        {/* UDP Profile Drawer */}
        <Drawer
          isOpen={activeDrawer === 'udp'}
          onClose={closeDrawer}
          title="UDP Profile Management"
        >
          {udpLoading ? (
            <div className="flex items-center justify-center py-8">
              <RefreshCw className="h-6 w-6 animate-spin" />
              <span className="ml-2">Loading UDP profiles...</span>
            </div>
          ) : udpProfiles && udpProfiles.length > 0 ? (
            <div className="space-y-4">
              {udpProfiles.map((profile) => (
                <Card key={profile.id}>
                  <CardHeader>
                    <CardTitle className="text-sm">UDP Connection</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-2 text-sm">
                      <div className="grid grid-cols-2 gap-2">
                        <div>
                          <span className="text-muted-foreground">Local Address:</span>
                          <div className="font-mono font-medium">{profile.udpLocalAddress || 'N/A'}</div>
                        </div>
                        <div>
                          <span className="text-muted-foreground">Local Port:</span>
                          <div className="font-medium">{profile.udpLocalPort || 'N/A'}</div>
                        </div>
                        <div>
                          <span className="text-muted-foreground">Remote Address:</span>
                          <div className="font-mono font-medium">{profile.udpRemoteAddress || 'N/A'}</div>
                        </div>
                        <div>
                          <span className="text-muted-foreground">Remote Port:</span>
                          <div className="font-medium">{profile.udpRemotePort || 'N/A'}</div>
                        </div>
                      </div>
                      <div className="grid grid-cols-2 gap-2">
                        <div>
                          <span className="text-muted-foreground">In Datagrams:</span>
                          <div className="font-medium">{profile.udpInDatagrams?.toLocaleString() || 0}</div>
                        </div>
                        <div>
                          <span className="text-muted-foreground">Out Datagrams:</span>
                          <div className="font-medium">{profile.udpOutDatagrams?.toLocaleString() || 0}</div>
                        </div>
                        <div>
                          <span className="text-muted-foreground">No Ports:</span>
                          <div className="font-medium">{profile.udpNoPorts?.toLocaleString() || 0}</div>
                        </div>
                        <div>
                          <span className="text-muted-foreground">In Errors:</span>
                          <div className="font-medium">{profile.udpInErrors?.toLocaleString() || 0}</div>
                        </div>
                      </div>
                      <div>
                        <span className="text-muted-foreground">Status:</span>
                        <Badge variant="outline">{profile.udpEntryStatus || 'UNKNOWN'}</Badge>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : (
            <div className="text-center py-8">
              <Database className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <h3 className="text-lg font-medium mb-2">No UDP profiles</h3>
              <p className="text-muted-foreground">
                No UDP profile data available for this device.
              </p>
            </div>
          )}
        </Drawer>
      </div>
    </div>
  )
} 