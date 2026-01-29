import { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'

interface PrivateRouteProps {
  children: ReactNode
}

/**
 * PrivateRoute component para proteção de rotas autenticadas
 * 
 * Funcionalidades:
 * - Verifica se o usuário está autenticado
 * - Redireciona para login se não autenticado
 * - Preserva a URL de destino para redirecionamento após login
 * - Implementa os requisitos 1.1 e 10.2 da especificação
 */
export default function PrivateRoute({ children }: PrivateRouteProps) {
  const { isAuthenticated, isLoading } = useAuthStore()
  const location = useLocation()

  // Mostra loading enquanto inicializa autenticação
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    )
  }

  // Redireciona para login se não autenticado, preservando a URL de destino
  if (!isAuthenticated) {
    return (
      <Navigate 
        to="/login" 
        state={{ from: location.pathname + location.search }} 
        replace 
      />
    )
  }

  return <>{children}</>
}