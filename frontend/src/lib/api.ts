// API utilities for making HTTP requests to the backend

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

// Generic API client
class ApiClient {
  private baseURL: string

  constructor(baseURL: string) {
    this.baseURL = baseURL
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${this.baseURL}${endpoint}`
    
    // Get auth token from localStorage (using the same key as auth service)
    const token = localStorage.getItem('auth_token')
    
    if (!token) {
      throw new Error('Authentication token not found. Please log in again.')
    }
    
    const config: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
        ...options.headers,
      },
      ...options,
    }

    try {
      const response = await fetch(url, config)
      
      if (response.status === 401) {
        // Token is invalid or expired - but don't delete it automatically
        // Let the auth context handle token validation and logout
        throw new Error('Authentication failed. Please log in again.')
      }
      
      if (response.status === 403) {
        throw new Error('Access denied. You do not have permission to access this resource.')
      }
      
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}))
        throw new Error(errorData.message || `HTTP error! status: ${response.status}`)
      }
      
      return await response.json()
    } catch (error) {
      console.error('API request failed:', error)
      throw error
    }
  }

  async get<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'GET' })
  }

  async post<T>(endpoint: string, data?: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    })
  }

  async put<T>(endpoint: string, data?: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
    })
  }

  async delete<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'DELETE' })
  }
}

// Create API client instance
export const apiClient = new ApiClient(API_BASE_URL)

// Type definitions for API responses
export interface AlertStatistics {
  total: number
  critical: number
  warning: number
  info: number
  resolved: number
  acknowledged: number
}

export interface DeviceConfiguration {
  id: number
  targetIp: string
  snmpPort: number
  snmpVersion: 'V1' | 'V2C' | 'V3'
  communityString: string
  snmpTimeout: number
  snmpRetries: number
  pollInterval: number
  enabled: boolean
  lastPollTime?: string
  lastPollStatus?: 'SUCCESS' | 'FAILED' | 'PENDING'
  errorMessage?: string
  consecutiveFailures: number
  securityName?: string
  authProtocol: 'NONE' | 'MD5' | 'SHA' | 'SHA224' | 'SHA256' | 'SHA384' | 'SHA512'
  authPassphrase?: string
  privProtocol: 'NONE' | 'DES' | 'AES' | 'AES192' | 'AES256'
  privPassphrase?: string
  contextName?: string
  createdAt: string
  updatedAt: string
}

export interface Device {
  id: number
  name: string
  description?: string
  systemObjectId?: string
  systemUptime?: number
  systemContact?: string
  systemName?: string
  systemLocation?: string
  systemServices?: number
  lastMonitored?: string
  monitoringEnabled: boolean
  status: 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE' | 'ERROR'
  type: 'ROUTER' | 'SWITCH' | 'SERVER' | 'WORKSTATION' | 'PRINTER' | 'FIREWALL' | 'ACCESS_POINT' | 'OTHER'
  configuration?: DeviceConfiguration
  deviceConfig?: DeviceConfiguration
  createdAt: string
  updatedAt: string
}

export interface DeviceCreateRequest {
  name: string
  description?: string
  systemObjectId?: string
  systemContact?: string
  systemName?: string
  systemLocation?: string
  monitoringEnabled: boolean
  status: 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE' | 'ERROR'
  type: 'ROUTER' | 'SWITCH' | 'SERVER' | 'WORKSTATION' | 'PRINTER' | 'FIREWALL' | 'ACCESS_POINT' | 'OTHER'
  configuration?: {
    targetIp: string
    snmpPort: number
    snmpVersion: 'V1' | 'V2C' | 'V3'
    communityString: string
    snmpTimeout: number
    snmpRetries: number
    pollInterval: number
    enabled: boolean
    securityName?: string
    authProtocol: 'NONE' | 'MD5' | 'SHA' | 'SHA224' | 'SHA256' | 'SHA384' | 'SHA512'
    authPassphrase?: string
    privProtocol: 'NONE' | 'DES' | 'AES' | 'AES192' | 'AES256'
    privPassphrase?: string
    contextName?: string
  }
}

export type DeviceUpdateRequest = Partial<DeviceCreateRequest>

export interface DeviceInterface {
  id: number
  ifIndex: number
  ifDescr: string
  ifType: string
  ifMtu: number
  ifSpeed: number
  ifPhysAddress: string
  ifAdminStatus: string
  ifOperStatus: string
  ifLastChange: number
  ifInOctets: number
  ifInUcastPkts: number
  ifInNucastPkts: number
  ifInDiscards: number
  ifInErrors: number
  ifInUnknownProtos: number
  ifOutOctets: number
  ifOutUcastPkts: number
  ifOutNucastPkts: number
  ifOutDiscards: number
  ifOutErrors: number
  ifOutQLen: number
  deviceId: number
  deviceName: string
  createdAt: string
  updatedAt: string
}

export interface DeviceListResponse {
  content: Device[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

export interface Alert {
  id: number
  type: string
  severity: 'CRITICAL' | 'WARNING' | 'INFO'
  status: 'NEW' | 'ACKNOWLEDGED' | 'RESOLVED'
  title: string
  description?: string
  sourceId?: number
  sourceType?: string
  sourceDescription?: string
  details?: string
  acknowledgedAt?: string
  resolvedAt?: string
  createdAt: string
  updatedAt: string
}

// API functions
export const api = {
  // Alert statistics
  getAlertStatistics: (): Promise<AlertStatistics> => 
    apiClient.get<AlertStatistics>('/alerts/statistics'),

  // Device list with pagination
  getDevices: (page: number = 0, size: number = 10): Promise<DeviceListResponse> => 
    apiClient.get<DeviceListResponse>(`/devices?page=${page}&size=${size}`),

  // Recent alerts
  getRecentAlerts: (limit: number = 10): Promise<Alert[]> => 
    apiClient.get<Alert[]>(`/alerts/recent?limit=${limit}`),

  // Device Management APIs
  // Get all devices with pagination, sorting, filtering
  getDevicesList: (page: number = 0, size: number = 10, sort?: string, order?: 'asc' | 'desc'): Promise<DeviceListResponse> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    })
    if (sort) params.append('sort', sort)
    if (order) params.append('order', order)
    return apiClient.get<DeviceListResponse>(`/devices?${params.toString()}`)
  },

  // Filter devices by status
  getDevicesByStatus: (status: string, page: number = 0, size: number = 10): Promise<DeviceListResponse> => 
    apiClient.get<DeviceListResponse>(`/devices/by-status/${status}?page=${page}&size=${size}`),

  // Filter devices by type
  getDevicesByType: (type: string, page: number = 0, size: number = 10): Promise<DeviceListResponse> => 
    apiClient.get<DeviceListResponse>(`/devices/by-type/${type}?page=${page}&size=${size}`),

  // Search devices by name
  searchDevices: (query: string, page: number = 0, size: number = 10): Promise<DeviceListResponse> => 
    apiClient.get<DeviceListResponse>(`/devices/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`),

  // Delete device
  deleteDevice: (id: number): Promise<void> => 
    apiClient.delete<void>(`/devices/${id}`),

  // Get device details
  getDevice: (id: number): Promise<Device> => 
    apiClient.get<Device>(`/devices/${id}`),

  // Update device
  updateDevice: (id: number, data: DeviceUpdateRequest): Promise<Device> => 
    apiClient.put<Device>(`/devices/${id}`, data),

  // Create device
  createDevice: (data: DeviceCreateRequest): Promise<Device> => 
    apiClient.post<Device>('/devices', data),

  // Trigger monitoring for device
  triggerMonitoring: (id: number): Promise<void> => 
    apiClient.post<void>(`/devices/${id}/monitor`),

  // Get device interfaces
  getDeviceInterfaces: (deviceId: number): Promise<DeviceInterface[]> => 
    apiClient.get<DeviceInterface[]>(`/device-interfaces/device/${deviceId}`),

  // Get device alerts
  getDeviceAlerts: (deviceId: number): Promise<Alert[]> => 
    apiClient.get<Alert[]>(`/alerts/${deviceId}`),
} 