import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import TransactionForm, { sugerirCategoria } from '../TransactionForm'
import { TransactionFormData } from '@/lib/schemas'

// Mock toast
vi.mock('react-hot-toast', () => ({
  default: {
    success: vi.fn(),
    error: vi.fn(),
  },
}))

describe('TransactionForm', () => {
  const mockOnSubmit = vi.fn()
  const mockOnCancel = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
  })

  const renderForm = (props = {}) => {
    return render(
      <TransactionForm
        onSubmit={mockOnSubmit}
        onCancel={mockOnCancel}
        submitLabel="Criar Transação"
        {...props}
      />
    )
  }

  describe('Form Rendering', () => {
    it('should render all form fields', () => {
      renderForm()

      expect(screen.getByLabelText(/Tipo/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/Valor/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/Descrição/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/Categoria/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/Data/i)).toBeInTheDocument()
    })

    it('should render submit and cancel buttons', () => {
      renderForm()

      expect(screen.getByRole('button', { name: /Criar Transação/i })).toBeInTheDocument()
      expect(screen.getByRole('button', { name: /Cancelar/i })).toBeInTheDocument()
    })

    it('should populate form with initial data when provided', () => {
      const initialData: TransactionFormData = {
        valor: 100.50,
        descricao: 'Compra teste',
        categoria: 'ALIMENTACAO',
        tipo: 'DESPESA',
        data: '2024-01-15',
      }

      renderForm({ initialData })

      expect(screen.getByLabelText(/Valor/i)).toHaveValue(100.50)
      expect(screen.getByLabelText(/Descrição/i)).toHaveValue('Compra teste')
      expect(screen.getByLabelText(/Categoria/i)).toHaveValue('ALIMENTACAO')
      expect(screen.getByLabelText(/Tipo/i)).toHaveValue('DESPESA')
      expect(screen.getByLabelText(/Data/i)).toHaveValue('2024-01-15')
    })

    it('should set current date as default', () => {
      renderForm()

      const today = new Date().toISOString().split('T')[0]
      expect(screen.getByLabelText(/Data/i)).toHaveValue(today)
    })
  })

  describe('Form Validation - Requirement 3.1', () => {
    it('should show error when valor is empty', async () => {
      const user = userEvent.setup()
      renderForm()

      const submitButton = screen.getByRole('button', { name: /Criar Transação/i })
      await user.click(submitButton)

      await waitFor(() => {
        // Zod returns "Valor deve ser um número" for empty number input
        expect(screen.getByText(/Valor deve ser um número/i)).toBeInTheDocument()
      })
    })

    it('should show error when valor is zero', async () => {
      const user = userEvent.setup()
      renderForm()

      const valorInput = screen.getByLabelText(/Valor/i)
      await user.clear(valorInput)
      await user.type(valorInput, '0')

      const submitButton = screen.getByRole('button', { name: /Criar Transação/i })
      await user.click(submitButton)

      await waitFor(() => {
        expect(screen.getByText(/Valor deve ser maior que zero/i)).toBeInTheDocument()
      })
    })

    it('should show error when valor is negative', async () => {
      const user = userEvent.setup()
      renderForm()

      const valorInput = screen.getByLabelText(/Valor/i)
      await user.clear(valorInput)
      await user.type(valorInput, '-50')

      const submitButton = screen.getByRole('button', { name: /Criar Transação/i })
      await user.click(submitButton)

      await waitFor(() => {
        expect(screen.getByText(/Valor deve ser maior que zero/i)).toBeInTheDocument()
      })
    })

    it('should accept valid decimal values with 2 decimal places', async () => {
      const user = userEvent.setup()
      renderForm()

      const valorInput = screen.getByLabelText(/Valor/i)
      await user.clear(valorInput)
      await user.type(valorInput, '123.45')

      const tipoSelect = screen.getByLabelText(/Tipo/i)
      await user.selectOptions(tipoSelect, 'DESPESA')

      const descricaoInput = screen.getByLabelText(/Descrição/i)
      await user.type(descricaoInput, 'Compra válida')

      const categoriaSelect = screen.getByLabelText(/Categoria/i)
      await user.selectOptions(categoriaSelect, 'ALIMENTACAO')

      const submitButton = screen.getByRole('button', { name: /Criar Transação/i })
      await user.click(submitButton)

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith(
          expect.objectContaining({
            valor: 123.45,
          })
        )
      })
    })

    it('should show error when descricao is empty', async () => {
      const user = userEvent.setup()
      renderForm()

      const submitButton = screen.getByRole('button', { name: /Criar Transação/i })
      await user.click(submitButton)

      await waitFor(() => {
        expect(screen.getByText(/Descrição é obrigatória/i)).toBeInTheDocument()
      })
    })

    it('should show error when descricao is too short', async () => {
      const user = userEvent.setup()
      renderForm()

      const descricaoInput = screen.getByLabelText(/Descrição/i)
      await user.type(descricaoInput, 'ab')

      const submitButton = screen.getByRole('button', { name: /Criar Transação/i })
      await user.click(submitButton)

      await waitFor(() => {
        expect(screen.getByText(/Descrição deve ter pelo menos 3 caracteres/i)).toBeInTheDocument()
      })
    })

    it('should show error when descricao is too long', async () => {
      const user = userEvent.setup()
      renderForm()

      const descricaoInput = screen.getByLabelText(/Descrição/i)
      const longText = 'a'.repeat(201)
      await user.type(descricaoInput, longText)

      const submitButton = screen.getByRole('button', { name: /Criar Transação/i })
      await user.click(submitButton)

      await waitFor(() => {
        expect(screen.getByText(/Descrição deve ter no máximo 200 caracteres/i)).toBeInTheDocument()
      })
    })

    it('should show error when categoria is not selected', async () => {
      const user = userEvent.setup()
      renderForm()

      const valorInput = screen.getByLabelText(/Valor/i)
      await user.type(valorInput, '100')

      const descricaoInput = screen.getByLabelText(/Descrição/i)
      await user.type(descricaoInput, 'Compra teste')

      const tipoSelect = screen.getByLabelText(/Tipo/i)
      await user.selectOptions(tipoSelect, 'DESPESA')

      const submitButton = screen.getByRole('button', { name: /Criar Transação/i })
      await user.click(submitButton)

      await waitFor(() => {
        expect(mockOnSubmit).not.toHaveBeenCalled()
      })
    })

    it('should show error when tipo is not selected', async () => {
      const user = userEvent.setup()
      renderForm()

      const valorInput = screen.getByLabelText(/Valor/i)
      await user.type(valorInput, '100')

      const descricaoInput = screen.getByLabelText(/Descrição/i)
      await user.type(descricaoInput, 'Compra teste')

      const categoriaSelect = screen.getByLabelText(/Categoria/i)
      await user.selectOptions(categoriaSelect, 'ALIMENTACAO')

      const submitButton = screen.getByRole('button', { name: /Criar Transação/i })
      await user.click(submitButton)

      await waitFor(() => {
        expect(mockOnSubmit).not.toHaveBeenCalled()
      })
    })
  })

  describe('Form Submission', () => {
    it('should submit valid form data', async () => {
      const user = userEvent.setup()
      mockOnSubmit.mockResolvedValue(undefined)
      renderForm()

      // Fill all required fields
      const tipoSelect = screen.getByLabelText(/Tipo/i)
      await user.selectOptions(tipoSelect, 'DESPESA')

      const valorInput = screen.getByLabelText(/Valor/i)
      await user.type(valorInput, '150.75')

      const descricaoInput = screen.getByLabelText(/Descrição/i)
      await user.type(descricaoInput, 'Compra no supermercado')

      const categoriaSelect = screen.getByLabelText(/Categoria/i)
      await user.selectOptions(categoriaSelect, 'ALIMENTACAO')

      const dataInput = screen.getByLabelText(/Data/i)
      await user.clear(dataInput)
      await user.type(dataInput, '2024-01-15')

      const submitButton = screen.getByRole('button', { name: /Criar Transação/i })
      await user.click(submitButton)

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith({
          tipo: 'DESPESA',
          valor: 150.75,
          descricao: 'Compra no supermercado',
          categoria: 'ALIMENTACAO',
          data: '2024-01-15',
        })
      })
    })

    it('should call onCancel when cancel button is clicked', async () => {
      const user = userEvent.setup()
      renderForm()

      const cancelButton = screen.getByRole('button', { name: /Cancelar/i })
      await user.click(cancelButton)

      expect(mockOnCancel).toHaveBeenCalled()
    })

    it('should disable form during submission', async () => {
      const user = userEvent.setup()
      mockOnSubmit.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)))
      renderForm()

      // Fill form
      const tipoSelect = screen.getByLabelText(/Tipo/i)
      await user.selectOptions(tipoSelect, 'DESPESA')

      const valorInput = screen.getByLabelText(/Valor/i)
      await user.type(valorInput, '100')

      const descricaoInput = screen.getByLabelText(/Descrição/i)
      await user.type(descricaoInput, 'Teste')

      const categoriaSelect = screen.getByLabelText(/Categoria/i)
      await user.selectOptions(categoriaSelect, 'ALIMENTACAO')

      const submitButton = screen.getByRole('button', { name: /Criar Transação/i })
      await user.click(submitButton)

      // Check if button is disabled during submission
      expect(submitButton).toBeDisabled()
    })
  })

  describe('Automatic Categorization - Requirement 3.4', () => {
    it('should suggest ALIMENTACAO for food-related descriptions', async () => {
      const user = userEvent.setup()
      renderForm()

      const descricaoInput = screen.getByLabelText(/Descrição/i)
      await user.type(descricaoInput, 'Compra no supermercado')

      await waitFor(() => {
        expect(screen.getByText(/Sugestão:/i)).toBeInTheDocument()
        // Use more specific selector to avoid matching the select option
        const suggestionText = screen.getByText((content, element) => {
          return element?.className === 'font-medium text-blue-700' && content === 'Alimentação'
        })
        expect(suggestionText).toBeInTheDocument()
      })
    })

    it('should suggest TRANSPORTE for transport-related descriptions', async () => {
      const user = userEvent.setup()
      renderForm()

      const descricaoInput = screen.getByLabelText(/Descrição/i)
      await user.type(descricaoInput, 'Corrida de Uber')

      await waitFor(() => {
        expect(screen.getByText(/Sugestão:/i)).toBeInTheDocument()
        // Use more specific selector to avoid matching the select option
        const suggestionText = screen.getByText((content, element) => {
          return element?.className === 'font-medium text-blue-700' && content === 'Transporte'
        })
        expect(suggestionText).toBeInTheDocument()
      })
    })

    it('should suggest MORADIA for housing-related descriptions', async () => {
      const user = userEvent.setup()
      renderForm()

      const descricaoInput = screen.getByLabelText(/Descrição/i)
      await user.type(descricaoInput, 'Pagamento de aluguel')

      await waitFor(() => {
        expect(screen.getByText(/Sugestão:/i)).toBeInTheDocument()
        // Use more specific selector to avoid matching the select option
        const suggestionText = screen.getByText((content, element) => {
          return element?.className === 'font-medium text-blue-700' && content === 'Moradia'
        })
        expect(suggestionText).toBeInTheDocument()
      })
    })

    it('should suggest SAUDE for health-related descriptions', async () => {
      const user = userEvent.setup()
      renderForm()

      const descricaoInput = screen.getByLabelText(/Descrição/i)
      await user.type(descricaoInput, 'Consulta médica')

      await waitFor(() => {
        expect(screen.getByText(/Sugestão:/i)).toBeInTheDocument()
        // Use more specific selector to avoid matching the select option
        const suggestionText = screen.getByText((content, element) => {
          return element?.className === 'font-medium text-blue-700' && content === 'Saúde'
        })
        expect(suggestionText).toBeInTheDocument()
      })
    })

    it('should apply suggested category when clicking Aplicar', async () => {
      const user = userEvent.setup()
      renderForm()

      const descricaoInput = screen.getByLabelText(/Descrição/i)
      await user.type(descricaoInput, 'Compra no mercado')

      await waitFor(() => {
        expect(screen.getByText(/Sugestão:/i)).toBeInTheDocument()
      })

      const aplicarButton = screen.getByRole('button', { name: /Aplicar/i })
      await user.click(aplicarButton)

      await waitFor(() => {
        const categoriaSelect = screen.getByLabelText(/Categoria/i) as HTMLSelectElement
        expect(categoriaSelect.value).toBe('ALIMENTACAO')
      })
    })

    it('should dismiss suggestion when clicking Ignorar', async () => {
      const user = userEvent.setup()
      renderForm()

      const descricaoInput = screen.getByLabelText(/Descrição/i)
      await user.type(descricaoInput, 'Compra no mercado')

      await waitFor(() => {
        expect(screen.getByText(/Sugestão:/i)).toBeInTheDocument()
      })

      const ignorarButton = screen.getByRole('button', { name: /Ignorar/i })
      await user.click(ignorarButton)

      await waitFor(() => {
        expect(screen.queryByText(/Sugestão:/i)).not.toBeInTheDocument()
      })
    })

    it('should not suggest category for very short descriptions', async () => {
      const user = userEvent.setup()
      renderForm()

      const descricaoInput = screen.getByLabelText(/Descrição/i)
      await user.type(descricaoInput, 'ab')

      await waitFor(() => {
        expect(screen.queryByText(/Sugestão:/i)).not.toBeInTheDocument()
      })
    })

    it('should suggest SALARIO for salary-related RECEITA', async () => {
      const user = userEvent.setup()
      renderForm()

      const tipoSelect = screen.getByLabelText(/Tipo/i)
      await user.selectOptions(tipoSelect, 'RECEITA')

      const descricaoInput = screen.getByLabelText(/Descrição/i)
      await user.type(descricaoInput, 'Pagamento de salário')

      await waitFor(() => {
        expect(screen.getByText(/Sugestão:/i)).toBeInTheDocument()
        // Use more specific selector to avoid matching the select option
        const suggestionText = screen.getByText((content, element) => {
          return element?.className === 'font-medium text-blue-700' && content === 'Salário'
        })
        expect(suggestionText).toBeInTheDocument()
      })
    })
  })

  describe('Category Suggestion Logic', () => {
    it('should return ALIMENTACAO for food keywords', () => {
      expect(sugerirCategoria('Compra no supermercado', 'DESPESA')).toBe('ALIMENTACAO')
      expect(sugerirCategoria('Pedido no ifood', 'DESPESA')).toBe('ALIMENTACAO')
      expect(sugerirCategoria('Almoço no restaurante', 'DESPESA')).toBe('ALIMENTACAO')
    })

    it('should return TRANSPORTE for transport keywords', () => {
      expect(sugerirCategoria('Corrida de uber', 'DESPESA')).toBe('TRANSPORTE')
      expect(sugerirCategoria('Gasolina do carro', 'DESPESA')).toBe('TRANSPORTE')
      expect(sugerirCategoria('Passagem de ônibus', 'DESPESA')).toBe('TRANSPORTE')
    })

    it('should return MORADIA for housing keywords', () => {
      expect(sugerirCategoria('Pagamento de aluguel', 'DESPESA')).toBe('MORADIA')
      expect(sugerirCategoria('Conta de luz', 'DESPESA')).toBe('MORADIA')
      expect(sugerirCategoria('Condomínio', 'DESPESA')).toBe('MORADIA')
    })

    it('should return SAUDE for health keywords', () => {
      expect(sugerirCategoria('Consulta médica', 'DESPESA')).toBe('SAUDE')
      expect(sugerirCategoria('Compra na farmácia', 'DESPESA')).toBe('SAUDE')
      expect(sugerirCategoria('Mensalidade da academia', 'DESPESA')).toBe('SAUDE')
    })

    it('should return SALARIO for salary keywords with RECEITA', () => {
      expect(sugerirCategoria('Pagamento de salário', 'RECEITA')).toBe('SALARIO')
      expect(sugerirCategoria('Recebimento do ordenado', 'RECEITA')).toBe('SALARIO')
    })

    it('should return FREELANCE for freelance keywords with RECEITA', () => {
      // Note: Some words may match other categories first, so use specific freelance terms
      expect(sugerirCategoria('Freela de design', 'RECEITA')).toBe('FREELANCE')
      expect(sugerirCategoria('Projeto freelance', 'RECEITA')).toBe('FREELANCE')
      expect(sugerirCategoria('Trabalho autônomo', 'RECEITA')).toBe('FREELANCE')
    })

    it('should return null for very short descriptions', () => {
      expect(sugerirCategoria('ab', 'DESPESA')).toBeNull()
      expect(sugerirCategoria('', 'DESPESA')).toBeNull()
    })

    it('should return null for descriptions without matching keywords', () => {
      expect(sugerirCategoria('xyz random text', 'DESPESA')).toBeNull()
    })

    it('should return OUTROS for RECEITA without specific category', () => {
      expect(sugerirCategoria('Venda de item usado', 'RECEITA')).toBe('OUTROS')
    })

    it('should be case-insensitive', () => {
      expect(sugerirCategoria('SUPERMERCADO', 'DESPESA')).toBe('ALIMENTACAO')
      expect(sugerirCategoria('Supermercado', 'DESPESA')).toBe('ALIMENTACAO')
      expect(sugerirCategoria('supermercado', 'DESPESA')).toBe('ALIMENTACAO')
    })

    it('should handle partial keyword matches', () => {
      expect(sugerirCategoria('Fui ao mercado hoje', 'DESPESA')).toBe('ALIMENTACAO')
      expect(sugerirCategoria('Paguei o uber', 'DESPESA')).toBe('TRANSPORTE')
    })
  })

  describe('Edge Cases', () => {
    it('should handle form with isLoading prop', () => {
      renderForm({ isLoading: true })

      const submitButton = screen.getByRole('button', { name: /Criar Transação/i })
      expect(submitButton).toBeDisabled()

      const valorInput = screen.getByLabelText(/Valor/i)
      expect(valorInput).toBeDisabled()
    })

    it('should handle very large valid values', async () => {
      const user = userEvent.setup()
      mockOnSubmit.mockResolvedValue(undefined)
      renderForm()

      const tipoSelect = screen.getByLabelText(/Tipo/i)
      await user.selectOptions(tipoSelect, 'RECEITA')

      const valorInput = screen.getByLabelText(/Valor/i)
      await user.type(valorInput, '999999.99')

      const descricaoInput = screen.getByLabelText(/Descrição/i)
      await user.type(descricaoInput, 'Grande transação')

      const categoriaSelect = screen.getByLabelText(/Categoria/i)
      await user.selectOptions(categoriaSelect, 'SALARIO')

      const submitButton = screen.getByRole('button', { name: /Criar Transação/i })
      await user.click(submitButton)

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith(
          expect.objectContaining({
            valor: 999999.99,
          })
        )
      })
    })

    it('should handle special characters in description', async () => {
      const user = userEvent.setup()
      mockOnSubmit.mockResolvedValue(undefined)
      renderForm()

      const tipoSelect = screen.getByLabelText(/Tipo/i)
      await user.selectOptions(tipoSelect, 'DESPESA')

      const valorInput = screen.getByLabelText(/Valor/i)
      await user.type(valorInput, '50')

      const descricaoInput = screen.getByLabelText(/Descrição/i)
      await user.type(descricaoInput, 'Compra @ Loja & Cia - 50% off!')

      const categoriaSelect = screen.getByLabelText(/Categoria/i)
      await user.selectOptions(categoriaSelect, 'OUTROS')

      const submitButton = screen.getByRole('button', { name: /Criar Transação/i })
      await user.click(submitButton)

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith(
          expect.objectContaining({
            descricao: 'Compra @ Loja & Cia - 50% off!',
          })
        )
      })
    })

    it('should not show cancel button when onCancel is not provided', () => {
      render(
        <TransactionForm
          onSubmit={mockOnSubmit}
          submitLabel="Criar"
        />
      )

      expect(screen.queryByRole('button', { name: /Cancelar/i })).not.toBeInTheDocument()
    })
  })
})
