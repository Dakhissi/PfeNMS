import { useQuery } from '@tanstack/react-query'
import { apiClient } from './api'

// Type definitions for profile APIs
export interface IcmpProfile {
  id: number
  deviceId: number
  deviceName: string
  icmpInMsgs: number
  icmpInErrors: number
  icmpInDestUnreachs: number
  icmpInTimeExcds: number
  icmpInParmProbs: number
  icmpInSrcQuenchs: number
  icmpInRedirects: number
  icmpInEchos: number
  icmpInEchoReps: number
  icmpInTimestamps: number
  icmpInTimestampReps: number
  icmpInAddrMasks: number
  icmpInAddrMaskReps: number
  icmpOutMsgs: number
  icmpOutErrors: number
  icmpOutDestUnreachs: number
  icmpOutTimeExcds: number
  icmpOutParmProbs: number
  icmpOutSrcQuenchs: number
  icmpOutRedirects: number
  icmpOutEchos: number
  icmpOutEchoReps: number
  icmpOutTimestamps: number
  icmpOutTimestampReps: number
  icmpOutAddrMasks: number
  icmpOutAddrMaskReps: number
  createdAt: string
  updatedAt: string
}

export interface SystemUnit {
  id: number
  deviceId: number
  deviceName: string
  index: number
  descr: string
  objectId: string
  upTime: number
  contact: string
  name: string
  location: string
  services: number
  createdAt: string
  updatedAt: string
}

export interface IpProfile {
  id: number
  deviceId: number
  deviceName: string
  name: string
  description: string
  ipAddress: string
  timeout: number
  interval: number
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export interface UdpProfile {
  id: number
  deviceId: number
  deviceName: string
  udpInDatagrams: number
  udpNoPorts: number
  udpInErrors: number
  udpOutDatagrams: number
  udpLocalAddress: string
  udpLocalPort: number
  udpRemoteAddress: string
  udpRemotePort: number
  udpEntryStatus: string
  createdAt: string
  updatedAt: string
}

// Query keys
export const profileKeys = {
  all: ['profiles'] as const,
  icmp: (deviceId: number) => [...profileKeys.all, 'icmp', deviceId] as const,
  systemUnits: (deviceId: number) => [...profileKeys.all, 'system-units', deviceId] as const,
  ip: (deviceId: number) => [...profileKeys.all, 'ip', deviceId] as const,
  udp: (deviceId: number) => [...profileKeys.all, 'udp', deviceId] as const,
}

// ICMP Profile hooks
export function useIcmpProfiles(deviceId: number) {
  return useQuery({
    queryKey: profileKeys.icmp(deviceId),
    queryFn: () => apiClient.get<IcmpProfile[]>(`/icmp-profiles/device/${deviceId}`),
    enabled: !!deviceId,
  })
}

// System Units hooks
export function useSystemUnits(deviceId: number) {
  return useQuery({
    queryKey: profileKeys.systemUnits(deviceId),
    queryFn: () => apiClient.get<SystemUnit[]>(`/system-units/device/${deviceId}`),
    enabled: !!deviceId,
  })
}

// IP Profile hooks
export function useIpProfiles(deviceId: number) {
  return useQuery({
    queryKey: profileKeys.ip(deviceId),
    queryFn: () => apiClient.get<IpProfile[]>(`/ip-profiles/device/${deviceId}`),
    enabled: !!deviceId,
  })
}

// UDP Profile hooks
export function useUdpProfiles(deviceId: number) {
  return useQuery({
    queryKey: profileKeys.udp(deviceId),
    queryFn: () => apiClient.get<UdpProfile[]>(`/udp-profiles/device/${deviceId}`),
    enabled: !!deviceId,
  })
} 