import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Badge } from "@/components/ui/badge"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Search, ArrowLeft, Play, Wifi, CheckCircle, XCircle } from "lucide-react"
import { useStartDiscovery, useTestPing } from "@/lib/discovery-hooks"
import type { DiscoveryStartRequest, PingTestRequest } from "@/lib/discovery-api"

export function DiscoveryStartPage() {
  const navigate = useNavigate()
  const startDiscoveryMutation = useStartDiscovery()
  const testPingMutation = useTestPing()
  
  const [formData, setFormData] = useState<DiscoveryStartRequest>({
    networkRange: '',
    scanType: 'PING',
    timeout: 5000,
    retries: 3
  })
  
  const [pingTest, setPingTest] = useState<PingTestRequest>({
    targetIp: '',
    timeout: 5000,
    retries: 3
  })
  
  const [pingResult, setPingResult] = useState<{ success: boolean; message: string; responseTime?: number } | null>(null)

  const handleInputChange = (field: keyof DiscoveryStartRequest, value: string | number) => {
    setFormData(prev => ({ ...prev, [field]: value }))
  }

  const handlePingTest = async () => {
    if (!pingTest.targetIp.trim()) {
      setPingResult({ success: false, message: 'Please enter a target IP address' })
      return
    }

    try {
      const result = await testPingMutation.mutateAsync(pingTest)
      setPingResult({
        success: result.success,
        message: result.message,
        responseTime: result.responseTime
      })
    } catch (error) {
      setPingResult({
        success: false,
        message: error instanceof Error ? error.message : 'Ping test failed'
      })
    }
  }

  const handleStartDiscovery = async () => {
    if (!formData.networkRange.trim()) {
      return
    }

    try {
      const result = await startDiscoveryMutation.mutateAsync(formData)
      // Navigate to discovery dashboard after successful start
      navigate('/discovery', { 
        state: { 
          message: `Discovery started successfully! ID: ${result.discoveryId}`,
          discoveryId: result.discoveryId 
        } 
      })
    } catch (error) {
      console.error('Failed to start discovery:', error)
    }
  }

  const validateNetworkRange = (range: string) => {
    // Basic validation for common network range formats
    const patterns = [
      /^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$/, // Single IP
      /^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\/\d{1,2}$/, // CIDR
      /^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}-\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$/, // IP range
    ]
    return patterns.some(pattern => pattern.test(range))
  }

  const isFormValid = formData.networkRange.trim() && validateNetworkRange(formData.networkRange)

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <Button
            variant="ghost"
            onClick={() => navigate('/discovery')}
            className="mb-4 flex items-center gap-2"
          >
            <ArrowLeft className="h-4 w-4" />
            Back to Discovery Dashboard
          </Button>
          <h1 className="text-3xl font-bold tracking-tight">Start Network Discovery</h1>
          <p className="text-muted-foreground mt-2">
            Configure and start a new network discovery to find devices on your network.
          </p>
        </div>

        <div className="grid gap-6 lg:grid-cols-2">
          {/* Discovery Configuration */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Search className="h-5 w-5" />
                Discovery Configuration
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="networkRange">Network Range *</Label>
                <Input
                  id="networkRange"
                  placeholder="e.g., 192.168.1.0/24 or 192.168.1.1-192.168.1.254"
                  value={formData.networkRange}
                  onChange={(e) => handleInputChange('networkRange', e.target.value)}
                />
                <p className="text-xs text-muted-foreground">
                  Enter IP range in CIDR notation (e.g., 192.168.1.0/24) or IP range (e.g., 192.168.1.1-192.168.1.254)
                </p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="scanType">Scan Type</Label>
                <select
                  id="scanType"
                  value={formData.scanType}
                  onChange={(e) => handleInputChange('scanType', e.target.value as 'PING' | 'SNMP' | 'FULL')}
                  className="flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  <option value="PING">Ping Scan</option>
                  <option value="SNMP">SNMP Scan</option>
                  <option value="FULL">Full Scan</option>
                </select>
                <p className="text-xs text-muted-foreground">
                  {formData.scanType === 'PING' && 'Basic ping scan to detect online devices'}
                  {formData.scanType === 'SNMP' && 'SNMP scan to gather device information'}
                  {formData.scanType === 'FULL' && 'Comprehensive scan with ping and SNMP'}
                </p>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="timeout">Timeout (ms)</Label>
                  <Input
                    id="timeout"
                    type="number"
                    min="1000"
                    max="30000"
                    value={formData.timeout}
                    onChange={(e) => handleInputChange('timeout', parseInt(e.target.value) || 5000)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="retries">Retries</Label>
                  <Input
                    id="retries"
                    type="number"
                    min="1"
                    max="10"
                    value={formData.retries}
                    onChange={(e) => handleInputChange('retries', parseInt(e.target.value) || 3)}
                  />
                </div>
              </div>

              <Button
                onClick={handleStartDiscovery}
                disabled={!isFormValid || startDiscoveryMutation.isPending}
                className="w-full"
              >
                {startDiscoveryMutation.isPending ? (
                  <div className="flex items-center gap-2">
                    <div className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
                    Starting Discovery...
                  </div>
                ) : (
                  <div className="flex items-center gap-2">
                    <Play className="h-4 w-4" />
                    Start Discovery
                  </div>
                )}
              </Button>
            </CardContent>
          </Card>

          {/* Ping Test */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Wifi className="h-5 w-5" />
                Test Connectivity
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="pingTarget">Target IP Address</Label>
                <Input
                  id="pingTarget"
                  placeholder="e.g., 192.168.1.1"
                  value={pingTest.targetIp}
                  onChange={(e) => setPingTest(prev => ({ ...prev, targetIp: e.target.value }))}
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="pingTimeout">Timeout (ms)</Label>
                  <Input
                    id="pingTimeout"
                    type="number"
                    min="1000"
                    max="30000"
                    value={pingTest.timeout}
                    onChange={(e) => setPingTest(prev => ({ ...prev, timeout: parseInt(e.target.value) || 5000 }))}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="pingRetries">Retries</Label>
                  <Input
                    id="pingRetries"
                    type="number"
                    min="1"
                    max="10"
                    value={pingTest.retries}
                    onChange={(e) => setPingTest(prev => ({ ...prev, retries: parseInt(e.target.value) || 3 }))}
                  />
                </div>
              </div>

              <Button
                onClick={handlePingTest}
                disabled={!pingTest.targetIp.trim() || testPingMutation.isPending}
                variant="outline"
                className="w-full"
              >
                {testPingMutation.isPending ? (
                  <div className="flex items-center gap-2">
                    <div className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
                    Testing...
                  </div>
                ) : (
                  <div className="flex items-center gap-2">
                    <Wifi className="h-4 w-4" />
                    Test Ping
                  </div>
                )}
              </Button>

              {/* Ping Result */}
              {pingResult && (
                <Alert className={pingResult.success ? "border-green-200 bg-green-50" : "border-red-200 bg-red-50"}>
                  <div className="flex items-center gap-2">
                    {pingResult.success ? (
                      <CheckCircle className="h-4 w-4 text-green-600" />
                    ) : (
                      <XCircle className="h-4 w-4 text-red-600" />
                    )}
                    <AlertDescription className={pingResult.success ? "text-green-800" : "text-red-800"}>
                      {pingResult.message}
                      {pingResult.responseTime && (
                        <span className="ml-2 text-sm">
                          (Response time: {pingResult.responseTime}ms)
                        </span>
                      )}
                    </AlertDescription>
                  </div>
                </Alert>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Scan Type Information */}
        <Card className="mt-6">
          <CardHeader>
            <CardTitle>Scan Type Information</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid gap-4 md:grid-cols-3">
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <Wifi className="h-4 w-4 text-blue-600" />
                  <h4 className="font-medium">Ping Scan</h4>
                </div>
                <p className="text-sm text-muted-foreground">
                  Basic connectivity test to detect online devices. Fast and lightweight.
                </p>
                <Badge variant="outline">Fast</Badge>
              </div>
              
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <Search className="h-4 w-4 text-green-600" />
                  <h4 className="font-medium">SNMP Scan</h4>
                </div>
                <p className="text-sm text-muted-foreground">
                  Gather detailed device information using SNMP protocol.
                </p>
                <Badge variant="outline">Detailed</Badge>
              </div>
              
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <Search className="h-4 w-4 text-purple-600" />
                  <h4 className="font-medium">Full Scan</h4>
                </div>
                <p className="text-sm text-muted-foreground">
                  Comprehensive scan combining ping and SNMP for complete device discovery.
                </p>
                <Badge variant="outline">Complete</Badge>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
} 