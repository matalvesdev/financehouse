import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useImportStore } from '../importStore'
import { importService } from '@/lib/api'
import type { ImportResult } from '@/types'

// Mock the API
vi.mock('@/lib/api', () => ({
  importService: {
    uploadFile: vi.fn(),
    confirmImport: vi.fn(),
  },
  getErrorMessage: vi.fn((error) => error.message || 'Unknown error'),
}))

const mockImportResult: ImportResult = {
  id: 'import-123',
  nomeArquivo: 'test.xlsx',
  status: 'CONCLUIDA',
  totalTransacoes: 5,
  transacoesValidas: 4,
  transacoesInvalidas: 1,
  duplicatasPotenciais: 1,
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
      descricao: 'Transporte',
      categoria: 'TRANSPORTE',
      tipo: 'DESPESA',
      data: '2024-01-18',
      duplicataPotencial: false,
      selecionada: true,
    },
  ],
  erros: [],
  criadoEm: '2024-01-20T10:00:00Z',
}

describe('importStore - Preview and Selection', () => {
  beforeEach(() => {
    // Reset store state
    useImportStore.setState({
      importResult: null,
      selectedTransactions: new Set(),
      isUploading: false,
      isConfirming: false,
      error: null,
      uploadProgress: 0,
    })
    vi.clearAllMocks()
  })

  describe('uploadFile', () => {
    it('should upload file and auto-select non-duplicate transactions', async () => {
      vi.mocked(importService.uploadFile).mockResolvedValueOnce(mockImportResult)

      const store = useImportStore.getState()
      const file = new File(['content'], 'test.xlsx', { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })

      await store.uploadFile(file)

      const state = useImportStore.getState()
      
      // Should set import result
      expect(state.importResult).toEqual(mockImportResult)
      
      // Should auto-select non-duplicate transactions (trans-1, trans-2, trans-4)
      expect(state.selectedTransactions.size).toBe(3)
      expect(state.selectedTransactions.has('trans-1')).toBe(true)
      expect(state.selectedTransactions.has('trans-2')).toBe(true)
      expect(state.selectedTransactions.has('trans-4')).toBe(true)
      
      // Should NOT auto-select duplicate (trans-3)
      expect(state.selectedTransactions.has('trans-3')).toBe(false)
      
      // Should set upload progress to 100
      expect(state.uploadProgress).toBe(100)
      expect(state.isUploading).toBe(false)
    })

    it('should set loading state during upload', async () => {
      vi.mocked(importService.uploadFile).mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve(mockImportResult), 100))
      )

      const store = useImportStore.getState()
      const file = new File(['content'], 'test.xlsx', { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })

      const uploadPromise = store.uploadFile(file)

      // Should be loading
      expect(useImportStore.getState().isUploading).toBe(true)
      expect(useImportStore.getState().uploadProgress).toBe(0)

      await uploadPromise

      // Should finish loading
      expect(useImportStore.getState().isUploading).toBe(false)
      expect(useImportStore.getState().uploadProgress).toBe(100)
    })

    it('should handle upload errors', async () => {
      const errorMessage = 'Invalid file format'
      vi.mocked(importService.uploadFile).mockRejectedValueOnce(new Error(errorMessage))

      const store = useImportStore.getState()
      const file = new File(['content'], 'test.txt', { type: 'text/plain' })

      await expect(store.uploadFile(file)).rejects.toThrow(errorMessage)

      const state = useImportStore.getState()
      expect(state.error).toBe(errorMessage)
      expect(state.isUploading).toBe(false)
      expect(state.uploadProgress).toBe(0)
      expect(state.importResult).toBeNull()
    })
  })

  describe('toggleTransaction', () => {
    beforeEach(() => {
      useImportStore.setState({
        importResult: mockImportResult,
        selectedTransactions: new Set(['trans-1', 'trans-2']),
      })
    })

    it('should add transaction to selection when not selected', () => {
      const store = useImportStore.getState()
      store.toggleTransaction('trans-3')

      const state = useImportStore.getState()
      expect(state.selectedTransactions.has('trans-3')).toBe(true)
      expect(state.selectedTransactions.size).toBe(3)
    })

    it('should remove transaction from selection when already selected', () => {
      const store = useImportStore.getState()
      store.toggleTransaction('trans-1')

      const state = useImportStore.getState()
      expect(state.selectedTransactions.has('trans-1')).toBe(false)
      expect(state.selectedTransactions.size).toBe(1)
    })

    it('should maintain immutability of selection set', () => {
      const store = useImportStore.getState()
      const originalSet = useImportStore.getState().selectedTransactions
      
      store.toggleTransaction('trans-3')

      const newSet = useImportStore.getState().selectedTransactions
      expect(newSet).not.toBe(originalSet)
    })
  })

  describe('selectAllTransactions', () => {
    beforeEach(() => {
      useImportStore.setState({
        importResult: mockImportResult,
        selectedTransactions: new Set(['trans-1']),
      })
    })

    it('should select all transactions including duplicates', () => {
      const store = useImportStore.getState()
      store.selectAllTransactions()

      const state = useImportStore.getState()
      expect(state.selectedTransactions.size).toBe(4)
      expect(state.selectedTransactions.has('trans-1')).toBe(true)
      expect(state.selectedTransactions.has('trans-2')).toBe(true)
      expect(state.selectedTransactions.has('trans-3')).toBe(true)
      expect(state.selectedTransactions.has('trans-4')).toBe(true)
    })

    it('should do nothing when no import result exists', () => {
      useImportStore.setState({
        importResult: null,
        selectedTransactions: new Set(),
      })

      const store = useImportStore.getState()
      store.selectAllTransactions()

      const state = useImportStore.getState()
      expect(state.selectedTransactions.size).toBe(0)
    })
  })

  describe('deselectAllTransactions', () => {
    beforeEach(() => {
      useImportStore.setState({
        importResult: mockImportResult,
        selectedTransactions: new Set(['trans-1', 'trans-2', 'trans-3']),
      })
    })

    it('should clear all selections', () => {
      const store = useImportStore.getState()
      store.deselectAllTransactions()

      const state = useImportStore.getState()
      expect(state.selectedTransactions.size).toBe(0)
    })
  })

  describe('confirmImport', () => {
    beforeEach(() => {
      useImportStore.setState({
        importResult: mockImportResult,
        selectedTransactions: new Set(['trans-1', 'trans-2']),
      })
    })

    it('should call API with import ID and selected transaction IDs', async () => {
      vi.mocked(importService.confirmImport).mockResolvedValueOnce(undefined)

      const store = useImportStore.getState()
      await store.confirmImport()

      expect(importService.confirmImport).toHaveBeenCalledWith(
        'import-123',
        ['trans-1', 'trans-2']
      )
    })

    it('should reset state after successful import', async () => {
      vi.mocked(importService.confirmImport).mockResolvedValueOnce(undefined)

      const store = useImportStore.getState()
      await store.confirmImport()

      const state = useImportStore.getState()
      expect(state.importResult).toBeNull()
      expect(state.selectedTransactions.size).toBe(0)
      expect(state.isConfirming).toBe(false)
    })

    it('should set loading state during confirmation', async () => {
      vi.mocked(importService.confirmImport).mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve(undefined), 100))
      )

      const store = useImportStore.getState()
      const confirmPromise = store.confirmImport()

      // Should be confirming
      expect(useImportStore.getState().isConfirming).toBe(true)

      await confirmPromise

      // Should finish confirming
      expect(useImportStore.getState().isConfirming).toBe(false)
    })

    it('should handle confirmation errors', async () => {
      const errorMessage = 'Import failed'
      vi.mocked(importService.confirmImport).mockRejectedValueOnce(new Error(errorMessage))

      const store = useImportStore.getState()
      await expect(store.confirmImport()).rejects.toThrow(errorMessage)

      const state = useImportStore.getState()
      expect(state.error).toBe(errorMessage)
      expect(state.isConfirming).toBe(false)
      // Should NOT reset import result on error
      expect(state.importResult).toEqual(mockImportResult)
    })

    it('should do nothing when no import result exists', async () => {
      useImportStore.setState({
        importResult: null,
        selectedTransactions: new Set(),
      })

      const store = useImportStore.getState()
      await store.confirmImport()

      expect(importService.confirmImport).not.toHaveBeenCalled()
    })
  })

  describe('reset', () => {
    beforeEach(() => {
      useImportStore.setState({
        importResult: mockImportResult,
        selectedTransactions: new Set(['trans-1', 'trans-2']),
        isUploading: false,
        isConfirming: false,
        error: 'Some error',
        uploadProgress: 100,
      })
    })

    it('should reset all state to initial values', () => {
      const store = useImportStore.getState()
      store.reset()

      const state = useImportStore.getState()
      expect(state.importResult).toBeNull()
      expect(state.selectedTransactions.size).toBe(0)
      expect(state.isUploading).toBe(false)
      expect(state.isConfirming).toBe(false)
      expect(state.error).toBeNull()
      expect(state.uploadProgress).toBe(0)
    })
  })

  describe('clearError', () => {
    beforeEach(() => {
      useImportStore.setState({
        error: 'Some error message',
      })
    })

    it('should clear error message', () => {
      const store = useImportStore.getState()
      store.clearError()

      const state = useImportStore.getState()
      expect(state.error).toBeNull()
    })
  })

  describe('Duplicate Detection - Requirement 2.3', () => {
    it('should not auto-select transactions marked as potential duplicates', async () => {
      const resultWithDuplicates: ImportResult = {
        ...mockImportResult,
        transacoes: [
          {
            id: 'trans-1',
            valor: 100.0,
            descricao: 'Transaction 1',
            categoria: 'ALIMENTACAO',
            tipo: 'DESPESA',
            data: '2024-01-15',
            duplicataPotencial: false,
            selecionada: true,
          },
          {
            id: 'trans-2',
            valor: 100.0,
            descricao: 'Transaction 2',
            categoria: 'ALIMENTACAO',
            tipo: 'DESPESA',
            data: '2024-01-15',
            duplicataPotencial: true,
            selecionada: false,
          },
          {
            id: 'trans-3',
            valor: 50.0,
            descricao: 'Transaction 3',
            categoria: 'TRANSPORTE',
            tipo: 'DESPESA',
            data: '2024-01-16',
            duplicataPotencial: true,
            selecionada: false,
          },
        ],
      }

      vi.mocked(importService.uploadFile).mockResolvedValueOnce(resultWithDuplicates)

      const store = useImportStore.getState()
      const file = new File(['content'], 'test.xlsx', { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })

      await store.uploadFile(file)

      const state = useImportStore.getState()
      
      // Only trans-1 should be selected (not a duplicate)
      expect(state.selectedTransactions.size).toBe(1)
      expect(state.selectedTransactions.has('trans-1')).toBe(true)
      expect(state.selectedTransactions.has('trans-2')).toBe(false)
      expect(state.selectedTransactions.has('trans-3')).toBe(false)
    })

    it('should allow manual selection of duplicate transactions', () => {
      useImportStore.setState({
        importResult: mockImportResult,
        selectedTransactions: new Set(['trans-1']),
      })

      const store = useImportStore.getState()
      
      // Manually select a duplicate transaction
      store.toggleTransaction('trans-3') // trans-3 is marked as duplicate

      const state = useImportStore.getState()
      expect(state.selectedTransactions.has('trans-3')).toBe(true)
    })
  })
})
