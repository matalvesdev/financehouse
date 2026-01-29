import { render, screen } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { vi } from 'vitest'
import {
  IncomeExpenseChart,
  CategoryChart,
  TrendChart,
  BudgetProgressChart,
  GoalProgressChart,
} from '../DashboardCharts'

// Mock recharts to avoid rendering issues in tests
vi.mock('recharts', () => ({
  ResponsiveContainer: ({ children }: any) => <div data-testid="responsive-container">{children}</div>,
  BarChart: ({ children, data }: any) => (
    <div data-testid="bar-chart" data-chart-data={JSON.stringify(data)}>
      {children}
    </div>
  ),
  Bar: ({ dataKey, fill }: any) => (
    <div data-testid={`bar-${dataKey}`} data-fill={fill} />
  ),
  XAxis: ({ dataKey }: any) => <div data-testid="x-axis" data-key={dataKey} />,
  YAxis: () => <div data-testid="y-axis" />,
  CartesianGrid: () => <div data-testid="cartesian-grid" />,
  Tooltip: ({ content }: any) => <div data-testid="tooltip">{content}</div>,
  Legend: () => <div data-testid="legend" />,
  PieChart: ({ children }: any) => <div data-testid="pie-chart">{children}</div>,
  Pie: ({ data, dataKey }: any) => (
    <div data-testid="pie" data-chart-data={JSON.stringify(data)} data-key={dataKey} />
  ),
  Cell: () => <div data-testid="cell" />,
  ComposedChart: ({ children, data }: any) => (
    <div data-testid="composed-chart" data-chart-data={JSON.stringify(data)}>
      {children}
    </div>
  ),
  Area: ({ dataKey, fill, stroke }: any) => (
    <div data-testid={`area-${dataKey}`} data-fill={fill} data-stroke={stroke} />
  ),
  AreaChart: ({ children }: any) => <div data-testid="area-chart">{children}</div>,
  Line: ({ dataKey, stroke }: any) => (
    <div data-testid={`line-${dataKey}`} data-stroke={stroke} />
  ),
  LineChart: ({ children }: any) => <div data-testid="line-chart">{children}</div>,
  RadialBarChart: ({ children, data }: any) => (
    <div data-testid="radial-bar-chart" data-chart-data={JSON.stringify(data)}>
      {children}
    </div>
  ),
  RadialBar: ({ dataKey }: any) => <div data-testid="radial-bar" data-key={dataKey} />,
}))

describe('IncomeExpenseChart', () => {
  describe('Requirement 4.2: Display monthly income vs expenses', () => {
    it('should render chart title', () => {
      render(
        <IncomeExpenseChart
          receitaMensal={5000}
          despesaMensal={3000}
          saldoMensal={2000}
        />
      )

      expect(screen.getByText('Receitas vs Despesas (Mês Atual)')).toBeInTheDocument()
    })

    it('should render bar chart component', () => {
      render(
        <IncomeExpenseChart
          receitaMensal={5000}
          despesaMensal={3000}
          saldoMensal={2000}
        />
      )

      expect(screen.getByTestId('bar-chart')).toBeInTheDocument()
    })

    it('should display monthly balance below chart', () => {
      render(
        <IncomeExpenseChart
          receitaMensal={5000}
          despesaMensal={3000}
          saldoMensal={2000}
        />
      )

      expect(screen.getByText('Saldo do Mês')).toBeInTheDocument()
      expect(screen.getByText('+R$ 2.000,00')).toBeInTheDocument()
    })

    it('should format positive balance with plus sign and green color', () => {
      render(
        <IncomeExpenseChart
          receitaMensal={5000}
          despesaMensal={3000}
          saldoMensal={2000}
        />
      )

      const balanceElement = screen.getByText('+R$ 2.000,00')
      expect(balanceElement).toHaveClass('text-green-600')
    })

    it('should format negative balance with minus sign and red color', () => {
      render(
        <IncomeExpenseChart
          receitaMensal={2000}
          despesaMensal={3000}
          saldoMensal={-1000}
        />
      )

      const balanceElement = screen.getByText((content, element) => {
        return element?.textContent === 'R$ -1.000,00' || element?.textContent === '-R$ 1.000,00'
      })
      expect(balanceElement).toHaveClass('text-red-600')
    })

    it('should format zero balance correctly', () => {
      render(
        <IncomeExpenseChart
          receitaMensal={3000}
          despesaMensal={3000}
          saldoMensal={0}
        />
      )

      const balanceElement = screen.getByText((content, element) => {
        return element?.textContent === '+R$ 0,00' || element?.textContent === 'R$ 0,00'
      })
      expect(balanceElement).toHaveClass('text-green-600')
    })

    it('should pass correct data to bar chart', () => {
      const { container } = render(
        <IncomeExpenseChart
          receitaMensal={8000}
          despesaMensal={5000}
          saldoMensal={3000}
        />
      )

      const barChart = container.querySelector('[data-testid="bar-chart"]')
      const chartData = barChart?.getAttribute('data-chart-data')
      expect(chartData).toBeTruthy()

      const data = JSON.parse(chartData!)
      expect(data).toEqual([
        {
          name: 'Resumo Mensal',
          Receitas: 8000,
          Despesas: 5000,
        },
      ])
    })

    it('should render income and expense bars', () => {
      render(
        <IncomeExpenseChart
          receitaMensal={5000}
          despesaMensal={3000}
          saldoMensal={2000}
        />
      )

      expect(screen.getByTestId('bar-Receitas')).toBeInTheDocument()
      expect(screen.getByTestId('bar-Despesas')).toBeInTheDocument()
    })

    it('should use correct colors for bars', () => {
      render(
        <IncomeExpenseChart
          receitaMensal={5000}
          despesaMensal={3000}
          saldoMensal={2000}
        />
      )

      const incomeBar = screen.getByTestId('bar-Receitas')
      const expenseBar = screen.getByTestId('bar-Despesas')

      expect(incomeBar).toHaveAttribute('data-fill', '#22C55E') // Green
      expect(expenseBar).toHaveAttribute('data-fill', '#EF4444') // Red
    })
  })
})

describe('CategoryChart', () => {
  const mockCategoryData = [
    { name: 'Alimentação', value: 1500, color: '#EF4444', total: 5000 },
    { name: 'Transporte', value: 800, color: '#F59E0B', total: 5000 },
    { name: 'Moradia', value: 2000, color: '#3B82F6', total: 5000 },
    { name: 'Lazer', value: 700, color: '#EC4899', total: 5000 },
  ]

  describe('Display category expenses', () => {
    it('should render chart title', () => {
      render(<CategoryChart categoryData={mockCategoryData} />)

      expect(screen.getByText('Despesas por Categoria')).toBeInTheDocument()
    })

    it('should render pie chart when data is available', () => {
      render(<CategoryChart categoryData={mockCategoryData} />)

      expect(screen.getByTestId('pie-chart')).toBeInTheDocument()
    })

    it('should pass correct data to pie chart', () => {
      const { container } = render(<CategoryChart categoryData={mockCategoryData} />)

      const pie = container.querySelector('[data-testid="pie"]')
      const chartData = pie?.getAttribute('data-chart-data')
      expect(chartData).toBeTruthy()

      const data = JSON.parse(chartData!)
      expect(data).toEqual(mockCategoryData)
    })

    it('should display category legend', () => {
      render(<CategoryChart categoryData={mockCategoryData} />)

      expect(screen.getByText('Alimentação')).toBeInTheDocument()
      expect(screen.getByText('Transporte')).toBeInTheDocument()
      expect(screen.getByText('Moradia')).toBeInTheDocument()
      expect(screen.getByText('Lazer')).toBeInTheDocument()
    })

    it('should show empty state when no data', () => {
      render(<CategoryChart categoryData={[]} />)

      expect(screen.getByText('Nenhuma despesa registrada')).toBeInTheDocument()
    })

    it('should not render pie chart when no data', () => {
      render(<CategoryChart categoryData={[]} />)

      expect(screen.queryByTestId('pie-chart')).not.toBeInTheDocument()
    })

    it('should render legend with correct colors', () => {
      const { container } = render(<CategoryChart categoryData={mockCategoryData} />)

      const legendItems = container.querySelectorAll('.w-3.h-3.rounded-full')
      expect(legendItems.length).toBe(mockCategoryData.length)

      legendItems.forEach((item, index) => {
        expect(item).toHaveStyle({ backgroundColor: mockCategoryData[index].color })
      })
    })
  })
})

describe('TrendChart', () => {
  const mockTrendData = [
    { mes: 'Jan/24', Receitas: 5000, Despesas: 3000, Saldo: 2000 },
    { mes: 'Fev/24', Receitas: 5500, Despesas: 3200, Saldo: 2300 },
    { mes: 'Mar/24', Receitas: 6000, Despesas: 3500, Saldo: 2500 },
    { mes: 'Abr/24', Receitas: 5800, Despesas: 4000, Saldo: 1800 },
    { mes: 'Mai/24', Receitas: 6200, Despesas: 3800, Saldo: 2400 },
    { mes: 'Jun/24', Receitas: 6500, Despesas: 4200, Saldo: 2300 },
  ]

  describe('Display trend over time', () => {
    it('should render chart title', () => {
      render(<TrendChart trendData={mockTrendData} />)

      expect(
        screen.getByText('Tendência de Receitas e Despesas (Últimos 6 Meses)')
      ).toBeInTheDocument()
    })

    it('should render composed chart when data is available', () => {
      render(<TrendChart trendData={mockTrendData} />)

      expect(screen.getByTestId('composed-chart')).toBeInTheDocument()
    })

    it('should pass correct data to chart', () => {
      const { container } = render(<TrendChart trendData={mockTrendData} />)

      const chart = container.querySelector('[data-testid="composed-chart"]')
      const chartData = chart?.getAttribute('data-chart-data')
      expect(chartData).toBeTruthy()

      const data = JSON.parse(chartData!)
      expect(data).toEqual(mockTrendData)
    })

    it('should render income area', () => {
      render(<TrendChart trendData={mockTrendData} />)

      expect(screen.getByTestId('area-Receitas')).toBeInTheDocument()
    })

    it('should render expense area', () => {
      render(<TrendChart trendData={mockTrendData} />)

      expect(screen.getByTestId('area-Despesas')).toBeInTheDocument()
    })

    it('should render balance line', () => {
      render(<TrendChart trendData={mockTrendData} />)

      expect(screen.getByTestId('line-Saldo')).toBeInTheDocument()
    })

    it('should use correct colors for areas and line', () => {
      render(<TrendChart trendData={mockTrendData} />)

      const incomeArea = screen.getByTestId('area-Receitas')
      const expenseArea = screen.getByTestId('area-Despesas')
      const balanceLine = screen.getByTestId('line-Saldo')

      expect(incomeArea).toHaveAttribute('data-stroke', '#22C55E') // Green
      expect(expenseArea).toHaveAttribute('data-stroke', '#EF4444') // Red
      expect(balanceLine).toHaveAttribute('data-stroke', '#3B82F6') // Blue
    })

    it('should show empty state when no data', () => {
      render(<TrendChart trendData={[]} />)

      expect(screen.getByText('Dados insuficientes para exibir tendência')).toBeInTheDocument()
    })

    it('should not render chart when no data', () => {
      render(<TrendChart trendData={[]} />)

      expect(screen.queryByTestId('composed-chart')).not.toBeInTheDocument()
    })
  })
})

describe('BudgetProgressChart', () => {
  const mockBudgetData = [
    {
      categoria: 'Alimentação',
      Gasto: 850,
      Limite: 1000,
      Disponível: 150,
      percentual: 85,
    },
    {
      categoria: 'Transporte',
      Gasto: 600,
      Limite: 500,
      Disponível: 0,
      percentual: 120,
    },
    {
      categoria: 'Moradia',
      Gasto: 1500,
      Limite: 2000,
      Disponível: 500,
      percentual: 75,
    },
  ]

  describe('Requirement 4.3: Display budget progress', () => {
    it('should render chart title', () => {
      render(
        <BrowserRouter>
          <BudgetProgressChart budgetChartData={mockBudgetData} />
        </BrowserRouter>
      )

      expect(screen.getByText('Progresso dos Orçamentos')).toBeInTheDocument()
    })

    it('should render link to budgets page', () => {
      render(
        <BrowserRouter>
          <BudgetProgressChart budgetChartData={mockBudgetData} />
        </BrowserRouter>
      )

      const link = screen.getByText('Ver todos')
      expect(link).toHaveAttribute('href', '/budgets')
    })

    it('should render bar chart when data is available', () => {
      render(
        <BrowserRouter>
          <BudgetProgressChart budgetChartData={mockBudgetData} />
        </BrowserRouter>
      )

      expect(screen.getByTestId('bar-chart')).toBeInTheDocument()
    })

    it('should pass correct data to chart', () => {
      const { container } = render(
        <BrowserRouter>
          <BudgetProgressChart budgetChartData={mockBudgetData} />
        </BrowserRouter>
      )

      const chart = container.querySelector('[data-testid="bar-chart"]')
      const chartData = chart?.getAttribute('data-chart-data')
      expect(chartData).toBeTruthy()

      const data = JSON.parse(chartData!)
      expect(data).toEqual(mockBudgetData)
    })

    it('should render spent and available bars', () => {
      render(
        <BrowserRouter>
          <BudgetProgressChart budgetChartData={mockBudgetData} />
        </BrowserRouter>
      )

      expect(screen.getByTestId('bar-Gasto')).toBeInTheDocument()
      expect(screen.getByTestId('bar-Disponível')).toBeInTheDocument()
    })

    it('should use correct colors for bars', () => {
      render(
        <BrowserRouter>
          <BudgetProgressChart budgetChartData={mockBudgetData} />
        </BrowserRouter>
      )

      const spentBar = screen.getByTestId('bar-Gasto')
      const availableBar = screen.getByTestId('bar-Disponível')

      expect(spentBar).toHaveAttribute('data-fill', '#EF4444') // Red
      expect(availableBar).toHaveAttribute('data-fill', '#22C55E') // Green
    })

    it('should show empty state when no budgets', () => {
      render(
        <BrowserRouter>
          <BudgetProgressChart budgetChartData={[]} />
        </BrowserRouter>
      )

      expect(screen.getByText('Nenhum orçamento ativo')).toBeInTheDocument()
      expect(screen.getByText('Criar primeiro orçamento')).toBeInTheDocument()
    })

    it('should have link to create budget in empty state', () => {
      render(
        <BrowserRouter>
          <BudgetProgressChart budgetChartData={[]} />
        </BrowserRouter>
      )

      const link = screen.getByText('Criar primeiro orçamento')
      expect(link).toHaveAttribute('href', '/budgets')
    })

    it('should not render chart when no data', () => {
      render(
        <BrowserRouter>
          <BudgetProgressChart budgetChartData={[]} />
        </BrowserRouter>
      )

      expect(screen.queryByTestId('bar-chart')).not.toBeInTheDocument()
    })
  })
})

describe('GoalProgressChart', () => {
  const mockGoalData = [
    {
      nome: 'Reserva de Emergência',
      percentual: 75,
      valor: 7500,
      meta: 10000,
      fill: '#22C55E',
    },
    {
      nome: 'Viagem',
      percentual: 50,
      valor: 5000,
      meta: 10000,
      fill: '#F59E0B',
    },
    {
      nome: 'Carro Novo',
      percentual: 30,
      valor: 15000,
      meta: 50000,
      fill: '#3B82F6',
    },
  ]

  describe('Requirement 4.4: Display goal progress', () => {
    it('should render chart title', () => {
      render(
        <BrowserRouter>
          <GoalProgressChart goalChartData={mockGoalData} />
        </BrowserRouter>
      )

      expect(screen.getByText('Progresso das Metas')).toBeInTheDocument()
    })

    it('should render link to goals page', () => {
      render(
        <BrowserRouter>
          <GoalProgressChart goalChartData={mockGoalData} />
        </BrowserRouter>
      )

      const link = screen.getByText('Ver todas')
      expect(link).toHaveAttribute('href', '/goals')
    })

    it('should render radial bar chart when data is available', () => {
      render(
        <BrowserRouter>
          <GoalProgressChart goalChartData={mockGoalData} />
        </BrowserRouter>
      )

      expect(screen.getByTestId('radial-bar-chart')).toBeInTheDocument()
    })

    it('should pass correct data to chart', () => {
      const { container } = render(
        <BrowserRouter>
          <GoalProgressChart goalChartData={mockGoalData} />
        </BrowserRouter>
      )

      const chart = container.querySelector('[data-testid="radial-bar-chart"]')
      const chartData = chart?.getAttribute('data-chart-data')
      expect(chartData).toBeTruthy()

      const data = JSON.parse(chartData!)
      expect(data).toEqual(mockGoalData)
    })

    it('should render radial bar with percentage data', () => {
      render(
        <BrowserRouter>
          <GoalProgressChart goalChartData={mockGoalData} />
        </BrowserRouter>
      )

      const radialBar = screen.getByTestId('radial-bar')
      expect(radialBar).toHaveAttribute('data-key', 'percentual')
    })

    it('should show empty state when no goals', () => {
      render(
        <BrowserRouter>
          <GoalProgressChart goalChartData={[]} />
        </BrowserRouter>
      )

      expect(screen.getByText('Nenhuma meta ativa')).toBeInTheDocument()
      expect(screen.getByText('Criar primeira meta')).toBeInTheDocument()
    })

    it('should have link to create goal in empty state', () => {
      render(
        <BrowserRouter>
          <GoalProgressChart goalChartData={[]} />
        </BrowserRouter>
      )

      const link = screen.getByText('Criar primeira meta')
      expect(link).toHaveAttribute('href', '/goals')
    })

    it('should not render chart when no data', () => {
      render(
        <BrowserRouter>
          <GoalProgressChart goalChartData={[]} />
        </BrowserRouter>
      )

      expect(screen.queryByTestId('radial-bar-chart')).not.toBeInTheDocument()
    })
  })
})

describe('Chart Calculations and Data Processing', () => {
  describe('Income vs Expense calculations', () => {
    it('should correctly calculate positive balance', () => {
      const receita = 8000
      const despesa = 5000
      const saldo = receita - despesa

      render(
        <IncomeExpenseChart
          receitaMensal={receita}
          despesaMensal={despesa}
          saldoMensal={saldo}
        />
      )

      expect(screen.getByText('+R$ 3.000,00')).toBeInTheDocument()
    })

    it('should correctly calculate negative balance', () => {
      const receita = 3000
      const despesa = 5000
      const saldo = receita - despesa

      render(
        <IncomeExpenseChart
          receitaMensal={receita}
          despesaMensal={despesa}
          saldoMensal={saldo}
        />
      )

      const balanceElement = screen.getByText((content, element) => {
        return element?.textContent === 'R$ -2.000,00' || element?.textContent === '-R$ 2.000,00'
      })
      expect(balanceElement).toBeInTheDocument()
    })

    it('should handle zero values', () => {
      render(
        <IncomeExpenseChart receitaMensal={0} despesaMensal={0} saldoMensal={0} />
      )

      const balanceElement = screen.getByText((content, element) => {
        return element?.textContent === '+R$ 0,00' || element?.textContent === 'R$ 0,00'
      })
      expect(balanceElement).toBeInTheDocument()
    })

    it('should handle large numbers', () => {
      render(
        <IncomeExpenseChart
          receitaMensal={100000}
          despesaMensal={75000}
          saldoMensal={25000}
        />
      )

      expect(screen.getByText('+R$ 25.000,00')).toBeInTheDocument()
    })

    it('should handle decimal values correctly', () => {
      render(
        <IncomeExpenseChart
          receitaMensal={5432.67}
          despesaMensal={3210.45}
          saldoMensal={2222.22}
        />
      )

      expect(screen.getByText('+R$ 2.222,22')).toBeInTheDocument()
    })
  })

  describe('Budget progress calculations', () => {
    it('should correctly calculate available amount', () => {
      const budgetData = [
        {
          categoria: 'Alimentação',
          Gasto: 750,
          Limite: 1000,
          Disponível: 250,
          percentual: 75,
        },
      ]

      const { container } = render(
        <BrowserRouter>
          <BudgetProgressChart budgetChartData={budgetData} />
        </BrowserRouter>
      )

      const chart = container.querySelector('[data-testid="bar-chart"]')
      const chartData = chart?.getAttribute('data-chart-data')
      const data = JSON.parse(chartData!)

      expect(data[0].Disponível).toBe(250)
    })

    it('should handle exceeded budget (zero available)', () => {
      const budgetData = [
        {
          categoria: 'Transporte',
          Gasto: 600,
          Limite: 500,
          Disponível: 0,
          percentual: 120,
        },
      ]

      const { container } = render(
        <BrowserRouter>
          <BudgetProgressChart budgetChartData={budgetData} />
        </BrowserRouter>
      )

      const chart = container.querySelector('[data-testid="bar-chart"]')
      const chartData = chart?.getAttribute('data-chart-data')
      const data = JSON.parse(chartData!)

      expect(data[0].Disponível).toBe(0)
    })
  })

  describe('Goal progress calculations', () => {
    it('should correctly represent completion percentage', () => {
      const goalData = [
        {
          nome: 'Meta Teste',
          percentual: 66.67,
          valor: 6667,
          meta: 10000,
          fill: '#22C55E',
        },
      ]

      const { container } = render(
        <BrowserRouter>
          <GoalProgressChart goalChartData={goalData} />
        </BrowserRouter>
      )

      const chart = container.querySelector('[data-testid="radial-bar-chart"]')
      const chartData = chart?.getAttribute('data-chart-data')
      const data = JSON.parse(chartData!)

      expect(data[0].percentual).toBe(66.67)
    })

    it('should handle 100% completion', () => {
      const goalData = [
        {
          nome: 'Meta Completa',
          percentual: 100,
          valor: 10000,
          meta: 10000,
          fill: '#22C55E',
        },
      ]

      const { container } = render(
        <BrowserRouter>
          <GoalProgressChart goalChartData={goalData} />
        </BrowserRouter>
      )

      const chart = container.querySelector('[data-testid="radial-bar-chart"]')
      const chartData = chart?.getAttribute('data-chart-data')
      const data = JSON.parse(chartData!)

      expect(data[0].percentual).toBe(100)
    })
  })
})
