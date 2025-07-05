import { useParams, Link } from "react-router-dom"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { useMibFileDetails } from "@/lib/mib-hooks"
import { 
  ArrowLeft, 
  FileText, 
  AlertTriangle,
  CheckCircle,
  Clock,
  RefreshCw,
  Trash2,
  Download,
  Calendar,
  HardDrive,
  Info
} from "lucide-react"

export function MibFileDetailsPage() {
  const { id } = useParams<{ id: string }>()
  const { data: fileDetails, isLoading, error } = useMibFileDetails(id || '')

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'PARSED': return <CheckCircle className="h-4 w-4 text-green-500" />
      case 'PARSING': return <Clock className="h-4 w-4 text-yellow-500" />
      case 'ERROR': return <AlertTriangle className="h-4 w-4 text-red-500" />
      default: return <Clock className="h-4 w-4 text-gray-500" />
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PARSED': return 'success'
      case 'PARSING': return 'warning'
      case 'ERROR': return 'destructive'
      default: return 'secondary'
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

  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="flex items-center justify-center py-8">
          <RefreshCw className="h-6 w-6 animate-spin" />
        </div>
      </div>
    )
  }

  if (error || !fileDetails) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto">
          <div className="flex items-center gap-4 mb-6">
            <Link to="/mib">
              <Button variant="ghost" className="flex items-center gap-2">
                <ArrowLeft className="h-4 w-4" />
                Back to MIB Browser
              </Button>
            </Link>
          </div>
          
          <Card>
            <CardContent className="flex items-center justify-center py-8">
              <div className="text-center">
                <AlertTriangle className="h-12 w-12 text-red-500 mx-auto mb-4" />
                <h2 className="text-xl font-semibold mb-2">File Not Found</h2>
                <p className="text-muted-foreground">
                  The MIB file you're looking for doesn't exist or has been deleted.
                </p>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="flex items-center gap-4 mb-6">
          <Link to="/mib">
            <Button variant="ghost" className="flex items-center gap-2">
              <ArrowLeft className="h-4 w-4" />
              Back to MIB Browser
            </Button>
          </Link>
        </div>

        {/* File Information */}
        <Card className="mb-6">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileText className="h-5 w-5" />
              {fileDetails.name}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-3">
                <div className="flex items-center gap-2">
                  <HardDrive className="h-4 w-4 text-muted-foreground" />
                  <span className="text-sm font-medium">File Size:</span>
                  <span className="text-sm text-muted-foreground">
                    {formatFileSize(fileDetails.size)}
                  </span>
                </div>
                
                <div className="flex items-center gap-2">
                  <Calendar className="h-4 w-4 text-muted-foreground" />
                  <span className="text-sm font-medium">Upload Date:</span>
                  <span className="text-sm text-muted-foreground">
                    {formatDate(fileDetails.uploadDate)}
                  </span>
                </div>
                
                <div className="flex items-center gap-2">
                  <Info className="h-4 w-4 text-muted-foreground" />
                  <span className="text-sm font-medium">Status:</span>
                  <div className="flex items-center gap-2">
                    {getStatusIcon(fileDetails.status)}
                    <Badge variant={getStatusColor(fileDetails.status) as "success" | "warning" | "destructive" | "secondary"}>
                      {fileDetails.status}
                    </Badge>
                  </div>
                </div>
              </div>
              
              <div className="space-y-3">
                {fileDetails.parsedNodes && (
                  <div className="flex items-center gap-2">
                    <FileText className="h-4 w-4 text-muted-foreground" />
                    <span className="text-sm font-medium">Parsed Nodes:</span>
                    <span className="text-sm text-muted-foreground">
                      {fileDetails.parsedNodes}
                    </span>
                  </div>
                )}
                
                {fileDetails.totalNodes && (
                  <div className="flex items-center gap-2">
                    <FileText className="h-4 w-4 text-muted-foreground" />
                    <span className="text-sm font-medium">Total Nodes:</span>
                    <span className="text-sm text-muted-foreground">
                      {fileDetails.totalNodes}
                    </span>
                  </div>
                )}
                
                {fileDetails.description && (
                  <div className="flex items-start gap-2">
                    <Info className="h-4 w-4 text-muted-foreground mt-0.5" />
                    <div>
                      <span className="text-sm font-medium">Description:</span>
                      <p className="text-sm text-muted-foreground mt-1">
                        {fileDetails.description}
                      </p>
                    </div>
                  </div>
                )}
              </div>
            </div>
            
            {fileDetails.errorMessage && (
              <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded-lg">
                <div className="flex items-center gap-2 text-red-700">
                  <AlertTriangle className="h-4 w-4" />
                  <span className="font-medium">Error:</span>
                </div>
                <p className="text-sm text-red-600 mt-1">
                  {fileDetails.errorMessage}
                </p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* File Content */}
        {fileDetails.content && (
          <Card>
            <CardHeader>
              <CardTitle>File Content</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="bg-muted p-4 rounded-lg">
                <pre className="text-sm overflow-x-auto whitespace-pre-wrap">
                  {fileDetails.content}
                </pre>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Actions */}
        <div className="flex items-center gap-4 mt-6">
          <Button variant="outline" className="flex items-center gap-2">
            <Download className="h-4 w-4" />
            Download
          </Button>
          
          <Button variant="destructive" className="flex items-center gap-2">
            <Trash2 className="h-4 w-4" />
            Delete File
          </Button>
        </div>
      </div>
    </div>
  )
} 