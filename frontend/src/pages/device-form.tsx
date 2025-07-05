import { useState, useEffect } from "react"
import { useParams, Link } from "react-router-dom"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { 
  useDevice, 
  useCreateDevice, 
  useUpdateDevice 
} from "@/lib/device-hooks"
import { type DeviceCreateRequest } from "@/lib/api"
import { 
  ArrowLeft, 
  Save, 
  RefreshCw,
  Server,
  Router,
  Network,
  Printer,
  Shield,
  Radio,
  Monitor,
  Wifi
} from "lucide-react"

export function DeviceFormPage() {
  const { id } = useParams<{ id: string }>()
  const isEditing = !!id
  const deviceId = parseInt(id || '0')

  const { data: existingDevice, isLoading: deviceLoading } = useDevice(deviceId)
  const createDeviceMutation = useCreateDevice()
  const updateDeviceMutation = useUpdateDevice()

  const [formData, setFormData] = useState<DeviceCreateRequest>({
    name: '',
    description: '',
    systemObjectId: '',
    systemContact: '',
    systemName: '',
    systemLocation: '',
    monitoringEnabled: true,
    status: 'ACTIVE',
    type: 'SERVER',
    configuration: {
      targetIp: '',
      snmpPort: 161,
      snmpVersion: 'V2C',
      communityString: 'public',
      snmpTimeout: 5000,
      snmpRetries: 3,
      pollInterval: 300,
      enabled: true,
      authProtocol: 'NONE',
      privProtocol: 'NONE'
    }
  })

  const [errors, setErrors] = useState<Record<string, string>>({})

  // Load existing device data when editing
  useEffect(() => {
    if (isEditing && existingDevice) {
      setFormData({
        name: existingDevice.name,
        description: existingDevice.description || '',
        systemObjectId: existingDevice.systemObjectId || '',
        systemContact: existingDevice.systemContact || '',
        systemName: existingDevice.systemName || '',
        systemLocation: existingDevice.systemLocation || '',
        monitoringEnabled: existingDevice.monitoringEnabled,
        status: existingDevice.status,
        type: existingDevice.type,
        configuration: (existingDevice.deviceConfig || existingDevice.configuration) ? {
          targetIp: (existingDevice.deviceConfig || existingDevice.configuration)?.targetIp || '',
          snmpPort: (existingDevice.deviceConfig || existingDevice.configuration)?.snmpPort || 161,
          snmpVersion: (existingDevice.deviceConfig || existingDevice.configuration)?.snmpVersion || 'V2C',
          communityString: (existingDevice.deviceConfig || existingDevice.configuration)?.communityString || '',
          snmpTimeout: (existingDevice.deviceConfig || existingDevice.configuration)?.snmpTimeout || 5000,
          snmpRetries: (existingDevice.deviceConfig || existingDevice.configuration)?.snmpRetries || 3,
          pollInterval: (existingDevice.deviceConfig || existingDevice.configuration)?.pollInterval || 300,
          enabled: (existingDevice.deviceConfig || existingDevice.configuration)?.enabled || true,
          securityName: (existingDevice.deviceConfig || existingDevice.configuration)?.securityName || '',
          authProtocol: (existingDevice.deviceConfig || existingDevice.configuration)?.authProtocol || 'NONE',
          authPassphrase: (existingDevice.deviceConfig || existingDevice.configuration)?.authPassphrase || '',
          privProtocol: (existingDevice.deviceConfig || existingDevice.configuration)?.privProtocol || 'NONE',
          privPassphrase: (existingDevice.deviceConfig || existingDevice.configuration)?.privPassphrase || '',
          contextName: (existingDevice.deviceConfig || existingDevice.configuration)?.contextName || ''
        } : {
          targetIp: '',
          snmpPort: 161,
          snmpVersion: 'V2C',
          communityString: 'public',
          snmpTimeout: 5000,
          snmpRetries: 3,
          pollInterval: 300,
          enabled: true,
          authProtocol: 'NONE',
          privProtocol: 'NONE'
        }
      })
    }
  }, [isEditing, existingDevice])

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

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {}

    if (!formData.name.trim()) {
      newErrors.name = 'Device name is required'
    }

    if (formData.name.length > 100) {
      newErrors.name = 'Device name must be less than 100 characters'
    }

    if (formData.description && formData.description.length > 500) {
      newErrors.description = 'Description must be less than 500 characters'
    }

    if (formData.systemContact && formData.systemContact.length > 100) {
      newErrors.systemContact = 'Contact must be less than 100 characters'
    }

    if (formData.systemName && formData.systemName.length > 100) {
      newErrors.systemName = 'System name must be less than 100 characters'
    }

    if (formData.systemLocation && formData.systemLocation.length > 100) {
      newErrors.systemLocation = 'Location must be less than 100 characters'
    }

    // Validate SNMP configuration
    if (formData.configuration) {
      if (!formData.configuration.targetIp.trim()) {
        newErrors['configuration.targetIp'] = 'Target IP is required'
      }

      if (formData.configuration.snmpPort < 1 || formData.configuration.snmpPort > 65535) {
        newErrors['configuration.snmpPort'] = 'SNMP port must be between 1 and 65535'
      }

      if (!formData.configuration.communityString.trim()) {
        newErrors['configuration.communityString'] = 'Community string is required'
      }

      if (formData.configuration.snmpTimeout < 1000) {
        newErrors['configuration.snmpTimeout'] = 'SNMP timeout must be at least 1000ms'
      }

      if (formData.configuration.snmpRetries < 0 || formData.configuration.snmpRetries > 10) {
        newErrors['configuration.snmpRetries'] = 'SNMP retries must be between 0 and 10'
      }

      if (formData.configuration.pollInterval < 30) {
        newErrors['configuration.pollInterval'] = 'Poll interval must be at least 30 seconds'
      }

      // V3 specific validations
      if (formData.configuration.snmpVersion === 'V3') {
        if (!formData.configuration.securityName?.trim()) {
          newErrors['configuration.securityName'] = 'Security name is required for SNMPv3'
        }
        
        if (formData.configuration.authProtocol !== 'NONE' && !formData.configuration.authPassphrase?.trim()) {
          newErrors['configuration.authPassphrase'] = 'Auth passphrase is required when auth protocol is set'
        }
        
        if (formData.configuration.privProtocol !== 'NONE' && !formData.configuration.privPassphrase?.trim()) {
          newErrors['configuration.privPassphrase'] = 'Privacy passphrase is required when privacy protocol is set'
        }
      }
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!validateForm()) {
      return
    }

    try {
      if (isEditing) {
        await updateDeviceMutation.mutateAsync({ id: deviceId, data: formData })
      } else {
        await createDeviceMutation.mutateAsync(formData)
      }
    } catch (error) {
      console.error('Failed to save device:', error)
    }
  }

  const handleInputChange = (field: keyof DeviceCreateRequest, value: string | boolean) => {
    setFormData(prev => ({ ...prev, [field]: value }))
    // Clear error when user starts typing
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }))
    }
  }

  const handleConfigurationChange = (field: string, value: string | number | boolean) => {
    setFormData(prev => ({
      ...prev,
      configuration: prev.configuration ? {
        ...prev.configuration,
        [field]: value
      } : {
        targetIp: '',
        snmpPort: 161,
        snmpVersion: 'V2C',
        communityString: 'public',
        snmpTimeout: 5000,
        snmpRetries: 3,
        pollInterval: 300,
        enabled: true,
        authProtocol: 'NONE',
        privProtocol: 'NONE',
        [field]: value
      }
    }))
    
    // Clear error when user starts typing
    const errorKey = `configuration.${field}`
    if (errors[errorKey]) {
      setErrors(prev => ({ ...prev, [errorKey]: '' }))
    }
  }

  if (isEditing && deviceLoading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="flex items-center justify-center py-8">
          <RefreshCw className="h-6 w-6 animate-spin" />
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center gap-4">
            <Link to={isEditing ? `/devices/${deviceId}` : '/devices'}>
              <Button variant="outline" size="sm">
                <ArrowLeft className="h-4 w-4 mr-2" />
                Back
              </Button>
            </Link>
            <div>
              <h1 className="text-3xl font-bold tracking-tight">
                {isEditing ? 'Edit Device' : 'Add New Device'}
              </h1>
              <p className="text-muted-foreground mt-2">
                {isEditing 
                  ? 'Update device information and settings'
                  : 'Configure a new network device for monitoring'
                }
              </p>
            </div>
          </div>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="grid gap-6 lg:grid-cols-3">
            {/* Main Form */}
            <div className="lg:col-span-2 space-y-6">
              {/* Basic Information */}
              <Card>
                <CardHeader>
                  <CardTitle>Basic Information</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div>
                    <Label htmlFor="name">Device Name *</Label>
                    <Input
                      id="name"
                      value={formData.name}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleInputChange('name', e.target.value)}
                      placeholder="Enter device name"
                      className={errors.name ? 'border-red-500' : ''}
                    />
                    {errors.name && (
                      <p className="text-sm text-red-500 mt-1">{errors.name}</p>
                    )}
                  </div>

                  <div>
                    <Label htmlFor="description">Description</Label>
                    <textarea
                      id="description"
                      value={formData.description}
                      onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => handleInputChange('description', e.target.value)}
                      placeholder="Enter device description"
                      rows={3}
                      className={`flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 ${errors.description ? 'border-red-500' : ''}`}
                    />
                    {errors.description && (
                      <p className="text-sm text-red-500 mt-1">{errors.description}</p>
                    )}
                  </div>

                  <div className="grid gap-4 md:grid-cols-2">
                    <div>
                      <Label htmlFor="type">Device Type *</Label>
                      <select
                        id="type"
                        value={formData.type}
                        onChange={(e: React.ChangeEvent<HTMLSelectElement>) => handleInputChange('type', e.target.value)}
                        className="flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                      >
                        <option value="ROUTER">Router</option>
                        <option value="SWITCH">Switch</option>
                        <option value="SERVER">Server</option>
                        <option value="WORKSTATION">Workstation</option>
                        <option value="PRINTER">Printer</option>
                        <option value="FIREWALL">Firewall</option>
                        <option value="ACCESS_POINT">Access Point</option>
                        <option value="OTHER">Other</option>
                      </select>
                    </div>

                    <div>
                      <Label htmlFor="status">Status *</Label>
                      <select
                        id="status"
                        value={formData.status}
                        onChange={(e: React.ChangeEvent<HTMLSelectElement>) => handleInputChange('status', e.target.value)}
                        className="flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                      >
                        <option value="ACTIVE">Active</option>
                        <option value="INACTIVE">Inactive</option>
                        <option value="MAINTENANCE">Maintenance</option>
                        <option value="ERROR">Error</option>
                      </select>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* System Information */}
              <Card>
                <CardHeader>
                  <CardTitle>System Information</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid gap-4 md:grid-cols-2">
                    <div>
                      <Label htmlFor="systemName">System Name</Label>
                      <Input
                        id="systemName"
                        value={formData.systemName}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleInputChange('systemName', e.target.value)}
                        placeholder="Enter system name"
                        className={errors.systemName ? 'border-red-500' : ''}
                      />
                      {errors.systemName && (
                        <p className="text-sm text-red-500 mt-1">{errors.systemName}</p>
                      )}
                    </div>

                    <div>
                      <Label htmlFor="systemLocation">Location</Label>
                      <Input
                        id="systemLocation"
                        value={formData.systemLocation}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleInputChange('systemLocation', e.target.value)}
                        placeholder="Enter device location"
                        className={errors.systemLocation ? 'border-red-500' : ''}
                      />
                      {errors.systemLocation && (
                        <p className="text-sm text-red-500 mt-1">{errors.systemLocation}</p>
                      )}
                    </div>
                  </div>

                  <div className="grid gap-4 md:grid-cols-2">
                    <div>
                      <Label htmlFor="systemContact">Contact</Label>
                      <Input
                        id="systemContact"
                        value={formData.systemContact}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleInputChange('systemContact', e.target.value)}
                        placeholder="Enter contact information"
                        className={errors.systemContact ? 'border-red-500' : ''}
                      />
                      {errors.systemContact && (
                        <p className="text-sm text-red-500 mt-1">{errors.systemContact}</p>
                      )}
                    </div>

                    <div>
                      <Label htmlFor="systemObjectId">System Object ID</Label>
                      <Input
                        id="systemObjectId"
                        value={formData.systemObjectId}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleInputChange('systemObjectId', e.target.value)}
                        placeholder="Enter system object ID"
                      />
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* SNMP Configuration */}
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Wifi className="h-5 w-5" />
                    SNMP Configuration
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  {/* Basic SNMP Settings */}
                  <div className="grid gap-4 md:grid-cols-2">
                    <div>
                      <Label htmlFor="targetIp">Target IP *</Label>
                      <Input
                        id="targetIp"
                        value={formData.configuration?.targetIp || ''}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleConfigurationChange('targetIp', e.target.value)}
                        placeholder="192.168.1.1"
                        className={errors['configuration.targetIp'] ? 'border-red-500' : ''}
                      />
                      {errors['configuration.targetIp'] && (
                        <p className="text-sm text-red-500 mt-1">{errors['configuration.targetIp']}</p>
                      )}
                    </div>

                    <div>
                      <Label htmlFor="snmpPort">SNMP Port *</Label>
                      <Input
                        id="snmpPort"
                        type="number"
                        value={formData.configuration?.snmpPort || 161}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleConfigurationChange('snmpPort', parseInt(e.target.value))}
                        min="1"
                        max="65535"
                        className={errors['configuration.snmpPort'] ? 'border-red-500' : ''}
                      />
                      {errors['configuration.snmpPort'] && (
                        <p className="text-sm text-red-500 mt-1">{errors['configuration.snmpPort']}</p>
                      )}
                    </div>
                  </div>

                  <div className="grid gap-4 md:grid-cols-2">
                    <div>
                      <Label htmlFor="snmpVersion">SNMP Version *</Label>
                      <select
                        id="snmpVersion"
                        value={formData.configuration?.snmpVersion || 'V2C'}
                        onChange={(e: React.ChangeEvent<HTMLSelectElement>) => handleConfigurationChange('snmpVersion', e.target.value)}
                        className="flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                      >
                        <option value="V1">SNMPv1</option>
                        <option value="V2C">SNMPv2c</option>
                        <option value="V3">SNMPv3</option>
                      </select>
                    </div>

                    <div>
                      <Label htmlFor="communityString">Community String *</Label>
                      <Input
                        id="communityString"
                        value={formData.configuration?.communityString || ''}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleConfigurationChange('communityString', e.target.value)}
                        placeholder="public"
                        className={errors['configuration.communityString'] ? 'border-red-500' : ''}
                      />
                      {errors['configuration.communityString'] && (
                        <p className="text-sm text-red-500 mt-1">{errors['configuration.communityString']}</p>
                      )}
                    </div>
                  </div>

                  {/* Polling Settings */}
                  <div className="grid gap-4 md:grid-cols-3">
                    <div>
                      <Label htmlFor="snmpTimeout">Timeout (ms) *</Label>
                      <Input
                        id="snmpTimeout"
                        type="number"
                        value={formData.configuration?.snmpTimeout || 5000}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleConfigurationChange('snmpTimeout', parseInt(e.target.value))}
                        min="1000"
                        step="1000"
                        className={errors['configuration.snmpTimeout'] ? 'border-red-500' : ''}
                      />
                      {errors['configuration.snmpTimeout'] && (
                        <p className="text-sm text-red-500 mt-1">{errors['configuration.snmpTimeout']}</p>
                      )}
                    </div>

                    <div>
                      <Label htmlFor="snmpRetries">Retries *</Label>
                      <Input
                        id="snmpRetries"
                        type="number"
                        value={formData.configuration?.snmpRetries || 3}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleConfigurationChange('snmpRetries', parseInt(e.target.value))}
                        min="0"
                        max="10"
                        className={errors['configuration.snmpRetries'] ? 'border-red-500' : ''}
                      />
                      {errors['configuration.snmpRetries'] && (
                        <p className="text-sm text-red-500 mt-1">{errors['configuration.snmpRetries']}</p>
                      )}
                    </div>

                    <div>
                      <Label htmlFor="pollInterval">Poll Interval (s) *</Label>
                      <Input
                        id="pollInterval"
                        type="number"
                        value={formData.configuration?.pollInterval || 300}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleConfigurationChange('pollInterval', parseInt(e.target.value))}
                        min="30"
                        step="30"
                        className={errors['configuration.pollInterval'] ? 'border-red-500' : ''}
                      />
                      {errors['configuration.pollInterval'] && (
                        <p className="text-sm text-red-500 mt-1">{errors['configuration.pollInterval']}</p>
                      )}
                    </div>
                  </div>

                  {/* SNMPv3 Settings */}
                  {formData.configuration?.snmpVersion === 'V3' && (
                    <div className="space-y-4 border-t pt-4">
                      <h4 className="font-medium">SNMPv3 Security Settings</h4>
                      
                      <div>
                        <Label htmlFor="securityName">Security Name *</Label>
                        <Input
                          id="securityName"
                          value={formData.configuration?.securityName || ''}
                          onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleConfigurationChange('securityName', e.target.value)}
                          placeholder="username"
                          className={errors['configuration.securityName'] ? 'border-red-500' : ''}
                        />
                        {errors['configuration.securityName'] && (
                          <p className="text-sm text-red-500 mt-1">{errors['configuration.securityName']}</p>
                        )}
                      </div>

                      <div className="grid gap-4 md:grid-cols-2">
                        <div>
                          <Label htmlFor="authProtocol">Auth Protocol</Label>
                          <select
                            id="authProtocol"
                            value={formData.configuration?.authProtocol || 'NONE'}
                            onChange={(e: React.ChangeEvent<HTMLSelectElement>) => handleConfigurationChange('authProtocol', e.target.value)}
                            className="flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                          >
                            <option value="NONE">None</option>
                            <option value="MD5">MD5</option>
                            <option value="SHA">SHA</option>
                            <option value="SHA224">SHA-224</option>
                            <option value="SHA256">SHA-256</option>
                            <option value="SHA384">SHA-384</option>
                            <option value="SHA512">SHA-512</option>
                          </select>
                        </div>

                        <div>
                          <Label htmlFor="privProtocol">Privacy Protocol</Label>
                          <select
                            id="privProtocol"
                            value={formData.configuration?.privProtocol || 'NONE'}
                            onChange={(e: React.ChangeEvent<HTMLSelectElement>) => handleConfigurationChange('privProtocol', e.target.value)}
                            className="flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                          >
                            <option value="NONE">None</option>
                            <option value="DES">DES</option>
                            <option value="AES">AES</option>
                            <option value="AES192">AES-192</option>
                            <option value="AES256">AES-256</option>
                          </select>
                        </div>
                      </div>

                      <div className="grid gap-4 md:grid-cols-2">
                        <div>
                          <Label htmlFor="authPassphrase">Auth Passphrase</Label>
                          <Input
                            id="authPassphrase"
                            type="password"
                            value={formData.configuration?.authPassphrase || ''}
                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleConfigurationChange('authPassphrase', e.target.value)}
                            placeholder="Auth passphrase"
                            className={errors['configuration.authPassphrase'] ? 'border-red-500' : ''}
                          />
                          {errors['configuration.authPassphrase'] && (
                            <p className="text-sm text-red-500 mt-1">{errors['configuration.authPassphrase']}</p>
                          )}
                        </div>

                        <div>
                          <Label htmlFor="privPassphrase">Privacy Passphrase</Label>
                          <Input
                            id="privPassphrase"
                            type="password"
                            value={formData.configuration?.privPassphrase || ''}
                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleConfigurationChange('privPassphrase', e.target.value)}
                            placeholder="Privacy passphrase"
                            className={errors['configuration.privPassphrase'] ? 'border-red-500' : ''}
                          />
                          {errors['configuration.privPassphrase'] && (
                            <p className="text-sm text-red-500 mt-1">{errors['configuration.privPassphrase']}</p>
                          )}
                        </div>
                      </div>

                      <div>
                        <Label htmlFor="contextName">Context Name</Label>
                        <Input
                          id="contextName"
                          value={formData.configuration?.contextName || ''}
                          onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleConfigurationChange('contextName', e.target.value)}
                          placeholder="Context name (optional)"
                        />
                      </div>
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>

            {/* Sidebar */}
            <div className="space-y-6">
              {/* Monitoring Settings */}
              <Card>
                <CardHeader>
                  <CardTitle>Monitoring Settings</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <Label htmlFor="monitoring">Enable Monitoring</Label>
                      <p className="text-sm text-muted-foreground">
                        Allow this device to be monitored
                      </p>
                    </div>
                    <button
                      type="button"
                      role="switch"
                      aria-checked={formData.monitoringEnabled}
                      data-state={formData.monitoringEnabled ? "checked" : "unchecked"}
                      className={`peer inline-flex h-6 w-11 shrink-0 cursor-pointer items-center rounded-full border-2 border-transparent transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background disabled:cursor-not-allowed disabled:opacity-50 ${formData.monitoringEnabled ? 'bg-primary' : 'bg-input'}`}
                      onClick={() => handleInputChange('monitoringEnabled', !formData.monitoringEnabled)}
                    >
                      <span
                        data-state={formData.monitoringEnabled ? "checked" : "unchecked"}
                        className={`pointer-events-none block h-5 w-5 rounded-full bg-background shadow-lg ring-0 transition-transform ${formData.monitoringEnabled ? 'translate-x-5' : 'translate-x-0'}`}
                      />
                    </button>
                  </div>

                  <div className="flex items-center justify-between">
                    <div>
                      <Label htmlFor="snmpEnabled">Enable SNMP</Label>
                      <p className="text-sm text-muted-foreground">
                        Enable SNMP polling
                      </p>
                    </div>
                    <button
                      type="button"
                      role="switch"
                      aria-checked={formData.configuration?.enabled}
                      data-state={formData.configuration?.enabled ? "checked" : "unchecked"}
                      className={`peer inline-flex h-6 w-11 shrink-0 cursor-pointer items-center rounded-full border-2 border-transparent transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background disabled:cursor-not-allowed disabled:opacity-50 ${formData.configuration?.enabled ? 'bg-primary' : 'bg-input'}`}
                      onClick={() => handleConfigurationChange('enabled', !formData.configuration?.enabled)}
                    >
                      <span
                        data-state={formData.configuration?.enabled ? "checked" : "unchecked"}
                        className={`pointer-events-none block h-5 w-5 rounded-full bg-background shadow-lg ring-0 transition-transform ${formData.configuration?.enabled ? 'translate-x-5' : 'translate-x-0'}`}
                      />
                    </button>
                  </div>
                </CardContent>
              </Card>

              {/* Actions */}
              <Card>
                <CardHeader>
                  <CardTitle>Actions</CardTitle>
                </CardHeader>
                <CardContent className="flex flex-col space-y-4">
                  <Button 
                    type="submit" 
                    className="w-full"
                    disabled={createDeviceMutation.isPending || updateDeviceMutation.isPending}
                  >
                    {(createDeviceMutation.isPending || updateDeviceMutation.isPending) ? (
                      <RefreshCw className="h-4 w-4 animate-spin mr-2" />
                    ) : (
                      <Save className="h-4 w-4 mr-2" />
                    )}
                    {isEditing ? 'Update Device' : 'Create Device'}
                  </Button>
                  
                  <Link to={isEditing ? `/devices/${deviceId}` : '/devices'} className="w-full">
                    <Button variant="outline" className="w-full">
                      Cancel
                    </Button>
                  </Link>
                </CardContent>
              </Card>

              {/* Device Type Preview */}
              <Card>
                <CardHeader>
                  <CardTitle>Device Preview</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-3 p-3 border rounded-lg">
                    {getTypeIcon(formData.type)}
                    <div>
                      <p className="font-medium">{formData.name || 'Device Name'}</p>
                      <p className="text-sm text-muted-foreground">
                        {formData.type.replace('_', ' ')}
                      </p>
                    </div>
                  </div>
                  {formData.configuration?.targetIp && (
                    <div className="mt-3 p-2 bg-muted rounded text-xs">
                      <p><strong>Target:</strong> {formData.configuration.targetIp}:{formData.configuration.snmpPort}</p>
                      <p><strong>Version:</strong> {formData.configuration.snmpVersion}</p>
                      <p><strong>Community:</strong> {formData.configuration.communityString}</p>
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>
          </div>
        </form>
      </div>
    </div>
  )
} 