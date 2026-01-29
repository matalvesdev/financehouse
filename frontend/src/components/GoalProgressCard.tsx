import { Link } from 'react-router-dom'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card'
import { Progress } from '@/components/ui/Progress'
import { Badge } from '@/components/ui/Badge'
import { Target, Calendar, TrendingUp, CheckCircle, AlertCircle } from 'lucide-react'
import { format, differenceInDays, isPast } from 'date-fns'
import { ptBR } from 'date-fns/locale'
import type { Goal } from '@/types'

interface GoalProgressCardProps {
  goal: Goal
}

export function GoalProgressCard({ goal }: GoalProgressCardProps) {
  const percentualConclusao = (goal.valorAtual / goal.valorAlvo) * 100
  const prazoDate = new Date(goal.prazo)
  const diasRestantes = differenceInDays(prazoDate, new Date())
  const isPrazoVencido = isPast(prazoDate) && goal.status !== 'CONCLUIDA'
  
  const getStatusVariant = () => {
    if (goal.status === 'CONCLUIDA') return 'success'
    if (goal.status === 'ATRASADA' || isPrazoVencido) return 'danger'
    if (percentualConclusao >= 80) return 'success'
    if (percentualConclusao >= 50) return 'warning'
    return 'default'
  }

  const getStatusIcon = () => {
    if (goal.status === 'CONCLUIDA') {
      return <CheckCircle className="h-4 w-4 text-green-600" />
    }
    if (goal.status === 'ATRASADA' || isPrazoVencido) {
      return <AlertCircle className="h-4 w-4 text-red-600" />
    }
    return <Target className="h-4 w-4 text-blue-600" />
  }

  const getStatusLabel = () => {
    if (goal.status === 'CONCLUIDA') return 'Concluída'
    if (goal.status === 'ATRASADA' || isPrazoVencido) return 'Atrasada'
    if (goal.status === 'CANCELADA') return 'Cancelada'
    return 'Em andamento'
  }

  const getStatusBadgeVariant = () => {
    if (goal.status === 'CONCLUIDA') return 'success'
    if (goal.status === 'ATRASADA' || isPrazoVencido) return 'destructive'
    if (goal.status === 'CANCELADA') return 'secondary'
    return 'default'
  }

  const getTipoLabel = () => {
    switch (goal.tipo) {
      case 'RESERVA_EMERGENCIA':
        return 'Reserva de Emergência'
      case 'VIAGEM':
        return 'Viagem'
      case 'COMPRA':
        return 'Compra'
      case 'INVESTIMENTO':
        return 'Investimento'
      default:
        return goal.tipo
    }
  }

  return (
    <Card className="hover:shadow-md transition-shadow">
      <CardHeader className="pb-3">
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <CardTitle className="text-base font-semibold">
              {goal.nome}
            </CardTitle>
            <p className="text-xs text-muted-foreground mt-1">
              {getTipoLabel()}
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
            <span className="text-muted-foreground">Progresso</span>
            <span className="font-semibold">
              R$ {goal.valorAtual.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
            </span>
          </div>
          
          <Progress 
            value={goal.valorAtual} 
            max={goal.valorAlvo}
            variant={getStatusVariant()}
            size="md"
          />
          
          <div className="flex items-center justify-between text-sm">
            <span className="text-muted-foreground">Meta</span>
            <span className="font-semibold">
              R$ {goal.valorAlvo.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
            </span>
          </div>

          <div className="flex items-center justify-between pt-2 border-t">
            <div className="flex items-center space-x-1">
              {getStatusIcon()}
              <span className="text-xs text-muted-foreground">
                {percentualConclusao.toFixed(1)}% concluído
              </span>
            </div>
            {goal.valorAtual < goal.valorAlvo && (
              <span className="text-xs text-blue-600 font-medium">
                R$ {(goal.valorAlvo - goal.valorAtual).toLocaleString('pt-BR', { minimumFractionDigits: 2 })} faltam
              </span>
            )}
          </div>

          <div className="flex items-center justify-between pt-2 border-t text-xs">
            <div className="flex items-center space-x-1 text-muted-foreground">
              <Calendar className="h-3 w-3" />
              <span>
                Prazo: {format(prazoDate, 'dd/MM/yyyy', { locale: ptBR })}
              </span>
            </div>
            {!isPrazoVencido && goal.status !== 'CONCLUIDA' && diasRestantes >= 0 && (
              <span className={diasRestantes <= 30 ? 'text-yellow-600 font-medium' : 'text-muted-foreground'}>
                {diasRestantes} {diasRestantes === 1 ? 'dia' : 'dias'}
              </span>
            )}
            {isPrazoVencido && goal.status !== 'CONCLUIDA' && (
              <span className="text-red-600 font-medium">
                Vencido há {Math.abs(diasRestantes)} {Math.abs(diasRestantes) === 1 ? 'dia' : 'dias'}
              </span>
            )}
          </div>

          {goal.dataEstimadaConclusao && goal.status === 'EM_ANDAMENTO' && (
            <div className="text-xs text-muted-foreground pt-1">
              Estimativa de conclusão: {format(new Date(goal.dataEstimadaConclusao), 'dd/MM/yyyy', { locale: ptBR })}
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}

interface GoalProgressListProps {
  goals: Goal[]
  maxDisplay?: number
}

export function GoalProgressList({ goals, maxDisplay = 4 }: GoalProgressListProps) {
  const displayGoals = goals.slice(0, maxDisplay)
  const hasMore = goals.length > maxDisplay

  if (goals.length === 0) {
    return (
      <Card>
        <CardContent className="py-8">
          <div className="text-center text-muted-foreground">
            <p className="mb-2">Nenhuma meta ativa</p>
            <Link
              to="/goals"
              className="text-primary hover:underline text-sm"
            >
              Criar primeira meta
            </Link>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {displayGoals.map((goal) => (
          <GoalProgressCard key={goal.id} goal={goal} />
        ))}
      </div>
      {hasMore && (
        <div className="text-center">
          <Link
            to="/goals"
            className="text-primary hover:underline text-sm inline-flex items-center"
          >
            Ver todas as {goals.length} metas
            <TrendingUp className="h-4 w-4 ml-1" />
          </Link>
        </div>
      )}
    </div>
  )
}
