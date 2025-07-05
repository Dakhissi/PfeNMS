import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { api, type DeviceCreateRequest, type DeviceUpdateRequest } from './api'

// Hook for device list with pagination, sorting, and filtering
export const useDevicesList = (
  page: number = 0, 
  size: number = 10, 
  sort?: string, 
  order?: 'asc' | 'desc'
) => {
  return useQuery({
    queryKey: ['devices', 'list', page, size, sort, order],
    queryFn: () => api.getDevicesList(page, size, sort, order),
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

// Hook for devices filtered by status
export const useDevicesByStatus = (status: string, page: number = 0, size: number = 10) => {
  return useQuery({
    queryKey: ['devices', 'by-status', status, page, size],
    queryFn: () => api.getDevicesByStatus(status, page, size),
    enabled: !!status,
    refetchInterval: 60000,
    staleTime: 30000,
    retry: (failureCount, error) => {
      if (error instanceof Error && error.message.includes('Authentication')) {
        return false
      }
      return failureCount < 3
    }
  })
}

// Hook for devices filtered by type
export const useDevicesByType = (type: string, page: number = 0, size: number = 10) => {
  return useQuery({
    queryKey: ['devices', 'by-type', type, page, size],
    queryFn: () => api.getDevicesByType(type, page, size),
    enabled: !!type,
    refetchInterval: 60000,
    staleTime: 30000,
    retry: (failureCount, error) => {
      if (error instanceof Error && error.message.includes('Authentication')) {
        return false
      }
      return failureCount < 3
    }
  })
}

// Hook for searching devices
export const useSearchDevices = (query: string, page: number = 0, size: number = 10) => {
  return useQuery({
    queryKey: ['devices', 'search', query, page, size],
    queryFn: () => api.searchDevices(query, page, size),
    enabled: !!query && query.length >= 2,
    refetchInterval: 60000,
    staleTime: 30000,
    retry: (failureCount, error) => {
      if (error instanceof Error && error.message.includes('Authentication')) {
        return false
      }
      return failureCount < 3
    }
  })
}

// Hook for getting device details
export const useDevice = (id: number) => {
  return useQuery({
    queryKey: ['device', id],
    queryFn: () => api.getDevice(id),
    enabled: !!id,
    refetchInterval: 30000, // Refetch every 30 seconds for device details
    staleTime: 15000,
    retry: (failureCount, error) => {
      if (error instanceof Error && error.message.includes('Authentication')) {
        return false
      }
      return failureCount < 3
    }
  })
}

// Hook for getting device interfaces
export const useDeviceInterfaces = (deviceId: number) => {
  return useQuery({
    queryKey: ['device-interfaces', deviceId],
    queryFn: () => api.getDeviceInterfaces(deviceId),
    enabled: !!deviceId,
    refetchInterval: 30000,
    staleTime: 15000,
    retry: (failureCount, error) => {
      if (error instanceof Error && error.message.includes('Authentication')) {
        return false
      }
      return failureCount < 3
    }
  })
}

// Hook for getting device alerts
export const useDeviceAlerts = (deviceId: number) => {
  return useQuery({
    queryKey: ['device-alerts', deviceId],
    queryFn: () => api.getDeviceAlerts(deviceId),
    enabled: !!deviceId,
    refetchInterval: 15000, // Refetch every 15 seconds for alerts
    staleTime: 5000,
    retry: (failureCount, error) => {
      if (error instanceof Error && error.message.includes('Authentication')) {
        return false
      }
      return failureCount < 3
    }
  })
}

// Mutation for creating a device
export const useCreateDevice = () => {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  
  return useMutation({
    mutationFn: (data: DeviceCreateRequest) => api.createDevice(data),
    onSuccess: () => {
      // Invalidate and refetch devices list
      queryClient.invalidateQueries({ queryKey: ['devices'] })
      // Redirect to dashboard
      navigate('/dashboard')
    },
    onError: (error) => {
      console.error('Failed to create device:', error)
    }
  })
}

// Mutation for updating a device
export const useUpdateDevice = () => {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: DeviceUpdateRequest }) => 
      api.updateDevice(id, data),
    onSuccess: (updatedDevice) => {
      // Update the device in cache
      queryClient.setQueryData(['device', updatedDevice.id], updatedDevice)
      // Invalidate devices list
      queryClient.invalidateQueries({ queryKey: ['devices'] })
      // Redirect to dashboard
      navigate('/dashboard')
    },
    onError: (error) => {
      console.error('Failed to update device:', error)
    }
  })
}

// Mutation for deleting a device
export const useDeleteDevice = () => {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  
  return useMutation({
    mutationFn: (id: number) => api.deleteDevice(id),
    onSuccess: () => {
      // Invalidate devices list
      queryClient.invalidateQueries({ queryKey: ['devices'] })
      // Redirect to dashboard
      navigate('/dashboard')
    },
    onError: (error) => {
      console.error('Failed to delete device:', error)
    }
  })
}

// Mutation for triggering device monitoring
export const useTriggerMonitoring = () => {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: (id: number) => api.triggerMonitoring(id),
    onSuccess: (_, deviceId) => {
      // Invalidate device data and related queries
      queryClient.invalidateQueries({ queryKey: ['device', deviceId] })
      queryClient.invalidateQueries({ queryKey: ['device-alerts', deviceId] })
      queryClient.invalidateQueries({ queryKey: ['device-interfaces', deviceId] })
    },
    onError: (error) => {
      console.error('Failed to trigger monitoring:', error)
    }
  })
} 