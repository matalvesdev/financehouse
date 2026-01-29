import { useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useAuthStore } from '@/stores/authStore'
import { registerSchema, type RegisterFormData } from '@/lib/schemas'
import { Input, Button } from '@/components/ui'
import { Eye, EyeOff, UserPlus, Wallet } from 'lucide-react'

export default function RegisterPage() {
  const { register: registerUser, isAuthenticated, isLoading, error: authError, clearError } = useAuthStore()
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  })

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />
  }

  const onSubmit = async (data: RegisterFormData) => {
    clearError()
    try {
      await registerUser(data.nome, data.email, data.password)
    } catch (error) {
      // Error is already handled by the auth store
      console.error('Registration failed:', error)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full bg-white rounded-xl shadow-lg p-8 space-y-8">
        <div className="text-center">
          <div className="mx-auto h-14 w-14 bg-blue-600 rounded-xl flex items-center justify-center">
            <Wallet className="h-8 w-8 text-white" />
          </div>
          <h2 className="mt-4 text-3xl font-bold text-gray-900">
            Crie sua conta
          </h2>
          <p className="mt-2 text-sm text-gray-600">
            Comece a controlar suas finanças agora
          </p>
        </div>
        
        <form className="space-y-6" onSubmit={handleSubmit(onSubmit)}>
          {authError && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
              {authError}
            </div>
          )}
          
          <div className="space-y-4">
            <Input
              label="Nome completo"
              type="text"
              autoComplete="name"
              placeholder="Digite seu nome completo"
              error={errors.nome?.message}
              {...register('nome')}
            />
            
            <Input
              label="Email"
              type="email"
              autoComplete="email"
              placeholder="Digite seu email"
              error={errors.email?.message}
              {...register('email')}
            />
            
            <div className="relative">
              <Input
                label="Senha"
                type={showPassword ? 'text' : 'password'}
                autoComplete="new-password"
                placeholder="Digite sua senha"
                error={errors.password?.message}
                helperText="Mínimo 8 caracteres, com letra maiúscula, minúscula, número e caractere especial"
                {...register('password')}
              />
              <button
                type="button"
                className="absolute inset-y-0 right-0 pr-3 flex items-center top-6"
                onClick={() => setShowPassword(!showPassword)}
                tabIndex={-1}
                aria-label={showPassword ? 'Ocultar senha' : 'Mostrar senha'}
              >
                {showPassword ? (
                  <EyeOff className="h-5 w-5 text-gray-400" />
                ) : (
                  <Eye className="h-5 w-5 text-gray-400" />
                )}
              </button>
            </div>
            
            <div className="relative">
              <Input
                label="Confirmar senha"
                type={showConfirmPassword ? 'text' : 'password'}
                autoComplete="new-password"
                placeholder="Confirme sua senha"
                error={errors.confirmPassword?.message}
                {...register('confirmPassword')}
              />
              <button
                type="button"
                className="absolute inset-y-0 right-0 pr-3 flex items-center top-6"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                tabIndex={-1}
                aria-label={showConfirmPassword ? 'Ocultar confirmação de senha' : 'Mostrar confirmação de senha'}
              >
                {showConfirmPassword ? (
                  <EyeOff className="h-5 w-5 text-gray-400" />
                ) : (
                  <Eye className="h-5 w-5 text-gray-400" />
                )}
              </button>
            </div>
          </div>

          <Button
            type="submit"
            className="w-full"
            isLoading={isLoading}
          >
            <UserPlus className="h-5 w-5 mr-2" />
            Criar conta
          </Button>
          
          <p className="text-center text-sm text-gray-600">
            Já tem uma conta?{' '}
            <Link
              to="/login"
              className="font-medium text-blue-600 hover:text-blue-500"
            >
              Entre aqui
            </Link>
          </p>
        </form>
      </div>
    </div>
  )
}