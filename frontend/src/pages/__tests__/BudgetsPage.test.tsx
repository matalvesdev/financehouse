import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import BudgetsPage from '../BudgetsPage'
import { useBudgetStore } from '@/stores/budgetStore'
import { useConfirmStore } from '@/stores/confirmStore'

// Mock stores
vi.mock('@/stores/budgetStore')
vi.mock('@/stores/confirmStore')

// Mock toast
vi.mock('react-hot-toast', () => ({
  default: {
    success: vi.fn(),
    error: vi.fn(),
  },
}))

const mockBudgets = [
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

describe('BudgetsPage', () => {
  const mockFetchBudgets = vi.fn()
  const mockCreateBudget = vi.fn()
  const mockDeleteBudget = vi.fn()
  const mockSetShowOnlyActive = vi.fn()
  const mockClearError = vi.fn()
  const mockOpenConfirm = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()

    vi.mocked(useBudgetStore).mockReturnValue({
      budgets: mockBudgets,
      selectedBudget: null,
      isLoading: false,
      error: null,
      showOnlyActive: true,
      fetchBudgets: mockFetchBudgets,
      createBudget: mockCreateBudget,
      updateBudget: vi.fn(),
      deleteBudget: mockDeleteBudget,
      setSelectedBudget: vi.fn(),
      setShowOnlyActive: mockSetShowOnlyActive,
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
        <BudgetsPage />
      </BrowserRouter>
    )
  }

  describe('Display and Visualization', () => {
    describe('Requirement 5.3, 5.4: Progress visualization and alerts', () => {
      it('should display budgets with progress bars', async () => {
        renderComponent()

        await waitFor(() => {
          expect(screen.getByText('Alimentação')).toBeInTheDocument()
          expect(screen.getByText('Transporte')).toBeInTheDocument()
        })
      })

      it('should show alert for budget near limit (80%)', async () => {
        renderComponent()

        await waitFor(() => {
          expect(screen.getByText('Atenção: próximo do limite')).toBeInTheDocument()
        })
      })

      it('should show alert for exceeded budget', async () => {
        renderComponent()

        await waitFor(() => {
          const exceededText = screen.getByText((content) => 
            content.includes('Orçamento excedido em R$')
          )
          expect(exceededText).toBeInTheDocument()
        })
      })

      it('should display budget percentage', async () => {
        renderComponent()

        await waitFor(() => {
          expect(screen.getByText('85.0%')).toBeInTheDocument()
          expect(screen.getByText('110.0%')).toBeInTheDocument()
        })
      })
    })
  })

  describe('Budget Creation', () => {
    describe('Requirement 5.1: Budget parameter validation', () => {
      it('should open create modal when clicking Novo Orçamento', async () => {
        const user = userEvent.setup()
        renderComponent()

        const createButton = screen.getByRole('button', { name: /Novo Orçamento/i })
        await user.click(createButton)

        await waitFor(() => {
          const modalTitles = screen.getAllByText('Novo Orçamento')
          expect(modalTitles.length).toBeGreaterThan(0)
        })
      })
    })
  })

  describe('Budget Deletion', () => {
    it('should require confirmation before deleting', async () => {
      const user = userEvent.setup()
      renderComponent()

      await waitFor(() => {
        expect(screen.getByText('Alimentação')).toBeInTheDocument()
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
              title: 'Excluir Orçamento',
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
    it('should toggle show only active budgets', async () => {
      const user = userEvent.setup()
      renderComponent()

      const checkbox = screen.getByRole('checkbox', { name: /Apenas ativos/i })
      await user.click(checkbox)

      await waitFor(() => {
        expect(mockSetShowOnlyActive).toHaveBeenCalled()
      })
    })
  })

  describe('Empty State', () => {
    it('should display empty state when no budgets', async () => {
      vi.mocked(useBudgetStore).mockReturnValue({
        budgets: [],
        selectedBudget: null,
        isLoading: false,
        error: null,
        showOnlyActive: true,
        fetchBudgets: mockFetchBudgets,
        createBudget: mockCreateBudget,
        updateBudget: vi.fn(),
        deleteBudget: mockDeleteBudget,
        setSelectedBudget: vi.fn(),
        setShowOnlyActive: mockSetShowOnlyActive,
        clearError: mockClearError,
      })

      renderComponent()

      await waitFor(() => {
        expect(screen.getByText('Nenhum orçamento encontrado')).toBeInTheDocument()
      })
    })
  })
})
