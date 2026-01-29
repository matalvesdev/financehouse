import { useState, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useBudgetStore } from '@/stores/budgetStore'
import { useConfirmStore } from '@/stores/confirmStore'
import { Button, Input, Select, Modal } from '@/components/ui'
import { budgetSchema, BudgetFormData, categoriaLabels, periodoOrcamentoLabels, Categoria, PeriodoOrcamento } from '@/lib/schemas'
import { Plus, Trash2, AlertTriangle, CheckCircle, AlertCircle } from 'lucide-react'
import { format } from 'date-fns'
import toast from 'react-hot-toast'
import type { Budget } from '@/types'

export default function BudgetsPage() {
  const {
    budgets,
    isLoading,
    error,
    showOnlyActive,
    fetchBudgets,
    createBudget,
    deleteBudget,
    setShowOnlyActive,
    clearError,
  } = useBudgetStore()

  const { openConfirm } = useConfirmStore()
  const [isModalOpen, setIsModalOpen] = useState(false)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<BudgetFormData>({
    resolver: zodResolver(budgetSchema),
  })

  useEffect(() => {
    fetchBudgets()
  }, [fetchBudgets])

  useEffect(() => {
    if (error) {
      toast.error(error)
      clearError()
    }
  }, [error, clearError])

  const handleOpenCreate = () => {
    reset({
      categoria: undefined,
      limite: undefined,
      periodo: undefined,
      inicioVigencia: format(new Date(), 'yyyy-MM-dd'),
    })
    setIsModalOpen(true)
  }

  const handleCloseModal = () => {
    setIsModalOpen(false)
    reset()
  }

  const onSubmit = async (data: BudgetFormData) => {
    // Calculate impact for confirmation dialog (Requirement 9.3)
    const impactMessage = `
      • Categoria: ${categoriaLabels[data.categoria as Categoria]}
      • Limite: R$ ${data.limite.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
      • Período: ${periodoOrcamentoLabels[data.periodo as PeriodoOrcamento]}
      • Você será notificado ao atingir 80% do limite
    `.trim()

    openConfirm({
      title: 'Criar Orçamento',
      message: `Confirme a criação do orçamento para "${categoriaLabels[data.categoria as Categoria]}". O sistema monitorará seus gastos nesta categoria.`,
      impact: impactMessage,
      confirmText: 'Criar',
      cancelText: 'Cancelar',
      variant: 'info',
      timeoutSeconds: 300,
      onConfirm: async () => {
        try {
          await createBudget(data)
          toast.success('Orçamento criado com sucesso!')
          handleCloseModal()
        } catch (err) {
          // Erro já tratado no store
        }
      },
    })
  }

  const handleDelete = (budget: Budget) => {
    openConfirm({
      title: 'Excluir Orçamento',
      message: `Tem certeza que deseja excluir o orçamento de "${categoriaLabels[budget.categoria as Categoria]}"? Esta ação não pode ser desfeita.`,
      confirmText: 'Excluir',
      cancelText: 'Cancelar',
      variant: 'danger',
      timeoutSeconds: 300,
      onConfirm: async () => {
        try {
          await deleteBudget(budget.id)
          toast.success('Orçamento excluído com sucesso!')
        } catch (err) {
          // Erro já tratado
        }
      },
    })
  }

  const getStatusIcon = (budget: Budget) => {
    if (budget.status === 'EXCEDIDO') {
      return <AlertTriangle className="h-5 w-5 text-red-500" />
    }
    if (budget.status === 'PROXIMO_LIMITE') {
      return <AlertCircle className="h-5 w-5 text-yellow-500" />
    }
    return <CheckCircle className="h-5 w-5 text-green-500" />
  }

  const getProgressColor = (percentual: number) => {
    if (percentual >= 100) return 'bg-red-500'
    if (percentual >= 80) return 'bg-yellow-500'
    return 'bg-green-500'
  }

  const categoriaOptions = Object.entries(categoriaLabels).map(([value, label]) => ({
    value,
    label,
  }))

  const periodoOptions = Object.entries(periodoOrcamentoLabels).map(([value, label]) => ({
    value,
    label,
  }))

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Orçamentos</h1>
          <p className="text-gray-600">Controle seus gastos por categoria</p>
        </div>
        <div className="flex items-center space-x-3">
          <label className="flex items-center space-x-2 text-sm text-gray-600">
            <input
              type="checkbox"
              checked={showOnlyActive}
              onChange={(e) => setShowOnlyActive(e.target.checked)}
              className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
            />
            <span>Apenas ativos</span>
          </label>
          <Button
            variant="primary"
            leftIcon={<Plus className="h-4 w-4" />}
            onClick={handleOpenCreate}
          >
            Novo Orçamento
          </Button>
        </div>
      </div>

      {/* Lista de Orçamentos */}
      {isLoading ? (
        <div className="flex justify-center items-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
        </div>
      ) : budgets.length === 0 ? (
        <div className="bg-white rounded-lg shadow-sm border p-8 text-center">
          <p className="text-gray-500">Nenhum orçamento encontrado</p>
          <Button
            variant="primary"
            className="mt-4"
            onClick={handleOpenCreate}
          >
            Criar primeiro orçamento
          </Button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {budgets.map((budget) => (
            <div
              key={budget.id}
              className="bg-white rounded-lg shadow-sm border p-6 space-y-4"
            >
              <div className="flex justify-between items-start">
                <div className="flex items-center space-x-2">
                  {getStatusIcon(budget)}
                  <h3 className="font-semibold text-gray-900">
                    {categoriaLabels[budget.categoria as Categoria]}
                  </h3>
                </div>
                <button
                  onClick={() => handleDelete(budget)}
                  className="text-gray-400 hover:text-red-600"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>

              <div>
                <div className="flex justify-between text-sm text-gray-600 mb-1">
                  <span>Gasto</span>
                  <span>
                    R$ {budget.gastoAtual.toLocaleString('pt-BR', { minimumFractionDigits: 2 })} /{' '}
                    R$ {budget.limite.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                  </span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2.5">
                  <div
                    className={`h-2.5 rounded-full transition-all ${getProgressColor(budget.percentualGasto)}`}
                    style={{ width: `${Math.min(budget.percentualGasto, 100)}%` }}
                  />
                </div>
                <p className="text-right text-sm text-gray-500 mt-1">
                  {budget.percentualGasto.toFixed(1)}%
                </p>
              </div>

              <div className="flex justify-between text-sm text-gray-500">
                <span>{periodoOrcamentoLabels[budget.periodo as PeriodoOrcamento]}</span>
                <span>
                  Restante: R${' '}
                  {Math.max(0, budget.limite - budget.gastoAtual).toLocaleString('pt-BR', {
                    minimumFractionDigits: 2,
                  })}
                </span>
              </div>

              {budget.status === 'EXCEDIDO' && (
                <div className="bg-red-50 text-red-700 text-sm p-2 rounded">
                  Orçamento excedido em R${' '}
                  {(budget.gastoAtual - budget.limite).toLocaleString('pt-BR', {
                    minimumFractionDigits: 2,
                  })}
                </div>
              )}

              {budget.status === 'PROXIMO_LIMITE' && (
                <div className="bg-yellow-50 text-yellow-700 text-sm p-2 rounded">
                  Atenção: próximo do limite
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Modal de Criar */}
      <Modal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        title="Novo Orçamento"
        size="md"
        footer={
          <>
            <Button variant="secondary" onClick={handleCloseModal}>
              Cancelar
            </Button>
            <Button
              variant="primary"
              isLoading={isSubmitting}
              onClick={handleSubmit(onSubmit)}
            >
              Criar
            </Button>
          </>
        }
      >
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Select
            label="Categoria"
            options={categoriaOptions}
            placeholder="Selecione uma categoria"
            error={errors.categoria?.message}
            {...register('categoria')}
          />
          <Input
            type="number"
            step="0.01"
            label="Limite"
            placeholder="0,00"
            error={errors.limite?.message}
            {...register('limite', { valueAsNumber: true })}
          />
          <Select
            label="Período"
            options={periodoOptions}
            placeholder="Selecione o período"
            error={errors.periodo?.message}
            {...register('periodo')}
          />
          <Input
            type="date"
            label="Início da Vigência"
            error={errors.inicioVigencia?.message}
            {...register('inicioVigencia')}
          />
        </form>
      </Modal>
    </div>
  )
}
