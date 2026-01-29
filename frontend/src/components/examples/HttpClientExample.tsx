import { useState } from 'react'
import Button from '@/components/ui/Button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import { 
  transactionService, 
  authService, 
  healthService,
  getErrorMessage, 
  categorizeError, 
  ErrorCategory,
  useNetworkStatus,
  cancelAllRequests
} from '@/lib/api'
import toast from 'react-hot-toast'

/**
 * Componente de exemplo demonstrando o uso do cliente HTTP
 * Este componente mostra como usar os diferentes services e funcionalidades
 */
export function HttpClientExample() {
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState<any>(null)
  const [latency, setLatency] = useState<number | null>(null)
  const isOnline = useNetworkStatus()

  // Exemplo de requisição básica
  const handleListTransactions = async () => {
    setLoading(true)
    try {
      const transactions = await transactionService.list({
        page: 0,
        size: 5,
        ordenacao: 'data',
        direcao: 'desc'
      })
      setResult(transactions)
      toast.success('Transações carregadas com sucesso!')
    } catch (error) {
      const message = getErrorMessage(error)
      toast.error(message)
      setResult({ error: message })
    } finally {
      setLoading(false)
    }
  }

  // Exemplo de tratamento de erro avançado
  const handleCreateTransaction = async () => {
    setLoading(true)
    try {
      const newTransaction = await transactionService.create({
        valor: 50.00,
        descricao: 'Exemplo de transação',
        categoria: 'ALIMENTACAO',
        tipo: 'DESPESA',
        data: new Date().toISOString().split('T')[0]
      })
      setResult(newTransaction)
      toast.success('Transação criada com sucesso!')
    } catch (error) {
      const categorized = categorizeError(error)
      
      switch (categorized.category) {
        case ErrorCategory.VALIDATION:
          toast.error('Dados inválidos. Verifique os campos.')
          break
        case ErrorCategory.AUTHENTICATION:
          toast.error('Sessão expirada. Redirecionando para login...')
          // Aqui você redirecionaria para login
          break
        case ErrorCategory.NETWORK:
          if (categorized.retryable) {
            toast.error('Erro de rede. Tente novamente.')
          } else {
            toast.error('Sem conexão com a internet.')
          }
          break
        default:
          toast.error(categorized.message)
      }
      
      setResult({ 
        error: categorized.message, 
        category: categorized.category,
        retryable: categorized.retryable 
      })
    } finally {
      setLoading(false)
    }
  }

  // Exemplo de health check com latência
  const handleHealthCheck = async () => {
    setLoading(true)
    try {
      const startTime = Date.now()
      await healthService.check()
      const endTime = Date.now()
      const responseTime = endTime - startTime
      
      setLatency(responseTime)
      setResult({ status: 'healthy', responseTime })
      toast.success(`Servidor saudável (${responseTime}ms)`)
    } catch (error) {
      const message = getErrorMessage(error)
      toast.error(`Servidor indisponível: ${message}`)
      setResult({ status: 'unhealthy', error: message })
    } finally {
      setLoading(false)
    }
  }

  // Exemplo de ping para medir latência
  const handlePing = async () => {
    setLoading(true)
    try {
      const pingTime = await healthService.ping()
      setLatency(pingTime)
      setResult({ ping: pingTime })
      toast.success(`Ping: ${pingTime}ms`)
    } catch (error) {
      const message = getErrorMessage(error)
      toast.error(`Ping falhou: ${message}`)
      setResult({ error: message })
    } finally {
      setLoading(false)
    }
  }

  // Exemplo de validação de token
  const handleValidateToken = async () => {
    setLoading(true)
    try {
      const validation = await authService.validateToken()
      setResult(validation)
      toast.success('Token válido!')
    } catch (error) {
      const message = getErrorMessage(error)
      toast.error(`Token inválido: ${message}`)
      setResult({ error: message })
    } finally {
      setLoading(false)
    }
  }

  // Exemplo de cancelamento de requisições
  const handleCancelAll = () => {
    const cancelledCount = cancelAllRequests()
    toast.success(`${cancelledCount} requisições canceladas`)
    setLoading(false)
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>HTTP Client Example</CardTitle>
          <CardDescription>
            Demonstração das funcionalidades do cliente HTTP com Axios
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Status da rede */}
          <div className="flex items-center gap-2">
            <span>Status da rede:</span>
            <Badge variant={isOnline ? 'success' : 'destructive'}>
              {isOnline ? 'Online' : 'Offline'}
            </Badge>
            {latency && (
              <Badge variant="outline">
                Latência: {latency}ms
              </Badge>
            )}
          </div>

          {/* Botões de exemplo */}
          <div className="grid grid-cols-2 gap-4">
            <Button 
              onClick={handleListTransactions}
              disabled={loading}
              variant="secondary"
            >
              Listar Transações
            </Button>

            <Button 
              onClick={handleCreateTransaction}
              disabled={loading}
              variant="secondary"
            >
              Criar Transação
            </Button>

            <Button 
              onClick={handleHealthCheck}
              disabled={loading}
              variant="secondary"
            >
              Health Check
            </Button>

            <Button 
              onClick={handlePing}
              disabled={loading}
              variant="secondary"
            >
              Ping Server
            </Button>

            <Button 
              onClick={handleValidateToken}
              disabled={loading}
              variant="secondary"
            >
              Validar Token
            </Button>

            <Button 
              onClick={handleCancelAll}
              disabled={!loading}
              variant="danger"
            >
              Cancelar Requisições
            </Button>
          </div>

          {/* Loading indicator */}
          {loading && (
            <div className="flex items-center justify-center p-4">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
              <span className="ml-2">Carregando...</span>
            </div>
          )}

          {/* Resultado */}
          {result && (
            <Card>
              <CardHeader>
                <CardTitle className="text-sm">Resultado da Requisição</CardTitle>
              </CardHeader>
              <CardContent>
                <pre className="text-xs bg-muted p-4 rounded overflow-auto max-h-64">
                  {JSON.stringify(result, null, 2)}
                </pre>
              </CardContent>
            </Card>
          )}
        </CardContent>
      </Card>

      {/* Documentação rápida */}
      <Card>
        <CardHeader>
          <CardTitle className="text-sm">Funcionalidades Implementadas</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 gap-2 text-sm">
            <div className="flex items-center gap-2">
              <Badge variant="outline" className="text-xs">✅</Badge>
              <span>JWT Interceptors</span>
            </div>
            <div className="flex items-center gap-2">
              <Badge variant="outline" className="text-xs">✅</Badge>
              <span>Token Refresh</span>
            </div>
            <div className="flex items-center gap-2">
              <Badge variant="outline" className="text-xs">✅</Badge>
              <span>Error Handling</span>
            </div>
            <div className="flex items-center gap-2">
              <Badge variant="outline" className="text-xs">✅</Badge>
              <span>Auto Retry</span>
            </div>
            <div className="flex items-center gap-2">
              <Badge variant="outline" className="text-xs">✅</Badge>
              <span>Request Cancellation</span>
            </div>
            <div className="flex items-center gap-2">
              <Badge variant="outline" className="text-xs">✅</Badge>
              <span>Network Monitoring</span>
            </div>
            <div className="flex items-center gap-2">
              <Badge variant="outline" className="text-xs">✅</Badge>
              <span>Development Logging</span>
            </div>
            <div className="flex items-center gap-2">
              <Badge variant="outline" className="text-xs">✅</Badge>
              <span>Timeout Configuration</span>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

export default HttpClientExample