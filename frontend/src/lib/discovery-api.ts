import { apiClient } from './api'

// Type definitions for discovery API
export interface DiscoveryStatus {
  discoveryId: string
  nodes: Array<{
    id: string
    name: string
    ipAddress: string
    macAddress: string
    deviceType: string
    systemDescription: string
    vendor: string
    model: string
    osVersion: string
    reachable: boolean
    attributes: Record<string, unknown> | null
    services: Record<string, unknown> | null
    interfaces: Array<{
      id: string
      name: string
      description: string
      ipAddress: string
      subnetMask: string | null
      macAddress: string
      status: string
      inOctets: number
      outOctets: number
      ifIndex: number
      ifSpeed: number
      ifType: string
    }>
  }>
  connections: Array<{
    id: string
    sourceNodeId: string
    targetNodeId: string
    sourceInterface: string | null
    targetInterface: string | null
    connectionType: string
    metric: number
    protocol: string | null
    redundant: boolean
  }>
  warnings: string[] | null
  status: 'RUNNING' | 'COMPLETE' | 'FAILED' | 'CANCELLED'
  totalDevicesDiscovered: number
  discoveryDurationMs: number
}

export interface DiscoveryStartRequest {
  networkRange: string
  scanType: 'PING' | 'SNMP' | 'FULL'
  timeout?: number
  retries?: number
}

export interface DiscoveryStartResponse {
  discoveryId: string
  message: string
}

export interface PingTestRequest {
  targetIp: string
  timeout?: number
  retries?: number
}

export interface PingTestResponse {
  success: boolean
  responseTime?: number
  message: string
}

export interface DiscoveryDevice {
  id: string
  ipAddress: string
  hostname?: string
  macAddress?: string
  deviceType?: string
  status: 'ONLINE' | 'OFFLINE' | 'UNKNOWN'
  responseTime?: number
  discoveredAt: string
}

// Discovery API service
export const discoveryApi = {
  // Get discovery status
  getDiscoveryStatus: async (discoveryId: string): Promise<DiscoveryStatus> => {
    return apiClient.get<DiscoveryStatus>(`/discovery/status/${discoveryId}`)
  },

  // Start network discovery
  startDiscovery: async (request: DiscoveryStartRequest): Promise<DiscoveryStartResponse> => {
    return apiClient.post<DiscoveryStartResponse>('/discovery/start', request)
  },

  // Test connectivity with ping
  testPing: async (request: PingTestRequest): Promise<PingTestResponse> => {
    return apiClient.post<PingTestResponse>('/discovery/ping', request)
  },

  // Cancel discovery
  cancelDiscovery: async (discoveryId: string): Promise<{ message: string }> => {
    return apiClient.post<{ message: string }>(`/discovery/cancel/${discoveryId}`)
  },

  // Get discovery history (optional - for dashboard)
  getDiscoveryHistory: async (): Promise<DiscoveryStatus[]> => {
    return apiClient.get<DiscoveryStatus[]>('/discovery/history')
  }
} 