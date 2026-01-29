import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Input, Select, Button } from '@/components/ui'
import { 
  transactionSchema, 
  TransactionFormData, 
  categoriaLabels, 
  tipoTransacaoLabels,
  Categoria,
  TipoTransacao 
} from '@/lib/schemas'
import { Sparkles } from 'lucide-react'

interface TransactionFormProps {
  initialData?: TransactionFormData
  onSubmit: (data: TransactionFormData) => Promise<void>
  onCancel?: () => void
  isLoading?: boolean
  submitLabel?: string
}

/**
 * Mapeamento de palavras-chave para categorias automáticas.
 * Baseado em padrões comuns de descrição de transações.
 */
const CATEGORY_KEYWORDS: Record<Categoria, string[]> = {
  ALIMENTACAO: [
    'mercado', 'supermercado', 'padaria', 'restaurante', 'lanchonete',
    'ifood', 'uber eats', 'rappi', 'comida', 'almoço', 'jantar',
    'café', 'pizza', 'hamburguer', 'açougue', 'hortifruti'
  ],
  TRANSPORTE: [
    'uber', '99', 'taxi', 'gasolina', 'combustível', 'ônibus', 'metrô',
    'estacionamento', 'pedágio', 'ipva', 'seguro auto', 'mecânico',
    'oficina', 'pneu', 'revisão', 'transporte'
  ],
  MORADIA: [
    'aluguel', 'condomínio', 'iptu', 'água', 'luz', 'energia', 'gás',
    'internet', 'telefone', 'celular', 'reforma', 'manutenção',
    'móveis', 'decoração', 'limpeza', 'contas'
  ],
  SAUDE: [
    'farmácia', 'remédio', 'médico', 'consulta', 'hospital', 'clínica',
    'dentista', 'exame', 'laboratório', 'plano de saúde', 'convênio',
    'academia', 'fisioterapia', 'psicólogo', 'terapia'
  ],
  EDUCACAO: [
    'escola', 'faculdade', 'universidade', 'curso', 'livro', 'material escolar',
    'mensalidade', 'matrícula', 'apostila', 'aula', 'professor',
    'educação', 'estudo', 'formação'
  ],
  LAZER: [
    'cinema', 'teatro', 'show', 'festa', 'viagem', 'hotel', 'passeio',
    'parque', 'diversão', 'entretenimento', 'streaming', 'netflix',
    'spotify', 'jogo', 'brinquedo', 'hobby'
  ],
  VESTUARIO: [
    'roupa', 'calça', 'camisa', 'sapato', 'tênis', 'vestido', 'blusa',
    'loja', 'shopping', 'moda', 'acessório', 'bolsa', 'relógio'
  ],
  SERVICOS: [
    'cabeleireiro', 'salão', 'barbeiro', 'manicure', 'lavanderia',
    'costureira', 'encanador', 'eletricista', 'serviço', 'manutenção',
    'reparo', 'conserto'
  ],
  INVESTIMENTO: [
    'investimento', 'ação', 'fundo', 'tesouro', 'cdb', 'lci', 'lca',
    'poupança', 'aplicação', 'renda fixa', 'renda variável', 'bolsa',
    'corretora', 'bitcoin', 'cripto'
  ],
  SALARIO: [
    'salário', 'pagamento', 'vencimento', 'ordenado', 'remuneração',
    'pró-labore', 'folha', 'holerite'
  ],
  FREELANCE: [
    'freelance', 'freela', 'bico', 'extra', 'projeto', 'consultoria',
    'serviço prestado', 'trabalho autônomo', 'prestação de serviço'
  ],
  OUTROS: []
}

/**
 * Sugere uma categoria baseada na descrição da transação.
 * Implementa categorização automática simples baseada em palavras-chave.
 * 
 * @param descricao - Descrição da transação
 * @param tipo - Tipo da transação (RECEITA ou DESPESA)
 * @returns Categoria sugerida ou null se não encontrar correspondência
 */
function sugerirCategoria(descricao: string, tipo?: TipoTransacao): Categoria | null {
  if (!descricao || descricao.length < 3) {
    return null
  }

  const descricaoLower = descricao.toLowerCase().trim()
  
  // Busca por palavras-chave em cada categoria
  for (const [categoria, keywords] of Object.entries(CATEGORY_KEYWORDS)) {
    for (const keyword of keywords) {
      if (descricaoLower.includes(keyword.toLowerCase())) {
        // Valida se a categoria é apropriada para o tipo de transação
        if (tipo === 'RECEITA' && !['SALARIO', 'FREELANCE', 'INVESTIMENTO', 'OUTROS'].includes(categoria)) {
          continue
        }
        return categoria as Categoria
      }
    }
  }

  // Se não encontrou correspondência, retorna categoria padrão baseada no tipo
  if (tipo === 'RECEITA') {
    return 'OUTROS'
  }
  
  return null
}

/**
 * Componente de formulário de transação com validação Zod completa
 * e categorização automática baseada em descrição.
 * 
 * Features:
 * - Validação completa com Zod
 * - Categorização automática inteligente
 * - Sugestão visual de categoria
 * - Feedback em tempo real
 * - Suporte para criação e edição
 * 
 * @example
 * ```tsx
 * <TransactionForm
 *   onSubmit={handleCreateTransaction}
 *   onCancel={handleCancel}
 *   submitLabel="Criar Transação"
 * />
 * ```
 */
export default function TransactionForm({
  initialData,
  onSubmit,
  onCancel,
  isLoading = false,
  submitLabel = 'Salvar'
}: TransactionFormProps) {
  const [suggestedCategory, setSuggestedCategory] = useState<Categoria | null>(null)
  const [showSuggestion, setShowSuggestion] = useState(false)

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors, isSubmitting, isDirty }
  } = useForm<TransactionFormData>({
    resolver: zodResolver(transactionSchema),
    defaultValues: initialData || {
      valor: undefined,
      descricao: '',
      categoria: undefined,
      tipo: undefined,
      data: new Date().toISOString().split('T')[0]
    }
  })

  // Watch para categorização automática
  const descricao = watch('descricao')
  const tipo = watch('tipo')
  const categoriaAtual = watch('categoria')

  // Efeito para sugerir categoria baseada na descrição
  useEffect(() => {
    if (!descricao || descricao.length < 3) {
      setSuggestedCategory(null)
      setShowSuggestion(false)
      return
    }

    // Não sugere se já tem categoria selecionada manualmente
    if (categoriaAtual && isDirty) {
      setShowSuggestion(false)
      return
    }

    const categoria = sugerirCategoria(descricao, tipo)
    
    if (categoria && categoria !== categoriaAtual) {
      setSuggestedCategory(categoria)
      setShowSuggestion(true)
    } else {
      setShowSuggestion(false)
    }
  }, [descricao, tipo, categoriaAtual, isDirty])

  // Aplica a sugestão de categoria
  const aplicarSugestao = () => {
    if (suggestedCategory) {
      setValue('categoria', suggestedCategory, { shouldValidate: true })
      setShowSuggestion(false)
    }
  }

  // Descarta a sugestão
  const descartarSugestao = () => {
    setShowSuggestion(false)
    setSuggestedCategory(null)
  }

  const handleFormSubmit = async (data: TransactionFormData) => {
    try {
      await onSubmit(data)
    } catch (error) {
      // Erro será tratado pelo componente pai
      throw error
    }
  }

  const categoriaOptions = Object.entries(categoriaLabels).map(([value, label]) => ({
    value,
    label
  }))

  const tipoOptions = Object.entries(tipoTransacaoLabels).map(([value, label]) => ({
    value,
    label
  }))

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
      {/* Tipo de Transação */}
      <Select
        label="Tipo"
        options={tipoOptions}
        placeholder="Selecione o tipo"
        error={errors.tipo?.message}
        disabled={isLoading || isSubmitting}
        {...register('tipo')}
      />

      {/* Valor */}
      <Input
        type="number"
        step="0.01"
        label="Valor"
        placeholder="0,00"
        error={errors.valor?.message}
        disabled={isLoading || isSubmitting}
        {...register('valor', { valueAsNumber: true })}
      />

      {/* Descrição */}
      <div className="space-y-2">
        <Input
          label="Descrição"
          placeholder="Ex: Compra no supermercado, Pagamento de aluguel..."
          error={errors.descricao?.message}
          disabled={isLoading || isSubmitting}
          {...register('descricao')}
        />
        
        {/* Sugestão de Categoria */}
        {showSuggestion && suggestedCategory && (
          <div className="flex items-center gap-2 p-3 bg-blue-50 border border-blue-200 rounded-lg">
            <Sparkles className="h-4 w-4 text-blue-600 flex-shrink-0" />
            <div className="flex-1 text-sm">
              <span className="text-gray-700">Sugestão: </span>
              <span className="font-medium text-blue-700">
                {categoriaLabels[suggestedCategory]}
              </span>
            </div>
            <div className="flex gap-2">
              <button
                type="button"
                onClick={aplicarSugestao}
                className="text-xs px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors"
              >
                Aplicar
              </button>
              <button
                type="button"
                onClick={descartarSugestao}
                className="text-xs px-3 py-1 bg-gray-200 text-gray-700 rounded hover:bg-gray-300 transition-colors"
              >
                Ignorar
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Categoria */}
      <Select
        label="Categoria"
        options={categoriaOptions}
        placeholder="Selecione uma categoria"
        error={errors.categoria?.message}
        disabled={isLoading || isSubmitting}
        {...register('categoria')}
      />

      {/* Data */}
      <Input
        type="date"
        label="Data"
        error={errors.data?.message}
        disabled={isLoading || isSubmitting}
        {...register('data')}
      />

      {/* Botões de Ação */}
      <div className="flex justify-end gap-3 pt-4">
        {onCancel && (
          <Button
            type="button"
            variant="secondary"
            onClick={onCancel}
            disabled={isLoading || isSubmitting}
          >
            Cancelar
          </Button>
        )}
        <Button
          type="submit"
          variant="primary"
          isLoading={isLoading || isSubmitting}
          disabled={isLoading || isSubmitting}
        >
          {submitLabel}
        </Button>
      </div>
    </form>
  )
}

// Export da função de sugestão para testes
export { sugerirCategoria }
