import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { authService, getErrorMessage } from '@/lib/api'
import type { User, AuthResponse } from '@/types'

interface AuthState {
  user: User | null
  accessToken: string | null
  refreshToken: string | null
  isAuthenticated: boolean
  isLoading: boolean
  error: string | null

  // Actions
  login: (email: string, password: string) => Promise<void>
  logout: () => Promise<void>
  register: (nome: string, email: string, password: string) => Promise<void>
  refreshAccessToken: () => Promise<void>
  initializeAuth: () => void
  setTokens: (accessToken: string, refreshToken: string) => void
  clearError: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      login: async (email: string, password: string) => {
        set({ isLoading: true, error: null })
        try {
          const response: AuthResponse = await authService.login(email, password)
          
          set({
            user: response.usuario,
            accessToken: response.accessToken,
            refreshToken: response.refreshToken,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          })
        } catch (error) {
          const errorMessage = getErrorMessage(error)
          set({ 
            isLoading: false, 
            error: errorMessage,
            isAuthenticated: false,
          })
          throw new Error(errorMessage)
        }
      },

      logout: async () => {
        const { accessToken } = get()
        
        try {
          if (accessToken) {
            await authService.logout()
          }
        } catch (error) {
          // Ignora erros de logout - faz logout local de qualquer forma
          console.warn('Erro ao fazer logout no servidor:', error)
        } finally {
          set({
            user: null,
            accessToken: null,
            refreshToken: null,
            isAuthenticated: false,
            error: null,
          })
        }
      },

      register: async (nome: string, email: string, password: string) => {
        set({ isLoading: true, error: null })
        try {
          await authService.register(nome, email, password)
          
          // Após registro bem-sucedido, faz login automaticamente
          await get().login(email, password)
        } catch (error) {
          const errorMessage = getErrorMessage(error)
          set({ 
            isLoading: false, 
            error: errorMessage 
          })
          throw new Error(errorMessage)
        }
      },

      refreshAccessToken: async () => {
        const { refreshToken } = get()
        
        if (!refreshToken) {
          throw new Error('No refresh token available')
        }

        try {
          const response = await authService.refresh(refreshToken)
          
          set({
            accessToken: response.accessToken,
            refreshToken: response.refreshToken,
          })
        } catch (error) {
          // Se falhar o refresh, faz logout
          get().logout()
          throw error
        }
      },

      initializeAuth: () => {
        const { accessToken, user } = get()
        if (accessToken && user) {
          set({ isAuthenticated: true })
        } else {
          set({ isAuthenticated: false })
        }
      },

      setTokens: (accessToken: string, refreshToken: string) => {
        set({ accessToken, refreshToken })
      },

      clearError: () => {
        set({ error: null })
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
)