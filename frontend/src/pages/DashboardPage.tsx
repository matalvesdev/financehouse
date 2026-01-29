import { useEffect, useMemo } from 'react'
import { Link } from 'react-router-dom'
import { useDashboardStore } from '@/stores/dashboardStore'
import { DollarSign, TrendingUp, TrendingDown, Target, PiggyBank, AlertTriangle, ArrowRight } from 'lucide-react'
import { format, subMonths, startOfMonth, endOfMonth } from 'date-fns'
import { ptBR } from 'date-fns/locale'
import { categoriaLabels, Categoria } from '@/lib/schemas'
import { BudgetStatusList } from '@/components/BudgetStatusCard'
import { GoalProgressList } from '@/components/GoalProgressCard'
import {
  IncomeExpenseChart,
  CategoryChart,
  TrendChart,
  BudgetProgressChart,
  GoalProgressChart,
} from '@/components/DashboardCharts'

// Cores para gráfico de pizza por categoria
const CATEGORY_COLORS: Record<string, string> = {
  ALIMENTACAO: '#EF4444',
  TRANSPORTE: '#F59E0B',
  MORADIA: '#3B82F6',
  SAUDE: '#10B981',
  EDUCACAO: '#8B5CF6',
  LAZER: '#EC4899',
  VESTUARIO: '#6366F1',
  SERVICOS: '#14B8A6',
  INVESTIMENTO: '#22C55E',
  SALARIO: '#06B6D4',
  FREELANCE: '#84CC16',
  OUTROS: '#6B7280',
}

// Componente de tooltip customizado para gráficos
const CustomTooltip = ({ active, payload, label }: any) => {
  if (active && payload && payload.length) {
    return (
      <div className="bg-white p-3 border border-gray-200 rounded-lg shadow-lg">
        <p className="font-semibold text-gray-900 mb-2">{label}</p>
        {payload.map((entry: any, index: number) => (
          <p key={index} className="text-sm" style={{ color: entry.color }}>
            {entry.name}: R$ {entry.value.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
          </p>
        ))}
      </div>
    )
  }
  return null
}

// Componente de tooltip para gráfico de pizza
const PieTooltip = ({ active, payload }: any) => {
  if (active && payload && payload.length) {
    const data = payload[0]
    return (
      <div className="bg-white p-3 border border-gray-200 rounded-lg shadow-lg">
        <p className="font-semibold text-gray-900">{data.name}</p>
        <p className="text-sm text-gray-600">
          R$ {data.value.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
        </p>
        <p className="text-xs text-gray-500">
          {((data.value / data.payload.total) * 100).toFixed(1)}% do total
        </p>
      </div>
    )
  }
  return null
}

export default function DashboardPage() {
  const { summary, recentTransactions, budgetsStatus, goalsProgress, isLoading, error, fetchDashboard } = useDashboardStore()

  // Calcular gastos por categoria das transações recentes
  const categoryData = useMemo(() => {
    const categoryTotals: Record<string, number> = {}
    let total = 0
    
    recentTransactions
      .filter(t => t.tipo === 'DESPESA')
      .forEach(t => {
        const cat = t.categoria
        categoryTotals[cat] = (categoryTotals[cat] || 0) + t.valor
        total += t.valor
      })
    
    return Object.entries(categoryTotals)
      .map(([categoria, valor]) => ({
        name: categoriaLabels[categoria as Categoria] || categoria,
        value: valor,
        color: CATEGORY_COLORS[categoria] || '#6B7280',
        total,
      }))
      .sort((a, b) => b.value - a.value)
      .slice(0, 6)
  }, [recentTransactions])

  // Gerar dados de tendência mensal (últimos 6 meses)
  const trendData = useMemo(() => {
    const months = []
    const today = new Date()
    
    for (let i = 5; i >= 0; i--) {
      const monthDate = subMonths(today, i)
      const monthStart = startOfMonth(monthDate)
      const monthEnd = endOfMonth(monthDate)
      
      // Filtrar transações do mês
      const monthTransactions = recentTransactions.filter(t => {
        const transDate = new Date(t.data)
        return transDate >= monthStart && transDate <= monthEnd
      })
      
      const receitas = monthTransactions
        .filter(t => t.tipo === 'RECEITA')
        .reduce((sum, t) => sum + t.valor, 0)
      
      const despesas = monthTransactions
        .filter(t => t.tipo === 'DESPESA')
        .reduce((sum, t) => sum + t.valor, 0)
      
      months.push({
        mes: format(monthDate, 'MMM/yy', { locale: ptBR }),
        Receitas: receitas,
        Despesas: despesas,
        Saldo: receitas - despesas,
      })
    }
    
    return months
  }, [recentTransactions])

  // Preparar dados de orçamentos para visualização
  const budgetChartData = useMemo(() => {
    return budgetsStatus
      .slice(0, 5)
      .map(budget => ({
        categoria: categoriaLabels[budget.categoria as Categoria] || budget.categoria,
        Gasto: budget.gastoAtual,
        Limite: budget.limite,
        Disponível: Math.max(0, budget.limite - budget.gastoAtual),
        percentual: (budget.gastoAtual / budget.limite) * 100,
      }))
  }, [budgetsStatus])

  // Preparar dados de metas para visualização radial
  const goalChartData = useMemo(() => {
    return goalsProgress
      .filter(g => g.status !== 'CANCELADA')
      .slice(0, 5)
      .map(goal => ({
        nome: goal.nome.length > 20 ? goal.nome.substring(0, 20) + '...' : goal.nome,
        percentual: goal.percentualConclusao,
        valor: goal.valorAtual,
        meta: goal.valorAlvo,
        fill: goal.status === 'CONCLUIDA' ? '#22C55E' : 
              goal.status === 'ATRASADA' ? '#EF4444' : 
              goal.percentualConclusao >= 80 ? '#22C55E' :
              goal.percentualConclusao >= 50 ? '#F59E0B' : '#3B82F6',
      }))
  }, [goalsProgress])

  useEffect(() => {
    fetchDashboard()
  }, [fetchDashboard])

  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
        {error}
      </div>
    )
  }

  const data = summary || {
    saldoAtual: 0,
    receitaMensal: 0,
    despesaMensal: 0,
    saldoMensal: 0,
    orcamentosAtivos: 0,
    orcamentosExcedidos: 0,
    orcamentosProximoLimite: 0,
    metasAtivas: 0,
    metasConcluidas: 0,
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-600">Visão geral da sua situação financeira</p>
      </div>

      {/* Cards de métricas principais */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="bg-white p-6 rounded-lg shadow-sm border">
          <div className="flex items-center">
            <div className="p-2 bg-green-100 rounded-lg">
              <DollarSign className="h-6 w-6 text-green-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Saldo Atual</p>
              <p className={`text-2xl font-bold ${data.saldoAtual >= 0 ? 'text-gray-900' : 'text-red-600'}`}>
                R$ {data.saldoAtual.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-sm border">
          <div className="flex items-center">
            <div className="p-2 bg-blue-100 rounded-lg">
              <TrendingUp className="h-6 w-6 text-blue-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Receita Mensal</p>
              <p className="text-2xl font-bold text-green-600">
                +R$ {data.receitaMensal.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-sm border">
          <div className="flex items-center">
            <div className="p-2 bg-red-100 rounded-lg">
              <TrendingDown className="h-6 w-6 text-red-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Despesa Mensal</p>
              <p className="text-2xl font-bold text-red-600">
                -R$ {data.despesaMensal.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-sm border">
          <div className="flex items-center">
            <div className="p-2 bg-purple-100 rounded-lg">
              <Target className="h-6 w-6 text-purple-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Metas Ativas</p>
              <p className="text-2xl font-bold text-gray-900">{data.metasAtivas}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Alertas de orçamento */}
      {(data.orcamentosExcedidos > 0 || data.orcamentosProximoLimite > 0) && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {data.orcamentosExcedidos > 0 && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-4 flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <AlertTriangle className="h-5 w-5 text-red-600" />
                <span className="text-red-800">
                  {data.orcamentosExcedidos} orçamento(s) excedido(s)
                </span>
              </div>
              <Link
                to="/budgets"
                className="text-red-600 hover:text-red-800 flex items-center"
              >
                Ver <ArrowRight className="h-4 w-4 ml-1" />
              </Link>
            </div>
          )}
          {data.orcamentosProximoLimite > 0 && (
            <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <AlertTriangle className="h-5 w-5 text-yellow-600" />
                <span className="text-yellow-800">
                  {data.orcamentosProximoLimite} orçamento(s) próximo(s) do limite
                </span>
              </div>
              <Link
                to="/budgets"
                className="text-yellow-600 hover:text-yellow-800 flex items-center"
              >
                Ver <ArrowRight className="h-4 w-4 ml-1" />
              </Link>
            </div>
          )}
        </div>
      )}

            {/* Seção de gráficos principais */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <IncomeExpenseChart 
          receitaMensal={data.receitaMensal}
          despesaMensal={data.despesaMensal}
          saldoMensal={data.saldoMensal}
        />
        <CategoryChart categoryData={categoryData} />
      </div>

      {/* Gráfico de Tendência */}
      <TrendChart trendData={trendData} />

      {/* Visualizações de Orçamentos e Metas */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <BudgetProgressChart budgetChartData={budgetChartData} />
        <GoalProgressChart goalChartData={goalChartData} />
      </div>

      {/* Seção de resumo */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white p-6 rounded-lg shadow-sm border">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">
            Resumo do Mês
          </h3>
          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <span className="text-gray-600">Receitas</span>
              <span className="font-semibold text-green-600">
                +R$ {data.receitaMensal.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
              </span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-600">Despesas</span>
              <span className="font-semibold text-red-600">
                -R$ {data.despesaMensal.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
              </span>
            </div>
            <hr />
            <div className="flex justify-between items-center">
              <span className="font-semibold text-gray-900">Saldo do Mês</span>
              <span className={`font-bold ${data.saldoMensal >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                {data.saldoMensal >= 0 ? '+' : ''}R$ {data.saldoMensal.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
              </span>
            </div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-sm border">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-semibold text-gray-900">
              Resumo Geral
            </h3>
          </div>
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <PiggyBank className="h-5 w-5 text-gray-400" />
                <span className="text-gray-600">Orçamentos ativos</span>
              </div>
              <span className="font-semibold">{data.orcamentosAtivos}</span>
            </div>
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <Target className="h-5 w-5 text-gray-400" />
                <span className="text-gray-600">Metas ativas</span>
              </div>
              <span className="font-semibold">{data.metasAtivas}</span>
            </div>
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <Target className="h-5 w-5 text-green-400" />
                <span className="text-gray-600">Metas concluídas</span>
              </div>
              <span className="font-semibold text-green-600">{data.metasConcluidas}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Status dos Orçamentos */}
      <div className="space-y-4">
        <div className="flex justify-between items-center">
          <h2 className="text-xl font-bold text-gray-900">Status dos Orçamentos</h2>
          <Link to="/budgets" className="text-blue-600 hover:text-blue-800 text-sm">
            Gerenciar orçamentos
          </Link>
        </div>
        <BudgetStatusList budgets={budgetsStatus} maxDisplay={4} />
      </div>

      {/* Progresso das Metas */}
      <div className="space-y-4">
        <div className="flex justify-between items-center">
          <h2 className="text-xl font-bold text-gray-900">Progresso das Metas</h2>
          <Link to="/goals" className="text-blue-600 hover:text-blue-800 text-sm">
            Gerenciar metas
          </Link>
        </div>
        <GoalProgressList goals={goalsProgress} maxDisplay={4} />
      </div>

      {/* Transações recentes */}
      <div className="bg-white p-6 rounded-lg shadow-sm border">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-lg font-semibold text-gray-900">
            Transações Recentes
          </h3>
          <Link to="/transactions" className="text-blue-600 hover:text-blue-800 text-sm">
            Ver todas
          </Link>
        </div>
        
        {recentTransactions.length === 0 ? (
          <div className="text-center text-gray-500 py-8">
            <p>Nenhuma transação encontrada</p>
            <Link
              to="/transactions"
              className="text-blue-600 hover:text-blue-800 text-sm mt-2 inline-block"
            >
              Criar primeira transação
            </Link>
          </div>
        ) : (
          <div className="divide-y">
            {recentTransactions.map((transaction) => (
              <div
                key={transaction.id}
                className="py-3 flex items-center justify-between"
              >
                <div className="flex items-center space-x-4">
                  <div
                    className={`p-2 rounded-full ${
                      transaction.tipo === 'RECEITA' ? 'bg-green-100' : 'bg-red-100'
                    }`}
                  >
                    {transaction.tipo === 'RECEITA' ? (
                      <TrendingUp className="h-4 w-4 text-green-600" />
                    ) : (
                      <TrendingDown className="h-4 w-4 text-red-600" />
                    )}
                  </div>
                  <div>
                    <p className="font-medium text-gray-900">
                      {transaction.descricao}
                    </p>
                    <p className="text-sm text-gray-500">
                      {categoriaLabels[transaction.categoria as Categoria]} •{' '}
                      {format(new Date(transaction.data), 'dd/MM/yyyy', { locale: ptBR })}
                    </p>
                  </div>
                </div>
                <span
                  className={`font-semibold ${
                    transaction.tipo === 'RECEITA' ? 'text-green-600' : 'text-red-600'
                  }`}
                >
                  {transaction.tipo === 'RECEITA' ? '+' : '-'}R${' '}
                  {transaction.valor.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}