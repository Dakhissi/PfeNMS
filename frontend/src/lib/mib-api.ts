import { apiClient } from './api'

// MIB API types - Updated to match actual backend response
export interface MibTreeNode {
  id: number
  name: string
  oid: string
  description?: string
  type: string
  access: string
  status: string
  syntaxType: string
  maxAccess?: string | null
  units?: string | null
  reference?: string | null
  indexObjects?: string | null
  augments?: string | null
  parentId?: number | null
  parentName?: string | null
  children: MibTreeNode[]
  mibFileId?: number
  mibFileName?: string
  createdAt: string
  updatedAt: string
}

export interface MibFile {
  id: number
  name: string
  filename: string
  filePath: string
  fileSize: number
  fileHash: string
  description?: string | null
  moduleName?: string | null
  moduleOid?: string | null
  version?: string | null
  organization?: string | null
  contactInfo?: string | null
  status: 'LOADED' | 'PARSING' | 'ERROR'
  loadErrorMessage?: string | null
  objectCount: number
  userId: number
  userName: string
  createdAt: string
  updatedAt: string
}

export interface MibFileDetails extends MibFile {
  content?: string
  parsedNodes?: number
  totalNodes?: number
}

// Updated to match backend response - backend returns List<MibObjectDto> directly
export type MibTreeResponse = MibTreeNode[]

export interface MibFilesResponse {
  files: MibFile[]
}

// MIB Browser types
export interface MibBrowserRequest {
  targetIp: string
  community: string
  oid: string
  snmpPort?: number
  timeout?: number
  retries?: number
}

export interface MibBrowserResponse {
  oid: string
  name?: string
  value: string
  type: string
  syntax?: string
  description?: string
  success: boolean
  errorMessage?: string
  responseTime: number
}

// MIB API functions
export const mibApi = {
  // Get MIB object tree - backend returns List<MibObjectDto> directly
  async getTree(): Promise<MibTreeResponse> {
    console.log('Fetching MIB tree...')
    try {
      const response = await apiClient.get<MibTreeResponse>('/mib/tree')
      console.log('MIB tree response:', response)
      return response
    } catch (error) {
      console.error('Error fetching MIB tree:', error)
      throw error
    }
  },

  // Upload MIB file
  async uploadFile(file: File): Promise<MibFile> {
    console.log('Uploading MIB file:', file.name)
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
      console.error('Upload failed:', errorData)
      throw new Error(errorData.message || `Upload failed: ${response.status}`)
    }

    const result = await response.json()
    console.log('Upload successful:', result)
    return result
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

  // Browse SNMP OID
  async browseOid(request: MibBrowserRequest): Promise<MibBrowserResponse> {
    return apiClient.post<MibBrowserResponse>('/mib/browse', request)
  },

  // Walk SNMP OID tree
  async walkOidTree(request: MibBrowserRequest): Promise<MibBrowserResponse[]> {
    return apiClient.post<MibBrowserResponse[]>('/mib/walk', request)
  },

  // Search MIB objects
  async searchObjects(query: string): Promise<MibTreeNode[]> {
    return apiClient.get<MibTreeNode[]>(`/mib/objects/search?query=${encodeURIComponent(query)}`)
  },

  // Get MIB object by OID
  async getObjectByOid(oid: string): Promise<MibTreeNode> {
    return apiClient.get<MibTreeNode>(`/mib/objects/oid/${encodeURIComponent(oid)}`)
  },
} 