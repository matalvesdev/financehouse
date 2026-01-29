import { create } from 'zustand'
import { goalService, getErrorMessage } from '@/lib/api'
import type { Goal, TipoMeta } from '@/types'

interface GoalState {
  goals: Goal[]
  selectedGoal: Goal | null
  isLoading: boolean
  error: string | null
  showOnlyActive: boolean
  filterTipo: TipoMeta | null

  // Actions
  fetchGoals: (onlyActive?: boolean, tipo?: TipoMeta) => Promise<void>
  createGoal: (data: {
    nome: string
    valorAlvo: number
    prazo: string
    tipo: TipoMeta
  }) => Promise<Goal>
  addProgress: (id: string, valor: number) => Promise<Goal>
  deleteGoal: (id: string) => Promise<void>
  setSelectedGoal: (goal: Goal | null) => void
  setShowOnlyActive: (show: boolean) => void
  setFilterTipo: (tipo: TipoMeta | null) => void
  clearError: () => void
}

export const useGoalStore = create<GoalState>((set, get) => ({
  goals: [],
  selectedGoal: null,
  isLoading: false,
  error: null,
  showOnlyActive: true,
  filterTipo: null,

  fetchGoals: async (onlyActive?: boolean, tipo?: TipoMeta) => {
    set({ isLoading: true, error: null })
    try {
      const showActive = onlyActive ?? get().showOnlyActive
      const tipoFilter = tipo ?? get().filterTipo
      const goals = await goalService.list(showActive, tipoFilter || undefined)
      
      set({
        goals,
        isLoading: false,
        showOnlyActive: showActive,
        filterTipo: tipoFilter,
      })
    } catch (error) {
      set({
        isLoading: false,
        error: getErrorMessage(error),
      })
    }
  },

  createGoal: async (data) => {
    set({ isLoading: true, error: null })
    try {
      const goal = await goalService.create(data)
      
      // Atualiza lista
      await get().fetchGoals()
      
      set({ isLoading: false })
      return goal
    } catch (error) {
      const errorMessage = getErrorMessage(error)
      set({ isLoading: false, error: errorMessage })
      throw new Error(errorMessage)
    }
  },

  addProgress: async (id, valor) => {
    set({ isLoading: true, error: null })
    try {
      const goal = await goalService.addProgress(id, valor)
      
      // Atualiza lista
      await get().fetchGoals()
      
      set({ isLoading: false, selectedGoal: null })
      return goal
    } catch (error) {
      const errorMessage = getErrorMessage(error)
      set({ isLoading: false, error: errorMessage })
      throw new Error(errorMessage)
    }
  },

  deleteGoal: async (id) => {
    set({ isLoading: true, error: null })
    try {
      await goalService.delete(id)
      
      // Atualiza lista
      await get().fetchGoals()
      
      set({ isLoading: false })
    } catch (error) {
      const errorMessage = getErrorMessage(error)
      set({ isLoading: false, error: errorMessage })
      throw new Error(errorMessage)
    }
  },

  setSelectedGoal: (goal) => {
    set({ selectedGoal: goal })
  },

  setShowOnlyActive: (show) => {
    set({ showOnlyActive: show })
    get().fetchGoals(show)
  },

  setFilterTipo: (tipo) => {
    set({ filterTipo: tipo })
    get().fetchGoals(get().showOnlyActive, tipo || undefined)
  },

  clearError: () => {
    set({ error: null })
  },
}))
