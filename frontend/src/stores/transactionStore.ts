import { create } from 'zustand'
import { transactionService, getErrorMessage } from '@/lib/api'
import type { Transaction, PaginatedResponse, TransactionSummary } from '@/types'

interface TransactionFilters {
  dataInicio?: string
  dataFim?: string
  categoria?: string
  tipo?: 'RECEITA' | 'DESPESA'
  ordenacao?: string
  direcao?: 'asc' | 'desc'
}

interface TransactionState {
  transactions: Transaction[]
  selectedTransaction: Transaction | null
  summary: TransactionSummary | null
  pagination: {
    page: number
    size: number
    totalElements: number
    totalPages: number
    hasNext: boolean
    hasPrevious: boolean
  }
  filters: TransactionFilters
  isLoading: boolean
  error: string | null

  // Actions
  fetchTransactions: (page?: number, filters?: TransactionFilters) => Promise<void>
  fetchSummary: (mes?: number, ano?: number) => Promise<void>
  createTransaction: (data: {
    valor: number
    descricao: string
    categoria: string
    tipo: 'RECEITA' | 'DESPESA'
    data: string
  }) => Promise<Transaction>
  updateTransaction: (id: string, data: {
    valor: number
    descricao: string
    categoria: string
    tipo: 'RECEITA' | 'DESPESA'
    data: string
  }) => Promise<Transaction>
  deleteTransaction: (id: string) => Promise<void>
  setSelectedTransaction: (transaction: Transaction | null) => void
  setFilters: (filters: TransactionFilters) => void
  clearFilters: () => void
  clearError: () => void
}

export const useTransactionStore = create<TransactionState>((set, get) => ({
  transactions: [],
  selectedTransaction: null,
  summary: null,
  pagination: {
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
    hasNext: false,
    hasPrevious: false,
  },
  filters: {},
  isLoading: false,
  error: null,

  fetchTransactions: async (page = 0, filters?: TransactionFilters) => {
    set({ isLoading: true, error: null })
    try {
      const currentFilters = filters || get().filters
      const response: PaginatedResponse<Transaction> = await transactionService.list({
        page,
        size: get().pagination.size,
        ...currentFilters,
      })

      set({
        transactions: response.content,
        pagination: {
          page: response.page,
          size: response.size,
          totalElements: response.totalElements,
          totalPages: response.totalPages,
          hasNext: response.hasNext,
          hasPrevious: response.hasPrevious,
        },
        filters: currentFilters,
        isLoading: false,
      })
    } catch (error) {
      set({
        isLoading: false,
        error: getErrorMessage(error),
      })
    }
  },

  fetchSummary: async (mes?: number, ano?: number) => {
    try {
      const summary = await transactionService.getSummary({ mes, ano })
      set({ summary })
    } catch (error) {
      console.error('Erro ao buscar resumo:', error)
    }
  },

  createTransaction: async (data) => {
    set({ isLoading: true, error: null })
    try {
      const transaction = await transactionService.create(data)
      
      // Atualiza lista e resumo
      await get().fetchTransactions(get().pagination.page)
      await get().fetchSummary()
      
      set({ isLoading: false })
      return transaction
    } catch (error) {
      const errorMessage = getErrorMessage(error)
      set({ isLoading: false, error: errorMessage })
      throw new Error(errorMessage)
    }
  },

  updateTransaction: async (id, data) => {
    set({ isLoading: true, error: null })
    try {
      const transaction = await transactionService.update(id, data)
      
      // Atualiza lista e resumo
      await get().fetchTransactions(get().pagination.page)
      await get().fetchSummary()
      
      set({ isLoading: false, selectedTransaction: null })
      return transaction
    } catch (error) {
      const errorMessage = getErrorMessage(error)
      set({ isLoading: false, error: errorMessage })
      throw new Error(errorMessage)
    }
  },

  deleteTransaction: async (id) => {
    set({ isLoading: true, error: null })
    try {
      await transactionService.delete(id)
      
      // Atualiza lista e resumo
      await get().fetchTransactions(get().pagination.page)
      await get().fetchSummary()
      
      set({ isLoading: false })
    } catch (error) {
      const errorMessage = getErrorMessage(error)
      set({ isLoading: false, error: errorMessage })
      throw new Error(errorMessage)
    }
  },

  setSelectedTransaction: (transaction) => {
    set({ selectedTransaction: transaction })
  },

  setFilters: (filters) => {
    set({ filters })
    get().fetchTransactions(0, filters)
  },

  clearFilters: () => {
    set({ filters: {} })
    get().fetchTransactions(0, {})
  },

  clearError: () => {
    set({ error: null })
  },
}))
