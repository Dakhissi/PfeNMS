import { useNavigate, useLocation } from "react-router-dom"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Search, Play, Clock, CheckCircle, XCircle, AlertTriangle, RefreshCw, Trash2, Monitor, Server, Wifi } from "lucide-react"
import { useCancelDiscovery, useDiscoveryStatus } from "@/lib/discovery-hooks"
import { useEffect } from "react"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"

export function DiscoveryDashboardPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const cancelDiscoveryMutation = useCancelDiscovery()
  
  // Get discoveryId from navigation state
  const discoveryId = location.state?.discoveryId
  const { data: discoveryStatus, isLoading: statusLoading, refetch: refetchStatus } = useDiscoveryStatus(discoveryId || '')

  // Auto-refresh after 5 seconds if we have a discoveryId
  useEffect(() => {
    if (discoveryId) {
      const timer = setTimeout(() => {
        refetchStatus()
      }, 5000)
      return () => clearTimeout(timer)
    }
  }, [discoveryId, refetchStatus])

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'RUNNING': return 'default'
      case 'COMPLETE': return 'success'
      case 'FAILED': return 'destructive'
      case 'CANCELLED': return 'secondary'
      default: return 'secondary'
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'RUNNING': return <RefreshCw className="h-4 w-4 animate-spin" />
      case 'COMPLETE': return <CheckCircle className="h-4 w-4" />
      case 'FAILED': return <XCircle className="h-4 w-4" />
      case 'CANCELLED': return <XCircle className="h-4 w-4" />
      default: return <Clock className="h-4 w-4" />
    }
  }

  const formatDuration = (ms: number) => {
    if (ms < 1000) return `${ms}ms`
    return `${(ms / 1000).toFixed(2)}s`
  }

  const getDeviceIcon = (deviceType: string) => {
    switch (deviceType.toUpperCase()) {
      case 'SERVER': return <Server className="h-4 w-4" />
      case 'SWITCH': return <Wifi className="h-4 w-4" />
      default: return <Wifi className="h-4 w-4" />
    }
  }

  const handleCancelDiscovery = async (discoveryId: string) => {
    try {
      await cancelDiscoveryMutation.mutateAsync(discoveryId)
    } catch (error) {
      console.error('Failed to cancel discovery:', error)
    }
  }

  const handleRefreshStatus = () => {
    if (discoveryId) {
      refetchStatus()
    }
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold tracking-tight">Network Discovery</h1>
              <p className="text-muted-foreground mt-2">
                Discover and monitor network devices automatically
              </p>
            </div>
            <Button onClick={() => navigate('/discovery/start')} className="flex items-center gap-2">
              <Search className="h-4 w-4" />
              Start Discovery
            </Button>
          </div>
        </div>

        {/* Quick Stats */}
        <div className="grid gap-4 md:grid-cols-4 mb-6">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Total Discoveries</CardTitle>
              <Search className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">-</div>
              <p className="text-xs text-muted-foreground">All time discoveries</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Running</CardTitle>
              <RefreshCw className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {discoveryStatus?.status === 'RUNNING' ? 1 : 0}
              </div>
              <p className="text-xs text-muted-foreground">Active discoveries</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Completed</CardTitle>
              <CheckCircle className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {discoveryStatus?.status === 'COMPLETE' ? 1 : 0}
              </div>
              <p className="text-xs text-muted-foreground">Successful discoveries</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Failed</CardTitle>
              <AlertTriangle className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {discoveryStatus?.status === 'FAILED' ? 1 : 0}
              </div>
              <p className="text-xs text-muted-foreground">Failed discoveries</p>
            </CardContent>
          </Card>
        </div>

        {/* Discovery Monitor */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle className="flex items-center gap-2">
                <Monitor className="h-5 w-5" />
                Discovery Monitor
              </CardTitle>
              <div className="flex items-center gap-2">
                {discoveryId && (
                  <Badge variant="outline" className="font-mono">
                    ID: {discoveryId}
                  </Badge>
                )}
                <Button 
                  variant="outline" 
                  size="sm" 
                  onClick={handleRefreshStatus} 
                  disabled={statusLoading || !discoveryId}
                >
                  <RefreshCw className={`h-4 w-4 ${statusLoading ? 'animate-spin' : ''}`} />
                  Refresh
                </Button>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            {!discoveryId ? (
              <div className="text-center py-8">
                <Monitor className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium mb-2">No active discovery</h3>
                <p className="text-muted-foreground mb-4">
                  Start a new discovery to monitor its progress here.
                </p>
                <Button onClick={() => navigate('/discovery/start')}>
                  <Play className="h-4 w-4 mr-2" />
                  Start Discovery
                </Button>
              </div>
            ) : statusLoading ? (
              <div className="flex items-center justify-center py-8">
                <RefreshCw className="h-6 w-6 animate-spin" />
                <span className="ml-2">Loading discovery status...</span>
              </div>
            ) : discoveryStatus ? (
              <div className="space-y-6">
                {/* Status Overview */}
                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                  <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                      <CardTitle className="text-sm font-medium">Status</CardTitle>
                      {getStatusIcon(discoveryStatus.status)}
                    </CardHeader>
                    <CardContent>
                      <Badge variant={getStatusColor(discoveryStatus.status)} className="text-sm">
                        {discoveryStatus.status}
                      </Badge>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                      <CardTitle className="text-sm font-medium">Devices Found</CardTitle>
                      <Search className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                      <div className="text-2xl font-bold">{discoveryStatus.totalDevicesDiscovered}</div>
                      <p className="text-xs text-muted-foreground">Discovered devices</p>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                      <CardTitle className="text-sm font-medium">Connections</CardTitle>
                      <Wifi className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                      <div className="text-2xl font-bold">{discoveryStatus.connections?.length || 0}</div>
                      <p className="text-xs text-muted-foreground">Network connections</p>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                      <CardTitle className="text-sm font-medium">Duration</CardTitle>
                      <Clock className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                      <div className="text-2xl font-bold">{formatDuration(discoveryStatus.discoveryDurationMs)}</div>
                      <p className="text-xs text-muted-foreground">Discovery time</p>
                    </CardContent>
                  </Card>
                </div>

                {/* Discovered Devices */}
                {discoveryStatus.nodes && discoveryStatus.nodes.length > 0 && (
                  <Card>
                    <CardHeader>
                      <CardTitle className="text-lg">Discovered Devices</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <Table>
                        <TableHeader>
                          <TableRow>
                            <TableHead>Device</TableHead>
                            <TableHead>IP Address</TableHead>
                            <TableHead>Type</TableHead>
                            <TableHead>Vendor</TableHead>
                            <TableHead>Model</TableHead>
                            <TableHead>Status</TableHead>
                            <TableHead>Interfaces</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {discoveryStatus.nodes.map((node) => (
                            <TableRow key={node.id}>
                              <TableCell>
                                <div className="flex items-center gap-2">
                                  {getDeviceIcon(node.deviceType)}
                                  <div>
                                    <div className="font-medium">{node.name}</div>
                                    <div className="text-sm text-muted-foreground">{node.macAddress}</div>
                                  </div>
                                </div>
                              </TableCell>
                              <TableCell className="font-mono text-sm">{node.ipAddress}</TableCell>
                              <TableCell>
                                <Badge variant="outline">{node.deviceType}</Badge>
                              </TableCell>
                              <TableCell>{node.vendor}</TableCell>
                              <TableCell>{node.model}</TableCell>
                              <TableCell>
                                <Badge variant={node.reachable ? "success" : "destructive"}>
                                  {node.reachable ? "Online" : "Offline"}
                                </Badge>
                              </TableCell>
                              <TableCell>
                                <Badge variant="secondary">{node.interfaces?.length || 0} interfaces</Badge>
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </CardContent>
                  </Card>
                )}

                {/* Network Connections */}
                {discoveryStatus.connections && discoveryStatus.connections.length > 0 && (
                  <Card>
                    <CardHeader>
                      <CardTitle className="text-lg">Network Connections</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <Table>
                        <TableHeader>
                          <TableRow>
                            <TableHead>Source Device</TableHead>
                            <TableHead>Target Device</TableHead>
                            <TableHead>Connection Type</TableHead>
                            <TableHead>Protocol</TableHead>
                            <TableHead>Redundant</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {discoveryStatus.connections.map((connection) => (
                            <TableRow key={connection.id}>
                              <TableCell className="font-mono text-sm">{connection.sourceNodeId}</TableCell>
                              <TableCell className="font-mono text-sm">{connection.targetNodeId}</TableCell>
                              <TableCell>
                                <Badge variant="outline">{connection.connectionType}</Badge>
                              </TableCell>
                              <TableCell>{connection.protocol || "N/A"}</TableCell>
                              <TableCell>
                                <Badge variant={connection.redundant ? "default" : "secondary"}>
                                  {connection.redundant ? "Yes" : "No"}
                                </Badge>
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </CardContent>
                  </Card>
                )}

                {/* Actions */}
                <div className="flex justify-end gap-2">
                  {discoveryStatus.status === 'RUNNING' && (
                    <Button
                      variant="destructive"
                      onClick={() => handleCancelDiscovery(discoveryStatus.discoveryId)}
                      disabled={cancelDiscoveryMutation.isPending}
                    >
                      <Trash2 className="h-4 w-4 mr-2" />
                      Cancel Discovery
                    </Button>
                  )}
                  <Button variant="outline" onClick={() => navigate('/discovery/start')}>
                    <Search className="h-4 w-4 mr-2" />
                    Start New Discovery
                  </Button>
                </div>
              </div>
            ) : (
              <div className="text-center py-8">
                <AlertTriangle className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium mb-2">Discovery not found</h3>
                <p className="text-muted-foreground mb-4">
                  The discovery with ID "{discoveryId}" could not be found.
                </p>
                <Button onClick={() => navigate('/discovery/start')}>
                  <Play className="h-4 w-4 mr-2" />
                  Start New Discovery
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
} 