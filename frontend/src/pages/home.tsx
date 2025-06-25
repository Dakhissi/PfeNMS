import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { useAuth } from "@/lib/auth-hooks"
import { 
  Network, 
  Activity, 
  AlertTriangle, 
  Shield, 
  Eye, 
  BarChart3,
  ArrowRight,
  CheckCircle,
  Zap,
  Globe
} from "lucide-react"
import { Link } from "react-router-dom"

export function HomePage() {
  const { isAuthenticated } = useAuth()

  const features = [
    {
      icon: Network,
      title: "Network Discovery",
      description: "Automatically discover and map your network topology using SNMP and advanced scanning techniques.",
      color: "text-blue-600"
    },
    {
      icon: Activity,
      title: "Real-time Monitoring",
      description: "Monitor network devices in real-time with customizable polling intervals and alert thresholds.",
      color: "text-green-600"
    },
    {
      icon: AlertTriangle,
      title: "Smart Alerts",
      description: "Receive intelligent alerts and notifications when network issues are detected.",
      color: "text-orange-600"
    },
    {
      icon: Shield,
      title: "Security Management",
      description: "Manage SNMP communities, access controls, and security policies for your network devices.",
      color: "text-purple-600"
    },
    {
      icon: Eye,
      title: "MIB Browser",
      description: "Browse and explore SNMP MIB files to understand device capabilities and configurations.",
      color: "text-indigo-600"
    },
    {
      icon: BarChart3,
      title: "Performance Analytics",
      description: "Track network performance metrics and generate comprehensive reports and analytics.",
      color: "text-pink-600"
    }
  ]

  const stats = [
    { label: "Devices Supported", value: "1000+", icon: Globe },
    { label: "Protocols", value: "SNMP v1/v2c/v3", icon: Zap },
    { label: "Real-time Alerts", value: "24/7", icon: AlertTriangle },
    { label: "Uptime", value: "99.9%", icon: CheckCircle }
  ]

  return (
    <div className="min-h-screen bg-background">
      {/* Hero Section */}
      <section className="relative overflow-hidden bg-gradient-to-br from-background via-background to-muted/20 py-24">
        <div className="container mx-auto px-4">
          <div className="mx-auto max-w-4xl text-center">
            <div className="mb-8 inline-flex items-center rounded-full border bg-background px-4 py-2 text-sm">
              <Zap className="mr-2 h-4 w-4 text-primary" />
              Modern Network Management Platform
            </div>
            <h1 className="mb-6 text-5xl font-bold tracking-tight lg:text-6xl">
              Network Management
              <span className="block bg-gradient-to-r from-primary to-primary/60 bg-clip-text text-transparent">
                Made Simple
              </span>
            </h1>
            <p className="mb-8 text-xl text-muted-foreground lg:text-2xl">
              Comprehensive network monitoring and management solution with real-time alerts, 
              automated discovery, and powerful analytics for modern IT infrastructure.
            </p>
            <div className="flex flex-col gap-4 sm:flex-row sm:justify-center">
              {isAuthenticated ? (
                <Button size="lg" asChild className="text-base">
                  <Link to="/dashboard">
                    Go to Dashboard
                    <ArrowRight className="ml-2 h-4 w-4" />
                  </Link>
                </Button>
              ) : (
                <>
                  <Button size="lg" asChild className="text-base">
                    <Link to="/register">
                      Get Started
                      <ArrowRight className="ml-2 h-4 w-4" />
                    </Link>
                  </Button>
                  <Button size="lg" variant="outline" asChild className="text-base">
                    <Link to="/login">Sign In</Link>
                  </Button>
                </>
              )}
            </div>
          </div>
        </div>
      </section>

      {/* Stats Section */}
      <section className="py-16 bg-muted/30">
        <div className="container mx-auto px-4">
          <div className="grid grid-cols-2 gap-8 md:grid-cols-4">
            {stats.map((stat, index) => (
              <div key={index} className="text-center">
                <div className="mb-2 flex justify-center">
                  <stat.icon className="h-8 w-8 text-primary" />
                </div>
                <div className="text-3xl font-bold text-foreground">{stat.value}</div>
                <div className="text-sm text-muted-foreground">{stat.label}</div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-24">
        <div className="container mx-auto px-4">
          <div className="mx-auto max-w-2xl text-center mb-16">
            <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">
              Powerful Features for Network Management
            </h2>
            <p className="mt-4 text-lg text-muted-foreground">
              Everything you need to monitor, manage, and secure your network infrastructure
            </p>
          </div>
          <div className="grid gap-8 md:grid-cols-2 lg:grid-cols-3">
            {features.map((feature, index) => (
              <Card key={index} className="group hover:shadow-lg transition-all duration-300">
                <CardHeader>
                  <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-lg bg-primary/10 group-hover:bg-primary/20 transition-colors">
                    <feature.icon className={`h-6 w-6 ${feature.color}`} />
                  </div>
                  <CardTitle className="text-xl">{feature.title}</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground leading-relaxed">
                    {feature.description}
                  </p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-24 bg-muted/30">
        <div className="container mx-auto px-4">
          <div className="mx-auto max-w-2xl text-center">
            <h2 className="text-3xl font-bold tracking-tight sm:text-4xl mb-4">
              Ready to Get Started?
            </h2>
            <p className="text-lg text-muted-foreground mb-8">
              Join thousands of IT professionals who trust our platform for their network management needs.
            </p>
            <div className="flex flex-col gap-4 sm:flex-row sm:justify-center">
              {isAuthenticated ? (
                <Button size="lg" asChild className="text-base">
                  <Link to="/dashboard">
                    Access Dashboard
                    <CheckCircle className="ml-2 h-4 w-4" />
                  </Link>
                </Button>
              ) : (
                <>
                  <Button size="lg" asChild className="text-base">
                    <Link to="/register">
                      Start Free Trial
                      <CheckCircle className="ml-2 h-4 w-4" />
                    </Link>
                  </Button>
                  <Button size="lg" variant="outline" asChild className="text-base">
                    <Link to="/login">Sign In to Dashboard</Link>
                  </Button>
                </>
              )}
            </div>
          </div>
        </div>
      </section>
    </div>
  )
} 