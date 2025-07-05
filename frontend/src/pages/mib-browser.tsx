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
  Eye
} from "lucide-react"
import { Link } from "react-router-dom"
import type { MibTreeNode } from "@/lib/mib-api"

interface TreeNodeProps {
  node: MibTreeNode
  level?: number
}

function TreeNode({ node, level = 0 }: TreeNodeProps) {
  const [isExpanded, setIsExpanded] = useState(false)
  const hasChildren = node.children && node.children.length > 0

  return (
    <div>
      <div 
        className={`flex items-center gap-2 py-1 px-2 hover:bg-muted/50 rounded cursor-pointer ${
          level > 0 ? 'ml-' + (level * 4) : ''
        }`}
        onClick={() => hasChildren && setIsExpanded(!isExpanded)}
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
        
        <div className="flex items-center gap-2 flex-1">
          {hasChildren ? (
            <Folder className="h-4 w-4 text-blue-500" />
          ) : (
            <FileText className="h-4 w-4 text-gray-500" />
          )}
          <span className="font-medium">{node.name}</span>
          {node.type && (
            <Badge variant="secondary" className="text-xs">
              {node.type}
            </Badge>
          )}
        </div>
        
        <div className="text-xs text-muted-foreground">
          {node.oid}
        </div>
      </div>
      
      {node.description && isExpanded && (
        <div className={`ml-8 mb-2 text-sm text-muted-foreground ${
          level > 0 ? 'ml-' + ((level + 2) * 4) : ''
        }`}>
          {node.description}
        </div>
      )}
      
      {isExpanded && hasChildren && (
        <div>
          {node.children?.map((child) => (
            <TreeNode key={child.id} node={child} level={level + 1} />
          ))}
        </div>
      )}
    </div>
  )
}

export function MibBrowserPage() {
  const [selectedFile, setSelectedFile] = useState<File | null>(null)

  // Data hooks
  const { data: treeData, isLoading: treeLoading, error: treeError } = useMibTree()
  const { data: filesData, isLoading: filesLoading } = useMibFiles()
  const uploadMutation = useUploadMibFile()
  const deleteMutation = useDeleteMibFile()

  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    if (file) {
      setSelectedFile(file)
    }
  }

  const handleUpload = async () => {
    if (!selectedFile) return

    try {
      await uploadMutation.mutateAsync(selectedFile)
      setSelectedFile(null)
      // Reset file input
      const fileInput = document.getElementById('mib-file-input') as HTMLInputElement
      if (fileInput) fileInput.value = ''
    } catch (error) {
      console.error('Upload failed:', error)
    }
  }

  const handleDeleteFile = async (fileId: string) => {
    if (confirm('Are you sure you want to delete this MIB file?')) {
      try {
        await deleteMutation.mutateAsync(fileId)
      } catch (error) {
        console.error('Delete failed:', error)
      }
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'PARSED': return <CheckCircle className="h-4 w-4 text-green-500" />
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

        <div className="grid gap-6 lg:grid-cols-2">
          {/* MIB Tree */}
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
              ) : treeData?.tree && treeData.tree.length > 0 ? (
                <div className="max-h-96 overflow-y-auto">
                  {treeData.tree.map((node) => (
                    <TreeNode key={node.id} node={node} />
                  ))}
                </div>
              ) : (
                <div className="text-center py-8 text-muted-foreground">
                  No MIB objects found. Upload a MIB file to get started.
                </div>
              )}
            </CardContent>
          </Card>

          {/* MIB Files Management */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <FileText className="h-5 w-5" />
                MIB Files
                {filesLoading && <RefreshCw className="h-4 w-4 animate-spin" />}
              </CardTitle>
            </CardHeader>
            <CardContent>
              {/* Upload Section */}
              <div className="mb-6 p-4 border rounded-lg">
                <h3 className="font-medium mb-3">Upload MIB File</h3>
                <div className="flex items-center gap-4">
                  <div className="flex-1">
                    <input
                      id="mib-file-input"
                      type="file"
                      accept=".mib,.txt"
                      onChange={handleFileSelect}
                      className="block w-full text-sm text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-primary file:text-primary-foreground hover:file:bg-primary/90"
                    />
                  </div>
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
                {uploadMutation.isError && (
                  <p className="text-sm text-red-500 mt-2">
                    {uploadMutation.error?.message || 'Upload failed'}
                  </p>
                )}
              </div>

              {/* Files List */}
              {filesData?.files && filesData.files.length > 0 ? (
                <div className="space-y-2">
                  {filesData.files.map((file) => (
                    <div
                      key={file.id}
                      className="flex items-center justify-between p-3 border rounded-lg hover:bg-muted/50"
                    >
                      <div className="flex items-center gap-3">
                        {getStatusIcon(file.status)}
                        <div>
                          <div className="font-medium">{file.name}</div>
                          <div className="text-sm text-muted-foreground">
                            {formatFileSize(file.size)} • {new Date(file.uploadDate).toLocaleDateString()}
                          </div>
                        </div>
                      </div>
                      
                      <div className="flex items-center gap-2">
                        <Link
                          to={`/mib/files/${file.id}`}
                          className="p-2 hover:bg-muted rounded"
                        >
                          <Eye className="h-4 w-4" />
                        </Link>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleDeleteFile(file.id)}
                          disabled={deleteMutation.isPending}
                          className="text-red-500 hover:text-red-700"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-8 text-muted-foreground">
                  No MIB files uploaded yet.
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
} 