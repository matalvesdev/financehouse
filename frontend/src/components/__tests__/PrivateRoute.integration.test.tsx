import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { vi, describe, it, expect, beforeEach } from 'vitest'
import PrivateRoute from '../PrivateRoute'
import { useAuthStore } from '@/stores/authStore'

// Mock do auth store
vi.mock('@/stores/authStore')

const mockUseAuthStore = vi.mocked(useAuthStore)

// Componentes de teste
const ProtectedDashboard = () => <div>Dashboard - Protected Content</div>
const ProtectedTransactions = () => <div>Transactions - Protected Content</div>
const LoginPage = () => (
  <div>
    <h1>Login Page</h1>
    <button onClick={() => mockLogin()}>Login</button>
  </div>
)

// Mock da função de login
const mockLogin = vi.fn()

// Aplicação de teste que simula a estrutura real
const TestApp = () => (
  <BrowserRouter>
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/*"
        element={
          <PrivateRoute>
            <Routes>
              <Route path="/" element={<ProtectedDashboard />} />
              <Route path="/dashboard" element={<ProtectedDashboard />} />
              <Route path="/transactions" element={<ProtectedTransactions />} />
            </Routes>
          </PrivateRoute>
        }
      />
    </Routes>
  </BrowserRouter>
)

describe('PrivateRoute Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockLogin.mockClear()
  })

  describe('Authentication Flow', () => {
    it('should redirect unauthenticated user to login', async () => {
      // Inicialmente não autenticado
      mockUseAuthStore.mockReturnValue({
        isAuthenticated: false,
        isLoading: false,
        user: null,
        accessToken: null,
        refreshToken: null,
        error: null,
        login: mockLogin,
        logout: vi.fn(),
        register: vi.fn(),
        refreshAccessToken: vi.fn(),
        initializeAuth: vi.fn(),
        setTokens: vi.fn(),
        clearError: vi.fn(),
      })

      // Navegar para rota protegida
      window.history.pushState({}, '', '/dashboard')
      
      render(<TestApp />)

      // Deve mostrar página de login
      expect(screen.getByText('Login Page')).toBeInTheDocument()
      expect(screen.queryByText('Dashboard - Protected Content')).not.toBeInTheDocument()
    })

    it('should handle loading state during authentication', async () => {
      // Estado de loading
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
            <ProtectedDashboard />
          </PrivateRoute>
        </BrowserRouter>
      )

      // Deve mostrar loading spinner
      const spinner = container.querySelector('.animate-spin')
      expect(spinner).toBeTruthy()
      
      // Não deve mostrar conteúdo protegido nem login
      expect(screen.queryByText('Dashboard - Protected Content')).not.toBeInTheDocument()
      expect(screen.queryByText('Login Page')).not.toBeInTheDocument()
    })

    it('should show protected content when authenticated', async () => {
      // Usuário autenticado
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

      // Navegar para rota protegida
      window.history.pushState({}, '', '/transactions')
      
      render(<TestApp />)

      // Deve mostrar a página de transações
      expect(screen.getByText('Transactions - Protected Content')).toBeInTheDocument()
      expect(screen.queryByText('Login Page')).not.toBeInTheDocument()
    })
  })

  describe('Route Protection Scenarios', () => {
    it('should protect multiple nested routes', () => {
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

      // Testar rota dashboard
      window.history.pushState({}, '', '/dashboard')
      const { unmount } = render(<TestApp />)
      
      // Deve redirecionar para login
      expect(screen.getAllByText('Login Page')[0]).toBeInTheDocument()
      expect(screen.queryByText(/Protected Content/)).not.toBeInTheDocument()
      
      unmount()
    })

    it('should allow access to all protected routes when authenticated', () => {
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

      // Testar acesso a dashboard
      window.history.pushState({}, '', '/dashboard')
      const { unmount } = render(<TestApp />)
      expect(screen.getByText('Dashboard - Protected Content')).toBeInTheDocument()
      unmount()

      // Testar acesso a transactions
      window.history.pushState({}, '', '/transactions')
      render(<TestApp />)
      expect(screen.getByText('Transactions - Protected Content')).toBeInTheDocument()
    })
  })

  describe('Error Handling', () => {
    it('should handle authentication errors gracefully', () => {
      mockUseAuthStore.mockReturnValue({
        isAuthenticated: false,
        isLoading: false,
        user: null,
        accessToken: null,
        refreshToken: null,
        error: 'Authentication failed',
        login: vi.fn(),
        logout: vi.fn(),
        register: vi.fn(),
        refreshAccessToken: vi.fn(),
        initializeAuth: vi.fn(),
        setTokens: vi.fn(),
        clearError: vi.fn(),
      })

      render(<TestApp />)

      // Deve redirecionar para login mesmo com erro
      expect(screen.getByText('Login Page')).toBeInTheDocument()
      expect(screen.queryByText(/Protected Content/)).not.toBeInTheDocument()
    })

    it('should handle token expiration scenario', () => {
      // Simular cenário onde token expira
      mockUseAuthStore.mockReturnValue({
        isAuthenticated: false, // Token expirado, não mais autenticado
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

      window.history.pushState({}, '', '/dashboard')
      render(<TestApp />)

      // Deve redirecionar para login
      expect(screen.getByText('Login Page')).toBeInTheDocument()
      expect(screen.queryByText('Dashboard - Protected Content')).not.toBeInTheDocument()
    })
  })

  describe('Performance', () => {
    it('should not cause unnecessary re-renders', () => {
      const renderSpy = vi.fn()
      
      const SpyComponent = () => {
        renderSpy()
        return <div>Protected Content</div>
      }

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

      render(
        <BrowserRouter>
          <PrivateRoute>
            <SpyComponent />
          </PrivateRoute>
        </BrowserRouter>
      )

      // Componente deve renderizar apenas uma vez
      expect(renderSpy).toHaveBeenCalledTimes(1)
    })
  })
})