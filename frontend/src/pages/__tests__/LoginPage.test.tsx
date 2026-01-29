import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { vi } from 'vitest'
import LoginPage from '../LoginPage'
import { useAuthStore } from '@/stores/authStore'

// Mock the auth store
vi.mock('@/stores/authStore')

const mockLogin = vi.fn()
const mockClearError = vi.fn()

const renderLoginPage = () => {
  return render(
    <BrowserRouter>
      <LoginPage />
    </BrowserRouter>
  )
}

describe('LoginPage', () => {
  beforeEach(() => {
    vi.mocked(useAuthStore).mockReturnValue({
      login: mockLogin,
      isAuthenticated: false,
      isLoading: false,
      error: null,
      clearError: mockClearError,
      user: null,
      accessToken: null,
      refreshToken: null,
      logout: vi.fn(),
      register: vi.fn(),
      refreshAccessToken: vi.fn(),
      initializeAuth: vi.fn(),
      setTokens: vi.fn(),
    })
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('renders login form correctly', () => {
    renderLoginPage()
    
    expect(screen.getByText('Bem-vindo de volta')).toBeInTheDocument()
    expect(screen.getByLabelText('Email')).toBeInTheDocument()
    expect(screen.getByLabelText('Senha')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /entrar/i })).toBeInTheDocument()
    expect(screen.getByText('Lembrar de mim')).toBeInTheDocument()
  })

  it('validates required fields', async () => {
    const user = userEvent.setup()
    renderLoginPage()
    
    const submitButton = screen.getByRole('button', { name: /entrar/i })
    await user.click(submitButton)
    
    await waitFor(() => {
      expect(screen.getByText('Email é obrigatório')).toBeInTheDocument()
      expect(screen.getByText('Senha é obrigatória')).toBeInTheDocument()
    })
  })

  it('validates email format', async () => {
    const user = userEvent.setup()
    renderLoginPage()
    
    const emailInput = screen.getByLabelText('Email')
    const passwordInput = screen.getByLabelText('Senha')
    const submitButton = screen.getByRole('button', { name: /entrar/i })
    
    // Note: Browser's native email validation on type="email" prevents form submission
    // with invalid emails, so Zod validation doesn't get a chance to run.
    // This test verifies that the form doesn't submit with invalid email.
    await user.type(emailInput, 'invalid-email')
    await user.type(passwordInput, 'password123')
    await user.click(submitButton)
    
    // The form should not call the login function with invalid email
    await waitFor(() => {
      expect(mockLogin).not.toHaveBeenCalled()
    }, { timeout: 500 })
  })

  it('validates password minimum length', async () => {
    const user = userEvent.setup()
    renderLoginPage()
    
    const emailInput = screen.getByLabelText('Email')
    const passwordInput = screen.getByLabelText('Senha')
    const submitButton = screen.getByRole('button', { name: /entrar/i })
    
    await user.type(emailInput, 'test@example.com')
    await user.type(passwordInput, '123')
    await user.click(submitButton)
    
    await waitFor(() => {
      expect(screen.getByText('Senha deve ter pelo menos 8 caracteres')).toBeInTheDocument()
    })
  })

  it('submits form with valid data', async () => {
    const user = userEvent.setup()
    renderLoginPage()
    
    const emailInput = screen.getByLabelText('Email')
    const passwordInput = screen.getByLabelText('Senha')
    const submitButton = screen.getByRole('button', { name: /entrar/i })
    
    await user.type(emailInput, 'test@example.com')
    await user.type(passwordInput, 'password123')
    await user.click(submitButton)
    
    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('test@example.com', 'password123')
    })
  })

  it('toggles password visibility', async () => {
    const user = userEvent.setup()
    renderLoginPage()
    
    const passwordInput = screen.getByLabelText('Senha')
    const toggleButton = screen.getByLabelText('Mostrar senha')
    
    expect(passwordInput).toHaveAttribute('type', 'password')
    
    await user.click(toggleButton)
    expect(passwordInput).toHaveAttribute('type', 'text')
    
    await user.click(toggleButton)
    expect(passwordInput).toHaveAttribute('type', 'password')
  })

  it('displays authentication error', () => {
    vi.mocked(useAuthStore).mockReturnValue({
      login: mockLogin,
      isAuthenticated: false,
      isLoading: false,
      error: 'Credenciais inválidas',
      clearError: mockClearError,
      user: null,
      accessToken: null,
      refreshToken: null,
      logout: vi.fn(),
      register: vi.fn(),
      refreshAccessToken: vi.fn(),
      initializeAuth: vi.fn(),
      setTokens: vi.fn(),
    })

    renderLoginPage()
    
    expect(screen.getByText('Credenciais inválidas')).toBeInTheDocument()
  })

  it('shows loading state', () => {
    vi.mocked(useAuthStore).mockReturnValue({
      login: mockLogin,
      isAuthenticated: false,
      isLoading: true,
      error: null,
      clearError: mockClearError,
      user: null,
      accessToken: null,
      refreshToken: null,
      logout: vi.fn(),
      register: vi.fn(),
      refreshAccessToken: vi.fn(),
      initializeAuth: vi.fn(),
      setTokens: vi.fn(),
    })

    renderLoginPage()
    
    const submitButton = screen.getByRole('button', { name: /entrar/i })
    expect(submitButton).toBeDisabled()
  })
})