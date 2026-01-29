import { render, screen } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { vi } from 'vitest'
import { BudgetStatusCard, BudgetStatusList } from '../BudgetStatusCard'
import type { Budget } from '@/types'

const mockBudgetActive: Budget = {
  id: '1',
  usuarioId: 'user1',
  categoria: 'ALIMENTACAO',
  limite: 1000.00,
  gastoAtual: 500.00,
  periodo: 'MENSAL',
  status: 'ATIVO',
  inicioVigencia: '2024-01-01',
  fimVigencia: '2024-01-31',
  percentualGasto: 50,
  criadoEm: '2024-01-01T00:00:00',
}

const mockBudgetNearLimit: Budget = {
  id: '2',
  usuarioId: 'user1',
  categoria: 'TRANSPORTE',
  limite: 500.00,
  gastoAtual: 450.00,
  periodo: 'MENSAL',
  status: 'PROXIMO_LIMITE',
  inicioVigencia: '2024-01-01',
  fimVigencia: '2024-01-31',
  percentualGasto: 90,
  criadoEm: '2024-01-01T00:00:00',
}

const mockBudgetExceeded: Budget = {
  id: '3',
  usuarioId: 'user1',
  categoria: 'LAZER',
  limite: 300.00,
  gastoAtual: 350.00,
  periodo: 'MENSAL',
  status: 'EXCEDIDO',
  inicioVigencia: '2024-01-01',
  fimVigencia: '2024-01-31',
  percentualGasto: 116.67,
  criadoEm: '2024-01-01T00:00:00',
}

const renderBudgetCard = (budget: Budget) => {
  return render(
    <BrowserRouter>
      <BudgetStatusCard budget={budget} />
    </BrowserRouter>
  )
}

const renderBudgetList = (budgets: Budget[], maxDisplay?: number) => {
  return render(
    <BrowserRouter>
      <BudgetStatusList budgets={budgets} maxDisplay={maxDisplay} />
    </BrowserRouter>
  )
}

describe('BudgetStatusCard', () => {
  describe('Display Budget Information', () => {
    it('should display budget category', () => {
      renderBudgetCard(mockBudgetActive)
      
      expect(screen.getByText('Alimentação')).toBeInTheDocument()
    })

    it('should display budget period', () => {
      renderBudgetCard(mockBudgetActive)
      
      expect(screen.getByText('MENSAL')).toBeInTheDocument()
    })

    it('should display current spending amount', () => {
      renderBudgetCard(mockBudgetActive)
      
      expect(screen.getByText('R$ 500,00')).toBeInTheDocument()
    })

    it('should display budget limit', () => {
      renderBudgetCard(mockBudgetActive)
      
      expect(screen.getByText('R$ 1.000,00')).toBeInTheDocument()
    })

    it('should display percentage used', () => {
      renderBudgetCard(mockBudgetActive)
      
      expect(screen.getByText('50.0% utilizado')).toBeInTheDocument()
    })
  })

  describe('Active Budget Status', () => {
    it('should show "No limite" badge for active budget', () => {
      renderBudgetCard(mockBudgetActive)
      
      expect(screen.getByText('No limite')).toBeInTheDocument()
    })

    it('should display remaining amount for active budget', () => {
      renderBudgetCard(mockBudgetActive)
      
      expect(screen.getByText('R$ 500,00 restante')).toBeInTheDocument()
    })

    it('should use success variant for progress bar', () => {
      const { container } = renderBudgetCard(mockBudgetActive)
      
      const progressBar = container.querySelector('.bg-green-500')
      expect(progressBar).toBeInTheDocument()
    })
  })

  describe('Near Limit Budget Status', () => {
    it('should show "Próximo do limite" badge', () => {
      renderBudgetCard(mockBudgetNearLimit)
      
      expect(screen.getByText('Próximo do limite')).toBeInTheDocument()
    })

    it('should display remaining amount', () => {
      renderBudgetCard(mockBudgetNearLimit)
      
      expect(screen.getByText('R$ 50,00 restante')).toBeInTheDocument()
    })

    it('should use warning variant for progress bar', () => {
      const { container } = renderBudgetCard(mockBudgetNearLimit)
      
      const progressBar = container.querySelector('.bg-yellow-500')
      expect(progressBar).toBeInTheDocument()
    })
  })

  describe('Exceeded Budget Status', () => {
    it('should show "Excedido" badge', () => {
      renderBudgetCard(mockBudgetExceeded)
      
      expect(screen.getByText('Excedido')).toBeInTheDocument()
    })

    it('should display amount over limit', () => {
      renderBudgetCard(mockBudgetExceeded)
      
      expect(screen.getByText('R$ 50,00 acima')).toBeInTheDocument()
    })

    it('should use danger variant for progress bar', () => {
      const { container } = renderBudgetCard(mockBudgetExceeded)
      
      const progressBar = container.querySelector('.bg-red-500')
      expect(progressBar).toBeInTheDocument()
    })
  })
})

describe('BudgetStatusList', () => {
  const multipleBudgets = [
    mockBudgetActive,
    mockBudgetNearLimit,
    mockBudgetExceeded,
    { ...mockBudgetActive, id: '4', categoria: 'MORADIA' },
    { ...mockBudgetActive, id: '5', categoria: 'SAUDE' },
  ]

  it('should display all budgets when count is less than maxDisplay', () => {
    renderBudgetList([mockBudgetActive, mockBudgetNearLimit])
    
    expect(screen.getByText('Alimentação')).toBeInTheDocument()
    expect(screen.getByText('Transporte')).toBeInTheDocument()
  })

  it('should limit display to maxDisplay budgets', () => {
    renderBudgetList(multipleBudgets, 3)
    
    // Should show first 3 budgets
    expect(screen.getByText('Alimentação')).toBeInTheDocument()
    expect(screen.getByText('Transporte')).toBeInTheDocument()
    expect(screen.getByText('Lazer')).toBeInTheDocument()
    
    // Should not show 4th and 5th
    expect(screen.queryByText('Moradia')).not.toBeInTheDocument()
  })

  it('should show "Ver todos" link when there are more budgets', () => {
    renderBudgetList(multipleBudgets, 3)
    
    expect(screen.getByText('Ver todos os 5 orçamentos')).toBeInTheDocument()
  })

  it('should not show "Ver todos" link when all budgets are displayed', () => {
    renderBudgetList([mockBudgetActive, mockBudgetNearLimit], 4)
    
    expect(screen.queryByText(/Ver todos/)).not.toBeInTheDocument()
  })

  it('should display empty state when no budgets', () => {
    renderBudgetList([])
    
    expect(screen.getByText('Nenhum orçamento ativo')).toBeInTheDocument()
    expect(screen.getByText('Criar primeiro orçamento')).toBeInTheDocument()
  })

  it('should have link to budgets page in empty state', () => {
    renderBudgetList([])
    
    const link = screen.getByText('Criar primeiro orçamento')
    expect(link).toHaveAttribute('href', '/budgets')
  })

  it('should render budgets in grid layout', () => {
    const { container } = renderBudgetList([mockBudgetActive, mockBudgetNearLimit])
    
    const grid = container.querySelector('.grid')
    expect(grid).toBeInTheDocument()
    expect(grid).toHaveClass('md:grid-cols-2')
  })
})
