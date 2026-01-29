import { z } from 'zod'

// ============================================
// Auth Schemas
// ============================================

export const loginSchema = z.object({
  email: z
    .string()
    .min(1, 'Email é obrigatório')
    .email('Email inválido'),
  password: z
    .string()
    .min(1, 'Senha é obrigatória')
    .min(8, 'Senha deve ter pelo menos 8 caracteres'),
})

export const registerSchema = z
  .object({
    nome: z
      .string()
      .min(1, 'Nome é obrigatório')
      .min(3, 'Nome deve ter pelo menos 3 caracteres')
      .max(100, 'Nome deve ter no máximo 100 caracteres'),
    email: z
      .string()
      .min(1, 'Email é obrigatório')
      .email('Email inválido'),
    password: z
      .string()
      .min(1, 'Senha é obrigatória')
      .min(8, 'Senha deve ter pelo menos 8 caracteres')
      .regex(/[A-Z]/, 'Senha deve conter pelo menos uma letra maiúscula')
      .regex(/[a-z]/, 'Senha deve conter pelo menos uma letra minúscula')
      .regex(/[0-9]/, 'Senha deve conter pelo menos um número')
      .regex(/[@$!%*?&]/, 'Senha deve conter pelo menos um caractere especial (@$!%*?&)'),
    confirmPassword: z.string().min(1, 'Confirmação de senha é obrigatória'),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Senhas não conferem',
    path: ['confirmPassword'],
  })

export type LoginFormData = z.infer<typeof loginSchema>
export type RegisterFormData = z.infer<typeof registerSchema>

// ============================================
// Transaction Schemas
// ============================================

export const tipoTransacaoEnum = z.enum(['RECEITA', 'DESPESA'])

export const categoriaEnum = z.enum([
  'ALIMENTACAO',
  'TRANSPORTE',
  'MORADIA',
  'SAUDE',
  'EDUCACAO',
  'LAZER',
  'VESTUARIO',
  'SERVICOS',
  'INVESTIMENTO',
  'SALARIO',
  'FREELANCE',
  'OUTROS',
])

export const transactionSchema = z.object({
  valor: z
    .number({ required_error: 'Valor é obrigatório', invalid_type_error: 'Valor deve ser um número' })
    .positive('Valor deve ser maior que zero')
    .multipleOf(0.01, 'Valor deve ter no máximo 2 casas decimais'),
  descricao: z
    .string()
    .min(1, 'Descrição é obrigatória')
    .min(3, 'Descrição deve ter pelo menos 3 caracteres')
    .max(200, 'Descrição deve ter no máximo 200 caracteres'),
  categoria: categoriaEnum,
  tipo: tipoTransacaoEnum,
  data: z
    .string()
    .min(1, 'Data é obrigatória')
    .refine((date) => !isNaN(Date.parse(date)), 'Data inválida'),
})

export const transactionFilterSchema = z.object({
  dataInicio: z.string().optional(),
  dataFim: z.string().optional(),
  categoria: categoriaEnum.optional(),
  tipo: tipoTransacaoEnum.optional(),
  ordenacao: z.enum(['data', 'valor', 'descricao']).optional(),
  direcao: z.enum(['asc', 'desc']).optional(),
})

export type TransactionFormData = z.infer<typeof transactionSchema>
export type TransactionFilterData = z.infer<typeof transactionFilterSchema>
export type TipoTransacao = z.infer<typeof tipoTransacaoEnum>
export type Categoria = z.infer<typeof categoriaEnum>

// ============================================
// Budget Schemas
// ============================================

export const periodoOrcamentoEnum = z.enum(['MENSAL', 'TRIMESTRAL', 'ANUAL'])

export const budgetSchema = z.object({
  categoria: categoriaEnum,
  limite: z
    .number({ required_error: 'Limite é obrigatório', invalid_type_error: 'Limite deve ser um número' })
    .positive('Limite deve ser maior que zero')
    .multipleOf(0.01, 'Limite deve ter no máximo 2 casas decimais'),
  periodo: periodoOrcamentoEnum,
  inicioVigencia: z
    .string()
    .min(1, 'Data de início é obrigatória')
    .refine((date) => !isNaN(Date.parse(date)), 'Data inválida'),
})

export const budgetUpdateSchema = z.object({
  limite: z
    .number({ required_error: 'Limite é obrigatório', invalid_type_error: 'Limite deve ser um número' })
    .positive('Limite deve ser maior que zero')
    .multipleOf(0.01, 'Limite deve ter no máximo 2 casas decimais'),
})

export type BudgetFormData = z.infer<typeof budgetSchema>
export type BudgetUpdateFormData = z.infer<typeof budgetUpdateSchema>
export type PeriodoOrcamento = z.infer<typeof periodoOrcamentoEnum>

// ============================================
// Goal Schemas
// ============================================

export const tipoMetaEnum = z.enum(['RESERVA_EMERGENCIA', 'VIAGEM', 'COMPRA', 'INVESTIMENTO'])

export const goalSchema = z.object({
  nome: z
    .string()
    .min(1, 'Nome é obrigatório')
    .min(3, 'Nome deve ter pelo menos 3 caracteres')
    .max(100, 'Nome deve ter no máximo 100 caracteres'),
  valorAlvo: z
    .number({ required_error: 'Valor alvo é obrigatório', invalid_type_error: 'Valor alvo deve ser um número' })
    .positive('Valor alvo deve ser maior que zero')
    .multipleOf(0.01, 'Valor alvo deve ter no máximo 2 casas decimais'),
  prazo: z
    .string()
    .min(1, 'Prazo é obrigatório')
    .refine((date) => !isNaN(Date.parse(date)), 'Data inválida')
    .refine((date) => new Date(date) > new Date(), 'Prazo deve ser uma data futura'),
  tipo: tipoMetaEnum,
})

export const goalProgressSchema = z.object({
  valor: z
    .number({ required_error: 'Valor é obrigatório', invalid_type_error: 'Valor deve ser um número' })
    .positive('Valor deve ser maior que zero')
    .multipleOf(0.01, 'Valor deve ter no máximo 2 casas decimais'),
})

export type GoalFormData = z.infer<typeof goalSchema>
export type GoalProgressFormData = z.infer<typeof goalProgressSchema>
export type TipoMeta = z.infer<typeof tipoMetaEnum>

// ============================================
// Import Schemas
// ============================================

export const importFileSchema = z.object({
  arquivo: z
    .instanceof(File, { message: 'Arquivo é obrigatório' })
    .refine(
      (file) => {
        const validTypes = [
          'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          'application/vnd.ms-excel',
          'text/csv',
        ]
        return validTypes.includes(file.type)
      },
      'Arquivo deve ser Excel (.xlsx, .xls) ou CSV'
    )
    .refine((file) => file.size <= 10 * 1024 * 1024, 'Arquivo deve ter no máximo 10MB'),
})

export const importConfirmSchema = z.object({
  transacoesConfirmadas: z.array(z.string()).min(1, 'Selecione pelo menos uma transação'),
})

export type ImportFileFormData = z.infer<typeof importFileSchema>
export type ImportConfirmFormData = z.infer<typeof importConfirmSchema>

// ============================================
// Confirmation Schema
// ============================================

export const confirmActionSchema = z.object({
  confirmed: z.boolean().refine((val) => val === true, 'Você deve confirmar a ação'),
})

export type ConfirmActionFormData = z.infer<typeof confirmActionSchema>

// ============================================
// Labels para exibição
// ============================================

export const categoriaLabels: Record<Categoria, string> = {
  ALIMENTACAO: 'Alimentação',
  TRANSPORTE: 'Transporte',
  MORADIA: 'Moradia',
  SAUDE: 'Saúde',
  EDUCACAO: 'Educação',
  LAZER: 'Lazer',
  VESTUARIO: 'Vestuário',
  SERVICOS: 'Serviços',
  INVESTIMENTO: 'Investimento',
  SALARIO: 'Salário',
  FREELANCE: 'Freelance',
  OUTROS: 'Outros',
}

export const tipoTransacaoLabels: Record<TipoTransacao, string> = {
  RECEITA: 'Receita',
  DESPESA: 'Despesa',
}

export const periodoOrcamentoLabels: Record<PeriodoOrcamento, string> = {
  MENSAL: 'Mensal',
  TRIMESTRAL: 'Trimestral',
  ANUAL: 'Anual',
}

export const tipoMetaLabels: Record<TipoMeta, string> = {
  RESERVA_EMERGENCIA: 'Reserva de Emergência',
  VIAGEM: 'Viagem',
  COMPRA: 'Compra',
  INVESTIMENTO: 'Investimento',
}
