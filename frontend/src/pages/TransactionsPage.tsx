import { useState, useEffect } from 'react'
import { useTransactionStore } from '@/stores/transactionStore'
import { useConfirmStore } from '@/stores/confirmStore'
import { Button, Input, Select, Modal } from '@/components/ui'
import TransactionForm from '@/components/TransactionForm'
import { TransactionFormData, categoriaLabels, tipoTransacaoLabels, Categoria, TipoTransacao } from '@/lib/schemas'
import { Plus, Edit2, Trash2, Filter, X, ChevronLeft, ChevronRight, ArrowUpDown, ArrowUp, ArrowDown } from 'lucide-react'
import { format } from 'date-fns'
import { ptBR } from 'date-fns/locale'
import toast from 'react-hot-toast'

export default function TransactionsPage() {
  const {
    transactions,
    pagination,
    filters,
    isLoading,
    error,
    selectedTransaction,
    fetchTransactions,
    createTransaction,
    updateTransaction,
    deleteTransaction,
    setSelectedTransaction,
    setFilters,
    clearFilters,
    clearError,
  } = useTransactionStore()

  const { openConfirm } = useConfirmStore()

  const [isModalOpen, setIsModalOpen] = useState(false)
  const [isFilterOpen, setIsFilterOpen] = useState(false)
  const [editMode, setEditMode] = useState(false)
  const [formInitialData, setFormInitialData] = useState<TransactionFormData | undefined>(undefined)

  useEffect(() => {
    fetchTransactions()
  }, [fetchTransactions])

  useEffect(() => {
    if (error) {
      toast.error(error)
      clearError()
    }
  }, [error, clearError])

  const handleOpenCreate = () => {
    setEditMode(false)
    setSelectedTransaction(null)
    setFormInitialData(undefined)
    setIsModalOpen(true)
  }

  const handleOpenEdit = (transaction: typeof transactions[0]) => {
    setEditMode(true)
    setSelectedTransaction(transaction)
    setFormInitialData({
      valor: transaction.valor,
      descricao: transaction.descricao,
      categoria: transaction.categoria as Categoria,
      tipo: transaction.tipo as TipoTransacao,
      data: transaction.data
    })
    setIsModalOpen(true)
  }

  const handleCloseModal = () => {
    setIsModalOpen(false)
    setEditMode(false)
    setSelectedTransaction(null)
    setFormInitialData(undefined)
  }

  const handleFormSubmit = async (data: TransactionFormData) => {
    // Calculate impact for confirmation dialog (Requirement 9.3)
    const impactMessage = data.tipo === 'RECEITA'
      ? `Sua receita mensal será aumentada em R$ ${data.valor.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}.`
      : `Seu gasto na categoria ${categoriaLabels[data.categoria]} será aumentado em R$ ${data.valor.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}.`

    const actionTitle = editMode ? 'Atualizar Transação' : 'Criar Transação'
    const actionMessage = editMode
      ? `Confirme a atualização da transação "${data.descricao}". As alterações afetarão seus relatórios e orçamentos.`
      : `Confirme a criação da transação "${data.descricao}". Esta transação será adicionada ao seu histórico financeiro.`

    openConfirm({
      title: actionTitle,
      message: actionMessage,
      impact: impactMessage,
      confirmText: editMode ? 'Atualizar' : 'Criar',
      cancelText: 'Cancelar',
      variant: 'info',
      timeoutSeconds: 300, // 5 minutos (Requirement 9.7)
      onConfirm: async () => {
        try {
          if (editMode && selectedTransaction) {
            await updateTransaction(selectedTransaction.id, data)
            toast.success('Transação atualizada com sucesso!')
          } else {
            await createTransaction(data)
            toast.success('Transação criada com sucesso!')
          }
          handleCloseModal()
        } catch (err) {
          // Erro já tratado no store
        }
      },
    })
  }

  const handleDelete = (transaction: typeof transactions[0]) => {
    // Calculate impact for confirmation dialog (Requirement 9.3)
    const impactMessage = transaction.tipo === 'RECEITA'
      ? `Sua receita mensal será reduzida em R$ ${transaction.valor.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}.`
      : `Seu gasto na categoria ${categoriaLabels[transaction.categoria as Categoria]} será reduzido em R$ ${transaction.valor.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}.`

    openConfirm({
      title: 'Excluir Transação',
      message: `Tem certeza que deseja excluir a transação "${transaction.descricao}"? Esta ação não pode ser desfeita.`,
      impact: impactMessage,
      confirmText: 'Excluir',
      cancelText: 'Cancelar',
      variant: 'danger',
      timeoutSeconds: 300, // 5 minutos (Requirement 9.7)
      onConfirm: async () => {
        try {
          await deleteTransaction(transaction.id)
          toast.success('Transação excluída com sucesso!')
        } catch (err) {
          // Erro já tratado
        }
      },
    })
  }

  const handlePageChange = (newPage: number) => {
    fetchTransactions(newPage)
  }

  const handleApplyFilters = (filterData: Partial<typeof filters>) => {
    setFilters(filterData)
    setIsFilterOpen(false)
  }

  const handleSort = (field: string) => {
    const newFilters = { ...filters }
    
    // Se já está ordenando por este campo, inverte a direção
    if (filters.ordenacao === field) {
      newFilters.direcao = filters.direcao === 'asc' ? 'desc' : 'asc'
    } else {
      // Novo campo de ordenação, começa com desc (mais recente primeiro)
      newFilters.ordenacao = field
      newFilters.direcao = 'desc'
    }
    
    setFilters(newFilters)
  }

  const getSortIcon = (field: string) => {
    if (filters.ordenacao !== field) {
      return <ArrowUpDown className="h-4 w-4 text-gray-400" />
    }
    return filters.direcao === 'asc' ? (
      <ArrowUp className="h-4 w-4 text-blue-600" />
    ) : (
      <ArrowDown className="h-4 w-4 text-blue-600" />
    )
  }

  const categoriaOptions = Object.entries(categoriaLabels).map(([value, label]) => ({
    value,
    label,
  }))

  const tipoOptions = Object.entries(tipoTransacaoLabels).map(([value, label]) => ({
    value,
    label,
  }))

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Transações</h1>
          <p className="text-gray-600">Gerencie suas receitas e despesas</p>
        </div>
        <div className="flex space-x-3">
          <Button
            variant="secondary"
            leftIcon={<Filter className="h-4 w-4" />}
            onClick={() => setIsFilterOpen(!isFilterOpen)}
          >
            Filtros
          </Button>
          <Button
            variant="primary"
            leftIcon={<Plus className="h-4 w-4" />}
            onClick={handleOpenCreate}
          >
            Nova Transação
          </Button>
        </div>
      </div>

      {/* Filtros */}
      {isFilterOpen && (
        <div className="bg-white p-4 rounded-lg shadow-sm border">
          <div className="flex justify-between items-center mb-4">
            <h3 className="font-semibold text-gray-900">Filtros</h3>
            <button
              onClick={() => setIsFilterOpen(false)}
              className="text-gray-400 hover:text-gray-500"
            >
              <X className="h-5 w-5" />
            </button>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <Input
              type="date"
              label="Data Início"
              value={filters.dataInicio || ''}
              onChange={(e) => handleApplyFilters({ ...filters, dataInicio: e.target.value })}
            />
            <Input
              type="date"
              label="Data Fim"
              value={filters.dataFim || ''}
              onChange={(e) => handleApplyFilters({ ...filters, dataFim: e.target.value })}
            />
            <Select
              label="Categoria"
              options={categoriaOptions}
              placeholder="Todas"
              value={filters.categoria || ''}
              onChange={(e) => handleApplyFilters({ ...filters, categoria: e.target.value || undefined })}
            />
            <Select
              label="Tipo"
              options={tipoOptions}
              placeholder="Todos"
              value={filters.tipo || ''}
              onChange={(e) => handleApplyFilters({ ...filters, tipo: (e.target.value as 'RECEITA' | 'DESPESA') || undefined })}
            />
          </div>
          <div className="mt-4 flex justify-end">
            <Button variant="ghost" onClick={clearFilters}>
              Limpar Filtros
            </Button>
          </div>
        </div>
      )}

      {/* Tabela de Transações */}
      <div className="bg-white rounded-lg shadow-sm border overflow-hidden">
        {isLoading ? (
          <div className="flex justify-center items-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
          </div>
        ) : transactions.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-gray-500">Nenhuma transação encontrada</p>
            <Button
              variant="primary"
              className="mt-4"
              onClick={handleOpenCreate}
            >
              Criar primeira transação
            </Button>
          </div>
        ) : (
          <>
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    <button
                      onClick={() => handleSort('data')}
                      className="flex items-center space-x-1 hover:text-gray-700 focus:outline-none"
                    >
                      <span>Data</span>
                      {getSortIcon('data')}
                    </button>
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    <button
                      onClick={() => handleSort('descricao')}
                      className="flex items-center space-x-1 hover:text-gray-700 focus:outline-none"
                    >
                      <span>Descrição</span>
                      {getSortIcon('descricao')}
                    </button>
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    <button
                      onClick={() => handleSort('categoria')}
                      className="flex items-center space-x-1 hover:text-gray-700 focus:outline-none"
                    >
                      <span>Categoria</span>
                      {getSortIcon('categoria')}
                    </button>
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    <button
                      onClick={() => handleSort('tipo')}
                      className="flex items-center space-x-1 hover:text-gray-700 focus:outline-none"
                    >
                      <span>Tipo</span>
                      {getSortIcon('tipo')}
                    </button>
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    <button
                      onClick={() => handleSort('valor')}
                      className="flex items-center space-x-1 hover:text-gray-700 focus:outline-none ml-auto"
                    >
                      <span>Valor</span>
                      {getSortIcon('valor')}
                    </button>
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Ações
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {transactions.map((transaction) => (
                  <tr key={transaction.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {format(new Date(transaction.data), 'dd/MM/yyyy', { locale: ptBR })}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {transaction.descricao}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {categoriaLabels[transaction.categoria as Categoria]}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span
                        className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                          transaction.tipo === 'RECEITA'
                            ? 'bg-green-100 text-green-800'
                            : 'bg-red-100 text-red-800'
                        }`}
                      >
                        {tipoTransacaoLabels[transaction.tipo as TipoTransacao]}
                      </span>
                    </td>
                    <td
                      className={`px-6 py-4 whitespace-nowrap text-sm font-medium text-right ${
                        transaction.tipo === 'RECEITA' ? 'text-green-600' : 'text-red-600'
                      }`}
                    >
                      {transaction.tipo === 'RECEITA' ? '+' : '-'}R${' '}
                      {transaction.valor.toLocaleString('pt-BR', {
                        minimumFractionDigits: 2,
                      })}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm">
                      <div className="flex justify-end space-x-2">
                        <button
                          onClick={() => handleOpenEdit(transaction)}
                          className="text-blue-600 hover:text-blue-800"
                        >
                          <Edit2 className="h-4 w-4" />
                        </button>
                        <button
                          onClick={() => handleDelete(transaction)}
                          className="text-red-600 hover:text-red-800"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            {/* Paginação */}
            <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
              <div className="flex-1 flex justify-between sm:hidden">
                <Button
                  variant="secondary"
                  size="sm"
                  disabled={!pagination.hasPrevious}
                  onClick={() => handlePageChange(pagination.page - 1)}
                >
                  Anterior
                </Button>
                <Button
                  variant="secondary"
                  size="sm"
                  disabled={!pagination.hasNext}
                  onClick={() => handlePageChange(pagination.page + 1)}
                >
                  Próximo
                </Button>
              </div>
              <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
                <div>
                  <p className="text-sm text-gray-700">
                    Mostrando{' '}
                    <span className="font-medium">
                      {pagination.page * pagination.size + 1}
                    </span>{' '}
                    a{' '}
                    <span className="font-medium">
                      {Math.min(
                        (pagination.page + 1) * pagination.size,
                        pagination.totalElements
                      )}
                    </span>{' '}
                    de{' '}
                    <span className="font-medium">{pagination.totalElements}</span>{' '}
                    resultados
                  </p>
                </div>
                <div>
                  <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px">
                    <button
                      onClick={() => handlePageChange(pagination.page - 1)}
                      disabled={!pagination.hasPrevious}
                      className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <ChevronLeft className="h-5 w-5" />
                    </button>
                    <span className="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-700">
                      Página {pagination.page + 1} de {pagination.totalPages}
                    </span>
                    <button
                      onClick={() => handlePageChange(pagination.page + 1)}
                      disabled={!pagination.hasNext}
                      className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <ChevronRight className="h-5 w-5" />
                    </button>
                  </nav>
                </div>
              </div>
            </div>
          </>
        )}
      </div>

      {/* Modal de Criar/Editar */}
      <Modal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        title={editMode ? 'Editar Transação' : 'Nova Transação'}
        size="md"
      >
        <TransactionForm
          initialData={formInitialData}
          onSubmit={handleFormSubmit}
          onCancel={handleCloseModal}
          isLoading={isLoading}
          submitLabel={editMode ? 'Salvar' : 'Criar'}
        />
      </Modal>
    </div>
  )
}
