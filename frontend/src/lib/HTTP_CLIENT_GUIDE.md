# HTTP Client Guide

## Overview

O cliente HTTP da aplicação é implementado usando Axios com interceptors avançados para JWT, refresh automático de tokens, tratamento centralizado de erros, retry automático e logging de desenvolvimento.

## Features Implementadas

### ✅ Configuração JWT com Interceptors
- **Request Interceptor**: Adiciona automaticamente o token JWT em todas as requisições
- **Response Interceptor**: Trata erros 401 e faz refresh automático do token
- **Queue Management**: Evita múltiplas tentativas de refresh simultâneas

### ✅ Refresh Automático de Tokens
- Detecta tokens expirados (erro 401)
- Faz refresh automático usando o refresh token
- Reexecuta requisições falhadas após refresh bem-sucedido
- Faz logout automático se o refresh falhar

### ✅ Tratamento Centralizado de Erros
- Categorização de erros por tipo (Network, Auth, Validation, etc.)
- Mensagens de erro padronizadas e user-friendly
- Mapeamento de códigos HTTP para mensagens específicas
- Suporte a erros de API customizados

### ✅ Retry Automático
- Retry para erros de rede (5xx, timeout, network errors)
- Delay exponencial entre tentativas (1s, 2s, 4s)
- Máximo de 2 tentativas por requisição
- Não faz retry para erros de validação (4xx)

### ✅ Logging de Desenvolvimento
- Logs detalhados de requisições e respostas em modo desenvolvimento
- Tracking de requisições com IDs únicos
- Logs de erros com contexto completo

### ✅ Configuração de Timeouts
- Timeouts específicos por tipo de operação:
  - Padrão: 30 segundos
  - Upload: 2 minutos
  - Download: 3 minutos
  - Autenticação: 15 segundos

### ✅ Cancelamento de Requisições
- Suporte a cancelamento de requisições individuais
- Função para cancelar todas as requisições ativas
- Cleanup automático de cancel tokens

### ✅ Monitoramento de Rede
- Verificação de status da rede
- Hook React para monitorar conectividade
- Fallback para navigator.onLine

## Estrutura do Código

### Configuração Base

```typescript
const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: TIMEOUTS.default,
  headers: {
    'Content-Type': 'application/json',
  },
  maxRedirects: 3,
  validateStatus: (status) => status < 500, // Não rejeita 4xx
})
```

### Interceptors

#### Request Interceptor
- Adiciona token JWT automaticamente
- Configura timeouts específicos por endpoint
- Adiciona cancel tokens para permitir cancelamento
- Faz logging em desenvolvimento

#### Response Interceptor
- Implementa retry automático para erros de rede
- Gerencia refresh de tokens JWT
- Faz logging de respostas e erros
- Limpa cancel tokens após conclusão

### Categorização de Erros

```typescript
enum ErrorCategory {
  NETWORK = 'NETWORK',           // Erros de conectividade
  AUTHENTICATION = 'AUTHENTICATION', // 401 - Token inválido/expirado
  AUTHORIZATION = 'AUTHORIZATION',   // 403 - Sem permissão
  VALIDATION = 'VALIDATION',         // 422 - Dados inválidos
  BUSINESS = 'BUSINESS',            // 404, 409 - Regras de negócio
  SERVER = 'SERVER',               // 5xx - Erros do servidor
  TIMEOUT = 'TIMEOUT',             // Timeout de requisição
  CANCELLED = 'CANCELLED',         // Requisição cancelada
  UNKNOWN = 'UNKNOWN',             // Erro não categorizado
}
```

### Services Disponíveis

#### Auth Service
```typescript
authService.login(email, senha)
authService.register(nome, email, senha)
authService.refresh(refreshToken)
authService.logout()
authService.validateToken()
```

#### Transaction Service
```typescript
transactionService.list(params)
transactionService.getById(id)
transactionService.create(data)
transactionService.update(id, data)
transactionService.delete(id)
transactionService.getSummary(params)
```

#### Budget Service
```typescript
budgetService.list(apenasAtivos)
budgetService.getById(id)
budgetService.create(data)
budgetService.update(id, data)
budgetService.delete(id)
```

#### Goal Service
```typescript
goalService.list(apenasAtivas, tipo)
goalService.getById(id)
goalService.create(data)
goalService.addProgress(id, valor)
goalService.delete(id)
```

#### Import Service
```typescript
importService.uploadFile(file, onUploadProgress)
importService.confirmImport(importId, transacoes)
importService.getImportStatus(importId)
```

#### Dashboard Service
```typescript
dashboardService.getSummary()
dashboardService.getRecentTransactions(limit)
dashboardService.getMonthlyChart(year)
dashboardService.getCategoryChart(month, year)
```

#### Health Service
```typescript
healthService.check()
healthService.ping() // Retorna latência em ms
```

#### Utility Service
```typescript
utilityService.getCategories()
utilityService.getUserSettings()
utilityService.updateUserSettings(settings)
utilityService.exportData(format, filters)
```

## Uso Prático

### Fazendo Requisições Básicas

```typescript
import { transactionService, getErrorMessage } from '@/lib/api'

try {
  const transactions = await transactionService.list({
    page: 0,
    size: 10,
    categoria: 'ALIMENTACAO'
  })
  console.log(transactions)
} catch (error) {
  const message = getErrorMessage(error)
  toast.error(message)
}
```

### Tratamento de Erros Avançado

```typescript
import { categorizeError, ErrorCategory } from '@/lib/api'

try {
  await transactionService.create(data)
} catch (error) {
  const categorized = categorizeError(error)
  
  switch (categorized.category) {
    case ErrorCategory.VALIDATION:
      // Mostrar erros de validação nos campos
      break
    case ErrorCategory.NETWORK:
      if (categorized.retryable) {
        // Mostrar opção de tentar novamente
      }
      break
    case ErrorCategory.AUTHENTICATION:
      // Redirecionar para login
      break
    default:
      toast.error(categorized.message)
  }
}
```

### Upload com Progress

```typescript
import { importService } from '@/lib/api'

const handleUpload = async (file: File) => {
  try {
    const result = await importService.uploadFile(file, (progressEvent) => {
      const progress = Math.round(
        (progressEvent.loaded * 100) / progressEvent.total
      )
      setUploadProgress(progress)
    })
    console.log('Upload concluído:', result)
  } catch (error) {
    console.error('Erro no upload:', getErrorMessage(error))
  }
}
```

### Monitoramento de Rede

```typescript
import { useNetworkStatus, checkNetworkStatus } from '@/lib/api'

function MyComponent() {
  const isOnline = useNetworkStatus()
  
  const handleRetry = async () => {
    const networkOk = await checkNetworkStatus()
    if (networkOk) {
      // Tentar novamente
    }
  }
  
  if (!isOnline) {
    return <div>Sem conexão com a internet</div>
  }
  
  return <div>Conectado</div>
}
```

### Cancelamento de Requisições

```typescript
import { cancelRequest, cancelAllRequests } from '@/lib/api'

// Cancelar requisição específica (se você tiver o requestId)
const cancelled = cancelRequest('req_123456')

// Cancelar todas as requisições ativas
const cancelledCount = cancelAllRequests()
console.log(`${cancelledCount} requisições canceladas`)
```

## Configuração de Ambiente

### Variáveis de Ambiente

```env
# .env
VITE_API_URL=http://localhost:8080/api
```

### Configuração de Desenvolvimento

O cliente automaticamente detecta o ambiente de desenvolvimento (`import.meta.env.DEV`) e ativa:
- Logs detalhados de requisições/respostas
- Informações de retry
- Tracking de performance

## Testes

O cliente HTTP possui cobertura completa de testes incluindo:
- Categorização de erros
- Extração de mensagens de erro
- Configuração de interceptors
- Integração com services
- Monitoramento de rede

Execute os testes com:
```bash
npm test api.test.ts
```

## Requisitos Atendidos

### ✅ Requirements 1.3 - Token Refresh
- Implementado refresh automático de tokens
- Queue management para evitar múltiplas tentativas
- Reexecução de requisições após refresh

### ✅ Requirements 10.2 - Segurança
- Tokens JWT gerenciados automaticamente
- Logout automático em caso de falha de autenticação
- Validação de tokens antes de requisições

## Próximos Passos

O cliente HTTP está completo e robusto, atendendo todos os requisitos da task 9.3:
- ✅ Configurar interceptors para JWT
- ✅ Implementar refresh automático de tokens  
- ✅ Tratamento centralizado de erros

Funcionalidades adicionais implementadas:
- ✅ Retry automático para erros de rede
- ✅ Logging de desenvolvimento
- ✅ Cancelamento de requisições
- ✅ Monitoramento de rede
- ✅ Timeouts configuráveis
- ✅ Testes abrangentes