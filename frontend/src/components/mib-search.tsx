import { useState, useMemo } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { 
  Search, 
  Filter, 
  X, 
  ChevronDown, 
  ChevronRight,
  FileText,
  Folder
} from "lucide-react"
import type { MibTreeNode } from "@/lib/mib-api"

interface MibSearchProps {
  nodes: MibTreeNode[]
  onNodeSelect: (node: MibTreeNode) => void
  selectedNode?: MibTreeNode | null
}

interface SearchFilters {
  name: string
  oid: string
  type: string
  access: string
  status: string
  description: string
}

export function MibSearch({ nodes, onNodeSelect, selectedNode }: MibSearchProps) {
  const [searchTerm, setSearchTerm] = useState("")
  const [showFilters, setShowFilters] = useState(false)
  const [filters, setFilters] = useState<SearchFilters>({
    name: "",
    oid: "",
    type: "",
    access: "",
    status: "",
    description: ""
  })

  // Get unique values for filter options
  const filterOptions = useMemo(() => {
    const types = new Set<string>()
    const accesses = new Set<string>()
    const statuses = new Set<string>()

    const collectOptions = (nodeList: MibTreeNode[]) => {
      nodeList.forEach(node => {
        if (node.type) types.add(node.type)
        if (node.access) accesses.add(node.access)
        if (node.status) statuses.add(node.status)
        if (node.children) collectOptions(node.children)
      })
    }

    collectOptions(nodes)
    return { types: Array.from(types), accesses: Array.from(accesses), statuses: Array.from(statuses) }
  }, [nodes])

  // Filter nodes based on search term and filters
  const filteredNodes = useMemo(() => {
    const filterNode = (node: MibTreeNode): MibTreeNode | null => {
      // Check if node matches search term
      const matchesSearch = !searchTerm || 
        node.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        node.oid.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (node.description && node.description.toLowerCase().includes(searchTerm.toLowerCase()))

      // Check if node matches filters
      const matchesFilters = 
        (!filters.name || node.name.toLowerCase().includes(filters.name.toLowerCase())) &&
        (!filters.oid || node.oid.toLowerCase().includes(filters.oid.toLowerCase())) &&
        (!filters.type || node.type === filters.type) &&
        (!filters.access || node.access === filters.access) &&
        (!filters.status || node.status === filters.status) &&
        (!filters.description || (node.description && node.description.toLowerCase().includes(filters.description.toLowerCase())))

      if (!matchesSearch || !matchesFilters) {
        // Check children
        const filteredChildren = node.children
          .map(child => filterNode(child))
          .filter(Boolean) as MibTreeNode[]

        if (filteredChildren.length > 0) {
          return { ...node, children: filteredChildren }
        }
        return null
      }

      // Recursively filter children
      const filteredChildren = node.children
        .map(child => filterNode(child))
        .filter(Boolean) as MibTreeNode[]

      return { ...node, children: filteredChildren }
    }

    return nodes
      .map(node => filterNode(node))
      .filter(Boolean) as MibTreeNode[]
  }, [nodes, searchTerm, filters])

  const clearFilters = () => {
    setFilters({
      name: "",
      oid: "",
      type: "",
      access: "",
      status: "",
      description: ""
    })
    setSearchTerm("")
  }

  const hasActiveFilters = searchTerm || Object.values(filters).some(v => v !== "")

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Search className="h-5 w-5" />
          MIB Object Search
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Search Input */}
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search MIB objects by name, OID, or description..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10"
          />
        </div>

        {/* Filter Toggle */}
        <div className="flex items-center justify-between">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setShowFilters(!showFilters)}
            className="flex items-center gap-2"
          >
            <Filter className="h-4 w-4" />
            Filters
            {showFilters ? <ChevronDown className="h-4 w-4" /> : <ChevronRight className="h-4 w-4" />}
          </Button>

          {hasActiveFilters && (
            <Button
              variant="ghost"
              size="sm"
              onClick={clearFilters}
              className="flex items-center gap-2 text-muted-foreground"
            >
              <X className="h-4 w-4" />
              Clear
            </Button>
          )}
        </div>

        {/* Advanced Filters */}
        {showFilters && (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            <div>
              <label className="text-sm font-medium">Name</label>
              <Input
                placeholder="Filter by name..."
                value={filters.name}
                onChange={(e) => setFilters(prev => ({ ...prev, name: e.target.value }))}
                className="mt-1"
              />
            </div>

            <div>
              <label className="text-sm font-medium">OID</label>
              <Input
                placeholder="Filter by OID..."
                value={filters.oid}
                onChange={(e) => setFilters(prev => ({ ...prev, oid: e.target.value }))}
                className="mt-1"
              />
            </div>

            <div>
              <label className="text-sm font-medium">Type</label>
              <select
                value={filters.type}
                onChange={(e) => setFilters(prev => ({ ...prev, type: e.target.value }))}
                className="w-full mt-1 p-2 border rounded-md text-sm"
              >
                <option value="">All Types</option>
                {filterOptions.types.map(type => (
                  <option key={type} value={type}>{type}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="text-sm font-medium">Access</label>
              <select
                value={filters.access}
                onChange={(e) => setFilters(prev => ({ ...prev, access: e.target.value }))}
                className="w-full mt-1 p-2 border rounded-md text-sm"
              >
                <option value="">All Access Levels</option>
                {filterOptions.accesses.map(access => (
                  <option key={access} value={access}>{access.replace('_', ' ')}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="text-sm font-medium">Status</label>
              <select
                value={filters.status}
                onChange={(e) => setFilters(prev => ({ ...prev, status: e.target.value }))}
                className="w-full mt-1 p-2 border rounded-md text-sm"
              >
                <option value="">All Statuses</option>
                {filterOptions.statuses.map(status => (
                  <option key={status} value={status}>{status}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="text-sm font-medium">Description</label>
              <Input
                placeholder="Filter by description..."
                value={filters.description}
                onChange={(e) => setFilters(prev => ({ ...prev, description: e.target.value }))}
                className="mt-1"
              />
            </div>
          </div>
        )}

        {/* Results Summary */}
        <div className="flex items-center justify-between text-sm text-muted-foreground">
          <span>
            {filteredNodes.length} result{filteredNodes.length !== 1 ? 's' : ''}
            {hasActiveFilters && ` (filtered from ${nodes.length} total)`}
          </span>
          {hasActiveFilters && (
            <div className="flex items-center gap-1">
              <span>Active filters:</span>
              {searchTerm && <Badge variant="secondary" className="text-xs">{searchTerm}</Badge>}
              {Object.entries(filters).map(([key, value]) => 
                value && <Badge key={key} variant="secondary" className="text-xs">{key}: {value}</Badge>
              )}
            </div>
          )}
        </div>

        {/* Search Results */}
        <div className="max-h-96 overflow-y-auto border rounded-lg">
          {filteredNodes.length > 0 ? (
            <div className="divide-y">
              {filteredNodes.map((node) => (
                <SearchResultNode
                  key={node.id}
                  node={node}
                  onNodeSelect={onNodeSelect}
                  selectedNode={selectedNode}
                />
              ))}
            </div>
          ) : (
            <div className="p-4 text-center text-muted-foreground">
              {hasActiveFilters ? 'No objects match your search criteria' : 'No MIB objects available'}
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}

interface SearchResultNodeProps {
  node: MibTreeNode
  onNodeSelect: (node: MibTreeNode) => void
  selectedNode?: MibTreeNode | null
}

function SearchResultNode({ node, onNodeSelect, selectedNode }: SearchResultNodeProps) {
  const isSelected = selectedNode?.id === node.id
  const hasChildren = node.children && node.children.length > 0

  const getAccessColor = (access: string) => {
    switch (access) {
      case 'READ_ONLY': return 'bg-blue-100 text-blue-800'
      case 'READ_WRITE': return 'bg-green-100 text-green-800'
      case 'WRITE_ONLY': return 'bg-orange-100 text-orange-800'
      case 'NOT_ACCESSIBLE': return 'bg-gray-100 text-gray-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  return (
    <div
      className={`p-3 hover:bg-muted/50 cursor-pointer transition-colors ${
        isSelected ? 'bg-blue-50 border-l-2 border-blue-500' : ''
      }`}
      onClick={() => onNodeSelect(node)}
    >
      <div className="flex items-center gap-3">
        {hasChildren ? (
          <Folder className="h-4 w-4 text-blue-500 flex-shrink-0" />
        ) : (
          <FileText className="h-4 w-4 text-gray-500 flex-shrink-0" />
        )}
        
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-1">
            <span className="font-medium truncate">{node.name}</span>
            {node.access && (
              <Badge variant="outline" className={`text-xs ${getAccessColor(node.access)}`}>
                {node.access.replace('_', ' ')}
              </Badge>
            )}
            {node.status && (
              <Badge variant="outline" className="text-xs">
                {node.status}
              </Badge>
            )}
          </div>
          
          <div className="text-sm text-muted-foreground font-mono mb-1">
            {node.oid}
          </div>
          
          {node.description && (
            <div className="text-sm text-muted-foreground line-clamp-2">
              {node.description}
            </div>
          )}
        </div>
      </div>
    </div>
  )
} 