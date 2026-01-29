import axios, { AxiosError, AxiosInstance, InternalAxiosRequestConfig, AxiosResponse, CancelTokenSource } from 'axios'
import { useAuthStore } from '@/stores/authStore'
import { useState, useEffect } from 'react'

// Extend Axios types to include metadata
declare module 'axios' {
  interface InternalAxiosRequestConfig {
    metadata?: {
      requestId: string
    }
  }
}

// Configuração base do Axios
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'
const IS_DEVELOPMENT = import.meta.env.DEV

// Configuração de timeouts por tipo de operação
const TIMEOUTS = {
  default: 30000,      // 30 segundos
  upload: 120000,      // 2 minutos para uploads
  download: 180000,    // 3 minutos para downloads
  auth: 15000,         // 15 segundos para autenticação
} as const

// Criar instância do Axios
export const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: TIMEOUTS.default,
  headers: {
    'Content-Type': 'application/json',
  },
  // Configurações adicionais para robustez
  maxRedirects: 3,
  validateStatus: (status) => status < 500, // Não rejeita 4xx, apenas 5xx
})

// Flag para evitar múltiplas tentativas de refresh simultâneas
let isRefreshing = false
let failedQueue: Array<{
  resolve: (token: string) => void
  reject: (error: Error) => void
}> = []

// Mapa de cancel tokens para cancelar requisições se necessário
const cancelTokens = new Map<string, CancelTokenSource>()

const processQueue = (error: Error | null, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token!)
    }
  })
  failedQueue = []
}

// Função para gerar ID único para requisições
const generateRequestId = (): string => {
  return `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

// Função para log de desenvolvimento
const logRequest = (config: InternalAxiosRequestConfig) => {
  if (IS_DEVELOPMENT) {
    console.group(`🚀 API Request: ${config.method?.toUpperCase()} ${config.url}`)
    console.log('Config:', {
      url: config.url,
      method: config.method,
      headers: config.headers,
      data: config.data,
      params: config.params,
    })
    console.groupEnd()
  }
}

const logResponse = (response: AxiosResponse) => {
  if (IS_DEVELOPMENT) {
    console.group(`✅ API Response: ${response.status} ${response.config.url}`)
    console.log('Response:', {
      status: response.status,
      statusText: response.statusText,
      data: response.data,
      headers: response.headers,
    })
    console.groupEnd()
  }
}

const logError = (error: AxiosError) => {
  if (IS_DEVELOPMENT) {
    console.group(`❌ API Error: ${error.config?.url}`)
    console.log('Error:', {
      message: error.message,
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      config: error.config,
    })
    console.groupEnd()
  }
}

// Request interceptor - adiciona token de autenticação e logging
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Adicionar ID único à requisição para tracking
    const requestId = generateRequestId()
    config.metadata = { ...config.metadata, requestId }
    
    // Log da requisição em desenvolvimento
    logRequest(config)
    
    // Adicionar token de autenticação
    const { accessToken } = useAuthStore.getState()
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`
    }
    
    // Configurar timeout específico baseado no endpoint
    if (config.url?.includes('/importacao/upload')) {
      config.timeout = TIMEOUTS.upload
    } else if (config.url?.includes('/auth/')) {
      config.timeout = TIMEOUTS.auth
    }
    
    // Adicionar cancel token para permitir cancelamento
    const cancelToken = axios.CancelToken.source()
    config.cancelToken = cancelToken.token
    cancelTokens.set(requestId, cancelToken)
    
    return config
  },
  (error: AxiosError) => {
    logError(error)
    return Promise.reject(error)
  }
)

// Response interceptor - trata erros, refresh de token e logging
api.interceptors.response.use(
  (response: AxiosResponse) => {
    // Log da resposta em desenvolvimento
    logResponse(response)
    
    // Limpar cancel token após sucesso
    const requestId = response.config.metadata?.requestId
    if (requestId) {
      cancelTokens.delete(requestId)
    }
    
    return response
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { 
      _retry?: boolean
      _retryCount?: number
      metadata?: { requestId: string }
    }
    
    // Log do erro
    logError(error)
    
    // Limpar cancel token após erro
    const requestId = originalRequest?.metadata?.requestId
    if (requestId) {
      cancelTokens.delete(requestId)
    }
    
    // Se a requisição foi cancelada, não tentar novamente
    if (axios.isCancel(error)) {
      return Promise.reject(error)
    }
    
    // Implementar retry para erros de rede (5xx ou timeout)
    const shouldRetry = (
      error.code === 'ECONNABORTED' || // timeout
      error.code === 'NETWORK_ERROR' ||
      (error.response?.status && error.response.status >= 500)
    ) && (!originalRequest._retryCount || originalRequest._retryCount < 2)
    
    if (shouldRetry && originalRequest) {
      originalRequest._retryCount = (originalRequest._retryCount || 0) + 1
      
      // Delay exponencial: 1s, 2s, 4s
      const delay = Math.pow(2, originalRequest._retryCount - 1) * 1000
      await new Promise(resolve => setTimeout(resolve, delay))
      
      if (IS_DEVELOPMENT) {
        console.log(`🔄 Retrying request (attempt ${originalRequest._retryCount}): ${originalRequest.url}`)
      }
      
      return api(originalRequest)
    }
    
    // Se erro 401 e não é uma requisição de refresh
    if (error.response?.status === 401 && !originalRequest._retry && !originalRequest.url?.includes('/auth/refresh')) {
      if (isRefreshing) {
        // Se já está fazendo refresh, aguarda na fila
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        })
          .then((token) => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${token}`
            }
            return api(originalRequest)
          })
          .catch((err) => Promise.reject(err))
      }

      originalRequest._retry = true
      isRefreshing = true

      const { refreshToken, setTokens, logout } = useAuthStore.getState()

      if (!refreshToken) {
        logout()
        return Promise.reject(error)
      }

      try {
        const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
          refreshToken,
        }, {
          timeout: TIMEOUTS.auth,
        })

        const { accessToken: newAccessToken, refreshToken: newRefreshToken } = response.data
        setTokens(newAccessToken, newRefreshToken)
        
        processQueue(null, newAccessToken)
        
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
        }
        return api(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError as Error, null)
        logout()
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

// Tipos de erro da API
export interface ApiError {
  message: string
  code?: string
  field?: string
  details?: Record<string, string[]>
}

// Tipos de erro categorizados
export enum ErrorCategory {
  NETWORK = 'NETWORK',
  AUTHENTICATION = 'AUTHENTICATION',
  AUTHORIZATION = 'AUTHORIZATION',
  VALIDATION = 'VALIDATION',
  BUSINESS = 'BUSINESS',
  SERVER = 'SERVER',
  TIMEOUT = 'TIMEOUT',
  CANCELLED = 'CANCELLED',
  UNKNOWN = 'UNKNOWN',
}

// Interface para erro categorizado
export interface CategorizedError {
  category: ErrorCategory
  message: string
  originalError: unknown
  retryable: boolean
}

// Função para categorizar erros
export const categorizeError = (error: unknown): CategorizedError => {
  if (axios.isCancel(error)) {
    return {
      category: ErrorCategory.CANCELLED,
      message: 'Requisição cancelada',
      originalError: error,
      retryable: false,
    }
  }

  if (axios.isAxiosError(error)) {
    const status = error.response?.status
    const code = error.code

    // Erros de rede
    if (code === 'ECONNABORTED' || code === 'NETWORK_ERROR' || !status) {
      return {
        category: code === 'ECONNABORTED' ? ErrorCategory.TIMEOUT : ErrorCategory.NETWORK,
        message: code === 'ECONNABORTED' ? 'Tempo limite excedido' : 'Erro de conexão',
        originalError: error,
        retryable: true,
      }
    }

    // Erros por status HTTP
    switch (status) {
      case 401:
        return {
          category: ErrorCategory.AUTHENTICATION,
          message: 'Sessão expirada. Por favor, faça login novamente.',
          originalError: error,
          retryable: false,
        }
      case 403:
        return {
          category: ErrorCategory.AUTHORIZATION,
          message: 'Você não tem permissão para realizar esta ação.',
          originalError: error,
          retryable: false,
        }
      case 422:
        return {
          category: ErrorCategory.VALIDATION,
          message: 'Dados inválidos. Verifique os campos e tente novamente.',
          originalError: error,
          retryable: false,
        }
      case 409:
        return {
          category: ErrorCategory.BUSINESS,
          message: 'Conflito de dados. O recurso já existe.',
          originalError: error,
          retryable: false,
        }
      case 404:
        return {
          category: ErrorCategory.BUSINESS,
          message: 'Recurso não encontrado.',
          originalError: error,
          retryable: false,
        }
      default:
        if (status >= 500) {
          return {
            category: ErrorCategory.SERVER,
            message: 'Erro interno do servidor. Tente novamente mais tarde.',
            originalError: error,
            retryable: true,
          }
        }
    }
  }

  return {
    category: ErrorCategory.UNKNOWN,
    message: error instanceof Error ? error.message : 'Ocorreu um erro inesperado.',
    originalError: error,
    retryable: false,
  }
}

// Helper para extrair mensagem de erro (mantido para compatibilidade)
export const getErrorMessage = (error: unknown): string => {
  const categorized = categorizeError(error)
  
  if (axios.isAxiosError(error)) {
    const apiError = error.response?.data as ApiError
    if (apiError?.message) {
      return apiError.message
    }
  }
  
  return categorized.message
}

// Utilitários para cancelamento de requisições
export const cancelRequest = (requestId: string): boolean => {
  const cancelToken = cancelTokens.get(requestId)
  if (cancelToken) {
    cancelToken.cancel('Requisição cancelada pelo usuário')
    cancelTokens.delete(requestId)
    return true
  }
  return false
}

export const cancelAllRequests = (): number => {
  let cancelledCount = 0
  cancelTokens.forEach((cancelToken) => {
    cancelToken.cancel('Todas as requisições foram canceladas')
    cancelledCount++
  })
  cancelTokens.clear()
  return cancelledCount
}

// Função para verificar status da rede
export const checkNetworkStatus = async (): Promise<boolean> => {
  try {
    await fetch(`${API_BASE_URL}/health`, {
      method: 'HEAD',
      mode: 'no-cors',
    })
    return true
  } catch {
    return navigator.onLine
  }
}

// Hook para monitorar status da rede
export const useNetworkStatus = () => {
  const [isOnline, setIsOnline] = useState(navigator.onLine)

  useEffect(() => {
    const handleOnline = () => setIsOnline(true)
    const handleOffline = () => setIsOnline(false)

    window.addEventListener('online', handleOnline)
    window.addEventListener('offline', handleOffline)

    return () => {
      window.removeEventListener('online', handleOnline)
      window.removeEventListener('offline', handleOffline)
    }
  }, [])

  return isOnline
}

// Services para cada domínio

// Auth Service
export const authService = {
  login: async (email: string, senha: string) => {
    const response = await api.post('/auth/login', { email, senha }, {
      timeout: TIMEOUTS.auth,
    })
    return response.data
  },

  register: async (nome: string, email: string, senha: string) => {
    const response = await api.post('/auth/register', { nome, email, senha }, {
      timeout: TIMEOUTS.auth,
    })
    return response.data
  },

  refresh: async (refreshToken: string) => {
    const response = await api.post('/auth/refresh', { refreshToken }, {
      timeout: TIMEOUTS.auth,
    })
    return response.data
  },

  logout: async () => {
    await api.post('/auth/logout', {}, {
      timeout: TIMEOUTS.auth,
    })
  },

  // Verificar se o token ainda é válido
  validateToken: async () => {
    const response = await api.get('/auth/validate', {
      timeout: TIMEOUTS.auth,
    })
    return response.data
  },
}

// Transaction Service
export const transactionService = {
  list: async (params?: {
    page?: number
    size?: number
    dataInicio?: string
    dataFim?: string
    categoria?: string
    tipo?: 'RECEITA' | 'DESPESA'
    ordenacao?: string
    direcao?: 'asc' | 'desc'
  }) => {
    const response = await api.get('/transacoes', { params })
    return response.data
  },

  getById: async (id: string) => {
    const response = await api.get(`/transacoes/${id}`)
    return response.data
  },

  create: async (data: {
    valor: number
    descricao: string
    categoria: string
    tipo: 'RECEITA' | 'DESPESA'
    data: string
  }) => {
    const response = await api.post('/transacoes', data)
    return response.data
  },

  update: async (
    id: string,
    data: {
      valor: number
      descricao: string
      categoria: string
      tipo: 'RECEITA' | 'DESPESA'
      data: string
    }
  ) => {
    const response = await api.put(`/transacoes/${id}`, data)
    return response.data
  },

  delete: async (id: string) => {
    await api.delete(`/transacoes/${id}`)
  },

  getSummary: async (params?: { mes?: number; ano?: number }) => {
    const response = await api.get('/transacoes/resumo', { params })
    return response.data
  },
}

// Budget Service
export const budgetService = {
  list: async (apenasAtivos = true) => {
    const response = await api.get('/orcamentos', { params: { apenasAtivos } })
    return response.data
  },

  getById: async (id: string) => {
    const response = await api.get(`/orcamentos/${id}`)
    return response.data
  },

  create: async (data: {
    categoria: string
    limite: number
    periodo: 'MENSAL' | 'TRIMESTRAL' | 'ANUAL'
    inicioVigencia: string
  }) => {
    const response = await api.post('/orcamentos', data)
    return response.data
  },

  update: async (
    id: string,
    data: {
      limite: number
    }
  ) => {
    const response = await api.put(`/orcamentos/${id}`, data)
    return response.data
  },

  delete: async (id: string) => {
    await api.delete(`/orcamentos/${id}`)
  },
}

// Goal Service
export const goalService = {
  list: async (apenasAtivas = true, tipo?: string) => {
    const response = await api.get('/metas', { params: { apenasAtivas, tipo } })
    return response.data
  },

  getById: async (id: string) => {
    const response = await api.get(`/metas/${id}`)
    return response.data
  },

  create: async (data: {
    nome: string
    valorAlvo: number
    prazo: string
    tipo: 'RESERVA_EMERGENCIA' | 'VIAGEM' | 'COMPRA' | 'INVESTIMENTO'
  }) => {
    const response = await api.post('/metas', data)
    return response.data
  },

  addProgress: async (id: string, valor: number) => {
    const response = await api.post(`/metas/${id}/progresso`, { valor })
    return response.data
  },

  delete: async (id: string) => {
    await api.delete(`/metas/${id}`)
  },
}

// Import Service
export const importService = {
  uploadFile: async (file: File, onUploadProgress?: (progressEvent: any) => void) => {
    const formData = new FormData()
    formData.append('arquivo', file)
    
    const response = await api.post('/importacao/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      timeout: TIMEOUTS.upload,
      onUploadProgress,
    })
    return response.data
  },

  confirmImport: async (importId: string, transacoes: string[]) => {
    const response = await api.post(`/importacao/${importId}/confirmar`, {
      transacoesConfirmadas: transacoes,
    })
    return response.data
  },

  getImportStatus: async (importId: string) => {
    const response = await api.get(`/importacao/${importId}/status`)
    return response.data
  },
}

// Dashboard Service
export const dashboardService = {
  getSummary: async () => {
    const response = await api.get('/dashboard/resumo')
    return response.data
  },

  getRecentTransactions: async (limit = 10) => {
    const response = await api.get('/transacoes', {
      params: { page: 0, size: limit, ordenacao: 'data', direcao: 'desc' },
    })
    return response.data
  },

  getMonthlyChart: async (year: number) => {
    const response = await api.get('/dashboard/grafico-mensal', {
      params: { ano: year },
    })
    return response.data
  },

  getCategoryChart: async (month?: number, year?: number) => {
    const response = await api.get('/dashboard/grafico-categorias', {
      params: { mes: month, ano: year },
    })
    return response.data
  },
}

// Health Service
export const healthService = {
  check: async () => {
    const response = await api.get('/health', {
      timeout: 5000, // Timeout curto para health check
    })
    return response.data
  },

  ping: async () => {
    const start = Date.now()
    await api.get('/health/ping', {
      timeout: 3000,
    })
    return Date.now() - start // Retorna latência em ms
  },
}

// Utility Service para operações auxiliares
export const utilityService = {
  // Buscar categorias disponíveis
  getCategories: async () => {
    const response = await api.get('/categorias')
    return response.data
  },

  // Buscar configurações do usuário
  getUserSettings: async () => {
    const response = await api.get('/usuario/configuracoes')
    return response.data
  },

  // Atualizar configurações do usuário
  updateUserSettings: async (settings: Record<string, any>) => {
    const response = await api.put('/usuario/configuracoes', settings)
    return response.data
  },

  // Exportar dados
  exportData: async (format: 'csv' | 'excel', filters?: Record<string, any>) => {
    const response = await api.get('/exportacao', {
      params: { formato: format, ...filters },
      responseType: 'blob',
      timeout: TIMEOUTS.download,
    })
    return response.data
  },
}

export default api
