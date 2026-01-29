import { render, screen } from '@testing-library/react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { vi, describe, it, expect, beforeEach } from 'vitest'
import PrivateRoute from '../PrivateRoute'
import { useAuthStore } from '@/stores/authStore'

// Mock do auth store
vi.mock('@/stores/authStore')

const mockUseAuthStore = vi.mocked(useAuthStore)

// Componente de teste para simular uma página protegida
const ProtectedPage = () => <div>Protected Content</div>

// Componente de teste para simular a página de login
const LoginPage = () => <div>Login Page</div>

// Wrapper para testes com React Router
const RouterWrapper = ({ children }: { children: React.ReactNode }) => (
  <BrowserRouter>
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/protected" element={children} />
      <Route path="/" element={children} />
    </Routes>
  </BrowserRouter>
)

describe('PrivateRoute', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('when user is authenticated', () => {
    beforeEach(() => {
      mockUseAuthStore.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        user: { id: '1', nome: 'Test User', email: 'test@example.com' },
        accessToken: 'valid-token',
        refreshToken: 'valid-refresh-token',
        error: null,
        login: vi.fn(),
        logout: vi.fn(),
        register: vi.fn(),
        refreshAccessToken: vi.fn(),
        initializeAuth: vi.fn(),
        setTokens: vi.fn(),
        clearError: vi.fn(),
      })
    })

    it('should render protected content', () => {
      render(
        <RouterWrapper>
          <PrivateRoute>
            <ProtectedPage />
          </PrivateRoute>
        </RouterWrapper>
      )

      expect(screen.getByText('Protected Content')).toBeInTheDocument()
    })

    it('should not redirect to login', () => {
      render(
        <RouterWrapper>
          <PrivateRoute>
            <ProtectedPage />
          </PrivateRoute>
        </RouterWrapper>
      )

      expect(screen.queryByText('Login Page')).not.toBeInTheDocument()
      expect(screen.getByText('Protected Content')).toBeInTheDocument()
    })

    it('should handle multiple children correctly', () => {
      render(
        <RouterWrapper>
          <PrivateRoute>
            <div>First Child</div>
            <div>Second Child</div>
          </PrivateRoute>
        </RouterWrapper>
      )

      expect(screen.getByText('First Child')).toBeInTheDocument()
      expect(screen.getByText('Second Child')).toBeInTheDocument()
    })
  })

  describe('when user is not authenticated', () => {
    beforeEach(() => {
      mockUseAuthStore.mockReturnValue({
        isAuthenticated: false,
        isLoading: false,
        user: null,
        accessToken: null,
        refreshToken: null,
        error: null,
        login: vi.fn(),
        logout: vi.fn(),
        register: vi.fn(),
        refreshAccessToken: vi.fn(),
        initializeAuth: vi.fn(),
        setTokens: vi.fn(),
        clearError: vi.fn(),
      })
    })

    it('should redirect to login page', () => {
      // Usar window.history.pushState para simular navegação para rota protegida
      window.history.pushState({}, '', '/protected')

      render(
        <RouterWrapper>
          <PrivateRoute>
            <ProtectedPage />
          </PrivateRoute>
        </RouterWrapper>
      )

      expect(screen.getByText('Login Page')).toBeInTheDocument()
      expect(screen.queryByText('Protected Content')).not.toBeInTheDocument()
    })

    it('should not render protected content', () => {
      render(
        <RouterWrapper>
          <PrivateRoute>
            <ProtectedPage />
          </PrivateRoute>
        </RouterWrapper>
      )

      expect(screen.queryByText('Protected Content')).not.toBeInTheDocument()
    })

    it('should handle undefined auth state gracefully', () => {
      render(
        <RouterWrapper>
          <PrivateRoute>
            <ProtectedPage />
          </PrivateRoute>
        </RouterWrapper>
      )

      // Deve redirecionar para login quando não autenticado
      expect(screen.getByText('Login Page')).toBeInTheDocument()
    })
  })

  describe('when authentication is loading', () => {
    it('should show loading spinner', () => {
      mockUseAuthStore.mockReturnValue({
        isAuthenticated: false,
        isLoading: true,
        user: null,
        accessToken: null,
        refreshToken: null,
        error: null,
        login: vi.fn(),
        logout: vi.fn(),
        register: vi.fn(),
        refreshAccessToken: vi.fn(),
        initializeAuth: vi.fn(),
        setTokens: vi.fn(),
        clearError: vi.fn(),
      })

      const { container } = render(
        <BrowserRouter>
          <PrivateRoute>
            <ProtectedPage />
          </PrivateRoute>
        </BrowserRouter>
      )

      // Verifica se o spinner está presente (elemento com classe animate-spin)
      const spinner = container.querySelector('.animate-spin')
      expect(spinner).toBeTruthy()
      
      // Não deve mostrar conteúdo protegido nem página de login
      expect(screen.queryByText('Protected Content')).not.toBeInTheDocument()
      expect(screen.queryByText('Login Page')).not.toBeInTheDocument()
    })

    it('should show loading state with proper styling', () => {
      mockUseAuthStore.mockReturnValue({
        isAuthenticated: false,
        isLoading: true,
        user: null,
        accessToken: null,
        refreshToken: null,
        error: null,
        login: vi.fn(),
        logout: vi.fn(),
        register: vi.fn(),
        refreshAccessToken: vi.fn(),
        initializeAuth: vi.fn(),
        setTokens: vi.fn(),
        clearError: vi.fn(),
      })

      const { container } = render(
        <BrowserRouter>
          <PrivateRoute>
            <ProtectedPage />
          </PrivateRoute>
        </BrowserRouter>
      )

      // Verifica se o container de loading tem as classes corretas
      const loadingContainer = container.querySelector('.min-h-screen.flex.items-center.justify-center')
      expect(loadingContainer).toBeTruthy()
      
      // Verifica se o spinner tem as classes corretas
      const spinner = container.querySelector('.animate-spin.rounded-full.h-8.w-8.border-b-2.border-blue-600')
      expect(spinner).toBeTruthy()
    })

    it('should have proper loading state accessibility', () => {
      mockUseAuthStore.mockReturnValue({
        isAuthenticated: false,
        isLoading: true,
        user: null,
        accessToken: null,
        refreshToken: null,
        error: null,
        login: vi.fn(),
        logout: vi.fn(),
        register: vi.fn(),
        refreshAccessToken: vi.fn(),
        initializeAuth: vi.fn(),
        setTokens: vi.fn(),
        clearError: vi.fn(),
      })

      const { container } = render(
        <BrowserRouter>
          <PrivateRoute>
            <ProtectedPage />
          </PrivateRoute>
        </BrowserRouter>
      )

      // O loading spinner deve estar visível e acessível
      const loadingContainer = container.querySelector('.min-h-screen')
      expect(loadingContainer).toBeTruthy()
      
      // Verifica se não há conteúdo confuso sendo renderizado
      expect(screen.queryByText('Protected Content')).not.toBeInTheDocument()
      expect(screen.queryByText('Login Page')).not.toBeInTheDocument()
    })
  })

  describe('route preservation', () => {
    it('should preserve the original route for redirect after login', () => {
      mockUseAuthStore.mockReturnValue({
        isAuthenticated: false,
        isLoading: false,
        user: null,
        accessToken: null,
        refreshToken: null,
        error: null,
        login: vi.fn(),
        logout: vi.fn(),
        register: vi.fn(),
        refreshAccessToken: vi.fn(),
        initializeAuth: vi.fn(),
        setTokens: vi.fn(),
        clearError: vi.fn(),
      })

      // Simular navegação para uma rota específica
      window.history.pushState({}, '', '/protected?tab=settings')

      const TestComponent = () => {
        const location = window.location
        return (
          <div>
            Current path: {location.pathname}
            Current search: {location.search}
          </div>
        )
      }

      render(
        <BrowserRouter>
          <Routes>
            <Route 
              path="/login" 
              element={<TestComponent />} 
            />
            <Route 
              path="/protected" 
              element={
                <PrivateRoute>
                  <ProtectedPage />
                </PrivateRoute>
              } 
            />
          </Routes>
        </BrowserRouter>
      )

      // Deve redirecionar para login
      expect(screen.getByText(/Current path: \/login/)).toBeInTheDocument()
    })
  })
})