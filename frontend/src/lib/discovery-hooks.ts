import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { discoveryApi } from './discovery-api'
import type { DiscoveryStartRequest, PingTestRequest } from './discovery-api'

// Query keys
export const discoveryKeys = {
  all: ['discovery'] as const,
  status: (discoveryId: string) => [...discoveryKeys.all, 'status', discoveryId] as const,
  history: () => [...discoveryKeys.all, 'history'] as const,
}

// Hook to get discovery status
export function useDiscoveryStatus(discoveryId: string) {
  return useQuery({
    queryKey: discoveryKeys.status(discoveryId),
    queryFn: () => discoveryApi.getDiscoveryStatus(discoveryId),
    enabled: !!discoveryId,
    refetchInterval: (query) => {
      // Poll every 2 seconds if discovery is running
      return query.state.data?.status === 'RUNNING' ? 2000 : false
    },
    refetchIntervalInBackground: true,
  })
}

// Hook to get discovery history
export function useDiscoveryHistory() {
  return useQuery({
    queryKey: discoveryKeys.history(),
    queryFn: () => discoveryApi.getDiscoveryHistory(),
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}

// Hook to start discovery
export function useStartDiscovery() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: (request: DiscoveryStartRequest) => discoveryApi.startDiscovery(request),
    onSuccess: () => {
      // Invalidate discovery history
      queryClient.invalidateQueries({ queryKey: discoveryKeys.history() })
    },
  })
}

// Hook to test ping
export function useTestPing() {
  return useMutation({
    mutationFn: (request: PingTestRequest) => discoveryApi.testPing(request),
  })
}

// Hook to cancel discovery
export function useCancelDiscovery() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: (discoveryId: string) => discoveryApi.cancelDiscovery(discoveryId),
    onSuccess: (_, discoveryId) => {
      // Invalidate the specific discovery status
      queryClient.invalidateQueries({ queryKey: discoveryKeys.status(discoveryId) })
      // Invalidate discovery history
      queryClient.invalidateQueries({ queryKey: discoveryKeys.history() })
    },
  })
} 