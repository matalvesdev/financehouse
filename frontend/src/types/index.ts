// ============================================
// Base Types
// ============================================

export interface User {
  id: string
  email: string
  nome: string
  ativo: boolean
  dadosIniciais: boolean
  criadoEm: string
}

export interface AuthResponse {
  usuario: User
  accessToken: string
  refreshToken: string
}

// ============================================
// Transaction Types
// ============================================

export type TipoTransacao = 'RECEITA' | 'DESPESA'

export type Categoria =
  | 'ALIMENTACAO'
  | 'TRANSPORTE'
  | 'MORADIA'
  | 'SAUDE'
  | 'EDUCACAO'
  | 'LAZER'
  | 'VESTUARIO'
  | 'SERVICOS'
  | 'INVESTIMENTO'
  | 'SALARIO'
  | 'FREELANCE'
  | 'OUTROS'

export interface Transaction {
  id: string
  usuarioId: string
  valor: number
  descricao: string
  categoria: Categoria
  tipo: TipoTransacao
  data: string
  ativa: boolean
  criadaEm: string
  atualizadaEm?: string
}

export interface TransactionSummary {
  saldoAtual: number
  receitaMensal: number
  despesaMensal: number
  saldoMensal: number
}

export interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
  hasNext: boolean
  hasPrevious: boolean
}

// ============================================
// Budget Types
// ============================================

export type PeriodoOrcamento = 'MENSAL' | 'TRIMESTRAL' | 'ANUAL'

export type StatusOrcamento = 'ATIVO' | 'PROXIMO_LIMITE' | 'EXCEDIDO' | 'ARQUIVADO'

export interface Budget {
  id: string
  usuarioId: string
  categoria: Categoria
  limite: number
  gastoAtual: number
  periodo: PeriodoOrcamento
  status: StatusOrcamento
  inicioVigencia: string
  fimVigencia: string
  percentualGasto: number
  criadoEm: string
  atualizadoEm?: string
}

// ============================================
// Goal Types
// ============================================

export type TipoMeta = 'RESERVA_EMERGENCIA' | 'VIAGEM' | 'COMPRA' | 'INVESTIMENTO'

export type StatusMeta = 'EM_ANDAMENTO' | 'CONCLUIDA' | 'CANCELADA' | 'ATRASADA'

export interface Goal {
  id: string
  usuarioId: string
  nome: string
  valorAlvo: number
  valorAtual: number
  prazo: string
  tipo: TipoMeta
  status: StatusMeta
  percentualConclusao: number
  dataEstimadaConclusao?: string
  criadaEm: string
  atualizadaEm?: string
}

// ============================================
// Import Types
// ============================================

export type StatusImportacao = 'PENDENTE' | 'PROCESSANDO' | 'CONCLUIDA' | 'ERRO'

export interface ImportedTransaction {
  id: string
  valor: number
  descricao: string
  categoria: Categoria
  tipo: TipoTransacao
  data: string
  duplicataPotencial: boolean
  transacaoExistenteId?: string
  selecionada: boolean
}

export interface ImportResult {
  id: string
  nomeArquivo: string
  status: StatusImportacao
  totalTransacoes: number
  transacoesValidas: number
  transacoesInvalidas: number
  duplicatasPotenciais: number
  transacoes: ImportedTransaction[]
  erros?: string[]
  criadoEm: string
}

// ============================================
// Dashboard Types
// ============================================

export interface DashboardSummary {
  saldoAtual: number
  receitaMensal: number
  despesaMensal: number
  saldoMensal: number
  orcamentosAtivos: number
  orcamentosExcedidos: number
  orcamentosProximoLimite: number
  metasAtivas: number
  metasConcluidas: number
}

export interface RecentTransaction {
  id: string
  valor: number
  descricao: string
  categoria: Categoria
  tipo: TipoTransacao
  data: string
}

// ============================================
// API Response Types
// ============================================

export interface ApiError {
  message: string
  code?: string
  field?: string
  details?: Record<string, string[]>
}

export interface ApiResponse<T> {
  data: T
  success: boolean
  message?: string
}

// ============================================
// Form Types
// ============================================

export interface SelectOption<T = string> {
  value: T
  label: string
}

export interface TableColumn<T> {
  key: keyof T | string
  header: string
  sortable?: boolean
  render?: (item: T) => React.ReactNode
}

// ============================================
// Confirmation Types
// ============================================

export interface ConfirmDialogOptions {
  title: string
  message: string
  impact?: string // Descrição do impacto da ação (Requirement 9.3)
  confirmText?: string
  cancelText?: string
  variant?: 'danger' | 'warning' | 'info'
  onConfirm: () => void | Promise<void>
  onCancel?: () => void
  timeoutSeconds?: number
}

// ============================================
// Notification Types
// ============================================

export type NotificationType = 'success' | 'error' | 'warning' | 'info'

export interface Notification {
  id: string
  type: NotificationType
  title: string
  message?: string
  duration?: number
}
