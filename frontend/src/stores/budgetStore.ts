import { create } from 'zustand'
import { budgetService, getErrorMessage } from '@/lib/api'
import type { Budget } from '@/types'

interface BudgetState {
  budgets: Budget[]
  selectedBudget: Budget | null
  isLoading: boolean
  error: string | null
  showOnlyActive: boolean

  // Actions
  fetchBudgets: (onlyActive?: boolean) => Promise<void>
  createBudget: (data: {
    categoria: string
    limite: number
    periodo: 'MENSAL' | 'TRIMESTRAL' | 'ANUAL'
    inicioVigencia: string
  }) => Promise<Budget>
  updateBudget: (id: string, data: { limite: number }) => Promise<Budget>
  deleteBudget: (id: string) => Promise<void>
  setSelectedBudget: (budget: Budget | null) => void
  setShowOnlyActive: (show: boolean) => void
  clearError: () => void
}

export const useBudgetStore = create<BudgetState>((set, get) => ({
  budgets: [],
  selectedBudget: null,
  isLoading: false,
  error: null,
  showOnlyActive: true,

  fetchBudgets: async (onlyActive?: boolean) => {
    set({ isLoading: true, error: null })
    try {
      const showActive = onlyActive ?? get().showOnlyActive
      const budgets = await budgetService.list(showActive)
      
      set({
        budgets,
        isLoading: false,
        showOnlyActive: showActive,
      })
    } catch (error) {
      set({
        isLoading: false,
        error: getErrorMessage(error),
      })
    }
  },

  createBudget: async (data) => {
    set({ isLoading: true, error: null })
    try {
      const budget = await budgetService.create(data)
      
      // Atualiza lista
      await get().fetchBudgets()
      
      set({ isLoading: false })
      return budget
    } catch (error) {
      const errorMessage = getErrorMessage(error)
      set({ isLoading: false, error: errorMessage })
      throw new Error(errorMessage)
    }
  },

  updateBudget: async (id, data) => {
    set({ isLoading: true, error: null })
    try {
      const budget = await budgetService.update(id, data)
      
      // Atualiza lista
      await get().fetchBudgets()
      
      set({ isLoading: false, selectedBudget: null })
      return budget
    } catch (error) {
      const errorMessage = getErrorMessage(error)
      set({ isLoading: false, error: errorMessage })
      throw new Error(errorMessage)
    }
  },

  deleteBudget: async (id) => {
    set({ isLoading: true, error: null })
    try {
      await budgetService.delete(id)
      
      // Atualiza lista
      await get().fetchBudgets()
      
      set({ isLoading: false })
    } catch (error) {
      const errorMessage = getErrorMessage(error)
      set({ isLoading: false, error: errorMessage })
      throw new Error(errorMessage)
    }
  },

  setSelectedBudget: (budget) => {
    set({ selectedBudget: budget })
  },

  setShowOnlyActive: (show) => {
    set({ showOnlyActive: show })
    get().fetchBudgets(show)
  },

  clearError: () => {
    set({ error: null })
  },
}))
