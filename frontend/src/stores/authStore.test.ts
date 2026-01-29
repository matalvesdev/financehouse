import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'

// Mock do módulo api antes de importar o store
vi.mock('@/lib/api', () => ({
  authService: {
    login: vi.fn(),
    register: vi.fn(),
    refresh: vi.fn(),
    logout: vi.fn(),
  },
  getErrorMessage: vi.fn((error) => error?.message || 'Erro desconhecido'),
}))

import { useAuthStore } from '@/stores/authStore'
import { authService } from '@/lib/api'

describe('AuthStore', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Reset do store usando o método getState().setTokens para limpar estado
    useAuthStore.setState({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,
    })
  })

  describe('login', () => {
    it('deve fazer login com sucesso e atualizar o estado', async () => {
      const mockResponse = {
        usuario: {
          id: '1',
          email: 'test@example.com',
          nome: 'Test User',
          ativo: true,
          dadosIniciais: false,
          criadoEm: '2024-01-01T00:00:00Z',
        },
        accessToken: 'access-token-123',
        refreshToken: 'refresh-token-456',
      }

      vi.mocked(authService.login).mockResolvedValue(mockResponse)

      const { result } = renderHook(() => useAuthStore())

      await act(async () => {
        await result.current.login('test@example.com', 'password123')
      })

      await waitFor(() => {
        expect(result.current.user).toEqual(mockResponse.usuario)
        expect(result.current.accessToken).toBe('access-token-123')
        expect(result.current.refreshToken).toBe('refresh-token-456')
        expect(result.current.isAuthenticated).toBe(true)
        expect(result.current.isLoading).toBe(false)
        expect(result.current.error).toBeNull()
      })
    })

    it('deve definir erro quando login falhar', async () => {
      vi.mocked(authService.login).mockRejectedValue(new Error('Credenciais inválidas'))

      const { result } = renderHook(() => useAuthStore())

      await act(async () => {
        try {
          await result.current.login('test@example.com', 'wrongpassword')
        } catch {
          // Esperado - o login lança erro
        }
      })

      await waitFor(() => {
        expect(result.current.user).toBeNull()
        expect(result.current.isAuthenticated).toBe(false)
        expect(result.current.isLoading).toBe(false)
        expect(result.current.error).toBe('Credenciais inválidas')
      })
    })
  })

  describe('logout', () => {
    it('deve limpar o estado ao fazer logout', async () => {
      // Primeiro faz login
      const mockResponse = {
        usuario: { id: '1', email: 'test@example.com', nome: 'Test User' },
        accessToken: 'token',
        refreshToken: 'refresh',
      }
      vi.mocked(authService.login).mockResolvedValue(mockResponse)
      vi.mocked(authService.logout).mockResolvedValue(undefined)

      const { result } = renderHook(() => useAuthStore())

      await act(async () => {
        await result.current.login('test@example.com', 'password123')
      })

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true)
      })

      // Depois faz logout
      await act(async () => {
        await result.current.logout()
      })

      await waitFor(() => {
        expect(result.current.user).toBeNull()
        expect(result.current.accessToken).toBeNull()
        expect(result.current.refreshToken).toBeNull()
        expect(result.current.isAuthenticated).toBe(false)
      })
    })
  })

  describe('register', () => {
    it('deve registrar e fazer login automaticamente', async () => {
      const mockUserResponse = {
        usuario: { id: '1', email: 'new@example.com', nome: 'New User' },
        accessToken: 'token',
        refreshToken: 'refresh',
      }

      vi.mocked(authService.register).mockResolvedValue({ id: '1' })
      vi.mocked(authService.login).mockResolvedValue(mockUserResponse)

      const { result } = renderHook(() => useAuthStore())

      await act(async () => {
        await result.current.register('New User', 'new@example.com', 'password123')
      })

      await waitFor(() => {
        expect(authService.register).toHaveBeenCalledWith('New User', 'new@example.com', 'password123')
        expect(result.current.isAuthenticated).toBe(true)
      })
    })
  })

  describe('initializeAuth', () => {
    it('deve definir isAuthenticated corretamente baseado no estado', () => {
      const { result } = renderHook(() => useAuthStore())

      act(() => {
        result.current.initializeAuth()
      })

      // Sem token e usuário, não deve estar autenticado
      expect(result.current.isAuthenticated).toBe(false)
    })
  })

  describe('clearError', () => {
    it('deve limpar o erro', async () => {
      vi.mocked(authService.login).mockRejectedValue(new Error('Erro'))

      const { result } = renderHook(() => useAuthStore())

      await act(async () => {
        try {
          await result.current.login('test@example.com', 'wrong')
        } catch {
          // Esperado - o login lança erro
        }
      })

      await waitFor(() => {
        expect(result.current.error).not.toBeNull()
      })

      act(() => {
        result.current.clearError()
      })

      expect(result.current.error).toBeNull()
    })
  })
})
