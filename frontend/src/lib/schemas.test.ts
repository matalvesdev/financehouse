import { describe, it, expect } from 'vitest'
import {
  loginSchema,
  registerSchema,
  transactionSchema,
  budgetSchema,
  goalSchema,
  goalProgressSchema,
} from '@/lib/schemas'

describe('Login Schema', () => {
  it('deve validar credenciais válidas', () => {
    const result = loginSchema.safeParse({
      email: 'test@example.com',
      password: 'password123',
    })
    expect(result.success).toBe(true)
  })

  it('deve rejeitar email inválido', () => {
    const result = loginSchema.safeParse({
      email: 'invalid-email',
      password: 'password123',
    })
    expect(result.success).toBe(false)
    if (!result.success) {
      expect(result.error.issues[0].path).toContain('email')
    }
  })

  it('deve rejeitar senha curta', () => {
    const result = loginSchema.safeParse({
      email: 'test@example.com',
      password: '1234567',
    })
    expect(result.success).toBe(false)
    if (!result.success) {
      expect(result.error.issues[0].path).toContain('password')
    }
  })
})

describe('Register Schema', () => {
  it('deve validar registro válido', () => {
    const result = registerSchema.safeParse({
      nome: 'Test User',
      email: 'test@example.com',
      password: 'Password1!',
      confirmPassword: 'Password1!',
    })
    expect(result.success).toBe(true)
  })

  it('deve rejeitar quando senhas não coincidem', () => {
    const result = registerSchema.safeParse({
      nome: 'Test User',
      email: 'test@example.com',
      password: 'Password1!',
      confirmPassword: 'DifferentPassword1!',
    })
    expect(result.success).toBe(false)
    if (!result.success) {
      // The error path is on 'confirmPassword' as defined in the refine
      expect(result.error.issues.some(issue => issue.path.includes('confirmPassword'))).toBe(true)
    }
  })

  it('deve rejeitar senha sem maiúscula', () => {
    const result = registerSchema.safeParse({
      nome: 'Test User',
      email: 'test@example.com',
      password: 'password1!',
      confirmPassword: 'password1!',
    })
    expect(result.success).toBe(false)
  })

  it('deve rejeitar senha sem número', () => {
    const result = registerSchema.safeParse({
      nome: 'Test User',
      email: 'test@example.com',
      password: 'Password!',
      confirmPassword: 'Password!',
    })
    expect(result.success).toBe(false)
  })
})

describe('Transaction Schema', () => {
  it('deve validar transação válida', () => {
    const result = transactionSchema.safeParse({
      valor: 100.5,
      descricao: 'Compra no mercado',
      categoria: 'ALIMENTACAO',
      tipo: 'DESPESA',
      data: '2024-01-15',
    })
    expect(result.success).toBe(true)
  })

  it('deve rejeitar valor negativo', () => {
    const result = transactionSchema.safeParse({
      valor: -100,
      descricao: 'Compra',
      categoria: 'ALIMENTACAO',
      tipo: 'DESPESA',
      data: '2024-01-15',
    })
    expect(result.success).toBe(false)
  })

  it('deve rejeitar valor com mais de 2 casas decimais', () => {
    const result = transactionSchema.safeParse({
      valor: 100.555,
      descricao: 'Compra',
      categoria: 'ALIMENTACAO',
      tipo: 'DESPESA',
      data: '2024-01-15',
    })
    expect(result.success).toBe(false)
  })

  it('deve rejeitar categoria inválida', () => {
    const result = transactionSchema.safeParse({
      valor: 100,
      descricao: 'Compra',
      categoria: 'CATEGORIA_INVALIDA',
      tipo: 'DESPESA',
      data: '2024-01-15',
    })
    expect(result.success).toBe(false)
  })

  it('deve rejeitar tipo inválido', () => {
    const result = transactionSchema.safeParse({
      valor: 100,
      descricao: 'Compra',
      categoria: 'ALIMENTACAO',
      tipo: 'TIPO_INVALIDO',
      data: '2024-01-15',
    })
    expect(result.success).toBe(false)
  })

  it('deve rejeitar descrição muito curta', () => {
    const result = transactionSchema.safeParse({
      valor: 100,
      descricao: 'AB',
      categoria: 'ALIMENTACAO',
      tipo: 'DESPESA',
      data: '2024-01-15',
    })
    expect(result.success).toBe(false)
  })
})

describe('Budget Schema', () => {
  it('deve validar orçamento válido', () => {
    const result = budgetSchema.safeParse({
      categoria: 'ALIMENTACAO',
      limite: 1000,
      periodo: 'MENSAL',
      inicioVigencia: '2024-01-01',
    })
    expect(result.success).toBe(true)
  })

  it('deve rejeitar limite zero', () => {
    const result = budgetSchema.safeParse({
      categoria: 'ALIMENTACAO',
      limite: 0,
      periodo: 'MENSAL',
      inicioVigencia: '2024-01-01',
    })
    expect(result.success).toBe(false)
  })

  it('deve rejeitar período inválido', () => {
    const result = budgetSchema.safeParse({
      categoria: 'ALIMENTACAO',
      limite: 1000,
      periodo: 'SEMANAL',
      inicioVigencia: '2024-01-01',
    })
    expect(result.success).toBe(false)
  })
})

describe('Goal Schema', () => {
  it('deve validar meta válida', () => {
    const futureDate = new Date()
    futureDate.setFullYear(futureDate.getFullYear() + 1)
    
    const result = goalSchema.safeParse({
      nome: 'Viagem para Europa',
      valorAlvo: 10000,
      prazo: futureDate.toISOString().split('T')[0],
      tipo: 'VIAGEM',
    })
    expect(result.success).toBe(true)
  })

  it('deve rejeitar prazo no passado', () => {
    const result = goalSchema.safeParse({
      nome: 'Meta antiga',
      valorAlvo: 1000,
      prazo: '2020-01-01',
      tipo: 'COMPRA',
    })
    expect(result.success).toBe(false)
  })

  it('deve rejeitar valor alvo negativo', () => {
    const futureDate = new Date()
    futureDate.setFullYear(futureDate.getFullYear() + 1)
    
    const result = goalSchema.safeParse({
      nome: 'Meta',
      valorAlvo: -1000,
      prazo: futureDate.toISOString().split('T')[0],
      tipo: 'COMPRA',
    })
    expect(result.success).toBe(false)
  })

  it('deve rejeitar tipo inválido', () => {
    const futureDate = new Date()
    futureDate.setFullYear(futureDate.getFullYear() + 1)
    
    const result = goalSchema.safeParse({
      nome: 'Meta',
      valorAlvo: 1000,
      prazo: futureDate.toISOString().split('T')[0],
      tipo: 'TIPO_INVALIDO',
    })
    expect(result.success).toBe(false)
  })
})

describe('Goal Progress Schema', () => {
  it('deve validar progresso válido', () => {
    const result = goalProgressSchema.safeParse({
      valor: 500,
    })
    expect(result.success).toBe(true)
  })

  it('deve rejeitar valor zero', () => {
    const result = goalProgressSchema.safeParse({
      valor: 0,
    })
    expect(result.success).toBe(false)
  })

  it('deve rejeitar valor negativo', () => {
    const result = goalProgressSchema.safeParse({
      valor: -100,
    })
    expect(result.success).toBe(false)
  })
})
