import { describe, it, expect, vi } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { z } from 'zod'
import {
  useValidatedForm,
  getFieldError,
  hasFieldError,
  getAllErrors,
  getSchemaDefaults,
  validateWithSchema,
  enumToSelectOptions,
  createDebouncedValidator,
  mergeSchemas,
} from './form-utils'

// Test schemas
const testSchema = z.object({
  name: z.string().min(1, 'Nome é obrigatório'),
  email: z.string().email('Email inválido'),
  age: z.number().min(18, 'Deve ser maior de idade'),
})

const testEnum = z.enum(['OPTION_A', 'OPTION_B', 'OPTION_C'])

describe('Form Utils', () => {
  describe('useValidatedForm', () => {
    it('deve criar um form com resolver Zod', () => {
      const { result } = renderHook(() => useValidatedForm(testSchema))
      
      expect(result.current.formState).toBeDefined()
      expect(result.current.register).toBeDefined()
      expect(result.current.handleSubmit).toBeDefined()
    })

    it('deve usar valores padrão quando fornecidos', () => {
      const defaultValues = { name: 'Test', email: 'test@example.com', age: 25 }
      const { result } = renderHook(() => 
        useValidatedForm(testSchema, { defaultValues })
      )
      
      expect(result.current.getValues()).toEqual(defaultValues)
    })
  })

  describe('getFieldError', () => {
    it('deve retornar mensagem de erro quando existe', () => {
      const errors = {
        name: { message: 'Nome é obrigatório' },
        email: { message: 'Email inválido' },
      }
      
      expect(getFieldError(errors, 'name')).toBe('Nome é obrigatório')
      expect(getFieldError(errors, 'email')).toBe('Email inválido')
    })

    it('deve retornar undefined quando não há erro', () => {
      const errors = {}
      
      expect(getFieldError(errors, 'name')).toBeUndefined()
    })
  })

  describe('hasFieldError', () => {
    it('deve retornar true quando há erro', () => {
      const errors = {
        name: { message: 'Nome é obrigatório' },
      }
      
      expect(hasFieldError(errors, 'name')).toBe(true)
    })

    it('deve retornar false quando não há erro', () => {
      const errors = {}
      
      expect(hasFieldError(errors, 'name')).toBe(false)
    })
  })

  describe('getAllErrors', () => {
    it('deve retornar todas as mensagens de erro', () => {
      const errors = {
        name: { message: 'Nome é obrigatório' },
        email: { message: 'Email inválido' },
        nested: {
          field: { message: 'Campo aninhado inválido' }
        }
      }
      
      const allErrors = getAllErrors(errors)
      
      expect(allErrors).toContain('Nome é obrigatório')
      expect(allErrors).toContain('Email inválido')
      expect(allErrors).toContain('Campo aninhado inválido')
      expect(allErrors).toHaveLength(3)
    })

    it('deve retornar array vazio quando não há erros', () => {
      const errors = {}
      
      expect(getAllErrors(errors)).toEqual([])
    })
  })

  describe('getSchemaDefaults', () => {
    it('deve retornar objeto vazio para schema sem defaults', () => {
      const defaults = getSchemaDefaults(testSchema)
      
      expect(defaults).toEqual({})
    })

    it('deve retornar defaults quando schema tem valores padrão', () => {
      const schemaWithDefaults = z.object({
        name: z.string().default('Default Name'),
        count: z.number().default(0),
      })
      
      const defaults = getSchemaDefaults(schemaWithDefaults)
      
      expect(defaults).toEqual({
        name: 'Default Name',
        count: 0,
      })
    })
  })

  describe('validateWithSchema', () => {
    it('deve retornar sucesso para dados válidos', () => {
      const validData = {
        name: 'John Doe',
        email: 'john@example.com',
        age: 25,
      }
      
      const result = validateWithSchema(testSchema, validData)
      
      expect(result.success).toBe(true)
      if (result.success) {
        expect(result.data).toEqual(validData)
      }
    })

    it('deve retornar erros para dados inválidos', () => {
      const invalidData = {
        name: '',
        email: 'invalid-email',
        age: 15,
      }
      
      const result = validateWithSchema(testSchema, invalidData)
      
      expect(result.success).toBe(false)
      if (!result.success) {
        expect(result.errors).toContain('Nome é obrigatório')
        expect(result.errors).toContain('Email inválido')
        expect(result.errors).toContain('Deve ser maior de idade')
      }
    })
  })

  describe('enumToSelectOptions', () => {
    it('deve converter enum para opções de select', () => {
      const enumObject = {
        OPTION_A: 'OPTION_A',
        OPTION_B: 'OPTION_B',
        OPTION_C: 'OPTION_C',
      }
      
      const options = enumToSelectOptions(enumObject)
      
      expect(options).toEqual([
        { value: 'OPTION_A', label: 'OPTION_A' },
        { value: 'OPTION_B', label: 'OPTION_B' },
        { value: 'OPTION_C', label: 'OPTION_C' },
      ])
    })

    it('deve usar labels customizados quando fornecidos', () => {
      const enumObject = {
        OPTION_A: 'OPTION_A',
        OPTION_B: 'OPTION_B',
      }
      
      const labels = {
        OPTION_A: 'Opção A',
        OPTION_B: 'Opção B',
      }
      
      const options = enumToSelectOptions(enumObject, labels)
      
      expect(options).toEqual([
        { value: 'OPTION_A', label: 'Opção A' },
        { value: 'OPTION_B', label: 'Opção B' },
      ])
    })
  })

  describe('createDebouncedValidator', () => {
    it('deve debouncer validação', async () => {
      const validator = createDebouncedValidator(testSchema, 100)
      const callback = vi.fn()
      
      // Chama múltiplas vezes rapidamente
      validator({ name: 'Test' }, callback)
      validator({ name: 'Test2' }, callback)
      validator({ name: 'Test3' }, callback)
      
      // Callback não deve ter sido chamado ainda
      expect(callback).not.toHaveBeenCalled()
      
      // Espera o debounce
      await new Promise(resolve => setTimeout(resolve, 150))
      
      // Callback deve ter sido chamado apenas uma vez
      expect(callback).toHaveBeenCalledTimes(1)
    })
  })

  describe('mergeSchemas', () => {
    it('deve mesclar dois schemas', () => {
      const schema1 = z.object({
        name: z.string(),
        age: z.number(),
      })
      
      const schema2 = z.object({
        email: z.string().email(),
        phone: z.string(),
      })
      
      const mergedSchema = mergeSchemas(schema1, schema2)
      
      const validData = {
        name: 'John',
        age: 25,
        email: 'john@example.com',
        phone: '123456789',
      }
      
      const result = mergedSchema.safeParse(validData)
      expect(result.success).toBe(true)
    })
  })
})