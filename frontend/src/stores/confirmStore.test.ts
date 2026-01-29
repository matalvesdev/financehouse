import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useConfirmStore, useConfirm } from '@/stores/confirmStore'

describe('ConfirmStore', () => {
  beforeEach(() => {
    const { result } = renderHook(() => useConfirmStore())
    act(() => {
      result.current.closeConfirm()
    })
  })

  describe('openConfirm', () => {
    it('deve abrir o dialog com as opções fornecidas', () => {
      const { result } = renderHook(() => useConfirmStore())

      const options = {
        title: 'Confirmar Ação',
        message: 'Tem certeza?',
        onConfirm: vi.fn(),
      }

      act(() => {
        result.current.openConfirm(options)
      })

      expect(result.current.isOpen).toBe(true)
      expect(result.current.options?.title).toBe('Confirmar Ação')
      expect(result.current.options?.message).toBe('Tem certeza?')
      expect(result.current.timeRemaining).toBe(300) // 5 minutos padrão
    })

    it('deve usar timeout personalizado', () => {
      const { result } = renderHook(() => useConfirmStore())

      act(() => {
        result.current.openConfirm({
          title: 'Test',
          message: 'Test',
          timeoutSeconds: 60,
          onConfirm: vi.fn(),
        })
      })

      expect(result.current.timeRemaining).toBe(60)
    })
  })

  describe('closeConfirm', () => {
    it('deve fechar o dialog e chamar onCancel', () => {
      const { result } = renderHook(() => useConfirmStore())
      const onCancel = vi.fn()

      act(() => {
        result.current.openConfirm({
          title: 'Test',
          message: 'Test',
          onConfirm: vi.fn(),
          onCancel,
        })
      })

      act(() => {
        result.current.closeConfirm()
      })

      expect(result.current.isOpen).toBe(false)
      expect(result.current.options).toBeNull()
      expect(onCancel).toHaveBeenCalled()
    })
  })

  describe('confirm', () => {
    it('deve chamar onConfirm e fechar o dialog', async () => {
      const { result } = renderHook(() => useConfirmStore())
      const onConfirm = vi.fn()

      act(() => {
        result.current.openConfirm({
          title: 'Test',
          message: 'Test',
          onConfirm,
        })
      })

      await act(async () => {
        await result.current.confirm()
      })

      expect(onConfirm).toHaveBeenCalled()
      expect(result.current.isOpen).toBe(false)
    })
  })

  describe('setTimeRemaining', () => {
    it('deve atualizar o tempo restante', () => {
      const { result } = renderHook(() => useConfirmStore())

      act(() => {
        result.current.openConfirm({
          title: 'Test',
          message: 'Test',
          onConfirm: vi.fn(),
        })
      })

      act(() => {
        result.current.setTimeRemaining(100)
      })

      expect(result.current.timeRemaining).toBe(100)
    })

    it('deve fechar o dialog quando tempo chegar a zero', () => {
      const { result } = renderHook(() => useConfirmStore())

      act(() => {
        result.current.openConfirm({
          title: 'Test',
          message: 'Test',
          onConfirm: vi.fn(),
        })
      })

      act(() => {
        result.current.setTimeRemaining(0)
      })

      expect(result.current.isOpen).toBe(false)
    })
  })
})

describe('useConfirm hook', () => {
  it('deve retornar uma promise que resolve true quando confirmado', async () => {
    const { result: storeResult } = renderHook(() => useConfirmStore())
    const { result: hookResult } = renderHook(() => useConfirm())

    let confirmPromise: Promise<boolean>

    act(() => {
      confirmPromise = hookResult.current({
        title: 'Test',
        message: 'Test',
      })
    })

    // Simula confirmação
    await act(async () => {
      await storeResult.current.confirm()
    })

    const confirmed = await confirmPromise!
    expect(confirmed).toBe(true)
  })

  it('deve retornar uma promise que resolve false quando cancelado', async () => {
    const { result: storeResult } = renderHook(() => useConfirmStore())
    const { result: hookResult } = renderHook(() => useConfirm())

    let confirmPromise: Promise<boolean>

    act(() => {
      confirmPromise = hookResult.current({
        title: 'Test',
        message: 'Test',
      })
    })

    // Simula cancelamento
    act(() => {
      storeResult.current.closeConfirm()
    })

    const confirmed = await confirmPromise!
    expect(confirmed).toBe(false)
  })
})
