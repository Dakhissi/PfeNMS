import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { mibApi } from './mib-api'

// Query keys
export const mibKeys = {
  all: ['mib'] as const,
  tree: () => [...mibKeys.all, 'tree'] as const,
  files: () => [...mibKeys.all, 'files'] as const,
  file: (id: string) => [...mibKeys.all, 'file', id] as const,
}

// Hooks for MIB tree
export function useMibTree() {
  return useQuery({
    queryKey: mibKeys.tree(),
    queryFn: mibApi.getTree,
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}

// Hooks for MIB files
export function useMibFiles() {
  return useQuery({
    queryKey: mibKeys.files(),
    queryFn: mibApi.getFiles,
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}

export function useMibFileDetails(id: string) {
  return useQuery({
    queryKey: mibKeys.file(id),
    queryFn: () => mibApi.getFileDetails(id),
    enabled: !!id,
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}

// Mutations
export function useUploadMibFile() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: mibApi.uploadFile,
    onSuccess: () => {
      // Invalidate and refetch files list
      queryClient.invalidateQueries({ queryKey: mibKeys.files() })
      // Also invalidate tree as new MIB might add new objects
      queryClient.invalidateQueries({ queryKey: mibKeys.tree() })
    },
  })
}

export function useDeleteMibFile() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: mibApi.deleteFile,
    onSuccess: () => {
      // Invalidate and refetch files list
      queryClient.invalidateQueries({ queryKey: mibKeys.files() })
      // Also invalidate tree as deleted MIB might affect tree
      queryClient.invalidateQueries({ queryKey: mibKeys.tree() })
    },
  })
} 