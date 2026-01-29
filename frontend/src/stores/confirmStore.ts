import { create } from 'zustand'
import type { ConfirmDialogOptions } from '@/types'

interface ConfirmState {
  isOpen: boolean
  options: ConfirmDialogOptions | null
  timeRemaining: number | null
  
  // Actions
  openConfirm: (options: ConfirmDialogOptions) => void
  closeConfirm: () => void
  confirm: () => Promise<void>
  setTimeRemaining: (time: number) => void
}

export const useConfirmStore = create<ConfirmState>((set, get) => ({
  isOpen: false,
  options: null,
  timeRemaining: null,

  openConfirm: (options) => {
    // Validate that title and message contain meaningful content
    const title = options.title?.trim()
    const message = options.message?.trim()
    
    if (!title || !message) {
      console.warn('Confirmation rejected: title and message must contain meaningful content')
      return
    }
    
    set({
      isOpen: true,
      options,
      timeRemaining: options.timeoutSeconds || 300, // 5 minutos padrão
    })
  },

  closeConfirm: () => {
    const { options } = get()
    if (options?.onCancel) {
      options.onCancel()
    }
    set({
      isOpen: false,
      options: null,
      timeRemaining: null,
    })
  },

  confirm: async () => {
    const { options } = get()
    if (options?.onConfirm) {
      await options.onConfirm()
    }
    set({
      isOpen: false,
      options: null,
      timeRemaining: null,
    })
  },

  setTimeRemaining: (time) => {
    if (time <= 0) {
      get().closeConfirm()
    } else {
      set({ timeRemaining: time })
    }
  },
}))

// Hook para usar confirmação de forma mais fácil
export const useConfirm = () => {
  const { openConfirm } = useConfirmStore()

  return (options: Omit<ConfirmDialogOptions, 'onConfirm'>) => {
    return new Promise<boolean>((resolve) => {
      openConfirm({
        ...options,
        onConfirm: async () => {
          resolve(true)
        },
        onCancel: () => {
          resolve(false)
        },
      })
    })
  }
}
