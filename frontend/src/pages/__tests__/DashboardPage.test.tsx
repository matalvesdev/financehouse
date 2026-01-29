import { render, screen, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { vi } from 'vitest'
import DashboardPage from '../DashboardPage'
import { useDashboardStore } from '@/stores/dashboardStore'
import type { DashboardSummary, RecentTransaction, Budget, Goal } from '@/types'

// Mock the dashboard store
vi.mock('@/stores/dashboardStore')

// Mock recharts to avoid rendering issues in tests
vi.mock('recharts', () => ({
  ResponsiveContainer: ({ children }: any) => <div>{children}</div>,
  BarChart: ({ children }: any) => <div data-testid="bar-chart">{children}</div>,
  Bar: () => <div />,
  XAxis: () => <div />,
  YAxis: () => <div />,
  CartesianGrid: () => <div />,
  Tooltip: () => <div />,
  Legend: () => <div />,
  PieChart: ({ children }: any) => <div data-testid="pie-chart">{children}</div>,
  Pie: () => <div />,
  Cell: () => <div />,
  ComposedChart: ({ children }: any) => <div data-testid="composed-chart">{children}</div>,
  Area: () => <div />,
  AreaChart: ({ children }: any) => <div data-testid="area-chart">{children}</div>,
  Line: () => <div />,
  LineChart: ({ children }: any) => <div data-testid="line-chart">{children}</div>,
  RadialBarChart: ({ children }: any) => <div data-testid="radial-bar-chart">{children}</div>,
  RadialBar: () => <div />,
}))

const mockFetchDashboard = vi.fn()
const mockClearError = vi.fn()

const mockSummary: DashboardSummary = {
  saldoAtual: 5000.00,
  receitaMensal: 8000.00,
  despesaMensal: 3000.00,
  saldoMensal: 5000.00,
  orcamentosAtivos: 3,
  orcamentosExcedidos: 1,
  orcamentosProximoLimite: 1,
  metasAtivas: 2,
  metasConcluidas: 1,
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
  {
    id: '2',
    usuarioId: 'user1',
    categoria: 'TRANSPORTE',
    limite: 500.00,
    gastoAtual: 600.00,
    periodo: 'MENSAL',
    status: 'EXCEDIDO',
    inicioVigencia: '2024-01-01',
    fimVigencia: '2024-01-31',
    percentualGasto: 120,
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
  {
    id: '2',
    usuarioId: 'user1',
    nome: 'Viagem para Europa',
    valorAlvo: 15000.00,
    valorAtual: 15000.00,
    prazo: '2024-06-30',
    tipo: 'VIAGEM',
    status: 'CONCLUIDA',
    percentualConclusao: 100,
    criadaEm: '2024-01-01T00:00:00',
  },
]

const renderDashboardPage = () => {
  return render(
    <BrowserRouter>
      <DashboardPage />
    </BrowserRouter>
  )
}

describe('DashboardPage', () => {
  beforeEach(() => {
    vi.mocked(useDashboardStore).mockReturnValue({
      summary: mockSummary,
      recentTransactions: mockTransactions,
      budgetsStatus: mockBudgets,
      goalsProgress: mockGoals,
      isLoading: false,
      error: null,
      fetchDashboard: mockFetchDashboard,
      clearError: mockClearError,
    })
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  describe('Requirement 4.1: Display current account balance', () => {
    it('should display current balance correctly', () => {
      renderDashboardPage()
      
      expect(screen.getByText('Saldo Atual')).toBeInTheDocument()
      expect(screen.getByText('R$ 5.000,00')).toBeInTheDocument()
    })

    it('should display negative balance in red', () => {
      vi.mocked(useDashboardStore).mockReturnValue({
        summary: { ...mockSummary, saldoAtual: -500.00 },
        recentTransactions: [],
        budgetsStatus: [],
        goalsProgress: [],
        isLoading: false,
        error: null,
        fetchDashboard: mockFetchDashboard,
        clearError: mockClearError,
      })

      renderDashboardPage()
      
      const balanceElement = screen.getByText('R$ -500,00')
      expect(balanceElement).toHaveClass('text-red-600')
    })
  })

  describe('Requirement 4.2: Show monthly income vs expenses comparison', () => {
    it('should display monthly income', () => {
      renderDashboardPage()
      
      expect(screen.getByText('Receita Mensal')).toBeInTheDocument()
      // Use getAllByText since this appears in multiple places
      const incomeElements = screen.getAllByText('+R$ 8.000,00')
      expect(incomeElements.length).toBeGreaterThan(0)
    })

    it('should display monthly expenses', () => {
      renderDashboardPage()
      
      expect(screen.getByText('Despesa Mensal')).toBeInTheDocument()
      // Use getAllByText since this appears in multiple places
      const expenseElements = screen.getAllByText('-R$ 3.000,00')
      expect(expenseElements.length).toBeGreaterThan(0)
    })

    it('should display monthly balance', () => {
      renderDashboardPage()
      
      // Should appear multiple times - in summary card and chart section
      const balanceLabels = screen.getAllByText('Saldo do Mês')
      expect(balanceLabels.length).toBeGreaterThan(0)
      
      const balanceElements = screen.getAllByText('+R$ 5.000,00')
      expect(balanceElements.length).toBeGreaterThan(0)
    })

    it('should render income vs expenses chart', () => {
      renderDashboardPage()
      
      expect(screen.getByTestId('bar-chart')).toBeInTheDocument()
      expect(screen.getByText('Receitas vs Despesas (Mês Atual)')).toBeInTheDocument()
    })
  })

  describe('Requirement 4.3: Display budget status for all active budgets', () => {
    it('should display budget status section', () => {
      renderDashboardPage()
      
      expect(screen.getByText('Status dos Orçamentos')).toBeInTheDocument()
    })

    it('should display budget cards with correct information', () => {
      renderDashboardPage()
      
      // Check for budget categories - use getAllByText since they appear in multiple places
      const alimentacaoElements = screen.getAllByText('Alimentação')
      expect(alimentacaoElements.length).toBeGreaterThan(0)
      
      const transporteElements = screen.getAllByText('Transporte')
      expect(transporteElements.length).toBeGreaterThan(0)
    })

    it('should show budget exceeded alert', () => {
      renderDashboardPage()
      
      expect(screen.getByText('1 orçamento(s) excedido(s)')).toBeInTheDocument()
    })

    it('should show budget near limit alert', () => {
      renderDashboardPage()
      
      expect(screen.getByText('1 orçamento(s) próximo(s) do limite')).toBeInTheDocument()
    })

    it('should display link to manage budgets', () => {
      renderDashboardPage()
      
      const links = screen.getAllByText('Gerenciar orçamentos')
      expect(links.length).toBeGreaterThan(0)
      expect(links[0]).toHaveAttribute('href', '/budgets')
    })
  })

  describe('Requirement 4.4: Show progress on active financial goals', () => {
    it('should display goals progress section', () => {
      renderDashboardPage()
      
      expect(screen.getByText('Progresso das Metas')).toBeInTheDocument()
    })

    it('should display goal cards with correct information', () => {
      renderDashboardPage()
      
      // Use getAllByText since goal names appear in multiple places (title and type)
      const reservaElements = screen.getAllByText('Reserva de Emergência')
      expect(reservaElements.length).toBeGreaterThan(0)
      
      expect(screen.getByText('Viagem para Europa')).toBeInTheDocument()
    })

    it('should display active goals count', () => {
      renderDashboardPage()
      
      expect(screen.getByText('Metas ativas')).toBeInTheDocument()
      // Use getAllByText since "2" appears in multiple places
      const twoElements = screen.getAllByText('2')
      expect(twoElements.length).toBeGreaterThan(0)
    })

    it('should display completed goals count', () => {
      renderDashboardPage()
      
      expect(screen.getByText('Metas concluídas')).toBeInTheDocument()
      expect(screen.getByText('1')).toBeInTheDocument()
    })

    it('should display link to manage goals', () => {
      renderDashboardPage()
      
      const links = screen.getAllByText('Gerenciar metas')
      expect(links.length).toBeGreaterThan(0)
      expect(links[0]).toHaveAttribute('href', '/goals')
    })
  })

  describe('Requirement 4.6: Display recent transactions (last 10) on the dashboard', () => {
    it('should display recent transactions section', () => {
      renderDashboardPage()
      
      expect(screen.getByText('Transações Recentes')).toBeInTheDocument()
    })

    it('should display transaction details', () => {
      renderDashboardPage()
      
      expect(screen.getByText('Supermercado')).toBeInTheDocument()
      expect(screen.getByText('Salário')).toBeInTheDocument()
    })

    it('should display transaction amounts with correct formatting', () => {
      renderDashboardPage()
      
      expect(screen.getByText('-R$ 150,00')).toBeInTheDocument()
      // Use getAllByText since this amount appears in multiple places
      const salaryElements = screen.getAllByText('+R$ 5.000,00')
      expect(salaryElements.length).toBeGreaterThan(0)
    })

    it('should display link to view all transactions', () => {
      renderDashboardPage()
      
      const links = screen.getAllByText('Ver todas')
      expect(links.length).toBeGreaterThan(0)
      expect(links[0]).toHaveAttribute('href', '/transactions')
    })

    it('should show message when no transactions exist', () => {
      vi.mocked(useDashboardStore).mockReturnValue({
        summary: mockSummary,
        recentTransactions: [],
        budgetsStatus: [],
        goalsProgress: [],
        isLoading: false,
        error: null,
        fetchDashboard: mockFetchDashboard,
        clearError: mockClearError,
      })

      renderDashboardPage()
      
      expect(screen.getByText('Nenhuma transação encontrada')).toBeInTheDocument()
    })
  })

  describe('Loading and Error States', () => {
    it('should display loading spinner when loading', () => {
      vi.mocked(useDashboardStore).mockReturnValue({
        summary: null,
        recentTransactions: [],
        budgetsStatus: [],
        goalsProgress: [],
        isLoading: true,
        error: null,
        fetchDashboard: mockFetchDashboard,
        clearError: mockClearError,
      })

      const { container } = renderDashboardPage()
      
      // Check for loading spinner by class
      const spinner = container.querySelector('.animate-spin')
      expect(spinner).toBeInTheDocument()
    })

    it('should display error message when error occurs', () => {
      vi.mocked(useDashboardStore).mockReturnValue({
        summary: null,
        recentTransactions: [],
        budgetsStatus: [],
        goalsProgress: [],
        isLoading: false,
        error: 'Failed to load dashboard data',
        fetchDashboard: mockFetchDashboard,
        clearError: mockClearError,
      })

      renderDashboardPage()
      
      expect(screen.getByText('Failed to load dashboard data')).toBeInTheDocument()
    })

    it('should fetch dashboard data on mount', () => {
      renderDashboardPage()
      
      expect(mockFetchDashboard).toHaveBeenCalledTimes(1)
    })
  })

  describe('Charts and Visualizations', () => {
    it('should render category expenses pie chart', () => {
      renderDashboardPage()
      
      expect(screen.getByTestId('pie-chart')).toBeInTheDocument()
      expect(screen.getByText('Despesas por Categoria')).toBeInTheDocument()
    })

    it('should show message when no expenses for category chart', () => {
      vi.mocked(useDashboardStore).mockReturnValue({
        summary: mockSummary,
        recentTransactions: [],
        budgetsStatus: [],
        goalsProgress: [],
        isLoading: false,
        error: null,
        fetchDashboard: mockFetchDashboard,
        clearError: mockClearError,
      })

      renderDashboardPage()
      
      expect(screen.getByText('Nenhuma despesa registrada')).toBeInTheDocument()
    })
  })

  describe('Navigation Links', () => {
    it('should have link to budgets page', () => {
      renderDashboardPage()
      
      const budgetLinks = screen.getAllByRole('link', { name: /ver/i })
      const budgetLink = budgetLinks.find(link => link.getAttribute('href') === '/budgets')
      expect(budgetLink).toBeInTheDocument()
    })

    it('should have link to transactions page', () => {
      renderDashboardPage()
      
      const transactionLinks = screen.getAllByRole('link', { name: /ver todas/i })
      const transactionLink = transactionLinks.find(link => link.getAttribute('href') === '/transactions')
      expect(transactionLink).toBeInTheDocument()
    })
  })
})
