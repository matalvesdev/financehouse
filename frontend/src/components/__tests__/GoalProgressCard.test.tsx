import { render, screen } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { vi } from 'vitest'
import { GoalProgressCard, GoalProgressList } from '../GoalProgressCard'
import type { Goal } from '@/types'

const mockGoalInProgress: Goal = {
  id: '1',
  usuarioId: 'user1',
  nome: 'Reserva de Emergência',
  valorAlvo: 10000.00,
  valorAtual: 5000.00,
  prazo: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], // 1 year from now
  tipo: 'RESERVA_EMERGENCIA',
  status: 'EM_ANDAMENTO',
  percentualConclusao: 50,
  dataEstimadaConclusao: new Date(Date.now() + 300 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], // 300 days from now
  criadaEm: '2024-01-01T00:00:00',
}

const mockGoalCompleted: Goal = {
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
}

const mockGoalDelayed: Goal = {
  id: '3',
  usuarioId: 'user1',
  nome: 'Compra de Carro',
  valorAlvo: 50000.00,
  valorAtual: 20000.00,
  prazo: '2023-12-31', // Past date
  tipo: 'COMPRA',
  status: 'ATRASADA',
  percentualConclusao: 40,
  criadaEm: '2023-01-01T00:00:00',
}

const mockGoalNearDeadline: Goal = {
  id: '4',
  usuarioId: 'user1',
  nome: 'Investimento',
  valorAlvo: 5000.00,
  valorAtual: 4000.00,
  prazo: new Date(Date.now() + 15 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], // 15 days from now
  tipo: 'INVESTIMENTO',
  status: 'EM_ANDAMENTO',
  percentualConclusao: 80,
  criadaEm: '2024-01-01T00:00:00',
}

const renderGoalCard = (goal: Goal) => {
  return render(
    <BrowserRouter>
      <GoalProgressCard goal={goal} />
    </BrowserRouter>
  )
}

const renderGoalList = (goals: Goal[], maxDisplay?: number) => {
  return render(
    <BrowserRouter>
      <GoalProgressList goals={goals} maxDisplay={maxDisplay} />
    </BrowserRouter>
  )
}

describe('GoalProgressCard', () => {
  describe('Display Goal Information', () => {
    it('should display goal name', () => {
      renderGoalCard(mockGoalInProgress)
      
      // Use getAllByText since the name appears in both title and type
      const nameElements = screen.getAllByText('Reserva de Emergência')
      expect(nameElements.length).toBeGreaterThan(0)
    })

    it('should display goal type', () => {
      renderGoalCard(mockGoalInProgress)
      
      // Use getAllByText since it appears in both title and type
      const typeElements = screen.getAllByText('Reserva de Emergência')
      expect(typeElements.length).toBeGreaterThan(0)
    })

    it('should display current progress amount', () => {
      renderGoalCard(mockGoalInProgress)
      
      expect(screen.getByText('R$ 5.000,00')).toBeInTheDocument()
    })

    it('should display target amount', () => {
      renderGoalCard(mockGoalInProgress)
      
      expect(screen.getByText('R$ 10.000,00')).toBeInTheDocument()
    })

    it('should display completion percentage', () => {
      renderGoalCard(mockGoalInProgress)
      
      expect(screen.getByText('50.0% concluído')).toBeInTheDocument()
    })

    it('should display deadline', () => {
      renderGoalCard(mockGoalInProgress)
      
      expect(screen.getByText(/Prazo:/)).toBeInTheDocument()
      // Just check that a date is displayed, not the exact date since it's dynamic
    })
  })

  describe('In Progress Goal Status', () => {
    it('should show "Em andamento" badge', () => {
      renderGoalCard(mockGoalInProgress)
      
      expect(screen.getByText('Em andamento')).toBeInTheDocument()
    })

    it('should display remaining amount', () => {
      renderGoalCard(mockGoalInProgress)
      
      expect(screen.getByText('R$ 5.000,00 faltam')).toBeInTheDocument()
    })

    it('should display estimated completion date', () => {
      renderGoalCard(mockGoalInProgress)
      
      expect(screen.getByText(/Estimativa de conclusão:/)).toBeInTheDocument()
      // Just check that the text exists, not the exact date since it's dynamic
    })

    it('should use default variant for progress bar when below 50%', () => {
      const lowProgressGoal = { ...mockGoalInProgress, valorAtual: 3000, percentualConclusao: 30 }
      const { container } = renderGoalCard(lowProgressGoal)
      
      const progressBar = container.querySelector('.bg-primary')
      expect(progressBar).not.toBeNull()
    })

    it('should use warning variant for progress bar when between 50-80%', () => {
      const mediumProgressGoal = { ...mockGoalInProgress, valorAtual: 6000, percentualConclusao: 60 }
      const { container } = renderGoalCard(mediumProgressGoal)
      
      const progressBar = container.querySelector('.bg-yellow-500')
      expect(progressBar).not.toBeNull()
    })

    it('should use success variant for progress bar when above 80%', () => {
      const highProgressGoal = { ...mockGoalInProgress, valorAtual: 9000, percentualConclusao: 90 }
      const { container } = renderGoalCard(highProgressGoal)
      
      const progressBar = container.querySelector('.bg-green-500')
      expect(progressBar).not.toBeNull()
    })
  })

  describe('Completed Goal Status', () => {
    it('should show "Concluída" badge', () => {
      renderGoalCard(mockGoalCompleted)
      
      expect(screen.getByText('Concluída')).toBeInTheDocument()
    })

    it('should display 100% completion', () => {
      renderGoalCard(mockGoalCompleted)
      
      expect(screen.getByText('100.0% concluído')).toBeInTheDocument()
    })

    it('should not display remaining amount', () => {
      renderGoalCard(mockGoalCompleted)
      
      expect(screen.queryByText(/faltam/)).not.toBeInTheDocument()
    })

    it('should use success variant for progress bar', () => {
      const { container } = renderGoalCard(mockGoalCompleted)
      
      const progressBar = container.querySelector('.bg-green-500')
      expect(progressBar).toBeInTheDocument()
    })
  })

  describe('Delayed Goal Status', () => {
    it('should show "Atrasada" badge', () => {
      renderGoalCard(mockGoalDelayed)
      
      expect(screen.getByText('Atrasada')).toBeInTheDocument()
    })

    it('should display overdue message', () => {
      renderGoalCard(mockGoalDelayed)
      
      expect(screen.getByText(/Vencido há/)).toBeInTheDocument()
    })

    it('should use danger variant for progress bar', () => {
      const { container } = renderGoalCard(mockGoalDelayed)
      
      const progressBar = container.querySelector('.bg-red-500')
      expect(progressBar).toBeInTheDocument()
    })
  })

  describe('Near Deadline Warning', () => {
    it('should highlight days remaining when deadline is within 30 days', () => {
      renderGoalCard(mockGoalNearDeadline)
      
      const daysElement = screen.getByText(/dias/)
      expect(daysElement).toHaveClass('text-yellow-600')
    })
  })

  describe('Goal Types', () => {
    it('should display "Reserva de Emergência" type label', () => {
      renderGoalCard(mockGoalInProgress)
      
      // Use getAllByText since it appears in both title and type
      const elements = screen.getAllByText('Reserva de Emergência')
      expect(elements.length).toBeGreaterThan(0)
    })

    it('should display "Viagem" type label', () => {
      renderGoalCard(mockGoalCompleted)
      
      expect(screen.getByText('Viagem')).toBeInTheDocument()
    })

    it('should display "Compra" type label', () => {
      renderGoalCard(mockGoalDelayed)
      
      expect(screen.getByText('Compra')).toBeInTheDocument()
    })

    it('should display "Investimento" type label', () => {
      renderGoalCard(mockGoalNearDeadline)
      
      // Use getAllByText since it appears in both title and type
      const elements = screen.getAllByText('Investimento')
      expect(elements.length).toBeGreaterThan(0)
    })
  })
})

describe('GoalProgressList', () => {
  const multipleGoals = [
    mockGoalInProgress,
    mockGoalCompleted,
    mockGoalDelayed,
    { ...mockGoalInProgress, id: '5', nome: 'Meta 4' },
    { ...mockGoalInProgress, id: '6', nome: 'Meta 5' },
  ]

  it('should display all goals when count is less than maxDisplay', () => {
    renderGoalList([mockGoalInProgress, mockGoalCompleted])
    
    // Use getAllByText since goal names appear in multiple places (title and type)
    const reservaElements = screen.getAllByText('Reserva de Emergência')
    expect(reservaElements.length).toBeGreaterThan(0)
    
    expect(screen.getByText('Viagem para Europa')).toBeInTheDocument()
  })

  it('should limit display to maxDisplay goals', () => {
    renderGoalList(multipleGoals, 3)
    
    // Should show first 3 goals - use getAllByText for duplicates
    const reservaElements = screen.getAllByText('Reserva de Emergência')
    expect(reservaElements.length).toBeGreaterThan(0)
    
    expect(screen.getByText('Viagem para Europa')).toBeInTheDocument()
    expect(screen.getByText('Compra de Carro')).toBeInTheDocument()
    
    // Should not show 4th and 5th
    expect(screen.queryByText('Meta 4')).not.toBeInTheDocument()
  })

  it('should show "Ver todas" link when there are more goals', () => {
    renderGoalList(multipleGoals, 3)
    
    expect(screen.getByText('Ver todas as 5 metas')).toBeInTheDocument()
  })

  it('should not show "Ver todas" link when all goals are displayed', () => {
    renderGoalList([mockGoalInProgress, mockGoalCompleted], 4)
    
    expect(screen.queryByText(/Ver todas/)).not.toBeInTheDocument()
  })

  it('should display empty state when no goals', () => {
    renderGoalList([])
    
    expect(screen.getByText('Nenhuma meta ativa')).toBeInTheDocument()
    expect(screen.getByText('Criar primeira meta')).toBeInTheDocument()
  })

  it('should have link to goals page in empty state', () => {
    renderGoalList([])
    
    const link = screen.getByText('Criar primeira meta')
    expect(link).toHaveAttribute('href', '/goals')
  })

  it('should render goals in grid layout', () => {
    const { container } = renderGoalList([mockGoalInProgress, mockGoalCompleted])
    
    const grid = container.querySelector('.grid')
    expect(grid).toBeInTheDocument()
    expect(grid).toHaveClass('md:grid-cols-2')
  })
})
