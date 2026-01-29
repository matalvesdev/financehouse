import { useState, useEffect } from 'react'
import { useImportStore } from '@/stores/importStore'
import { useConfirmStore } from '@/stores/confirmStore'
import { Button } from '@/components/ui'
import FileUpload from '@/components/FileUpload'
import { categoriaLabels, tipoTransacaoLabels, Categoria, TipoTransacao } from '@/lib/schemas'
import { AlertTriangle, CheckCircle, X, ChevronDown, ChevronUp } from 'lucide-react'
import { format } from 'date-fns'
import { ptBR } from 'date-fns/locale'
import toast from 'react-hot-toast'
import { useNavigate } from 'react-router-dom'

export default function ImportPage() {
  const navigate = useNavigate()
  const {
    importResult,
    selectedTransactions,
    isUploading,
    isConfirming,
    error,
    uploadFile,
    confirmImport,
    toggleTransaction,
    selectAllTransactions,
    deselectAllTransactions,
    reset,
    clearError,
  } = useImportStore()

  const { openConfirm } = useConfirmStore()

  const [showDuplicates, setShowDuplicates] = useState(false)

  useEffect(() => {
    if (error) {
      toast.error(error)
      clearError()
    }
  }, [error, clearError])

  const handleFileSelect = async (file: File) => {
    try {
      await uploadFile(file)
      toast.success('Arquivo processado com sucesso!')
    } catch (err) {
      // Error already handled by store
    }
  }

  const handleConfirmImport = () => {
    if (selectedTransactions.size === 0) {
      toast.error('Selecione pelo menos uma transação para importar')
      return
    }

    // Calculate impact details for confirmation (Requirement 9.3)
    const selectedTransactionsList = importResult?.transacoes.filter(t => 
      selectedTransactions.has(t.id)
    ) || []
    
    const totalReceitas = selectedTransactionsList
      .filter(t => t.tipo === 'RECEITA')
      .reduce((sum, t) => sum + t.valor, 0)
    
    const totalDespesas = selectedTransactionsList
      .filter(t => t.tipo === 'DESPESA')
      .reduce((sum, t) => sum + t.valor, 0)
    
    const duplicatesSelected = selectedTransactionsList.filter(t => t.duplicataPotencial).length

    const impactMessage = `
      • ${selectedTransactions.size} transações serão adicionadas ao sistema
      • Receitas: R$ ${totalReceitas.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
      • Despesas: R$ ${totalDespesas.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
      • Impacto no saldo: R$ ${(totalReceitas - totalDespesas).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
      ${duplicatesSelected > 0 ? `• ⚠️ ${duplicatesSelected} possíveis duplicatas incluídas` : ''}
    `.trim()

    openConfirm({
      title: 'Confirmar Importação',
      message: `Você está prestes a importar ${selectedTransactions.size} transações. Esta ação adicionará as transações selecionadas ao sistema e marcará seus dados iniciais como carregados.`,
      impact: impactMessage,
      confirmText: 'Importar',
      cancelText: 'Cancelar',
      variant: 'info',
      timeoutSeconds: 300,
      onConfirm: async () => {
        try {
          await confirmImport()
          toast.success('Transações importadas com sucesso!')
          navigate('/transactions')
        } catch (err) {
          // Erro já tratado
        }
      },
    })
  }

  const handleCancel = () => {
    openConfirm({
      title: 'Cancelar Importação',
      message: 'Tem certeza que deseja cancelar a importação? Os dados processados serão descartados.',
      confirmText: 'Cancelar Importação',
      cancelText: 'Voltar',
      variant: 'warning',
      onConfirm: () => {
        reset()
      },
    })
  }

  const duplicates = importResult?.transacoes.filter((t) => t.duplicataPotencial) || []
  const nonDuplicates = importResult?.transacoes.filter((t) => !t.duplicataPotencial) || []

  if (!importResult) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Importar Planilha</h1>
          <p className="text-gray-600">
            Importe seus dados financeiros de um arquivo Excel ou CSV
          </p>
        </div>

        {/* Upload Component */}
        <FileUpload
          onFileSelect={handleFileSelect}
          isUploading={isUploading}
          maxSizeMB={10}
        />

        {/* Instruções */}
        <div className="bg-gray-50 rounded-lg p-6">
          <h3 className="font-semibold text-gray-900 mb-4">
            Formato esperado do arquivo
          </h3>
          <p className="text-sm text-gray-600 mb-4">
            O arquivo deve conter as seguintes colunas:
          </p>
          <ul className="text-sm text-gray-600 space-y-2">
            <li className="flex items-center">
              <CheckCircle className="h-4 w-4 text-green-500 mr-2" />
              <strong>Data:</strong>&nbsp;Data da transação (formato: DD/MM/YYYY)
            </li>
            <li className="flex items-center">
              <CheckCircle className="h-4 w-4 text-green-500 mr-2" />
              <strong>Descrição:</strong>&nbsp;Descrição da transação
            </li>
            <li className="flex items-center">
              <CheckCircle className="h-4 w-4 text-green-500 mr-2" />
              <strong>Valor:</strong>&nbsp;Valor da transação (positivo para receita, negativo para despesa)
            </li>
            <li className="flex items-center">
              <CheckCircle className="h-4 w-4 text-green-500 mr-2" />
              <strong>Categoria:</strong>&nbsp;Categoria da transação (opcional)
            </li>
          </ul>
        </div>
      </div>
    )
  }

  // Preview das transações
  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Preview da Importação</h1>
          <p className="text-gray-600">
            {importResult.nomeArquivo} -{' '}
            {importResult.transacoesValidas} transações válidas
          </p>
        </div>
        <div className="flex space-x-3">
          <Button variant="secondary" onClick={handleCancel}>
            Cancelar
          </Button>
          <Button
            variant="primary"
            isLoading={isConfirming}
            onClick={handleConfirmImport}
            disabled={selectedTransactions.size === 0}
          >
            Importar {selectedTransactions.size} Transações
          </Button>
        </div>
      </div>

      {/* Resumo */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-white p-4 rounded-lg border">
          <p className="text-sm text-gray-600">Total processadas</p>
          <p className="text-2xl font-bold text-gray-900">
            {importResult.totalTransacoes}
          </p>
        </div>
        <div className="bg-white p-4 rounded-lg border">
          <p className="text-sm text-gray-600">Válidas</p>
          <p className="text-2xl font-bold text-green-600">
            {importResult.transacoesValidas}
          </p>
        </div>
        <div className="bg-white p-4 rounded-lg border">
          <p className="text-sm text-gray-600">Inválidas</p>
          <p className="text-2xl font-bold text-red-600">
            {importResult.transacoesInvalidas}
          </p>
        </div>
        <div className="bg-white p-4 rounded-lg border">
          <p className="text-sm text-gray-600">Duplicatas potenciais</p>
          <p className="text-2xl font-bold text-yellow-600">
            {importResult.duplicatasPotenciais}
          </p>
        </div>
      </div>

      {/* Seleção */}
      <div className="bg-white p-4 rounded-lg border flex justify-between items-center">
        <div className="flex items-center space-x-4">
          <span className="text-sm text-gray-600">
            {selectedTransactions.size} de {importResult.transacoesValidas} selecionadas
          </span>
          <Button variant="ghost" size="sm" onClick={selectAllTransactions}>
            Selecionar todas
          </Button>
          <Button variant="ghost" size="sm" onClick={deselectAllTransactions}>
            Limpar seleção
          </Button>
        </div>
      </div>

      {/* Duplicatas */}
      {duplicates.length > 0 && (
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg">
          <button
            className="w-full px-4 py-3 flex items-center justify-between text-left"
            onClick={() => setShowDuplicates(!showDuplicates)}
          >
            <div className="flex items-center space-x-2">
              <AlertTriangle className="h-5 w-5 text-yellow-600" />
              <span className="font-medium text-yellow-800">
                {duplicates.length} transações podem ser duplicadas
              </span>
            </div>
            {showDuplicates ? (
              <ChevronUp className="h-5 w-5 text-yellow-600" />
            ) : (
              <ChevronDown className="h-5 w-5 text-yellow-600" />
            )}
          </button>
          
          {showDuplicates && (
            <div className="px-4 pb-4">
              <p className="text-sm text-yellow-700 mb-4">
                Estas transações são similares a transações já existentes. Revise antes de importar.
              </p>
              <div className="space-y-2">
                {duplicates.map((transaction) => (
                  <div
                    key={transaction.id}
                    className="flex items-center space-x-4 bg-white p-3 rounded border"
                  >
                    <input
                      type="checkbox"
                      checked={selectedTransactions.has(transaction.id)}
                      onChange={() => toggleTransaction(transaction.id)}
                      className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                    />
                    <span className="text-sm text-gray-600 w-24">
                      {format(new Date(transaction.data), 'dd/MM/yyyy', { locale: ptBR })}
                    </span>
                    <span className="flex-1 text-sm">{transaction.descricao}</span>
                    <span className="text-sm text-gray-500">
                      {categoriaLabels[transaction.categoria as Categoria]}
                    </span>
                    <span
                      className={`text-sm font-medium ${
                        transaction.tipo === 'RECEITA' ? 'text-green-600' : 'text-red-600'
                      }`}
                    >
                      {transaction.tipo === 'RECEITA' ? '+' : '-'}R${' '}
                      {transaction.valor.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Transações válidas */}
      <div className="bg-white rounded-lg border overflow-hidden">
        <div className="px-4 py-3 border-b bg-gray-50">
          <h3 className="font-semibold text-gray-900">Transações para importar</h3>
        </div>
        <div className="divide-y max-h-96 overflow-y-auto">
          {nonDuplicates.map((transaction) => (
            <div
              key={transaction.id}
              className="flex items-center space-x-4 px-4 py-3 hover:bg-gray-50"
            >
              <input
                type="checkbox"
                checked={selectedTransactions.has(transaction.id)}
                onChange={() => toggleTransaction(transaction.id)}
                className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
              />
              <span className="text-sm text-gray-600 w-24">
                {format(new Date(transaction.data), 'dd/MM/yyyy', { locale: ptBR })}
              </span>
              <span className="flex-1 text-sm">{transaction.descricao}</span>
              <span className="text-sm text-gray-500 w-32">
                {categoriaLabels[transaction.categoria as Categoria]}
              </span>
              <span
                className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full w-20 justify-center ${
                  transaction.tipo === 'RECEITA'
                    ? 'bg-green-100 text-green-800'
                    : 'bg-red-100 text-red-800'
                }`}
              >
                {tipoTransacaoLabels[transaction.tipo as TipoTransacao]}
              </span>
              <span
                className={`text-sm font-medium w-28 text-right ${
                  transaction.tipo === 'RECEITA' ? 'text-green-600' : 'text-red-600'
                }`}
              >
                {transaction.tipo === 'RECEITA' ? '+' : '-'}R${' '}
                {transaction.valor.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* Erros */}
      {importResult.erros && importResult.erros.length > 0 && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <div className="flex items-center space-x-2 mb-2">
            <X className="h-5 w-5 text-red-600" />
            <span className="font-medium text-red-800">
              Erros encontrados durante o processamento
            </span>
          </div>
          <ul className="text-sm text-red-700 space-y-1 ml-7">
            {importResult.erros.map((erro, index) => (
              <li key={index}>{erro}</li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}
