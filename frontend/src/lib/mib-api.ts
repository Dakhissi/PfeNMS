import { apiClient } from './api'

// MIB API types
export interface MibTreeNode {
  id: string
  name: string
  oid: string
  type?: string
  description?: string
  children?: MibTreeNode[]
}

export interface MibFile {
  id: string
  name: string
  size: number
  uploadDate: string
  description?: string
  status: 'PARSED' | 'PARSING' | 'ERROR'
  errorMessage?: string
}

export interface MibFileDetails extends MibFile {
  content?: string
  parsedNodes?: number
  totalNodes?: number
}

export interface MibTreeResponse {
  tree: MibTreeNode[]
}

export interface MibFilesResponse {
  files: MibFile[]
}

// MIB API functions
export const mibApi = {
  // Get MIB object tree
  async getTree(): Promise<MibTreeResponse> {
    return apiClient.get<MibTreeResponse>('/mib/tree')
  },

  // Upload MIB file
  async uploadFile(file: File): Promise<MibFile> {
    const formData = new FormData()
    formData.append('file', file)
    
    const token = localStorage.getItem('auth_token')
    if (!token) {
      throw new Error('Authentication token not found')
    }

    const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/mib/files/upload`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
      body: formData,
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      throw new Error(errorData.message || `Upload failed: ${response.status}`)
    }

    return response.json()
  },

  // Get list of MIB files
  async getFiles(): Promise<MibFilesResponse> {
    return apiClient.get<MibFilesResponse>('/mib/files')
  },

  // Get MIB file details
  async getFileDetails(id: string): Promise<MibFileDetails> {
    return apiClient.get<MibFileDetails>(`/mib/files/${id}`)
  },

  // Delete MIB file
  async deleteFile(id: string): Promise<void> {
    return apiClient.delete<void>(`/mib/files/${id}`)
  },
} 