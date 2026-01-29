import { useEffect } from 'react'
import { useValidatedForm, enumToSelectOptions } from '@/lib/form-utils'
import { 
  transactionSchema, 
  categoriaLabels, 
  tipoTransacaoLabels,
  categoriaEnum,
  tipoTransacaoEnum,
  type TransactionFormData 
} from '@/lib/schemas'
import { 
  Form, 
  FormField, 
  FormSection, 
  FormActions, 
  FormInput, 
  FormSelect, 
  Button 
} from '@/components/ui'
import { format } from 'date-fns'
import toast from 'react-hot-toast'

interface EnhancedTransactionFormProps {
  initialData?: Partial<TransactionFormData>
  onSubmit: (data: TransactionFormData) => Promise<void>
  onCancel?: () => void
  isLoading?: boolean
  mode?: 'create' | 'edit'
}

/**
 * Enhanced Transaction Form demonstrating the improved Zod + React Hook Form integration
 * Uses the new form utilities and components for a cleaner, more maintainable implementation
 */
export default function EnhancedTransactionForm({
  initialData,
  onSubmit,
  onCancel,
  isLoading = false,
  mode = 'create'
}: EnhancedTransactionFormProps) {
  // Use the enhanced form hook with automatic Zod integration
  const form = useValidatedForm(transactionSchema, {
    defaultValues: {
      data: format(new Date(), 'yyyy-MM-dd'),
      ...initialData,
    },
  })

  const { reset, watch } = form

  // Watch the transaction type to provide contextual help
  const tipoTransacao = watch('tipo')

  // Reset form when initial data changes (useful for edit mode)
  useEffect(() => {
    if (initialData) {
      reset({
        data: format(new Date(), 'yyyy-MM-dd'),
        ...initialData,
      })
    }
  }, [initialData, reset])

  // Create select options from Zod enums using the utility function
  const categoriaOptions = enumToSelectOptions(categoriaEnum._def.values.reduce((acc: any, value: string) => {
    acc[value] = value
    return acc
  }, {}), categoriaLabels)

  const tipoOptions = enumToSelectOptions(tipoTransacaoEnum._def.values.reduce((acc: any, value: string) => {
    acc[value] = value
    return acc
  }, {}), tipoTransacaoLabels)

  const handleSubmit = async (data: TransactionFormData) => {
    try {
      await onSubmit(data)
      toast.success(
        mode === 'create' 
          ? 'Transação criada com sucesso!' 
          : 'Transação atualizada com sucesso!'
      )
    } catch (error) {
      toast.error('Erro ao salvar transação. Tente novamente.')
      throw error // Re-throw to prevent form from resetting
    }
  }

  const getContextualHelp = () => {
    if (tipoTransacao === 'RECEITA') {
      return 'Registre aqui suas receitas como salário, freelances, vendas, etc.'
    } else if (tipoTransacao === 'DESPESA') {
      return 'Registre aqui seus gastos como compras, contas, serviços, etc.'
    }
    return 'Selecione o tipo da transação para ver dicas específicas.'
  }

  return (
    <Form
      form={form}
      onSubmit={handleSubmit}
      showErrorSummary={true}
      className="max-w-2xl mx-auto"
    >
      <FormSection
        title={mode === 'create' ? 'Nova Transação' : 'Editar Transação'}
        description={getContextualHelp()}
      >
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <FormField>
            <FormInput
              form={form}
              name="valor"
              label="Valor"
              type="number"
              step="0.01"
              min="0"
              placeholder="0,00"
              helperText="Digite o valor da transação"
              registerOptions={{ valueAsNumber: true }}
            />
          </FormField>

          <FormField>
            <FormSelect
              form={form}
              name="tipo"
              label="Tipo"
              options={tipoOptions}
              placeholder="Selecione o tipo"
              helperText="Receita ou despesa"
            />
          </FormField>
        </div>

        <FormField>
          <FormInput
            form={form}
            name="descricao"
            label="Descrição"
            placeholder="Descreva a transação"
            helperText="Seja específico para facilitar a organização"
          />
        </FormField>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <FormField>
            <FormSelect
              form={form}
              name="categoria"
              label="Categoria"
              options={categoriaOptions}
              placeholder="Selecione uma categoria"
              helperText="Escolha a categoria mais apropriada"
            />
          </FormField>

          <FormField>
            <FormInput
              form={form}
              name="data"
              label="Data"
              type="date"
              helperText="Data da transação"
            />
          </FormField>
        </div>
      </FormSection>

      <FormActions>
        {onCancel && (
          <Button
            type="button"
            variant="secondary"
            onClick={onCancel}
            disabled={isLoading}
          >
            Cancelar
          </Button>
        )}
        <Button
          type="submit"
          variant="primary"
          isLoading={isLoading}
        >
          {mode === 'create' ? 'Criar Transação' : 'Salvar Alterações'}
        </Button>
      </FormActions>
    </Form>
  )
}