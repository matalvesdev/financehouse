import { useState, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useGoalStore } from '@/stores/goalStore'
import { useConfirmStore } from '@/stores/confirmStore'
import { Button, Input, Select, Modal } from '@/components/ui'
import { goalSchema, goalProgressSchema, GoalFormData, GoalProgressFormData, tipoMetaLabels, TipoMeta } from '@/lib/schemas'
import { Plus, Trash2, Target, PlusCircle, Calendar, TrendingUp } from 'lucide-react'
import { format, differenceInDays } from 'date-fns'
import { ptBR } from 'date-fns/locale'
import toast from 'react-hot-toast'
import type { Goal } from '@/types'

export default function GoalsPage() {
  const {
    goals,
    isLoading,
    error,
    showOnlyActive,
    filterTipo,
    fetchGoals,
    createGoal,
    addProgress,
    deleteGoal,
    setShowOnlyActive,
    setFilterTipo,
    clearError,
  } = useGoalStore()

  const { openConfirm } = useConfirmStore()
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false)
  const [isProgressModalOpen, setIsProgressModalOpen] = useState(false)
  const [selectedGoal, setSelectedGoal] = useState<Goal | null>(null)

  const {
    register: registerCreate,
    handleSubmit: handleSubmitCreate,
    reset: resetCreate,
    formState: { errors: errorsCreate, isSubmitting: isSubmittingCreate },
  } = useForm<GoalFormData>({
    resolver: zodResolver(goalSchema),
  })

  const {
    register: registerProgress,
    handleSubmit: handleSubmitProgress,
    reset: resetProgress,
    formState: { errors: errorsProgress, isSubmitting: isSubmittingProgress },
  } = useForm<GoalProgressFormData>({
    resolver: zodResolver(goalProgressSchema),
  })

  useEffect(() => {
    fetchGoals()
  }, [fetchGoals])

  useEffect(() => {
    if (error) {
      toast.error(error)
      clearError()
    }
  }, [error, clearError])

  const handleOpenCreate = () => {
    resetCreate({
      nome: '',
      valorAlvo: undefined,
      prazo: '',
      tipo: undefined,
    })
    setIsCreateModalOpen(true)
  }

  const handleCloseCreate = () => {
    setIsCreateModalOpen(false)
    resetCreate()
  }

  const handleOpenProgress = (goal: Goal) => {
    setSelectedGoal(goal)
    resetProgress({ valor: undefined })
    setIsProgressModalOpen(true)
  }

  const handleCloseProgress = () => {
    setIsProgressModalOpen(false)
    setSelectedGoal(null)
    resetProgress()
  }

  const onSubmitCreate = async (data: GoalFormData) => {
    // Calculate impact for confirmation dialog (Requirement 9.3)
    const diasAteVencimento = differenceInDays(new Date(data.prazo), new Date())
    const impactMessage = `
      • Valor alvo: R$ ${data.valorAlvo.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
      • Tipo: ${tipoMetaLabels[data.tipo as TipoMeta]}
      • Prazo: ${format(new Date(data.prazo), 'dd/MM/yyyy', { locale: ptBR })} (${diasAteVencimento} dias)
      • O sistema acompanhará seu progresso e enviará lembretes
    `.trim()

    openConfirm({
      title: 'Criar Meta Financeira',
      message: `Confirme a criação da meta "${data.nome}". O sistema monitorará seu progresso e enviará notificações.`,
      impact: impactMessage,
      confirmText: 'Criar',
      cancelText: 'Cancelar',
      variant: 'info',
      timeoutSeconds: 300,
      onConfirm: async () => {
        try {
          await createGoal(data)
          toast.success('Meta criada com sucesso!')
          handleCloseCreate()
        } catch (err) {
          // Erro já tratado no store
        }
      },
    })
  }

  const onSubmitProgress = async (data: GoalProgressFormData) => {
    if (!selectedGoal) return
    
    // Calculate impact for confirmation dialog (Requirement 9.3)
    const novoValorAtual = selectedGoal.valorAtual + data.valor
    const novoPercentual = (novoValorAtual / selectedGoal.valorAlvo) * 100
    const faltante = Math.max(0, selectedGoal.valorAlvo - novoValorAtual)
    
    const impactMessage = `
      • Valor atual: R$ ${selectedGoal.valorAtual.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
      • Novo valor: R$ ${novoValorAtual.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
      • Progresso: ${novoPercentual.toFixed(1)}%
      • Falta: R$ ${faltante.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
      ${novoPercentual >= 100 ? '• 🎉 Meta será concluída!' : ''}
    `.trim()

    openConfirm({
      title: 'Adicionar Progresso',
      message: `Confirme a adição de R$ ${data.valor.toLocaleString('pt-BR', { minimumFractionDigits: 2 })} à meta "${selectedGoal.nome}".`,
      impact: impactMessage,
      confirmText: 'Adicionar',
      cancelText: 'Cancelar',
      variant: 'info',
      timeoutSeconds: 300,
      onConfirm: async () => {
        try {
          await addProgress(selectedGoal.id, data.valor)
          toast.success('Progresso adicionado com sucesso!')
          handleCloseProgress()
        } catch (err) {
          // Erro já tratado no store
        }
      },
    })
  }

  const handleDelete = (goal: Goal) => {
    openConfirm({
      title: 'Excluir Meta',
      message: `Tem certeza que deseja excluir a meta "${goal.nome}"? Esta ação não pode ser desfeita.`,
      confirmText: 'Excluir',
      cancelText: 'Cancelar',
      variant: 'danger',
      timeoutSeconds: 300,
      onConfirm: async () => {
        try {
          await deleteGoal(goal.id)
          toast.success('Meta excluída com sucesso!')
        } catch (err) {
          // Erro já tratado
        }
      },
    })
  }

  const getProgressColor = (percentual: number) => {
    if (percentual >= 100) return 'bg-green-500'
    if (percentual >= 75) return 'bg-blue-500'
    if (percentual >= 50) return 'bg-yellow-500'
    return 'bg-gray-400'
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'CONCLUIDA':
        return 'bg-green-100 text-green-800'
      case 'EM_ANDAMENTO':
        return 'bg-blue-100 text-blue-800'
      case 'ATRASADA':
        return 'bg-red-100 text-red-800'
      case 'CANCELADA':
        return 'bg-gray-100 text-gray-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'CONCLUIDA':
        return 'Concluída'
      case 'EM_ANDAMENTO':
        return 'Em Andamento'
      case 'ATRASADA':
        return 'Atrasada'
      case 'CANCELADA':
        return 'Cancelada'
      default:
        return status
    }
  }

  const tipoOptions = Object.entries(tipoMetaLabels).map(([value, label]) => ({
    value,
    label,
  }))

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Metas Financeiras</h1>
          <p className="text-gray-600">Acompanhe seus objetivos financeiros</p>
        </div>
        <Button
          variant="primary"
          leftIcon={<Plus className="h-4 w-4" />}
          onClick={handleOpenCreate}
        >
          Nova Meta
        </Button>
      </div>

      {/* Filtros */}
      <div className="bg-white p-4 rounded-lg shadow-sm border">
        <div className="flex flex-wrap gap-4 items-center">
          <label className="flex items-center space-x-2 text-sm text-gray-600">
            <input
              type="checkbox"
              checked={showOnlyActive}
              onChange={(e) => setShowOnlyActive(e.target.checked)}
              className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
            />
            <span>Apenas ativas</span>
          </label>
          <Select
            options={tipoOptions}
            placeholder="Todos os tipos"
            value={filterTipo || ''}
            onChange={(e) => setFilterTipo((e.target.value as TipoMeta) || null)}
            className="w-48"
          />
        </div>
      </div>

      {/* Lista de Metas */}
      {isLoading ? (
        <div className="flex justify-center items-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
        </div>
      ) : goals.length === 0 ? (
        <div className="bg-white rounded-lg shadow-sm border p-8 text-center">
          <Target className="h-12 w-12 text-gray-400 mx-auto mb-4" />
          <p className="text-gray-500">Nenhuma meta encontrada</p>
          <Button
            variant="primary"
            className="mt-4"
            onClick={handleOpenCreate}
          >
            Criar primeira meta
          </Button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {goals.map((goal) => {
            const diasRestantes = differenceInDays(new Date(goal.prazo), new Date())
            
            return (
              <div
                key={goal.id}
                className="bg-white rounded-lg shadow-sm border p-6 space-y-4"
              >
                <div className="flex justify-between items-start">
                  <div>
                    <div className="flex items-center space-x-2">
                      <Target className="h-5 w-5 text-purple-600" />
                      <h3 className="font-semibold text-gray-900">{goal.nome}</h3>
                    </div>
                    <span className="text-sm text-gray-500">
                      {tipoMetaLabels[goal.tipo as TipoMeta]}
                    </span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <span
                      className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(goal.status)}`}
                    >
                      {getStatusLabel(goal.status)}
                    </span>
                    <button
                      onClick={() => handleDelete(goal)}
                      className="text-gray-400 hover:text-red-600"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                </div>

                {/* Progresso */}
                <div>
                  <div className="flex justify-between text-sm text-gray-600 mb-1">
                    <span>Progresso</span>
                    <span>
                      R$ {goal.valorAtual.toLocaleString('pt-BR', { minimumFractionDigits: 2 })} /{' '}
                      R$ {goal.valorAlvo.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                    </span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-3">
                    <div
                      className={`h-3 rounded-full transition-all ${getProgressColor(goal.percentualConclusao)}`}
                      style={{ width: `${Math.min(goal.percentualConclusao, 100)}%` }}
                    />
                  </div>
                  <p className="text-right text-sm text-gray-500 mt-1">
                    {goal.percentualConclusao.toFixed(1)}%
                  </p>
                </div>

                {/* Informações */}
                <div className="grid grid-cols-2 gap-4 text-sm">
                  <div className="flex items-center space-x-2 text-gray-600">
                    <Calendar className="h-4 w-4" />
                    <span>
                      Prazo: {format(new Date(goal.prazo), 'dd/MM/yyyy', { locale: ptBR })}
                    </span>
                  </div>
                  <div className="flex items-center space-x-2 text-gray-600">
                    <TrendingUp className="h-4 w-4" />
                    <span>
                      {diasRestantes > 0
                        ? `${diasRestantes} dias restantes`
                        : diasRestantes === 0
                        ? 'Vence hoje'
                        : `${Math.abs(diasRestantes)} dias atrasado`}
                    </span>
                  </div>
                </div>

                {/* Falta para completar */}
                <div className="pt-2 border-t">
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-600">
                      Falta: R${' '}
                      {Math.max(0, goal.valorAlvo - goal.valorAtual).toLocaleString('pt-BR', {
                        minimumFractionDigits: 2,
                      })}
                    </span>
                    {goal.status === 'EM_ANDAMENTO' && (
                      <Button
                        variant="secondary"
                        size="sm"
                        leftIcon={<PlusCircle className="h-4 w-4" />}
                        onClick={() => handleOpenProgress(goal)}
                      >
                        Adicionar
                      </Button>
                    )}
                  </div>
                </div>

                {goal.status === 'CONCLUIDA' && (
                  <div className="bg-green-50 text-green-700 text-sm p-2 rounded text-center">
                    🎉 Meta alcançada!
                  </div>
                )}
              </div>
            )
          })}
        </div>
      )}

      {/* Modal de Criar */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={handleCloseCreate}
        title="Nova Meta"
        size="md"
        footer={
          <>
            <Button variant="secondary" onClick={handleCloseCreate}>
              Cancelar
            </Button>
            <Button
              variant="primary"
              isLoading={isSubmittingCreate}
              onClick={handleSubmitCreate(onSubmitCreate)}
            >
              Criar
            </Button>
          </>
        }
      >
        <form onSubmit={handleSubmitCreate(onSubmitCreate)} className="space-y-4">
          <Input
            label="Nome da Meta"
            placeholder="Ex: Viagem para Europa"
            error={errorsCreate.nome?.message}
            {...registerCreate('nome')}
          />
          <Input
            type="number"
            step="0.01"
            label="Valor Alvo"
            placeholder="0,00"
            error={errorsCreate.valorAlvo?.message}
            {...registerCreate('valorAlvo', { valueAsNumber: true })}
          />
          <Input
            type="date"
            label="Prazo"
            error={errorsCreate.prazo?.message}
            {...registerCreate('prazo')}
          />
          <Select
            label="Tipo"
            options={tipoOptions}
            placeholder="Selecione o tipo"
            error={errorsCreate.tipo?.message}
            {...registerCreate('tipo')}
          />
        </form>
      </Modal>

      {/* Modal de Adicionar Progresso */}
      <Modal
        isOpen={isProgressModalOpen}
        onClose={handleCloseProgress}
        title={`Adicionar Progresso - ${selectedGoal?.nome}`}
        size="sm"
        footer={
          <>
            <Button variant="secondary" onClick={handleCloseProgress}>
              Cancelar
            </Button>
            <Button
              variant="primary"
              isLoading={isSubmittingProgress}
              onClick={handleSubmitProgress(onSubmitProgress)}
            >
              Adicionar
            </Button>
          </>
        }
      >
        <form onSubmit={handleSubmitProgress(onSubmitProgress)} className="space-y-4">
          <Input
            type="number"
            step="0.01"
            label="Valor"
            placeholder="0,00"
            helperText={
              selectedGoal
                ? `Falta R$ ${Math.max(0, selectedGoal.valorAlvo - selectedGoal.valorAtual).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`
                : undefined
            }
            error={errorsProgress.valor?.message}
            {...registerProgress('valor', { valueAsNumber: true })}
          />
        </form>
      </Modal>
    </div>
  )
}
