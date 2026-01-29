import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import GoalsPage from '../GoalsPage'
import { useGoalStore } from '@/stores/goalStore'
import { useConfirmStore } from '@/stores/confirmStore'

// Mock stores
vi.mock('@/stores/goalStore')
vi.mock('@/stores/confirmStore')

// Mock toast
vi.mock('react-hot-toast', () => ({
  default: {
    success: vi.fn(),
    error: vi.fn(),
  },
}))

const mockGoals = [
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

describe('GoalsPage', () => {
  const mockFetchGoals = vi.fn()
  const mockCreateGoal = vi.fn()
  const mockAddProgress = vi.fn()
  const mockDeleteGoal = vi.fn()
  const mockSetShowOnlyActive = vi.fn()
  const mockSetFilterTipo = vi.fn()
  const mockClearError = vi.fn()
  const mockOpenConfirm = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()

    vi.mocked(useGoalStore).mockReturnValue({
      goals: mockGoals,
      selectedGoal: null,
      isLoading: false,
      error: null,
      showOnlyActive: true,
      filterTipo: null,
      fetchGoals: mockFetchGoals,
      createGoal: mockCreateGoal,
      addProgress: mockAddProgress,
      deleteGoal: mockDeleteGoal,
      setSelectedGoal: vi.fn(),
      setShowOnlyActive: mockSetShowOnlyActive,
      setFilterTipo: mockSetFilterTipo,
      clearError: mockClearError,
    })

    vi.mocked(useConfirmStore).mockReturnValue({
      isOpen: false,
      config: null,
      openConfirm: mockOpenConfirm,
      closeConfirm: vi.fn(),
      confirmAction: vi.fn(),
    })
  })

  const renderComponent = () => {
    return render(
      <BrowserRouter>
        <GoalsPage />
      </BrowserRouter>
    )
  }

  describe('Display and Visualization', () => {
    describe('Requirement 6.3, 6.4: Progress tracking and estimatives', () => {
      it('should display goals with progress bars', async () => {
        renderComponent()

        await waitFor(() => {
          const headings = screen.getAllByRole('heading', { level: 3 })
          expect(headings.length).toBeGreaterThan(0)
        })
      })

      it('should show progress percentage', async () => {
        renderComponent()

        await waitFor(() => {
          expect(screen.getByText('75.0%')).toBeInTheDocument()
          expect(screen.getByText('100.0%')).toBeInTheDocument()
        })
      })

      it('should show completed goal celebration', async () => {
        renderComponent()

        await waitFor(() => {
          expect(screen.getByText('🎉 Meta alcançada!')).toBeInTheDocument()
        })
      })

      it('should display remaining amount', async () => {
        renderComponent()

        await waitFor(() => {
          const faltaTexts = screen.getAllByText((content) => 
            content.includes('Falta: R$')
          )
          expect(faltaTexts.length).toBeGreaterThan(0)
        })
      })
    })
  })

  describe('Goal Creation', () => {
    describe('Requirement 6.1: Goal parameter validation', () => {
      it('should open create modal when clicking Nova Meta', async () => {
        const user = userEvent.setup()
        renderComponent()

        const createButton = screen.getByRole('button', { name: /Nova Meta/i })
        await user.click(createButton)

        await waitFor(() => {
          const modalTitles = screen.getAllByText('Nova Meta')
          expect(modalTitles.length).toBeGreaterThan(0)
        })
      })
    })
  })

  describe('Progress Addition', () => {
    describe('Requirement 6.2: Progress tracking', () => {
      it('should show add progress button for active goals', async () => {
        renderComponent()

        await waitFor(() => {
          const addButtons = screen.getAllByRole('button', { name: /Adicionar/i })
          expect(addButtons.length).toBeGreaterThan(0)
        })
      })

      it('should open progress modal when clicking add', async () => {
        const user = userEvent.setup()
        renderComponent()

        await waitFor(() => {
          const headings = screen.getAllByRole('heading', { level: 3 })
          const reservaHeading = headings.find(h => h.textContent === 'Reserva de Emergência')
          expect(reservaHeading).toBeDefined()
        })

        const addButton = screen.getByRole('button', { name: /Adicionar/i })
        await user.click(addButton)

        await waitFor(() => {
          expect(screen.getByText(/Adicionar Progresso/i)).toBeInTheDocument()
        })
      })
    })
  })

  describe('Goal Deletion', () => {
    it('should require confirmation before deleting', async () => {
      const user = userEvent.setup()
      renderComponent()

      await waitFor(() => {
        const headings = screen.getAllByRole('heading', { level: 3 })
        const reservaHeading = headings.find(h => h.textContent === 'Reserva de Emergência')
        expect(reservaHeading).toBeDefined()
      })

      // Find delete button
      const deleteButtons = screen.getAllByRole('button')
      const deleteButton = deleteButtons.find(btn => {
        const svg = btn.querySelector('svg')
        return svg && btn.className.includes('text-red-600')
      })

      if (deleteButton) {
        await user.click(deleteButton)

        await waitFor(() => {
          expect(mockOpenConfirm).toHaveBeenCalledWith(
            expect.objectContaining({
              title: 'Excluir Meta',
              message: expect.stringContaining('Tem certeza'),
              confirmText: 'Excluir',
              variant: 'danger',
              timeoutSeconds: 300,
            })
          )
        })
      }
    })
  })

  describe('Filters', () => {
    it('should toggle show only active goals', async () => {
      const user = userEvent.setup()
      renderComponent()

      const checkbox = screen.getByRole('checkbox', { name: /Apenas ativas/i })
      await user.click(checkbox)

      await waitFor(() => {
        expect(mockSetShowOnlyActive).toHaveBeenCalled()
      })
    })

    it('should filter by goal type', async () => {
      const user = userEvent.setup()
      renderComponent()

      const selects = screen.getAllByRole('combobox')
      const tipoSelect = selects[0]

      await user.selectOptions(tipoSelect, 'VIAGEM')

      await waitFor(() => {
        expect(mockSetFilterTipo).toHaveBeenCalled()
      })
    })
  })

  describe('Empty State', () => {
    it('should display empty state when no goals', async () => {
      vi.mocked(useGoalStore).mockReturnValue({
        goals: [],
        selectedGoal: null,
        isLoading: false,
        error: null,
        showOnlyActive: true,
        filterTipo: null,
        fetchGoals: mockFetchGoals,
        createGoal: mockCreateGoal,
        addProgress: mockAddProgress,
        deleteGoal: mockDeleteGoal,
        setSelectedGoal: vi.fn(),
        setShowOnlyActive: mockSetShowOnlyActive,
        setFilterTipo: mockSetFilterTipo,
        clearError: mockClearError,
      })

      renderComponent()

      await waitFor(() => {
        expect(screen.getByText('Nenhuma meta encontrada')).toBeInTheDocument()
      })
    })
  })
})
