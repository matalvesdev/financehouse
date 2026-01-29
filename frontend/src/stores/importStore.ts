import { create } from 'zustand'
import { importService, getErrorMessage } from '@/lib/api'
import type { ImportResult, ImportedTransaction } from '@/types'

interface ImportState {
  importResult: ImportResult | null
  selectedTransactions: Set<string>
  isUploading: boolean
  isConfirming: boolean
  error: string | null
  uploadProgress: number

  // Actions
  uploadFile: (file: File) => Promise<void>
  confirmImport: () => Promise<void>
  toggleTransaction: (id: string) => void
  selectAllTransactions: () => void
  deselectAllTransactions: () => void
  reset: () => void
  clearError: () => void
}

export const useImportStore = create<ImportState>((set, get) => ({
  importResult: null,
  selectedTransactions: new Set(),
  isUploading: false,
  isConfirming: false,
  error: null,
  uploadProgress: 0,

  uploadFile: async (file) => {
    set({ isUploading: true, error: null, uploadProgress: 0 })
    try {
      const result = await importService.uploadFile(file)
      
      // Seleciona automaticamente transações não duplicadas
      const selectedIds = new Set<string>(
        result.transacoes
          .filter((t: ImportedTransaction) => !t.duplicataPotencial)
          .map((t: ImportedTransaction) => t.id)
      )
      
      set({
        importResult: result,
        selectedTransactions: selectedIds,
        isUploading: false,
        uploadProgress: 100,
      })
    } catch (error) {
      const errorMessage = getErrorMessage(error)
      set({
        isUploading: false,
        error: errorMessage,
        uploadProgress: 0,
      })
      throw new Error(errorMessage)
    }
  },

  confirmImport: async () => {
    const { importResult, selectedTransactions } = get()
    if (!importResult) return

    set({ isConfirming: true, error: null })
    try {
      await importService.confirmImport(
        importResult.id,
        Array.from(selectedTransactions)
      )
      
      set({
        isConfirming: false,
        importResult: null,
        selectedTransactions: new Set(),
      })
    } catch (error) {
      const errorMessage = getErrorMessage(error)
      set({ isConfirming: false, error: errorMessage })
      throw new Error(errorMessage)
    }
  },

  toggleTransaction: (id) => {
    const { selectedTransactions } = get()
    const newSelected = new Set(selectedTransactions)
    
    if (newSelected.has(id)) {
      newSelected.delete(id)
    } else {
      newSelected.add(id)
    }
    
    set({ selectedTransactions: newSelected })
  },

  selectAllTransactions: () => {
    const { importResult } = get()
    if (!importResult) return
    
    const allIds = new Set(importResult.transacoes.map((t) => t.id))
    set({ selectedTransactions: allIds })
  },

  deselectAllTransactions: () => {
    set({ selectedTransactions: new Set() })
  },

  reset: () => {
    set({
      importResult: null,
      selectedTransactions: new Set(),
      isUploading: false,
      isConfirming: false,
      error: null,
      uploadProgress: 0,
    })
  },

  clearError: () => {
    set({ error: null })
  },
}))
