import { describe, it, expect, beforeEach, vi } from 'vitest'
import { useGoalStore } from '../goalStore'
import { goalService } from '@/lib/api'
import type { Goal } from '@/types'

// Mock the API service
vi.mock('@/lib/api', () => ({
  goalService: {
    list: vi.fn(),
    create: vi.fn(),
    addProgress: vi.fn(),
    delete: vi.fn(),
  },
  getErrorMessage: vi.fn((error: any) => error.message || 'An error occurred'),
}))

describe('goalStore', () => {
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
    {
      id: '2',
      usuarioId: 'user1',
      nome: 'Viagem Europa',
      valorAlvo: 15000.00,
      valorAtual: 15000.00,
      prazo: '2024-06-30',
      tipo: 'VIAGEM',
      status: 'CONCLUIDA',
      percentualConclusao: 100,
      criadaEm: '2024-01-01T00:00:00',
    },
  ]

  beforeEach(() => {
    // Reset store state
    useGoalStore.setState({
      goals: [],
      selectedGoal: null,
      isLoading: false,
      error: null,
      showOnlyActive: true,
      filterTipo: null,
    })

    vi.clearAllMocks()
  })

  describe('Initial State', () => {
    it('should have correct initial state', () => {
      const state = useGoalStore.getState()

      expect(state.goals).toEqual([])
      expect(state.selectedGoal).toBeNull()
      expect(state.isLoading).toBe(false)
      expect(state.error).toBeNull()
      expect(state.showOnlyActive).toBe(true)
      expect(state.filterTipo).toBeNull()
    })
  })

  describe('fetchGoals', () => {
    describe('Requirement 6.1: Goal parameter validation', () => {
      it('should fetch goals successfully', async () => {
        vi.mocked(goalService.list).mockResolvedValue(mockGoals)

        await useGoalStore.getState().fetchGoals()

        const state = useGoalStore.getState()
        expect(state.goals).toEqual(mockGoals)
        expect(state.isLoading).toBe(false)
        expect(state.error).toBeNull()
      })

      it('should call API with correct parameters', async () => {
        vi.mocked(goalService.list).mockResolvedValue(mockGoals)

        await useGoalStore.getState().fetchGoals(true, 'VIAGEM')

        expect(goalService.list).toHaveBeenCalledWith(true, 'VIAGEM')
      })

      it('should handle error when fetching fails', async () => {
        const errorMessage = 'Failed to fetch goals'
        vi.mocked(goalService.list).mockRejectedValue(new Error(errorMessage))

        await useGoalStore.getState().fetchGoals()

        const state = useGoalStore.getState()
        expect(state.isLoading).toBe(false)
        expect(state.error).toBe(errorMessage)
      })
    })
  })

  describe('createGoal', () => {
    describe('Requirement 6.1: Goal creation', () => {
      it('should create goal successfully', async () => {
        const newGoal = mockGoals[0]
        vi.mocked(goalService.create).mockResolvedValue(newGoal)
        vi.mocked(goalService.list).mockResolvedValue([newGoal])

        const result = await useGoalStore.getState().createGoal({
          nome: 'Reserva de Emergência',
          valorAlvo: 10000.00,
          prazo: '2024-12-31',
          tipo: 'RESERVA_EMERGENCIA',
        })

        expect(result).toEqual(newGoal)
        expect(goalService.create).toHaveBeenCalled()
      })

      it('should throw error on creation failure', async () => {
        const errorMessage = 'Failed to create goal'
        vi.mocked(goalService.create).mockRejectedValue(new Error(errorMessage))

        await expect(
          useGoalStore.getState().createGoal({
            nome: 'Reserva de Emergência',
            valorAlvo: 10000.00,
            prazo: '2024-12-31',
            tipo: 'RESERVA_EMERGENCIA',
          })
        ).rejects.toThrow(errorMessage)
      })
    })
  })

  describe('Goal Progress Tracking', () => {
    describe('Requirement 6.2, 6.3: Progress tracking and calculation', () => {
      it('should track goal progress percentage', async () => {
        vi.mocked(goalService.list).mockResolvedValue(mockGoals)

        await useGoalStore.getState().fetchGoals()

        const inProgressGoal = useGoalStore.getState().goals.find(
          g => g.status === 'EM_ANDAMENTO'
        )
        expect(inProgressGoal).toBeDefined()
        expect(inProgressGoal?.percentualConclusao).toBe(75)
      })

      it('should track completed goals', async () => {
        vi.mocked(goalService.list).mockResolvedValue(mockGoals)

        await useGoalStore.getState().fetchGoals()

        const completedGoal = useGoalStore.getState().goals.find(
          g => g.status === 'CONCLUIDA'
        )
        expect(completedGoal).toBeDefined()
        expect(completedGoal?.percentualConclusao).toBe(100)
      })

      it('should add progress to goal', async () => {
        const updatedGoal = { ...mockGoals[0], valorAtual: 8500.00, percentualConclusao: 85 }
        vi.mocked(goalService.addProgress).mockResolvedValue(updatedGoal)
        vi.mocked(goalService.list).mockResolvedValue([updatedGoal])

        const result = await useGoalStore.getState().addProgress('1', 1000.00)

        expect(result).toEqual(updatedGoal)
        expect(goalService.addProgress).toHaveBeenCalledWith('1', 1000.00)
      })
    })
  })

  describe('deleteGoal', () => {
    it('should delete goal successfully', async () => {
      vi.mocked(goalService.delete).mockResolvedValue(undefined)
      vi.mocked(goalService.list).mockResolvedValue([])

      await useGoalStore.getState().deleteGoal('1')

      expect(goalService.delete).toHaveBeenCalledWith('1')
    })
  })
})
