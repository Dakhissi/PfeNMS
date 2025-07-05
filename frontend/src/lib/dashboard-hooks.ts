import { useQuery } from '@tanstack/react-query'
import { api, type Device } from './api'

// Hook for alert statistics
export const useAlertStatistics = () => {
  return useQuery({
    queryKey: ['alertStatistics'],
    queryFn: api.getAlertStatistics,
    refetchInterval: 30000, // Refetch every 30 seconds
    staleTime: 10000, // Consider data stale after 10 seconds
    retry: (failureCount, error) => {
      // Don't retry on authentication errors, but don't redirect either
      if (error instanceof Error && error.message.includes('Authentication')) {
        return false
      }
      return failureCount < 3
    }
  })
}

// Hook for device list with pagination
export const useDevices = (page: number = 0, size: number = 10) => {
  return useQuery({
    queryKey: ['devices', page, size],
    queryFn: () => api.getDevices(page, size),
    refetchInterval: 60000, // Refetch every minute
    staleTime: 30000, // Consider data stale after 30 seconds
    retry: (failureCount, error) => {
      // Don't retry on authentication errors, but don't redirect either
      if (error instanceof Error && error.message.includes('Authentication')) {
        return false
      }
      return failureCount < 3
    }
  })
}

// Hook for recent alerts
export const useRecentAlerts = (limit: number = 10) => {
  return useQuery({
    queryKey: ['recentAlerts', limit],
    queryFn: () => api.getRecentAlerts(limit),
    refetchInterval: 15000, // Refetch every 15 seconds
    staleTime: 5000, // Consider data stale after 5 seconds
    retry: (failureCount, error) => {
      // Don't retry on authentication errors, but don't redirect either
      if (error instanceof Error && error.message.includes('Authentication')) {
        return false
      }
      return failureCount < 3
    }
  })
}

// Hook for device status summary
export const useDeviceStatusSummary = () => {
  const { data: devicesData } = useDevices(0, 1000) // Get all devices for summary
  
  const summary = {
    total: 0,
    active: 0,
    inactive: 0,
    maintenance: 0,
    error: 0
  }
  
  if (devicesData?.content) {
    summary.total = devicesData.content.length
    devicesData.content.forEach((device: Device) => {
      switch (device.status) {
        case 'ACTIVE':
          summary.active++
          break
        case 'INACTIVE':
          summary.inactive++
          break
        case 'MAINTENANCE':
          summary.maintenance++
          break
        case 'ERROR':
          summary.error++
          break
      }
    })
  }
  
  return summary
} 