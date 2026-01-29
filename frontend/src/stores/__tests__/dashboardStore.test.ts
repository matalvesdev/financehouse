import { describe, it, expect, beforeEach, vi } from 'vitest'
import { useDashboardStore } from '../dashboardStore'
import { dashboardService, budgetService, goalService } from '@/lib/api'
import type { DashboardSummary, RecentTransaction, Budget, Goal } from '@/types'

// Mock the API services
vi.mock('@/lib/api', () => ({
  dashboardService: {
    getSummary: vi.fn(),
    getRecentTransactions: vi.fn(),
  },
  budgetService: {
    list: vi.fn(),
  },
  goalService: {
    list: vi.fn(),
  },
  getErrorMessage: vi.fn((error: any) => error.message || 'An error occurred'),
}))

describe('dashboardStore', () => {
  const mockSummary: DashboardSummary = {
    saldoAtual: 10000.00,
    receitaMensal: 8000.00,
    despesaMensal: 5000.00,
    saldoMensal: 3000.00,
    orcamentosAtivos: 5,
    orcamentosExcedidos: 1,
    orcamentosProximoLimite: 2,
    metasAtivas: 3,
    metasConcluidas: 2,
  }

  const mockTransactions: RecentTransaction[] = [
    {
      id: '1',
      valor: 150.00,
      descricao: 'Supermercado',
      categoria: 'ALIMENTACAO',
      tipo: 'DESPESA',
      data: '2024-01-15',
    },
    {
      id: '2',
      valor: 5000.00,
      descricao: 'Salário',
      categoria: 'SALARIO',
      tipo: 'RECEITA',
      data: '2024-01-01',
    },
  ]

  const mockBudgets: Budget[] = [
    {
      id: '1',
      usuarioId: 'user1',
      categoria: 'ALIMENTACAO',
      limite: 1000.00,
      gastoAtual: 850.00,
      periodo: 'MENSAL',
      status: 'PROXIMO_LIMITE',
      inicioVigencia: '2024-01-01',
      fimVigencia: '2024-01-31',
      percentualGasto: 85,
      criadoEm: '2024-01-01T00:00:00',
    },
  ]

  const mockGoals: Goal[] = [
    {
      id: '1',
      usuarioId: 'user1',
      nome: 'Reserva de Emergência',
      valorAlvo: 10000.00,
      valorAtual: 7500.00,
      prazo: '2024-12-31',
      tipo: 'RESERVA_EMERGENCIA',
      status: 'EM_ANDAMENTO',
      percentualConclusao: 75,
      criadaEm: '2024-01-01T00:00:00',
    },
  ]

  beforeEach(() => {
    // Reset store state
    useDashboardStore.setState({
      summary: null,
      recentTransactions: [],
      budgetsStatus: [],
      goalsProgress: [],
      isLoading: false,
      error: null,
    })

    // Clear all mocks
    vi.clearAllMocks()
  })

  describe('Initial State', () => {
    it('should have correct initial state', () => {
      const state = useDashboardStore.getState()

      expect(state.summary).toBeNull()
      expect(state.recentTransactions).toEqual([])
      expect(state.budgetsStatus).toEqual([])
      expect(state.goalsProgress).toEqual([])
      expect(state.isLoading).toBe(false)
      expect(state.error).toBeNull()
    })
  })

  describe('fetchDashboard', () => {
    describe('Requirement 4.1, 4.2, 4.3, 4.4: Fetch and display dashboard data', () => {
      it('should set loading state when fetching', async () => {
        vi.mocked(dashboardService.getSummary).mockImplementation(
          () => new Promise(() => {}) // Never resolves
        )

        const promise = useDashboardStore.getState().fetchDashboard()

        // Check loading state immediately
        expect(useDashboardStore.getState().isLoading).toBe(true)
        expect(useDashboardStore.getState().error).toBeNull()
      })

      it('should fetch all dashboard data successfully', async () => {
        vi.mocked(dashboardService.getSummary).mockResolvedValue(mockSummary)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue(mockTransactions)
        vi.mocked(budgetService.list).mockResolvedValue(mockBudgets)
        vi.mocked(goalService.list).mockResolvedValue(mockGoals)

        await useDashboardStore.getState().fetchDashboard()

        const state = useDashboardStore.getState()

        expect(state.summary).toEqual(mockSummary)
        expect(state.recentTransactions).toEqual(mockTransactions)
        expect(state.budgetsStatus).toEqual(mockBudgets)
        expect(state.goalsProgress).toEqual(mockGoals)
        expect(state.isLoading).toBe(false)
        expect(state.error).toBeNull()
      })

      it('should call API services with correct parameters', async () => {
        vi.mocked(dashboardService.getSummary).mockResolvedValue(mockSummary)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue(mockTransactions)
        vi.mocked(budgetService.list).mockResolvedValue(mockBudgets)
        vi.mocked(goalService.list).mockResolvedValue(mockGoals)

        await useDashboardStore.getState().fetchDashboard()

        expect(dashboardService.getSummary).toHaveBeenCalledTimes(1)
        expect(dashboardService.getRecentTransactions).toHaveBeenCalledWith(10)
        expect(budgetService.list).toHaveBeenCalledWith(true) // apenas ativos
        expect(goalService.list).toHaveBeenCalledWith(true) // apenas ativas
      })

      it('should handle paginated transaction response', async () => {
        const paginatedResponse = {
          content: mockTransactions,
          totalElements: 2,
          totalPages: 1,
          page: 0,
          size: 10,
          hasNext: false,
          hasPrevious: false,
        }

        vi.mocked(dashboardService.getSummary).mockResolvedValue(mockSummary)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue(paginatedResponse as any)
        vi.mocked(budgetService.list).mockResolvedValue(mockBudgets)
        vi.mocked(goalService.list).mockResolvedValue(mockGoals)

        await useDashboardStore.getState().fetchDashboard()

        const state = useDashboardStore.getState()
        expect(state.recentTransactions).toEqual(mockTransactions)
      })

      it('should handle paginated budget response', async () => {
        const paginatedResponse = {
          content: mockBudgets,
          totalElements: 1,
          totalPages: 1,
          page: 0,
          size: 10,
          hasNext: false,
          hasPrevious: false,
        }

        vi.mocked(dashboardService.getSummary).mockResolvedValue(mockSummary)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue(mockTransactions)
        vi.mocked(budgetService.list).mockResolvedValue(paginatedResponse as any)
        vi.mocked(goalService.list).mockResolvedValue(mockGoals)

        await useDashboardStore.getState().fetchDashboard()

        const state = useDashboardStore.getState()
        expect(state.budgetsStatus).toEqual(mockBudgets)
      })

      it('should handle paginated goal response', async () => {
        const paginatedResponse = {
          content: mockGoals,
          totalElements: 1,
          totalPages: 1,
          page: 0,
          size: 10,
          hasNext: false,
          hasPrevious: false,
        }

        vi.mocked(dashboardService.getSummary).mockResolvedValue(mockSummary)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue(mockTransactions)
        vi.mocked(budgetService.list).mockResolvedValue(mockBudgets)
        vi.mocked(goalService.list).mockResolvedValue(paginatedResponse as any)

        await useDashboardStore.getState().fetchDashboard()

        const state = useDashboardStore.getState()
        expect(state.goalsProgress).toEqual(mockGoals)
      })

      it('should handle error when fetching fails', async () => {
        const errorMessage = 'Failed to fetch dashboard data'
        vi.mocked(dashboardService.getSummary).mockRejectedValue(new Error(errorMessage))

        await useDashboardStore.getState().fetchDashboard()

        const state = useDashboardStore.getState()

        expect(state.isLoading).toBe(false)
        expect(state.error).toBe(errorMessage)
        expect(state.summary).toBeNull()
      })

      it('should clear previous error on new fetch', async () => {
        // Set initial error state
        useDashboardStore.setState({ error: 'Previous error' })

        vi.mocked(dashboardService.getSummary).mockResolvedValue(mockSummary)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue(mockTransactions)
        vi.mocked(budgetService.list).mockResolvedValue(mockBudgets)
        vi.mocked(goalService.list).mockResolvedValue(mockGoals)

        await useDashboardStore.getState().fetchDashboard()

        const state = useDashboardStore.getState()
        expect(state.error).toBeNull()
      })
    })
  })

  describe('clearError', () => {
    it('should clear error state', () => {
      useDashboardStore.setState({ error: 'Some error' })

      useDashboardStore.getState().clearError()

      expect(useDashboardStore.getState().error).toBeNull()
    })

    it('should not affect other state when clearing error', () => {
      useDashboardStore.setState({
        summary: mockSummary,
        recentTransactions: mockTransactions,
        error: 'Some error',
      })

      useDashboardStore.getState().clearError()

      const state = useDashboardStore.getState()
      expect(state.error).toBeNull()
      expect(state.summary).toEqual(mockSummary)
      expect(state.recentTransactions).toEqual(mockTransactions)
    })
  })

  describe('Dashboard Metrics Calculations', () => {
    describe('Requirement 4.1: Current balance display', () => {
      it('should correctly store positive balance', async () => {
        const summaryWithPositiveBalance = {
          ...mockSummary,
          saldoAtual: 15000.00,
        }

        vi.mocked(dashboardService.getSummary).mockResolvedValue(summaryWithPositiveBalance)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue([])
        vi.mocked(budgetService.list).mockResolvedValue([])
        vi.mocked(goalService.list).mockResolvedValue([])

        await useDashboardStore.getState().fetchDashboard()

        expect(useDashboardStore.getState().summary?.saldoAtual).toBe(15000.00)
      })

      it('should correctly store negative balance', async () => {
        const summaryWithNegativeBalance = {
          ...mockSummary,
          saldoAtual: -2500.00,
        }

        vi.mocked(dashboardService.getSummary).mockResolvedValue(summaryWithNegativeBalance)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue([])
        vi.mocked(budgetService.list).mockResolvedValue([])
        vi.mocked(goalService.list).mockResolvedValue([])

        await useDashboardStore.getState().fetchDashboard()

        expect(useDashboardStore.getState().summary?.saldoAtual).toBe(-2500.00)
      })

      it('should correctly store zero balance', async () => {
        const summaryWithZeroBalance = {
          ...mockSummary,
          saldoAtual: 0,
        }

        vi.mocked(dashboardService.getSummary).mockResolvedValue(summaryWithZeroBalance)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue([])
        vi.mocked(budgetService.list).mockResolvedValue([])
        vi.mocked(goalService.list).mockResolvedValue([])

        await useDashboardStore.getState().fetchDashboard()

        expect(useDashboardStore.getState().summary?.saldoAtual).toBe(0)
      })
    })

    describe('Requirement 4.2: Monthly income vs expenses', () => {
      it('should correctly store monthly income', async () => {
        vi.mocked(dashboardService.getSummary).mockResolvedValue(mockSummary)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue([])
        vi.mocked(budgetService.list).mockResolvedValue([])
        vi.mocked(goalService.list).mockResolvedValue([])

        await useDashboardStore.getState().fetchDashboard()

        expect(useDashboardStore.getState().summary?.receitaMensal).toBe(8000.00)
      })

      it('should correctly store monthly expenses', async () => {
        vi.mocked(dashboardService.getSummary).mockResolvedValue(mockSummary)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue([])
        vi.mocked(budgetService.list).mockResolvedValue([])
        vi.mocked(goalService.list).mockResolvedValue([])

        await useDashboardStore.getState().fetchDashboard()

        expect(useDashboardStore.getState().summary?.despesaMensal).toBe(5000.00)
      })

      it('should correctly store monthly balance', async () => {
        vi.mocked(dashboardService.getSummary).mockResolvedValue(mockSummary)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue([])
        vi.mocked(budgetService.list).mockResolvedValue([])
        vi.mocked(goalService.list).mockResolvedValue([])

        await useDashboardStore.getState().fetchDashboard()

        const summary = useDashboardStore.getState().summary
        expect(summary?.saldoMensal).toBe(3000.00)
        // Verify calculation: income - expenses = balance
        expect(summary?.saldoMensal).toBe(summary!.receitaMensal - summary!.despesaMensal)
      })

      it('should handle zero income and expenses', async () => {
        const summaryWithZeros = {
          ...mockSummary,
          receitaMensal: 0,
          despesaMensal: 0,
          saldoMensal: 0,
        }

        vi.mocked(dashboardService.getSummary).mockResolvedValue(summaryWithZeros)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue([])
        vi.mocked(budgetService.list).mockResolvedValue([])
        vi.mocked(goalService.list).mockResolvedValue([])

        await useDashboardStore.getState().fetchDashboard()

        const summary = useDashboardStore.getState().summary
        expect(summary?.receitaMensal).toBe(0)
        expect(summary?.despesaMensal).toBe(0)
        expect(summary?.saldoMensal).toBe(0)
      })
    })

    describe('Requirement 4.3: Budget status metrics', () => {
      it('should correctly store active budgets count', async () => {
        vi.mocked(dashboardService.getSummary).mockResolvedValue(mockSummary)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue([])
        vi.mocked(budgetService.list).mockResolvedValue([])
        vi.mocked(goalService.list).mockResolvedValue([])

        await useDashboardStore.getState().fetchDashboard()

        expect(useDashboardStore.getState().summary?.orcamentosAtivos).toBe(5)
      })

      it('should correctly store exceeded budgets count', async () => {
        vi.mocked(dashboardService.getSummary).mockResolvedValue(mockSummary)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue([])
        vi.mocked(budgetService.list).mockResolvedValue([])
        vi.mocked(goalService.list).mockResolvedValue([])

        await useDashboardStore.getState().fetchDashboard()

        expect(useDashboardStore.getState().summary?.orcamentosExcedidos).toBe(1)
      })

      it('should correctly store near limit budgets count', async () => {
        vi.mocked(dashboardService.getSummary).mockResolvedValue(mockSummary)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue([])
        vi.mocked(budgetService.list).mockResolvedValue([])
        vi.mocked(goalService.list).mockResolvedValue([])

        await useDashboardStore.getState().fetchDashboard()

        expect(useDashboardStore.getState().summary?.orcamentosProximoLimite).toBe(2)
      })

      it('should store budget details for display', async () => {
        vi.mocked(dashboardService.getSummary).mockResolvedValue(mockSummary)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue([])
        vi.mocked(budgetService.list).mockResolvedValue(mockBudgets)
        vi.mocked(goalService.list).mockResolvedValue([])

        await useDashboardStore.getState().fetchDashboard()

        const budgets = useDashboardStore.getState().budgetsStatus
        expect(budgets).toHaveLength(1)
        expect(budgets[0].categoria).toBe('ALIMENTACAO')
        expect(budgets[0].gastoAtual).toBe(850.00)
        expect(budgets[0].limite).toBe(1000.00)
        expect(budgets[0].status).toBe('PROXIMO_LIMITE')
      })
    })

    describe('Requirement 4.4: Goal progress metrics', () => {
      it('should correctly store active goals count', async () => {
        vi.mocked(dashboardService.getSummary).mockResolvedValue(mockSummary)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue([])
        vi.mocked(budgetService.list).mockResolvedValue([])
        vi.mocked(goalService.list).mockResolvedValue([])

        await useDashboardStore.getState().fetchDashboard()

        expect(useDashboardStore.getState().summary?.metasAtivas).toBe(3)
      })

      it('should correctly store completed goals count', async () => {
        vi.mocked(dashboardService.getSummary).mockResolvedValue(mockSummary)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue([])
        vi.mocked(budgetService.list).mockResolvedValue([])
        vi.mocked(goalService.list).mockResolvedValue([])

        await useDashboardStore.getState().fetchDashboard()

        expect(useDashboardStore.getState().summary?.metasConcluidas).toBe(2)
      })

      it('should store goal details for display', async () => {
        vi.mocked(dashboardService.getSummary).mockResolvedValue(mockSummary)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue([])
        vi.mocked(budgetService.list).mockResolvedValue([])
        vi.mocked(goalService.list).mockResolvedValue(mockGoals)

        await useDashboardStore.getState().fetchDashboard()

        const goals = useDashboardStore.getState().goalsProgress
        expect(goals).toHaveLength(1)
        expect(goals[0].nome).toBe('Reserva de Emergência')
        expect(goals[0].valorAtual).toBe(7500.00)
        expect(goals[0].valorAlvo).toBe(10000.00)
        expect(goals[0].percentualConclusao).toBe(75)
      })
    })

    describe('Requirement 4.6: Recent transactions', () => {
      it('should store recent transactions', async () => {
        vi.mocked(dashboardService.getSummary).mockResolvedValue(mockSummary)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue(mockTransactions)
        vi.mocked(budgetService.list).mockResolvedValue([])
        vi.mocked(goalService.list).mockResolvedValue([])

        await useDashboardStore.getState().fetchDashboard()

        const transactions = useDashboardStore.getState().recentTransactions
        expect(transactions).toHaveLength(2)
        expect(transactions[0].descricao).toBe('Supermercado')
        expect(transactions[1].descricao).toBe('Salário')
      })

      it('should handle empty transactions list', async () => {
        vi.mocked(dashboardService.getSummary).mockResolvedValue(mockSummary)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue([])
        vi.mocked(budgetService.list).mockResolvedValue([])
        vi.mocked(goalService.list).mockResolvedValue([])

        await useDashboardStore.getState().fetchDashboard()

        expect(useDashboardStore.getState().recentTransactions).toEqual([])
      })

      it('should limit to 10 recent transactions', async () => {
        vi.mocked(dashboardService.getSummary).mockResolvedValue(mockSummary)
        vi.mocked(dashboardService.getRecentTransactions).mockResolvedValue(mockTransactions)
        vi.mocked(budgetService.list).mockResolvedValue([])
        vi.mocked(goalService.list).mockResolvedValue([])

        await useDashboardStore.getState().fetchDashboard()

        expect(dashboardService.getRecentTransactions).toHaveBeenCalledWith(10)
      })
    })
  })

  describe('Error Handling', () => {
    it('should handle network errors', async () => {
      vi.mocked(dashboardService.getSummary).mockRejectedValue(new Error('Network error'))

      await useDashboardStore.getState().fetchDashboard()

      expect(useDashboardStore.getState().error).toBe('Network error')
      expect(useDashboardStore.getState().isLoading).toBe(false)
    })

    it('should handle API errors', async () => {
      vi.mocked(dashboardService.getSummary).mockRejectedValue(new Error('API error'))

      await useDashboardStore.getState().fetchDashboard()

      expect(useDashboardStore.getState().error).toBe('API error')
    })

    it('should not update data on error', async () => {
      // Set initial data
      useDashboardStore.setState({
        summary: mockSummary,
        recentTransactions: mockTransactions,
      })

      vi.mocked(dashboardService.getSummary).mockRejectedValue(new Error('Error'))

      await useDashboardStore.getState().fetchDashboard()

      // Data should remain unchanged
      expect(useDashboardStore.getState().summary).toEqual(mockSummary)
      expect(useDashboardStore.getState().recentTransactions).toEqual(mockTransactions)
    })
  })
})
