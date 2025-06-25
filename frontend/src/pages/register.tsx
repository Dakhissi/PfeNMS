import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Link } from "react-router-dom"
import { useState } from "react"
import { Eye, EyeOff, Lock, User, Mail, ArrowRight, UserCheck } from "lucide-react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { registerSchema, type RegisterFormData } from "@/lib/auth"
import { useAuth } from "@/lib/auth-hooks"

export function RegisterPage() {
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const { register: registerUser } = useAuth()

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  })

  const password = watch("password")
  const confirmPassword = watch("confirmPassword")

  const onSubmit = async (data: RegisterFormData) => {
    setIsLoading(true)
    setError(null)
    
    try {
      await registerUser(data.name, data.username, data.email, data.password)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background to-muted/20 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-primary/10">
            <User className="h-8 w-8 text-primary" />
          </div>
          <p className="text-3xl font-bold tracking-tight">Create account</p>
          <p className="mt-2 text-muted-foreground">
            Join us and start managing your network today
          </p>
        </div>

        <Card className="shadow-lg border-0">
          <CardHeader className="space-y-1 pb-4">
            <CardTitle className="text-xl font-semibold">Sign Up</CardTitle>
            <p className="text-sm text-muted-foreground">
              Fill in your details to create your account
            </p>
          </CardHeader>
          <CardContent>
            {error && (
              <div className="mb-4 p-3 text-sm text-red-600 bg-red-50 border border-red-200 rounded-md">
                {error}
              </div>
            )}

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
              <div className="space-y-2">
                <Label htmlFor="name" className="text-sm font-medium">
                  Full Name
                </Label>
                <div className="relative">
                  <UserCheck className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                  <Input
                    id="name"
                    {...register("name")}
                    type="text"
                    autoComplete="name"
                    className="pl-10 h-11"
                    placeholder="Enter your full name"
                  />
                </div>
                {errors.name && (
                  <p className="text-sm text-red-600">{errors.name.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="username" className="text-sm font-medium">
                  Username
                </Label>
                <div className="relative">
                  <User className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                  <Input
                    id="username"
                    {...register("username")}
                    type="text"
                    autoComplete="username"
                    className="pl-10 h-11"
                    placeholder="Choose a username"
                  />
                </div>
                {errors.username && (
                  <p className="text-sm text-red-600">{errors.username.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="email" className="text-sm font-medium">
                  Email
                </Label>
                <div className="relative">
                  <Mail className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                  <Input
                    id="email"
                    {...register("email")}
                    type="email"
                    autoComplete="email"
                    className="pl-10 h-11"
                    placeholder="Enter your email"
                  />
                </div>
                {errors.email && (
                  <p className="text-sm text-red-600">{errors.email.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="password" className="text-sm font-medium">
                  Password
                </Label>
                <div className="relative">
                  <Lock className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                  <Input
                    id="password"
                    {...register("password")}
                    type={showPassword ? "text" : "password"}
                    autoComplete="new-password"
                    className="pl-10 pr-10 h-11"
                    placeholder="Create a strong password"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-3 text-muted-foreground hover:text-foreground"
                  >
                    {showPassword ? (
                      <EyeOff className="h-4 w-4" />
                    ) : (
                      <Eye className="h-4 w-4" />
                    )}
                  </button>
                </div>
                {errors.password && (
                  <p className="text-sm text-red-600">{errors.password.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="confirmPassword" className="text-sm font-medium">
                  Confirm Password
                </Label>
                <div className="relative">
                  <Lock className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                  <Input
                    id="confirmPassword"
                    {...register("confirmPassword")}
                    type={showConfirmPassword ? "text" : "password"}
                    autoComplete="new-password"
                    className="pl-10 pr-10 h-11"
                    placeholder="Confirm your password"
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute right-3 top-3 text-muted-foreground hover:text-foreground"
                  >
                    {showConfirmPassword ? (
                      <EyeOff className="h-4 w-4" />
                    ) : (
                      <Eye className="h-4 w-4" />
                    )}
                  </button>
                </div>
                {errors.confirmPassword && (
                  <p className="text-sm text-red-600">{errors.confirmPassword.message}</p>
                )}
                {confirmPassword && password && (
                  <div className="flex items-center space-x-2 text-sm">
                    {password === confirmPassword ? (
                      <>
                        <div className="h-4 w-4 rounded-full bg-green-600 flex items-center justify-center">
                          <div className="h-2 w-2 rounded-full bg-white" />
                        </div>
                        <span className="text-green-600">Passwords match</span>
                      </>
                    ) : (
                      <>
                        <div className="h-4 w-4 rounded-full border-2 border-red-600" />
                        <span className="text-red-600">Passwords do not match</span>
                      </>
                    )}
                  </div>
                )}
              </div>

              <Button 
                type="submit" 
                className="w-full h-11 text-base font-medium" 
                disabled={isLoading}
              >
                {isLoading ? (
                  <div className="flex items-center">
                    <div className="mr-2 h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
                    Creating account...
                  </div>
                ) : (
                  <div className="flex items-center">
                    Create Account
                    <ArrowRight className="ml-2 h-4 w-4" />
                  </div>
                )}
              </Button>
            </form>

            <div className="mt-6 text-center">
              <p className="text-sm text-muted-foreground">
                Already have an account?{' '}
                <Link 
                  to="/login" 
                  className="font-medium text-primary hover:underline"
                >
                  Sign in here
                </Link>
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
} 