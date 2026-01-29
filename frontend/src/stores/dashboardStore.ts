import { create } from 'zustand'
import { dashboardService, budgetService, goalService, getErrorMessage } from '@/lib/api'
import type { DashboardSummary, RecentTransaction, Budget, Goal } from '@/types'

interface DashboardState {
  summary: DashboardSummary | null
  recentTransactions: RecentTransaction[]
  budgetsStatus: Budget[]
  goalsProgress: Goal[]
  isLoading: boolean
  error: string | null

  // Actions
  fetchDashboard: () => Promise<void>
  clearError: () => void
}

export const useDashboardStore = create<DashboardState>((set) => ({
  summary: null,
  recentTransactions: [],
  budgetsStatus: [],
  goalsProgress: [],
  isLoading: false,
  error: null,

  fetchDashboard: async () => {
    set({ isLoading: true, error: null })
    try {
      const [summaryResponse, transactionsResponse, budgetsResponse, goalsResponse] = await Promise.all([
        dashboardService.getSummary(),
        dashboardService.getRecentTransactions(10),
        budgetService.list(true), // apenas ativos
        goalService.list(true), // apenas ativas
      ])

      set({
        summary: summaryResponse,
        recentTransactions: transactionsResponse.content || transactionsResponse,
        budgetsStatus: Array.isArray(budgetsResponse) ? budgetsResponse : budgetsResponse.content || [],
        goalsProgress: Array.isArray(goalsResponse) ? goalsResponse : goalsResponse.content || [],
        isLoading: false,
      })
    } catch (error) {
      set({
        isLoading: false,
        error: getErrorMessage(error),
      })
    }
  },

  clearError: () => {
    set({ error: null })
  },
}))
