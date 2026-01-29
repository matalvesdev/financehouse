import { describe, it, expect, beforeEach, vi } from 'vitest'
import { useBudgetStore } from '../budgetStore'
import { budgetService } from '@/lib/api'
import type { Budget } from '@/types'

// Mock the API service
vi.mock('@/lib/api', () => ({
  budgetService: {
    list: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
  },
  getErrorMessage: vi.fn((error: any) => error.message || 'An error occurred'),
}))

describe('budgetStore', () => {
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
    {
      id: '2',
      usuarioId: 'user1',
      categoria: 'TRANSPORTE',
      limite: 500.00,
      gastoAtual: 550.00,
      periodo: 'MENSAL',
      status: 'EXCEDIDO',
      inicioVigencia: '2024-01-01',
      fimVigencia: '2024-01-31',
      percentualGasto: 110,
      criadoEm: '2024-01-01T00:00:00',
    },
  ]

  beforeEach(() => {
    // Reset store state
    useBudgetStore.setState({
      budgets: [],
      selectedBudget: null,
      isLoading: false,
      error: null,
      showOnlyActive: true,
    })

    vi.clearAllMocks()
  })

  describe('Initial State', () => {
    it('should have correct initial state', () => {
      const state = useBudgetStore.getState()

      expect(state.budgets).toEqual([])
      expect(state.selectedBudget).toBeNull()
      expect(state.isLoading).toBe(false)
      expect(state.error).toBeNull()
      expect(state.showOnlyActive).toBe(true)
    })
  })

  describe('fetchBudgets', () => {
    describe('Requirement 5.1: Budget parameter validation', () => {
      it('should fetch budgets successfully', async () => {
        vi.mocked(budgetService.list).mockResolvedValue(mockBudgets)

        await useBudgetStore.getState().fetchBudgets()

        const state = useBudgetStore.getState()
        expect(state.budgets).toEqual(mockBudgets)
        expect(state.isLoading).toBe(false)
        expect(state.error).toBeNull()
      })

      it('should call API with correct parameters', async () => {
        vi.mocked(budgetService.list).mockResolvedValue(mockBudgets)

        await useBudgetStore.getState().fetchBudgets(true)

        expect(budgetService.list).toHaveBeenCalledWith(true)
      })

      it('should handle error when fetching fails', async () => {
        const errorMessage = 'Failed to fetch budgets'
        vi.mocked(budgetService.list).mockRejectedValue(new Error(errorMessage))

        await useBudgetStore.getState().fetchBudgets()

        const state = useBudgetStore.getState()
        expect(state.isLoading).toBe(false)
        expect(state.error).toBe(errorMessage)
      })
    })
  })

  describe('createBudget', () => {
    describe('Requirement 5.1: Budget creation', () => {
      it('should create budget successfully', async () => {
        const newBudget = mockBudgets[0]
        vi.mocked(budgetService.create).mockResolvedValue(newBudget)
        vi.mocked(budgetService.list).mockResolvedValue([newBudget])

        const result = await useBudgetStore.getState().createBudget({
          categoria: 'ALIMENTACAO',
          limite: 1000.00,
          periodo: 'MENSAL',
          inicioVigencia: '2024-01-01',
        })

        expect(result).toEqual(newBudget)
        expect(budgetService.create).toHaveBeenCalled()
      })

      it('should throw error on creation failure', async () => {
        const errorMessage = 'Failed to create budget'
        vi.mocked(budgetService.create).mockRejectedValue(new Error(errorMessage))

        await expect(
          useBudgetStore.getState().createBudget({
            categoria: 'ALIMENTACAO',
            limite: 1000.00,
            periodo: 'MENSAL',
            inicioVigencia: '2024-01-01',
          })
        ).rejects.toThrow(errorMessage)
      })
    })
  })

  describe('Budget Status Tracking', () => {
    describe('Requirement 5.2: Real-time spending tracking', () => {
      it('should track budget near limit (80%)', async () => {
        vi.mocked(budgetService.list).mockResolvedValue(mockBudgets)

        await useBudgetStore.getState().fetchBudgets()

        const nearLimitBudget = useBudgetStore.getState().budgets.find(
          b => b.status === 'PROXIMO_LIMITE'
        )
        expect(nearLimitBudget).toBeDefined()
        expect(nearLimitBudget?.percentualGasto).toBeGreaterThanOrEqual(80)
      })

      it('should track budget exceeded', async () => {
        vi.mocked(budgetService.list).mockResolvedValue(mockBudgets)

        await useBudgetStore.getState().fetchBudgets()

        const exceededBudget = useBudgetStore.getState().budgets.find(
          b => b.status === 'EXCEDIDO'
        )
        expect(exceededBudget).toBeDefined()
        expect(exceededBudget?.percentualGasto).toBeGreaterThan(100)
      })
    })
  })

  describe('deleteBudget', () => {
    it('should delete budget successfully', async () => {
      vi.mocked(budgetService.delete).mockResolvedValue(undefined)
      vi.mocked(budgetService.list).mockResolvedValue([])

      await useBudgetStore.getState().deleteBudget('1')

      expect(budgetService.delete).toHaveBeenCalledWith('1')
    })
  })
})
