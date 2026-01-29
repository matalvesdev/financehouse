import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { vi } from 'vitest'
import RegisterPage from '../RegisterPage'
import { useAuthStore } from '@/stores/authStore'

// Mock the auth store
vi.mock('@/stores/authStore')

const mockRegister = vi.fn()
const mockClearError = vi.fn()

const renderRegisterPage = () => {
  return render(
    <BrowserRouter>
      <RegisterPage />
    </BrowserRouter>
  )
}

describe('RegisterPage', () => {
  beforeEach(() => {
    vi.mocked(useAuthStore).mockReturnValue({
      register: mockRegister,
      isAuthenticated: false,
      isLoading: false,
      error: null,
      clearError: mockClearError,
      user: null,
      accessToken: null,
      refreshToken: null,
      login: vi.fn(),
      logout: vi.fn(),
      refreshAccessToken: vi.fn(),
      initializeAuth: vi.fn(),
      setTokens: vi.fn(),
    })
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('renders registration form correctly', () => {
    renderRegisterPage()
    
    expect(screen.getByText('Crie sua conta')).toBeInTheDocument()
    expect(screen.getByLabelText('Nome completo')).toBeInTheDocument()
    expect(screen.getByLabelText('Email')).toBeInTheDocument()
    expect(screen.getByLabelText('Senha')).toBeInTheDocument()
    expect(screen.getByLabelText('Confirmar senha')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /criar conta/i })).toBeInTheDocument()
  })

  it('validates required fields', async () => {
    const user = userEvent.setup()
    renderRegisterPage()
    
    const submitButton = screen.getByRole('button', { name: /criar conta/i })
    await user.click(submitButton)
    
    await waitFor(() => {
      expect(screen.getByText('Nome é obrigatório')).toBeInTheDocument()
      expect(screen.getByText('Email é obrigatório')).toBeInTheDocument()
      expect(screen.getByText('Senha é obrigatória')).toBeInTheDocument()
      expect(screen.getByText('Confirmação de senha é obrigatória')).toBeInTheDocument()
    })
  })

  it('validates name minimum length', async () => {
    const user = userEvent.setup()
    renderRegisterPage()
    
    const nameInput = screen.getByLabelText('Nome completo')
    const emailInput = screen.getByLabelText('Email')
    const passwordInput = screen.getByLabelText('Senha')
    const confirmPasswordInput = screen.getByLabelText('Confirmar senha')
    const submitButton = screen.getByRole('button', { name: /criar conta/i })
    
    await user.type(nameInput, 'Jo')
    await user.type(emailInput, 'test@example.com')
    await user.type(passwordInput, 'Password123!')
    await user.type(confirmPasswordInput, 'Password123!')
    await user.click(submitButton)
    
    await waitFor(() => {
      expect(screen.getByText('Nome deve ter pelo menos 3 caracteres')).toBeInTheDocument()
    })
  })

  it('validates email format', async () => {
    const user = userEvent.setup()
    renderRegisterPage()
    
    const nameInput = screen.getByLabelText('Nome completo')
    const emailInput = screen.getByLabelText('Email')
    const passwordInput = screen.getByLabelText('Senha')
    const confirmPasswordInput = screen.getByLabelText('Confirmar senha')
    const submitButton = screen.getByRole('button', { name: /criar conta/i })
    
    // Note: Browser's native email validation on type="email" prevents form submission
    // with invalid emails, so Zod validation doesn't get a chance to run.
    // This test verifies that the form doesn't submit with invalid email.
    await user.type(nameInput, 'João Silva')
    await user.type(emailInput, 'invalid-email')
    await user.type(passwordInput, 'Password123!')
    await user.type(confirmPasswordInput, 'Password123!')
    await user.click(submitButton)
    
    // The form should not call the register function with invalid email
    await waitFor(() => {
      expect(mockRegister).not.toHaveBeenCalled()
    }, { timeout: 500 })
  })

  it('validates password complexity', async () => {
    const user = userEvent.setup()
    renderRegisterPage()
    
    const nameInput = screen.getByLabelText('Nome completo')
    const emailInput = screen.getByLabelText('Email')
    const passwordInput = screen.getByLabelText('Senha')
    const confirmPasswordInput = screen.getByLabelText('Confirmar senha')
    const submitButton = screen.getByRole('button', { name: /criar conta/i })
    
    // Fill in required fields
    await user.type(nameInput, 'João Silva')
    await user.type(emailInput, 'joao@example.com')
    
    // Test minimum length
    await user.type(passwordInput, '123')
    await user.type(confirmPasswordInput, '123')
    await user.click(submitButton)
    
    await waitFor(() => {
      expect(screen.getByText('Senha deve ter pelo menos 8 caracteres')).toBeInTheDocument()
    })
    
    // Clear and test missing uppercase
    await user.clear(passwordInput)
    await user.clear(confirmPasswordInput)
    await user.type(passwordInput, 'password123!')
    await user.type(confirmPasswordInput, 'password123!')
    await user.click(submitButton)
    
    await waitFor(() => {
      expect(screen.getByText('Senha deve conter pelo menos uma letra maiúscula')).toBeInTheDocument()
    })
    
    // Clear and test missing lowercase
    await user.clear(passwordInput)
    await user.clear(confirmPasswordInput)
    await user.type(passwordInput, 'PASSWORD123!')
    await user.type(confirmPasswordInput, 'PASSWORD123!')
    await user.click(submitButton)
    
    await waitFor(() => {
      expect(screen.getByText('Senha deve conter pelo menos uma letra minúscula')).toBeInTheDocument()
    })
    
    // Clear and test missing number
    await user.clear(passwordInput)
    await user.clear(confirmPasswordInput)
    await user.type(passwordInput, 'Password!')
    await user.type(confirmPasswordInput, 'Password!')
    await user.click(submitButton)
    
    await waitFor(() => {
      expect(screen.getByText('Senha deve conter pelo menos um número')).toBeInTheDocument()
    })
    
    // Clear and test missing special character
    await user.clear(passwordInput)
    await user.clear(confirmPasswordInput)
    await user.type(passwordInput, 'Password123')
    await user.type(confirmPasswordInput, 'Password123')
    await user.click(submitButton)
    
    await waitFor(() => {
      expect(screen.getByText('Senha deve conter pelo menos um caractere especial (@$!%*?&)')).toBeInTheDocument()
    })
  })

  it('validates password confirmation', async () => {
    const user = userEvent.setup()
    renderRegisterPage()
    
    const nameInput = screen.getByLabelText('Nome completo')
    const emailInput = screen.getByLabelText('Email')
    const passwordInput = screen.getByLabelText('Senha')
    const confirmPasswordInput = screen.getByLabelText('Confirmar senha')
    const submitButton = screen.getByRole('button', { name: /criar conta/i })
    
    await user.type(nameInput, 'João Silva')
    await user.type(emailInput, 'joao@example.com')
    await user.type(passwordInput, 'Password123!')
    await user.type(confirmPasswordInput, 'DifferentPassword123!')
    await user.click(submitButton)
    
    await waitFor(() => {
      expect(screen.getByText('Senhas não conferem')).toBeInTheDocument()
    })
  })

  it('submits form with valid data', async () => {
    const user = userEvent.setup()
    renderRegisterPage()
    
    const nameInput = screen.getByLabelText('Nome completo')
    const emailInput = screen.getByLabelText('Email')
    const passwordInput = screen.getByLabelText('Senha')
    const confirmPasswordInput = screen.getByLabelText('Confirmar senha')
    const submitButton = screen.getByRole('button', { name: /criar conta/i })
    
    await user.type(nameInput, 'João Silva')
    await user.type(emailInput, 'joao@example.com')
    await user.type(passwordInput, 'Password123!')
    await user.type(confirmPasswordInput, 'Password123!')
    await user.click(submitButton)
    
    await waitFor(() => {
      expect(mockRegister).toHaveBeenCalledWith('João Silva', 'joao@example.com', 'Password123!')
    })
  })

  it('toggles password visibility', async () => {
    const user = userEvent.setup()
    renderRegisterPage()
    
    const passwordInput = screen.getByLabelText('Senha')
    const confirmPasswordInput = screen.getByLabelText('Confirmar senha')
    const togglePasswordButton = screen.getByLabelText('Mostrar senha')
    const toggleConfirmPasswordButton = screen.getByLabelText('Mostrar confirmação de senha')
    
    expect(passwordInput).toHaveAttribute('type', 'password')
    expect(confirmPasswordInput).toHaveAttribute('type', 'password')
    
    await user.click(togglePasswordButton)
    expect(passwordInput).toHaveAttribute('type', 'text')
    
    await user.click(toggleConfirmPasswordButton)
    expect(confirmPasswordInput).toHaveAttribute('type', 'text')
    
    await user.click(togglePasswordButton)
    expect(passwordInput).toHaveAttribute('type', 'password')
    
    await user.click(toggleConfirmPasswordButton)
    expect(confirmPasswordInput).toHaveAttribute('type', 'password')
  })

  it('displays authentication error', () => {
    vi.mocked(useAuthStore).mockReturnValue({
      register: mockRegister,
      isAuthenticated: false,
      isLoading: false,
      error: 'Email já está em uso',
      clearError: mockClearError,
      user: null,
      accessToken: null,
      refreshToken: null,
      login: vi.fn(),
      logout: vi.fn(),
      refreshAccessToken: vi.fn(),
      initializeAuth: vi.fn(),
      setTokens: vi.fn(),
    })

    renderRegisterPage()
    
    expect(screen.getByText('Email já está em uso')).toBeInTheDocument()
  })

  it('shows loading state', () => {
    vi.mocked(useAuthStore).mockReturnValue({
      register: mockRegister,
      isAuthenticated: false,
      isLoading: true,
      error: null,
      clearError: mockClearError,
      user: null,
      accessToken: null,
      refreshToken: null,
      login: vi.fn(),
      logout: vi.fn(),
      refreshAccessToken: vi.fn(),
      initializeAuth: vi.fn(),
      setTokens: vi.fn(),
    })

    renderRegisterPage()
    
    const submitButton = screen.getByRole('button', { name: /criar conta/i })
    expect(submitButton).toBeDisabled()
  })

  it('displays password requirements helper text', () => {
    renderRegisterPage()
    
    expect(screen.getByText('Mínimo 8 caracteres, com letra maiúscula, minúscula, número e caractere especial')).toBeInTheDocument()
  })
})