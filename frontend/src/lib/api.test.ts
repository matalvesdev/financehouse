import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import axios from 'axios'
import { api, getErrorMessage, categorizeError, ErrorCategory, cancelAllRequests } from './api'
import { useAuthStore } from '@/stores/authStore'

// Mock do Zustand store
vi.mock('@/stores/authStore', () => ({
  useAuthStore: {
    getState: vi.fn(),
  },
}))

// Mock do axios
vi.mock('axios', async () => {
  const actual = await vi.importActual<typeof import('axios')>('axios')
  return {
    ...actual,
    default: {
      ...actual.default,
      create: vi.fn(() => ({
        defaults: {
          baseURL: 'http://localhost:8080/api',
          timeout: 30000,
          headers: { 'Content-Type': 'application/json' },
          validateStatus: (status: number) => status < 500,
        },
        interceptors: {
          request: { use: vi.fn() },
          response: { use: vi.fn() },
        },
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn(),
      })),
      post: vi.fn(),
      isAxiosError: vi.fn(),
      isCancel: vi.fn(),
      CancelToken: {
        source: vi.fn(() => ({
          token: 'mock-cancel-token',
          cancel: vi.fn(),
        })),
      },
    },
  }
})

describe('API Client', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Mock do estado inicial do auth store
    vi.mocked(useAuthStore.getState).mockReturnValue({
      accessToken: 'mock-access-token',
      refreshToken: 'mock-refresh-token',
      setTokens: vi.fn(),
      logout: vi.fn(),
    } as any)
  })

  afterEach(() => {
    cancelAllRequests()
  })

  describe('Error Categorization', () => {
    it('should categorize network errors correctly', () => {
      const networkError = {
        code: 'NETWORK_ERROR',
        message: 'Network Error',
      }
      vi.mocked(axios.isAxiosError).mockReturnValue(true)
      vi.mocked(axios.isCancel).mockReturnValue(false)

      const result = categorizeError(networkError)

      expect(result.category).toBe(ErrorCategory.NETWORK)
      expect(result.retryable).toBe(true)
      expect(result.message).toBe('Erro de conexão')
    })

    it('should categorize timeout errors correctly', () => {
      const timeoutError = {
        code: 'ECONNABORTED',
        message: 'timeout of 30000ms exceeded',
      }
      vi.mocked(axios.isAxiosError).mockReturnValue(true)
      vi.mocked(axios.isCancel).mockReturnValue(false)

      const result = categorizeError(timeoutError)

      expect(result.category).toBe(ErrorCategory.TIMEOUT)
      expect(result.retryable).toBe(true)
      expect(result.message).toBe('Tempo limite excedido')
    })

    it('should categorize authentication errors correctly', () => {
      const authError = {
        response: { status: 401 },
        message: 'Unauthorized',
      }
      vi.mocked(axios.isAxiosError).mockReturnValue(true)
      vi.mocked(axios.isCancel).mockReturnValue(false)

      const result = categorizeError(authError)

      expect(result.category).toBe(ErrorCategory.AUTHENTICATION)
      expect(result.retryable).toBe(false)
      expect(result.message).toBe('Sessão expirada. Por favor, faça login novamente.')
    })

    it('should categorize authorization errors correctly', () => {
      const authzError = {
        response: { status: 403 },
        message: 'Forbidden',
      }
      vi.mocked(axios.isAxiosError).mockReturnValue(true)
      vi.mocked(axios.isCancel).mockReturnValue(false)

      const result = categorizeError(authzError)

      expect(result.category).toBe(ErrorCategory.AUTHORIZATION)
      expect(result.retryable).toBe(false)
      expect(result.message).toBe('Você não tem permissão para realizar esta ação.')
    })

    it('should categorize validation errors correctly', () => {
      const validationError = {
        response: { status: 422 },
        message: 'Validation Error',
      }
      vi.mocked(axios.isAxiosError).mockReturnValue(true)
      vi.mocked(axios.isCancel).mockReturnValue(false)

      const result = categorizeError(validationError)

      expect(result.category).toBe(ErrorCategory.VALIDATION)
      expect(result.retryable).toBe(false)
      expect(result.message).toBe('Dados inválidos. Verifique os campos e tente novamente.')
    })

    it('should categorize server errors correctly', () => {
      const serverError = {
        response: { status: 500 },
        message: 'Internal Server Error',
      }
      vi.mocked(axios.isAxiosError).mockReturnValue(true)
      vi.mocked(axios.isCancel).mockReturnValue(false)

      const result = categorizeError(serverError)

      expect(result.category).toBe(ErrorCategory.SERVER)
      expect(result.retryable).toBe(true)
      expect(result.message).toBe('Erro interno do servidor. Tente novamente mais tarde.')
    })

    it('should categorize cancelled requests correctly', () => {
      const cancelledError = {
        message: 'Request cancelled',
      }
      vi.mocked(axios.isCancel).mockReturnValue(true)

      const result = categorizeError(cancelledError)

      expect(result.category).toBe(ErrorCategory.CANCELLED)
      expect(result.retryable).toBe(false)
      expect(result.message).toBe('Requisição cancelada')
    })

    it('should categorize unknown errors correctly', () => {
      const unknownError = new Error('Unknown error')
      vi.mocked(axios.isAxiosError).mockReturnValue(false)
      vi.mocked(axios.isCancel).mockReturnValue(false)

      const result = categorizeError(unknownError)

      expect(result.category).toBe(ErrorCategory.UNKNOWN)
      expect(result.retryable).toBe(false)
      expect(result.message).toBe('Unknown error')
    })
  })

  describe('Error Message Extraction', () => {
    it('should extract API error message when available', () => {
      const apiError = {
        response: {
          status: 400,
          data: {
            message: 'Custom API error message',
          },
        },
      }
      vi.mocked(axios.isAxiosError).mockReturnValue(true)

      const message = getErrorMessage(apiError)

      expect(message).toBe('Custom API error message')
    })

    it('should fallback to categorized message when API message not available', () => {
      const error = {
        response: { status: 401 },
        message: 'Unauthorized',
      }
      vi.mocked(axios.isAxiosError).mockReturnValue(true)
      vi.mocked(axios.isCancel).mockReturnValue(false)

      const message = getErrorMessage(error)

      expect(message).toBe('Sessão expirada. Por favor, faça login novamente.')
    })

    it('should handle non-axios errors', () => {
      const error = new Error('Regular error')
      vi.mocked(axios.isAxiosError).mockReturnValue(false)
      vi.mocked(axios.isCancel).mockReturnValue(false)

      const message = getErrorMessage(error)

      expect(message).toBe('Regular error')
    })

    it('should handle unknown error types', () => {
      const error = 'string error'
      vi.mocked(axios.isAxiosError).mockReturnValue(false)
      vi.mocked(axios.isCancel).mockReturnValue(false)

      const message = getErrorMessage(error)

      expect(message).toBe('Ocorreu um erro inesperado.')
    })
  })

  describe('Request Configuration', () => {
    it('should have correct base configuration', () => {
      expect(api.defaults.baseURL).toBe('http://localhost:8080/api')
      expect(api.defaults.timeout).toBe(30000)
      expect(api.defaults.headers['Content-Type']).toBe('application/json')
    })

    it('should validate status correctly', () => {
      // Status < 500 should be valid (not rejected)
      expect(api.defaults.validateStatus!(200)).toBe(true)
      expect(api.defaults.validateStatus!(400)).toBe(true)
      expect(api.defaults.validateStatus!(404)).toBe(true)
      expect(api.defaults.validateStatus!(499)).toBe(true)
      
      // Status >= 500 should be invalid (rejected)
      expect(api.defaults.validateStatus!(500)).toBe(false)
      expect(api.defaults.validateStatus!(503)).toBe(false)
    })
  })

  describe('Network Status', () => {
    it('should check network status', async () => {
      // Mock fetch para simular sucesso
      global.fetch = vi.fn().mockResolvedValue({
        ok: true,
      })

      const { checkNetworkStatus } = await import('./api')
      const isOnline = await checkNetworkStatus()

      expect(isOnline).toBe(true)
    })

    it('should fallback to navigator.onLine when fetch fails', async () => {
      // Mock fetch para simular falha
      global.fetch = vi.fn().mockRejectedValue(new Error('Network error'))
      
      // Mock navigator.onLine
      Object.defineProperty(navigator, 'onLine', {
        writable: true,
        value: false,
      })

      const { checkNetworkStatus } = await import('./api')
      const isOnline = await checkNetworkStatus()

      expect(isOnline).toBe(false)
    })
  })
})

describe('Service Integration', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(useAuthStore.getState).mockReturnValue({
      accessToken: 'mock-access-token',
      refreshToken: 'mock-refresh-token',
      setTokens: vi.fn(),
      logout: vi.fn(),
    } as any)
  })

  describe('Auth Service', () => {
    it('should call login with correct parameters', async () => {
      const mockResponse = { data: { accessToken: 'new-token' } }
      vi.mocked(api.post).mockResolvedValue(mockResponse)

      const { authService } = await import('./api')
      const result = await authService.login('test@example.com', 'password')

      expect(api.post).toHaveBeenCalledWith('/auth/login', {
        email: 'test@example.com',
        senha: 'password',
      }, {
        timeout: 15000,
      })
      expect(result).toEqual({ accessToken: 'new-token' })
    })

    it('should call register with correct parameters', async () => {
      const mockResponse = { data: { success: true } }
      vi.mocked(api.post).mockResolvedValue(mockResponse)

      const { authService } = await import('./api')
      const result = await authService.register('Test User', 'test@example.com', 'password')

      expect(api.post).toHaveBeenCalledWith('/auth/register', {
        nome: 'Test User',
        email: 'test@example.com',
        senha: 'password',
      }, {
        timeout: 15000,
      })
      expect(result).toEqual({ success: true })
    })
  })

  describe('Transaction Service', () => {
    it('should list transactions with correct parameters', async () => {
      const mockResponse = { data: { content: [] } }
      vi.mocked(api.get).mockResolvedValue(mockResponse)

      const { transactionService } = await import('./api')
      const result = await transactionService.list({
        page: 0,
        size: 10,
        categoria: 'ALIMENTACAO',
      })

      expect(api.get).toHaveBeenCalledWith('/transacoes', {
        params: {
          page: 0,
          size: 10,
          categoria: 'ALIMENTACAO',
        },
      })
      expect(result).toEqual({ content: [] })
    })

    it('should create transaction with correct parameters', async () => {
      const mockResponse = { data: { id: '123' } }
      vi.mocked(api.post).mockResolvedValue(mockResponse)

      const transactionData = {
        valor: 100.50,
        descricao: 'Test transaction',
        categoria: 'ALIMENTACAO',
        tipo: 'DESPESA' as const,
        data: '2024-01-15',
      }

      const { transactionService } = await import('./api')
      const result = await transactionService.create(transactionData)

      expect(api.post).toHaveBeenCalledWith('/transacoes', transactionData)
      expect(result).toEqual({ id: '123' })
    })
  })

  describe('Import Service', () => {
    it('should upload file with correct configuration', async () => {
      const mockResponse = { data: { importId: '123' } }
      vi.mocked(api.post).mockResolvedValue(mockResponse)

      const file = new File(['content'], 'test.csv', { type: 'text/csv' })
      const onUploadProgress = vi.fn()

      const { importService } = await import('./api')
      const result = await importService.uploadFile(file, onUploadProgress)

      expect(api.post).toHaveBeenCalledWith(
        '/importacao/upload',
        expect.any(FormData),
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
          timeout: 120000,
          onUploadProgress,
        }
      )
      expect(result).toEqual({ importId: '123' })
    })
  })
})