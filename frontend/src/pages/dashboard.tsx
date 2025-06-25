import { useAuth } from "@/lib/auth-hooks"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { LogOut, User, Mail } from "lucide-react"

export function DashboardPage() {
  const { user, logout } = useAuth()

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-4xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
          <p className="text-muted-foreground mt-2">
            Welcome to your Network Manager dashboard
          </p>
        </div>

        <div className="grid gap-6">
          {/* Welcome Card */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <User className="h-5 w-5" />
                Welcome, {user?.username}!
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-muted-foreground mb-4">
                You are now logged into your Network Manager account. This is your personal dashboard where you can manage your network configurations and monitor your systems.
              </p>
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Mail className="h-4 w-4" />
                <span>{user?.email}</span>
              </div>
            </CardContent>
          </Card>

          {/* Quick Actions */}
          <Card>
            <CardHeader>
              <CardTitle>Quick Actions</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                <Button variant="outline" className="h-20 flex flex-col gap-2">
                  <span className="text-lg">📊</span>
                  <span>View Analytics</span>
                </Button>
                <Button variant="outline" className="h-20 flex flex-col gap-2">
                  <span className="text-lg">⚙️</span>
                  <span>Settings</span>
                </Button>
                <Button variant="outline" className="h-20 flex flex-col gap-2">
                  <span className="text-lg">📋</span>
                  <span>Reports</span>
                </Button>
              </div>
            </CardContent>
          </Card>

          {/* Logout Section */}
          <Card>
            <CardHeader>
              <CardTitle>Account</CardTitle>
            </CardHeader>
            <CardContent>
              <Button 
                variant="destructive" 
                onClick={logout}
                className="flex items-center gap-2"
              >
                <LogOut className="h-4 w-4" />
                Logout
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
} 