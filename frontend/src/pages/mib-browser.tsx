import { useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { 
  useMibTree, 
  useMibFiles, 
  useUploadMibFile, 
  useDeleteMibFile 
} from "@/lib/mib-hooks"
import { 
  ChevronDown, 
  ChevronRight, 
  Upload, 
  Trash2, 
  FileText, 
  Folder, 
  AlertTriangle,
  CheckCircle,
  Clock,
  RefreshCw,
  Eye,
  Search,
  Info,
  Network
} from "lucide-react"
import { Link } from "react-router-dom"
import type { MibTreeNode } from "@/lib/mib-api"
import { MibSearch } from "@/components/mib-search"
import { MibBrowser } from "@/components/mib-browser"

interface TreeNodeProps {
  node: MibTreeNode
  level?: number
  onNodeSelect?: (node: MibTreeNode) => void
  selectedNode?: MibTreeNode | null
}

function TreeNode({ node, level = 0, onNodeSelect, selectedNode }: TreeNodeProps) {
  const [isExpanded, setIsExpanded] = useState(false)
  const hasChildren = node.children && node.children.length > 0
  const isSelected = selectedNode?.id === node.id

  const handleNodeClick = () => {
    if (hasChildren) {
      setIsExpanded(!isExpanded)
    }
    onNodeSelect?.(node)
  }

  const getAccessColor = (access: string) => {
    switch (access) {
      case 'READ_ONLY': return 'bg-blue-100 text-blue-800'
      case 'READ_WRITE': return 'bg-green-100 text-green-800'
      case 'WRITE_ONLY': return 'bg-orange-100 text-orange-800'
      case 'NOT_ACCESSIBLE': return 'bg-gray-100 text-gray-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'CURRENT': return 'bg-green-100 text-green-800'
      case 'DEPRECATED': return 'bg-yellow-100 text-yellow-800'
      case 'OBSOLETE': return 'bg-red-100 text-red-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  return (
    <div>
      <div 
        className={`flex items-center gap-2 py-2 px-3 hover:bg-muted/50 rounded cursor-pointer transition-colors ${
          level > 0 ? 'ml-' + (level * 4) : ''
        } ${isSelected ? 'bg-blue-50 border border-blue-200' : ''}`}
        onClick={handleNodeClick}
      >
        {hasChildren ? (
          <button className="p-1">
            {isExpanded ? (
              <ChevronDown className="h-4 w-4" />
            ) : (
              <ChevronRight className="h-4 w-4" />
            )}
          </button>
        ) : (
          <div className="w-6" />
        )}
        
        <div className="flex items-center gap-2 flex-1 min-w-0">
          {hasChildren ? (
            <Folder className="h-4 w-4 text-blue-500 flex-shrink-0" />
          ) : (
            <FileText className="h-4 w-4 text-gray-500 flex-shrink-0" />
          )}
          <span className="font-medium truncate">{node.name}</span>
          
          <div className="flex gap-1 flex-shrink-0">
            {node.access && (
              <Badge variant="outline" className={`text-xs ${getAccessColor(node.access)}`}>
                {node.access.replace('_', ' ')}
              </Badge>
            )}
            {node.status && (
              <Badge variant="outline" className={`text-xs ${getStatusColor(node.status)}`}>
                {node.status}
              </Badge>
            )}
          </div>
        </div>
        
        <div className="text-xs text-muted-foreground flex-shrink-0">
          {node.oid}
        </div>
      </div>
      
      {node.description && isExpanded && (
        <div className={`ml-8 mb-2 text-sm text-muted-foreground p-2 bg-muted/30 rounded ${
          level > 0 ? 'ml-' + ((level + 2) * 4) : ''
        }`}>
          {node.description}
        </div>
      )}
      
      {isExpanded && hasChildren && (
        <div>
          {node.children?.map((child) => (
            <TreeNode 
              key={child.id} 
              node={child} 
              level={level + 1} 
              onNodeSelect={onNodeSelect}
              selectedNode={selectedNode}
            />
          ))}
        </div>
      )}
    </div>
  )
}

export function MibBrowserPage() {
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [selectedNode, setSelectedNode] = useState<MibTreeNode | null>(null)
  const [viewMode, setViewMode] = useState<'tree' | 'search' | 'browser'>('tree')

  // Data hooks
  const { data: treeData, isLoading: treeLoading, error: treeError } = useMibTree()
  const { data: filesData, isLoading: filesLoading } = useMibFiles()
  const uploadMutation = useUploadMibFile()
  const deleteMutation = useDeleteMibFile()

  // Debug logging
  console.log('MIB Browser - treeData:', treeData)
  console.log('MIB Browser - treeLoading:', treeLoading)
  console.log('MIB Browser - treeError:', treeError)
  console.log('MIB Browser - filesData:', filesData)

  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    if (file) {
      setSelectedFile(file)
    }
  }

  const handleUpload = async () => {
    if (!selectedFile) return

    console.log('Starting upload for file:', selectedFile.name)
    try {
      const result = await uploadMutation.mutateAsync(selectedFile)
      console.log('Upload completed successfully:', result)
      setSelectedFile(null)
      // Reset file input
      const fileInput = document.getElementById('mib-file-input') as HTMLInputElement
      if (fileInput) fileInput.value = ''
    } catch (error) {
      console.error('Upload failed:', error)
    }
  }

  const handleDeleteFile = async (fileId: number) => {
    if (confirm('Are you sure you want to delete this MIB file?')) {
      try {
        await deleteMutation.mutateAsync(fileId.toString())
      } catch (error) {
        console.error('Delete failed:', error)
      }
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'LOADED': return <CheckCircle className="h-4 w-4 text-green-500" />
      case 'PARSING': return <Clock className="h-4 w-4 text-yellow-500" />
      case 'ERROR': return <AlertTriangle className="h-4 w-4 text-red-500" />
      default: return <Clock className="h-4 w-4 text-gray-500" />
    }
  }

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString()
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold tracking-tight">MIB Browser</h1>
          <p className="text-muted-foreground mt-2">
            Manage and browse Management Information Base (MIB) files and objects
          </p>
        </div>

        {/* View Mode Toggle */}
        <div className="mb-6">
          <div className="flex items-center gap-2">
            <Button
              variant={viewMode === 'tree' ? 'default' : 'outline'}
              size="sm"
              onClick={() => setViewMode('tree')}
              className="flex items-center gap-2"
            >
              <Folder className="h-4 w-4" />
              Tree View
            </Button>
            <Button
              variant={viewMode === 'search' ? 'default' : 'outline'}
              size="sm"
              onClick={() => setViewMode('search')}
              className="flex items-center gap-2"
            >
              <Search className="h-4 w-4" />
              Search View
            </Button>
            <Button
              variant={viewMode === 'browser' ? 'default' : 'outline'}
              size="sm"
              onClick={() => setViewMode('browser')}
              className="flex items-center gap-2"
            >
              <Network className="h-4 w-4" />
              SNMP Browser
            </Button>
          </div>
        </div>

        <div className="grid gap-6 lg:grid-cols-3">
          {/* MIB Tree, Search, or Browser */}
          <div className="lg:col-span-2">
            {viewMode === 'tree' ? (
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Folder className="h-5 w-5" />
                    MIB Object Tree
                    {treeLoading && <RefreshCw className="h-4 w-4 animate-spin" />}
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  {treeError ? (
                    <div className="flex items-center justify-center py-8 text-muted-foreground">
                      <AlertTriangle className="h-6 w-6 mr-2" />
                      Failed to load MIB tree
                    </div>
                  ) : treeLoading ? (
                    <div className="flex items-center justify-center py-8">
                      <RefreshCw className="h-6 w-6 animate-spin" />
                    </div>
                  ) : treeData && treeData.length > 0 ? (
                    <div className="max-h-96 overflow-y-auto">
                      {treeData.map((node) => (
                        <TreeNode 
                          key={node.id} 
                          node={node} 
                          onNodeSelect={setSelectedNode}
                          selectedNode={selectedNode}
                        />
                      ))}
                    </div>
                  ) : (
                    <div className="text-center py-8 text-muted-foreground">
                      No MIB objects found. Upload a MIB file to get started.
                    </div>
                  )}
                </CardContent>
              </Card>
            ) : viewMode === 'search' ? (
              <MibSearch
                nodes={treeData || []}
                onNodeSelect={setSelectedNode}
                selectedNode={selectedNode}
              />
            ) : (
              <MibBrowser />
            )}
          </div>

          {/* Selected Node Details - only show for tree and search modes */}
          {viewMode !== 'browser' && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Info className="h-5 w-5" />
                  Object Details
                </CardTitle>
              </CardHeader>
              <CardContent>
                {selectedNode ? (
                  <div className="space-y-4">
                    <div>
                      <h3 className="font-semibold text-lg">{selectedNode.name}</h3>
                      <p className="text-sm text-muted-foreground font-mono">{selectedNode.oid}</p>
                    </div>
                    
                    {selectedNode.description && (
                      <div>
                        <h4 className="font-medium text-sm mb-1">Description</h4>
                        <p className="text-sm text-muted-foreground">{selectedNode.description}</p>
                      </div>
                    )}
                    
                    <div className="grid grid-cols-2 gap-2 text-sm">
                      <div>
                        <span className="font-medium">Type:</span>
                        <p className="text-muted-foreground">{selectedNode.type}</p>
                      </div>
                      <div>
                        <span className="font-medium">Access:</span>
                        <p className="text-muted-foreground">{selectedNode.access}</p>
                      </div>
                      <div>
                        <span className="font-medium">Status:</span>
                        <p className="text-muted-foreground">{selectedNode.status}</p>
                      </div>
                      <div>
                        <span className="font-medium">Syntax:</span>
                        <p className="text-muted-foreground">{selectedNode.syntaxType}</p>
                      </div>
                    </div>
                    
                    {selectedNode.mibFileName && (
                      <div>
                        <h4 className="font-medium text-sm mb-1">Source File</h4>
                        <p className="text-sm text-muted-foreground">{selectedNode.mibFileName}</p>
                      </div>
                    )}
                    
                    {selectedNode.children && selectedNode.children.length > 0 && (
                      <div>
                        <h4 className="font-medium text-sm mb-1">Children</h4>
                        <p className="text-sm text-muted-foreground">{selectedNode.children.length} child objects</p>
                      </div>
                    )}
                  </div>
                ) : (
                  <div className="text-center py-8 text-muted-foreground">
                    Select a MIB object to view details
                  </div>
                )}
              </CardContent>
            </Card>
          )}
        </div>

        {/* MIB Files Management */}
        <Card className="mt-6">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileText className="h-5 w-5" />
              MIB Files Management
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {/* Upload Section */}
              <div className="flex items-center gap-4 p-4 border rounded-lg">
                <input
                  id="mib-file-input"
                  type="file"
                  accept=".mib,.txt"
                  onChange={handleFileSelect}
                  className="flex-1"
                />
                <Button 
                  onClick={handleUpload}
                  disabled={!selectedFile || uploadMutation.isPending}
                  className="flex items-center gap-2"
                >
                  {uploadMutation.isPending ? (
                    <RefreshCw className="h-4 w-4 animate-spin" />
                  ) : (
                    <Upload className="h-4 w-4" />
                  )}
                  Upload
                </Button>
              </div>

              {/* Files List */}
              {filesLoading ? (
                <div className="flex items-center justify-center py-8">
                  <RefreshCw className="h-6 w-6 animate-spin" />
                </div>
              ) : filesData?.files && filesData.files.length > 0 ? (
                <div className="space-y-2">
                  {filesData.files.map((file) => (
                    <div key={file.id} className="flex items-center justify-between p-3 border rounded-lg">
                      <div className="flex items-center gap-3">
                        {getStatusIcon(file.status)}
                        <div>
                          <h4 className="font-medium">{file.name}</h4>
                          <p className="text-sm text-muted-foreground">
                            {formatFileSize(file.fileSize)} • {file.objectCount} objects • {formatDate(file.createdAt)}
                          </p>
                        </div>
                      </div>
                      
                      <div className="flex items-center gap-2">
                        <Link to={`/mib/files/${file.id}`}>
                          <Button variant="outline" size="sm" className="flex items-center gap-2">
                            <Eye className="h-4 w-4" />
                            Details
                          </Button>
                        </Link>
                        <Button 
                          variant="outline" 
                          size="sm" 
                          onClick={() => handleDeleteFile(file.id)}
                          disabled={deleteMutation.isPending}
                          className="flex items-center gap-2 text-red-600 hover:text-red-700"
                        >
                          <Trash2 className="h-4 w-4" />
                          Delete
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-8 text-muted-foreground">
                  No MIB files uploaded yet. Upload your first MIB file to get started.
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
} 