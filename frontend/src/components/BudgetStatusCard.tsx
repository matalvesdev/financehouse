import { Link } from 'react-router-dom'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card'
import { Progress } from '@/components/ui/Progress'
import { Badge } from '@/components/ui/Badge'
import { AlertTriangle, TrendingUp, CheckCircle } from 'lucide-react'
import { categoriaLabels, Categoria } from '@/lib/schemas'
import type { Budget } from '@/types'

interface BudgetStatusCardProps {
  budget: Budget
}

export function BudgetStatusCard({ budget }: BudgetStatusCardProps) {
  const percentualGasto = (budget.gastoAtual / budget.limite) * 100
  
  const getStatusVariant = () => {
    if (budget.status === 'EXCEDIDO') return 'danger'
    if (budget.status === 'PROXIMO_LIMITE') return 'warning'
    return 'success'
  }

  const getStatusIcon = () => {
    if (budget.status === 'EXCEDIDO') {
      return <AlertTriangle className="h-4 w-4 text-red-600" />
    }
    if (budget.status === 'PROXIMO_LIMITE') {
      return <AlertTriangle className="h-4 w-4 text-yellow-600" />
    }
    return <CheckCircle className="h-4 w-4 text-green-600" />
  }

  const getStatusLabel = () => {
    switch (budget.status) {
      case 'EXCEDIDO':
        return 'Excedido'
      case 'PROXIMO_LIMITE':
        return 'Próximo do limite'
      case 'ATIVO':
        return 'No limite'
      default:
        return budget.status
    }
  }

  const getStatusBadgeVariant = () => {
    if (budget.status === 'EXCEDIDO') return 'destructive'
    if (budget.status === 'PROXIMO_LIMITE') return 'warning'
    return 'success'
  }

  return (
    <Card className="hover:shadow-md transition-shadow">
      <CardHeader className="pb-3">
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <CardTitle className="text-base font-semibold">
              {categoriaLabels[budget.categoria as Categoria] || budget.categoria}
            </CardTitle>
            <p className="text-xs text-muted-foreground mt-1">
              {budget.periodo}
            </p>
          </div>
          <Badge variant={getStatusBadgeVariant()} className="ml-2">
            {getStatusLabel()}
          </Badge>
        </div>
      </CardHeader>
      <CardContent>
        <div className="space-y-3">
          <div className="flex items-center justify-between text-sm">
            <span className="text-muted-foreground">Gasto</span>
            <span className="font-semibold">
              R$ {budget.gastoAtual.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
            </span>
          </div>
          
          <Progress 
            value={budget.gastoAtual} 
            max={budget.limite}
            variant={getStatusVariant()}
            size="md"
          />
          
          <div className="flex items-center justify-between text-sm">
            <span className="text-muted-foreground">Limite</span>
            <span className="font-semibold">
              R$ {budget.limite.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
            </span>
          </div>

          <div className="flex items-center justify-between pt-2 border-t">
            <div className="flex items-center space-x-1">
              {getStatusIcon()}
              <span className="text-xs text-muted-foreground">
                {percentualGasto.toFixed(1)}% utilizado
              </span>
            </div>
            {budget.gastoAtual < budget.limite && (
              <span className="text-xs text-green-600 font-medium">
                R$ {(budget.limite - budget.gastoAtual).toLocaleString('pt-BR', { minimumFractionDigits: 2 })} restante
              </span>
            )}
            {budget.gastoAtual > budget.limite && (
              <span className="text-xs text-red-600 font-medium">
                R$ {(budget.gastoAtual - budget.limite).toLocaleString('pt-BR', { minimumFractionDigits: 2 })} acima
              </span>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  )
}

interface BudgetStatusListProps {
  budgets: Budget[]
  maxDisplay?: number
}

export function BudgetStatusList({ budgets, maxDisplay = 4 }: BudgetStatusListProps) {
  const displayBudgets = budgets.slice(0, maxDisplay)
  const hasMore = budgets.length > maxDisplay

  if (budgets.length === 0) {
    return (
      <Card>
        <CardContent className="py-8">
          <div className="text-center text-muted-foreground">
            <p className="mb-2">Nenhum orçamento ativo</p>
            <Link
              to="/budgets"
              className="text-primary hover:underline text-sm"
            >
              Criar primeiro orçamento
            </Link>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {displayBudgets.map((budget) => (
          <BudgetStatusCard key={budget.id} budget={budget} />
        ))}
      </div>
      {hasMore && (
        <div className="text-center">
          <Link
            to="/budgets"
            className="text-primary hover:underline text-sm inline-flex items-center"
          >
            Ver todos os {budgets.length} orçamentos
            <TrendingUp className="h-4 w-4 ml-1" />
          </Link>
        </div>
      )}
    </div>
  )
}
