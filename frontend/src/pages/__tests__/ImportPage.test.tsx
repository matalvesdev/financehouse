import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import ImportPage from '../ImportPage'
import { useImportStore } from '@/stores/importStore'
import { useConfirmStore } from '@/stores/confirmStore'
import type { ImportResult } from '@/types'

// Mock stores
vi.mock('@/stores/importStore')
vi.mock('@/stores/confirmStore')

// Mock react-router-dom navigate
const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  }
})

// Mock toast
vi.mock('react-hot-toast', () => ({
  default: {
    success: vi.fn(),
    error: vi.fn(),
  },
}))

const mockImportResult: ImportResult = {
  id: 'import-123',
  nomeArquivo: 'financas.xlsx',
  status: 'CONCLUIDA',
  totalTransacoes: 10,
  transacoesValidas: 8,
  transacoesInvalidas: 2,
  duplicatasPotenciais: 2,
  transacoes: [
    {
      id: 'trans-1',
      valor: 100.0,
      descricao: 'Salário',
      categoria: 'SALARIO',
      tipo: 'RECEITA',
      data: '2024-01-15',
      duplicataPotencial: false,
      selecionada: true,
    },
    {
      id: 'trans-2',
      valor: 50.0,
      descricao: 'Supermercado',
      categoria: 'ALIMENTACAO',
      tipo: 'DESPESA',
      data: '2024-01-16',
      duplicataPotencial: false,
      selecionada: true,
    },
    {
      id: 'trans-3',
      valor: 30.0,
      descricao: 'Restaurante',
      categoria: 'ALIMENTACAO',
      tipo: 'DESPESA',
      data: '2024-01-17',
      duplicataPotencial: true,
      selecionada: false,
    },
    {
      id: 'trans-4',
      valor: 20.0,
      descricao: 'Uber',
      categoria: 'TRANSPORTE',
      tipo: 'DESPESA',
      data: '2024-01-18',
      duplicataPotencial: true,
      selecionada: false,
    },
  ],
  erros: ['Linha 5: Data inválida', 'Linha 8: Valor ausente'],
  criadoEm: '2024-01-20T10:00:00Z',
}

describe('ImportPage - Preview and Confirmation', () => {
  const mockUploadFile = vi.fn()
  const mockConfirmImport = vi.fn()
  const mockToggleTransaction = vi.fn()
  const mockSelectAllTransactions = vi.fn()
  const mockDeselectAllTransactions = vi.fn()
  const mockReset = vi.fn()
  const mockClearError = vi.fn()
  const mockOpenConfirm = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    mockNavigate.mockClear()

    // Setup default mock implementations
    vi.mocked(useImportStore).mockReturnValue({
      importResult: null,
      selectedTransactions: new Set(),
      isUploading: false,
      isConfirming: false,
      error: null,
      uploadProgress: 0,
      uploadFile: mockUploadFile,
      confirmImport: mockConfirmImport,
      toggleTransaction: mockToggleTransaction,
      selectAllTransactions: mockSelectAllTransactions,
      deselectAllTransactions: mockDeselectAllTransactions,
      reset: mockReset,
      clearError: mockClearError,
    })

    vi.mocked(useConfirmStore).mockReturnValue({
      isOpen: false,
      options: null,
      timeRemaining: null,
      openConfirm: mockOpenConfirm,
      closeConfirm: vi.fn(),
      confirm: vi.fn(),
      setTimeRemaining: vi.fn(),
    })
  })

  describe('Upload State', () => {
    it('should display upload component when no import result exists', () => {
      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      expect(screen.getByText('Importar Planilha')).toBeInTheDocument()
      expect(
        screen.getByText('Importe seus dados financeiros de um arquivo Excel ou CSV')
      ).toBeInTheDocument()
      expect(screen.getByText('Formato esperado do arquivo')).toBeInTheDocument()
    })

    it('should display file format instructions', () => {
      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      expect(screen.getByText(/Data:/)).toBeInTheDocument()
      expect(screen.getByText(/Descrição:/)).toBeInTheDocument()
      expect(screen.getByText(/Valor:/)).toBeInTheDocument()
      expect(screen.getByText(/Categoria:/)).toBeInTheDocument()
    })
  })

  describe('Preview Display - Requirement 2.3, 9.3', () => {
    beforeEach(() => {
      vi.mocked(useImportStore).mockReturnValue({
        importResult: mockImportResult,
        selectedTransactions: new Set(['trans-1', 'trans-2']),
        isUploading: false,
        isConfirming: false,
        error: null,
        uploadProgress: 100,
        uploadFile: mockUploadFile,
        confirmImport: mockConfirmImport,
        toggleTransaction: mockToggleTransaction,
        selectAllTransactions: mockSelectAllTransactions,
        deselectAllTransactions: mockDeselectAllTransactions,
        reset: mockReset,
        clearError: mockClearError,
      })
    })

    it('should display preview header with file name and valid transactions count', () => {
      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      expect(screen.getByText('Preview da Importação')).toBeInTheDocument()
      expect(screen.getByText(/financas.xlsx/)).toBeInTheDocument()
      expect(screen.getByText(/8 transações válidas/)).toBeInTheDocument()
    })

    it('should display summary statistics correctly', () => {
      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      // Get all summary cards
      const summaryCards = screen.getAllByRole('generic').filter(
        (el) => el.className.includes('bg-white p-4 rounded-lg border')
      )

      // Total processadas
      expect(screen.getByText('Total processadas')).toBeInTheDocument()
      expect(screen.getByText('10')).toBeInTheDocument()

      // Válidas
      expect(screen.getByText('Válidas')).toBeInTheDocument()
      expect(screen.getByText('8')).toBeInTheDocument()

      // Inválidas
      const invalidasCard = summaryCards.find((card) =>
        card.textContent?.includes('Inválidas')
      )
      expect(invalidasCard).toBeDefined()
      expect(invalidasCard?.textContent).toContain('2')

      // Duplicatas potenciais
      const duplicatesCard = summaryCards.find((card) =>
        card.textContent?.includes('Duplicatas potenciais')
      )
      expect(duplicatesCard).toBeDefined()
      expect(duplicatesCard?.textContent).toContain('2')
    })

    it('should display selection count correctly', () => {
      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      expect(screen.getByText('2 de 8 selecionadas')).toBeInTheDocument()
    })

    it('should highlight potential duplicates in separate section - Requirement 2.3', () => {
      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      const duplicateSection = screen.getByText(/2 transações podem ser duplicadas/)
      expect(duplicateSection).toBeInTheDocument()
    })

    it('should display error messages when present', () => {
      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      expect(screen.getByText('Erros encontrados durante o processamento')).toBeInTheDocument()
      expect(screen.getByText('Linha 5: Data inválida')).toBeInTheDocument()
      expect(screen.getByText('Linha 8: Valor ausente')).toBeInTheDocument()
    })
  })

  describe('Transaction Selection', () => {
    beforeEach(() => {
      vi.mocked(useImportStore).mockReturnValue({
        importResult: mockImportResult,
        selectedTransactions: new Set(['trans-1', 'trans-2']),
        isUploading: false,
        isConfirming: false,
        error: null,
        uploadProgress: 100,
        uploadFile: mockUploadFile,
        confirmImport: mockConfirmImport,
        toggleTransaction: mockToggleTransaction,
        selectAllTransactions: mockSelectAllTransactions,
        deselectAllTransactions: mockDeselectAllTransactions,
        reset: mockReset,
        clearError: mockClearError,
      })
    })

    it('should call selectAllTransactions when "Selecionar todas" is clicked', async () => {
      const user = userEvent.setup()
      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      const selectAllButton = screen.getByText('Selecionar todas')
      await user.click(selectAllButton)

      expect(mockSelectAllTransactions).toHaveBeenCalledTimes(1)
    })

    it('should call deselectAllTransactions when "Limpar seleção" is clicked', async () => {
      const user = userEvent.setup()
      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      const clearButton = screen.getByText('Limpar seleção')
      await user.click(clearButton)

      expect(mockDeselectAllTransactions).toHaveBeenCalledTimes(1)
    })

    it('should call toggleTransaction when checkbox is clicked', async () => {
      const user = userEvent.setup()
      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      const checkboxes = screen.getAllByRole('checkbox')
      await user.click(checkboxes[0])

      expect(mockToggleTransaction).toHaveBeenCalled()
    })
  })

  describe('Confirmation System - Requirements 9.1, 9.3', () => {
    beforeEach(() => {
      vi.mocked(useImportStore).mockReturnValue({
        importResult: mockImportResult,
        selectedTransactions: new Set(['trans-1', 'trans-2']),
        isUploading: false,
        isConfirming: false,
        error: null,
        uploadProgress: 100,
        uploadFile: mockUploadFile,
        confirmImport: mockConfirmImport,
        toggleTransaction: mockToggleTransaction,
        selectAllTransactions: mockSelectAllTransactions,
        deselectAllTransactions: mockDeselectAllTransactions,
        reset: mockReset,
        clearError: mockClearError,
      })
    })

    it('should show error when trying to import with no transactions selected', async () => {
      const user = userEvent.setup()
      
      // Mock empty selection
      vi.mocked(useImportStore).mockReturnValue({
        importResult: mockImportResult,
        selectedTransactions: new Set(),
        isUploading: false,
        isConfirming: false,
        error: null,
        uploadProgress: 100,
        uploadFile: mockUploadFile,
        confirmImport: mockConfirmImport,
        toggleTransaction: mockToggleTransaction,
        selectAllTransactions: mockSelectAllTransactions,
        deselectAllTransactions: mockDeselectAllTransactions,
        reset: mockReset,
        clearError: mockClearError,
      })

      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      const importButton = screen.getByText(/Importar 0 Transações/)
      await user.click(importButton)

      // Should not open confirmation dialog
      expect(mockOpenConfirm).not.toHaveBeenCalled()
    })

    it('should open confirmation dialog with action details - Requirement 9.1', async () => {
      const user = userEvent.setup()
      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      const importButton = screen.getByText(/Importar 2 Transações/)
      await user.click(importButton)

      expect(mockOpenConfirm).toHaveBeenCalledTimes(1)
      const confirmOptions = mockOpenConfirm.mock.calls[0][0]
      
      expect(confirmOptions.title).toBe('Confirmar Importação')
      expect(confirmOptions.message).toContain('2 transações')
      expect(confirmOptions.message).toContain('dados iniciais como carregados')
    })

    it('should show impact of import action - Requirement 9.3', async () => {
      const user = userEvent.setup()
      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      const importButton = screen.getByText(/Importar 2 Transações/)
      await user.click(importButton)

      const confirmOptions = mockOpenConfirm.mock.calls[0][0]
      
      // Should include impact details
      expect(confirmOptions.impact).toBeDefined()
      expect(confirmOptions.impact).toContain('2 transações serão adicionadas')
      expect(confirmOptions.impact).toContain('Receitas:')
      expect(confirmOptions.impact).toContain('Despesas:')
      expect(confirmOptions.impact).toContain('Impacto no saldo:')
    })

    it('should calculate impact correctly with mixed transactions', async () => {
      const user = userEvent.setup()
      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      const importButton = screen.getByText(/Importar 2 Transações/)
      await user.click(importButton)

      const confirmOptions = mockOpenConfirm.mock.calls[0][0]
      
      // trans-1: RECEITA 100.00
      // trans-2: DESPESA 50.00
      // Impact: +50.00
      expect(confirmOptions.impact).toContain('R$ 100,00') // Receitas
      expect(confirmOptions.impact).toContain('R$ 50,00') // Despesas
      expect(confirmOptions.impact).toContain('R$ 50,00') // Impacto no saldo
    })

    it('should warn about duplicates in impact message when duplicates are selected', async () => {
      const user = userEvent.setup()
      
      // Mock with duplicate selected
      vi.mocked(useImportStore).mockReturnValue({
        importResult: mockImportResult,
        selectedTransactions: new Set(['trans-1', 'trans-3']), // trans-3 is duplicate
        isUploading: false,
        isConfirming: false,
        error: null,
        uploadProgress: 100,
        uploadFile: mockUploadFile,
        confirmImport: mockConfirmImport,
        toggleTransaction: mockToggleTransaction,
        selectAllTransactions: mockSelectAllTransactions,
        deselectAllTransactions: mockDeselectAllTransactions,
        reset: mockReset,
        clearError: mockClearError,
      })

      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      const importButton = screen.getByText(/Importar 2 Transações/)
      await user.click(importButton)

      const confirmOptions = mockOpenConfirm.mock.calls[0][0]
      expect(confirmOptions.impact).toContain('1 possíveis duplicatas incluídas')
    })

    it('should set timeout to 5 minutes (300 seconds)', async () => {
      const user = userEvent.setup()
      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      const importButton = screen.getByText(/Importar 2 Transações/)
      await user.click(importButton)

      const confirmOptions = mockOpenConfirm.mock.calls[0][0]
      expect(confirmOptions.timeoutSeconds).toBe(300)
    })

    it('should navigate to transactions page after successful import', async () => {
      const user = userEvent.setup()
      mockConfirmImport.mockResolvedValueOnce(undefined)

      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      const importButton = screen.getByText(/Importar 2 Transações/)
      await user.click(importButton)

      // Execute the onConfirm callback
      const confirmOptions = mockOpenConfirm.mock.calls[0][0]
      await confirmOptions.onConfirm()

      await waitFor(() => {
        expect(mockConfirmImport).toHaveBeenCalledTimes(1)
        expect(mockNavigate).toHaveBeenCalledWith('/transactions')
      })
    })
  })

  describe('Cancel Import', () => {
    beforeEach(() => {
      vi.mocked(useImportStore).mockReturnValue({
        importResult: mockImportResult,
        selectedTransactions: new Set(['trans-1', 'trans-2']),
        isUploading: false,
        isConfirming: false,
        error: null,
        uploadProgress: 100,
        uploadFile: mockUploadFile,
        confirmImport: mockConfirmImport,
        toggleTransaction: mockToggleTransaction,
        selectAllTransactions: mockSelectAllTransactions,
        deselectAllTransactions: mockDeselectAllTransactions,
        reset: mockReset,
        clearError: mockClearError,
      })
    })

    it('should open confirmation dialog when cancel is clicked', async () => {
      const user = userEvent.setup()
      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      const cancelButton = screen.getByText('Cancelar')
      await user.click(cancelButton)

      expect(mockOpenConfirm).toHaveBeenCalledTimes(1)
      const confirmOptions = mockOpenConfirm.mock.calls[0][0]
      
      expect(confirmOptions.title).toBe('Cancelar Importação')
      expect(confirmOptions.message).toContain('dados processados serão descartados')
      expect(confirmOptions.variant).toBe('warning')
    })

    it('should reset import state when cancel is confirmed', async () => {
      const user = userEvent.setup()
      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      const cancelButton = screen.getByText('Cancelar')
      await user.click(cancelButton)

      // Execute the onConfirm callback
      const confirmOptions = mockOpenConfirm.mock.calls[0][0]
      confirmOptions.onConfirm()

      expect(mockReset).toHaveBeenCalledTimes(1)
    })
  })

  describe('Duplicate Transactions Section', () => {
    beforeEach(() => {
      vi.mocked(useImportStore).mockReturnValue({
        importResult: mockImportResult,
        selectedTransactions: new Set(['trans-1', 'trans-2']),
        isUploading: false,
        isConfirming: false,
        error: null,
        uploadProgress: 100,
        uploadFile: mockUploadFile,
        confirmImport: mockConfirmImport,
        toggleTransaction: mockToggleTransaction,
        selectAllTransactions: mockSelectAllTransactions,
        deselectAllTransactions: mockDeselectAllTransactions,
        reset: mockReset,
        clearError: mockClearError,
      })
    })

    it('should toggle duplicate section visibility when clicked', async () => {
      const user = userEvent.setup()
      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      const duplicateHeader = screen.getByText(/2 transações podem ser duplicadas/)
      
      // Initially collapsed - should not show duplicate transactions
      expect(screen.queryByText('Restaurante')).not.toBeInTheDocument()

      // Click to expand
      await user.click(duplicateHeader)

      // Should now show duplicate transactions
      await waitFor(() => {
        expect(screen.getByText('Restaurante')).toBeInTheDocument()
        expect(screen.getByText('Uber')).toBeInTheDocument()
      })
    })

    it('should display warning message about duplicates', async () => {
      const user = userEvent.setup()
      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      const duplicateHeader = screen.getByText(/2 transações podem ser duplicadas/)
      await user.click(duplicateHeader)

      await waitFor(() => {
        expect(
          screen.getByText(/similares a transações já existentes/)
        ).toBeInTheDocument()
      })
    })
  })

  describe('Loading States', () => {
    it('should disable import button when confirming', () => {
      vi.mocked(useImportStore).mockReturnValue({
        importResult: mockImportResult,
        selectedTransactions: new Set(['trans-1']),
        isUploading: false,
        isConfirming: true,
        error: null,
        uploadProgress: 100,
        uploadFile: mockUploadFile,
        confirmImport: mockConfirmImport,
        toggleTransaction: mockToggleTransaction,
        selectAllTransactions: mockSelectAllTransactions,
        deselectAllTransactions: mockDeselectAllTransactions,
        reset: mockReset,
        clearError: mockClearError,
      })

      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      const importButton = screen.getByText(/Importar 1 Transações/)
      expect(importButton).toBeDisabled()
    })

    it('should disable import button when no transactions selected', () => {
      vi.mocked(useImportStore).mockReturnValue({
        importResult: mockImportResult,
        selectedTransactions: new Set(),
        isUploading: false,
        isConfirming: false,
        error: null,
        uploadProgress: 100,
        uploadFile: mockUploadFile,
        confirmImport: mockConfirmImport,
        toggleTransaction: mockToggleTransaction,
        selectAllTransactions: mockSelectAllTransactions,
        deselectAllTransactions: mockDeselectAllTransactions,
        reset: mockReset,
        clearError: mockClearError,
      })

      render(
        <BrowserRouter>
          <ImportPage />
        </BrowserRouter>
      )

      const importButton = screen.getByText(/Importar 0 Transações/)
      expect(importButton).toBeDisabled()
    })
  })
})
