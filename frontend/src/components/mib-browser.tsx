import { useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Badge } from "@/components/ui/badge"
import { Textarea } from "@/components/ui/textarea"
import { 
  Search, 
  RefreshCw, 
  Network, 
  AlertTriangle, 
  CheckCircle, 
  Copy,
  TreePine
} from "lucide-react"
import { mibApi } from "@/lib/mib-api"
import type { MibBrowserRequest, MibBrowserResponse } from "@/lib/mib-api"

export function MibBrowser() {
  const [request, setRequest] = useState<MibBrowserRequest>({
    targetIp: "",
    community: "public",
    oid: "",
    snmpPort: 161,
    timeout: 5000,
    retries: 3
  })
  
  const [response, setResponse] = useState<MibBrowserResponse | null>(null)
  const [walkResults, setWalkResults] = useState<MibBrowserResponse[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [isWalking, setIsWalking] = useState(false)

  const handleInputChange = (field: keyof MibBrowserRequest, value: string | number) => {
    setRequest(prev => ({
      ...prev,
      [field]: value
    }))
  }

  const validateRequest = (): boolean => {
    if (!request.targetIp.trim()) {
      alert("Target IP is required")
      return false
    }
    if (!request.oid.trim()) {
      alert("OID is required")
      return false
    }
    if (!request.community.trim()) {
      alert("Community string is required")
      return false
    }
    return true
  }

  const handleBrowse = async () => {
    if (!validateRequest()) return

    setIsLoading(true)
    setResponse(null)
    
    try {
      const result = await mibApi.browseOid(request)
      setResponse(result)
      if (result.success) {
        alert("SNMP query completed successfully")
      } else {
        alert(result.errorMessage || "SNMP query failed")
      }
    } catch (error) {
      console.error("Browse error:", error)
      alert("Failed to browse OID")
    } finally {
      setIsLoading(false)
    }
  }

  const handleWalk = async () => {
    if (!validateRequest()) return

    setIsWalking(true)
    setWalkResults([])
    
    try {
      const results = await mibApi.walkOidTree(request)
      setWalkResults(results)
      alert(`Walk completed: ${results.length} objects found`)
    } catch (error) {
      console.error("Walk error:", error)
      alert("Failed to walk OID tree")
    } finally {
      setIsWalking(false)
    }
  }

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text)
    alert("Copied to clipboard")
  }

  const getResponseIcon = (success: boolean) => {
    if (success) {
      return <CheckCircle className="h-4 w-4 text-green-500" />
    }
    return <AlertTriangle className="h-4 w-4 text-red-500" />
  }

  const getResponseColor = (success: boolean) => {
    return success ? "border-green-200 bg-green-50" : "border-red-200 bg-red-50"
  }

  return (
    <div className="space-y-6">
      {/* Request Form */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Network className="h-5 w-5" />
            SNMP Browser
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="targetIp">Target IP Address</Label>
              <Input
                id="targetIp"
                placeholder="192.168.1.1"
                value={request.targetIp}
                onChange={(e) => handleInputChange('targetIp', e.target.value)}
              />
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="community">Community String</Label>
              <Input
                id="community"
                placeholder="public"
                value={request.community}
                onChange={(e) => handleInputChange('community', e.target.value)}
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="oid">Object Identifier (OID)</Label>
            <Input
              id="oid"
              placeholder="1.3.6.1.2.1.1.1.0"
              value={request.oid}
              onChange={(e) => handleInputChange('oid', e.target.value)}
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="space-y-2">
              <Label htmlFor="snmpPort">SNMP Port</Label>
              <Input
                id="snmpPort"
                type="number"
                placeholder="161"
                value={request.snmpPort}
                onChange={(e) => handleInputChange('snmpPort', parseInt(e.target.value) || 161)}
              />
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="timeout">Timeout (ms)</Label>
              <Input
                id="timeout"
                type="number"
                placeholder="5000"
                value={request.timeout}
                onChange={(e) => handleInputChange('timeout', parseInt(e.target.value) || 5000)}
              />
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="retries">Retries</Label>
              <Input
                id="retries"
                type="number"
                placeholder="3"
                value={request.retries}
                onChange={(e) => handleInputChange('retries', parseInt(e.target.value) || 3)}
              />
            </div>
          </div>

          <div className="flex gap-2">
            <Button 
              onClick={handleBrowse}
              disabled={isLoading || isWalking}
              className="flex items-center gap-2"
            >
              {isLoading ? (
                <RefreshCw className="h-4 w-4 animate-spin" />
              ) : (
                <Search className="h-4 w-4" />
              )}
              Browse OID
            </Button>
            
            <Button 
              onClick={handleWalk}
              disabled={isLoading || isWalking}
              variant="outline"
              className="flex items-center gap-2"
            >
              {isWalking ? (
                <RefreshCw className="h-4 w-4 animate-spin" />
              ) : (
                <TreePine className="h-4 w-4" />
              )}
              Walk Tree
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Single Response */}
      {response && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              {getResponseIcon(response.success)}
              SNMP Response
              {response.responseTime > 0 && (
                <Badge variant="outline" className="ml-auto">
                  {response.responseTime}ms
                </Badge>
              )}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className={`p-4 border rounded-lg ${getResponseColor(response.success)}`}>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <Label className="text-sm font-medium">OID</Label>
                  <div className="flex items-center gap-2 mt-1">
                    <code className="text-sm bg-muted px-2 py-1 rounded">
                      {response.oid}
                    </code>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => copyToClipboard(response.oid)}
                    >
                      <Copy className="h-3 w-3" />
                    </Button>
                  </div>
                </div>
                
                {response.name && (
                  <div>
                    <Label className="text-sm font-medium">Name</Label>
                    <p className="text-sm mt-1">{response.name}</p>
                  </div>
                )}
                
                <div className="md:col-span-2">
                  <Label className="text-sm font-medium">Value</Label>
                  <div className="flex items-center gap-2 mt-1">
                    <Textarea
                      value={response.value}
                      readOnly
                      className="min-h-[60px] font-mono text-sm"
                    />
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => copyToClipboard(response.value)}
                    >
                      <Copy className="h-3 w-3" />
                    </Button>
                  </div>
                </div>
                
                <div>
                  <Label className="text-sm font-medium">Type</Label>
                  <Badge variant="outline" className="mt-1">
                    {response.type}
                  </Badge>
                </div>
                
                {response.syntax && (
                  <div>
                    <Label className="text-sm font-medium">Syntax</Label>
                    <Badge variant="outline" className="mt-1">
                      {response.syntax}
                    </Badge>
                  </div>
                )}
              </div>
              
              {response.errorMessage && (
                <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded-lg">
                  <Label className="text-sm font-medium text-red-800">Error</Label>
                  <p className="text-sm text-red-700 mt-1">{response.errorMessage}</p>
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Walk Results */}
      {walkResults.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <TreePine className="h-5 w-5" />
              Walk Results ({walkResults.length} objects)
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="max-h-96 overflow-y-auto space-y-2">
              {walkResults.map((result, index) => (
                <div
                  key={index}
                  className={`p-3 border rounded-lg ${getResponseColor(result.success)}`}
                >
                  <div className="flex items-start justify-between gap-2">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        {getResponseIcon(result.success)}
                        <code className="text-sm font-mono">{result.oid}</code>
                        {result.name && (
                          <Badge variant="outline" className="text-xs">
                            {result.name}
                          </Badge>
                        )}
                      </div>
                      <p className="text-sm text-muted-foreground break-all">
                        {result.value}
                      </p>
                    </div>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => copyToClipboard(result.value)}
                    >
                      <Copy className="h-3 w-3" />
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
} 