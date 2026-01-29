# Design Document: Sistema de Gestão Financeira Doméstica

## Overview

O sistema de gestão financeira doméstica é uma aplicação web fullstack que implementa os princípios de Domain-Driven Design com arquitetura hexagonal. O sistema mantém o usuário no controle total de todas as decisões financeiras, utilizando IA apenas como assessora, nunca como executora automática.

### Princípios Arquiteturais

- **Human-in-the-loop**: Toda ação requer confirmação explícita do usuário
- **Decision ≠ Action**: Sistema separa claramente recomendações de execuções
- **Domain First**: Lógica de negócio isolada de detalhes técnicos
- **Backend Soberano**: Backend governa todo o estado da aplicação
- **Frontend Orquestrador**: Frontend apenas orquestra decisões do usuário

## Architecture

### Visão Geral da Arquitetura

```mermaid
graph TB
    subgraph "Frontend (React + TypeScript)"
        UI[Interface do Usuário]
        State[Zustand State Management]
        Auth[JWT Authentication]
        API[API Client (Axios)]
    end
    
    subgraph "Backend (Java 21 + Spring Boot)"
        subgraph "Web Layer"
            Controllers[REST Controllers]
            Security[Spring Security + JWT]
        end
        
        subgraph "Application Layer"
            UseCases[Use Cases]
            Ports[Ports/Interfaces]
            DTOs[DTOs]
        end
        
        subgraph "Domain Layer"
            Entities[Domain Entities]
            ValueObjects[Value Objects]
            DomainServices[Domain Services]
        end
        
        subgraph "Infrastructure Layer"
            Repositories[JPA Repositories]
            AIService[AI Service Implementation]
            FileProcessor[File Processing]
        end
    end
    
    subgraph "External Services"
        DB[(PostgreSQL)]
        AIProvider[AI Provider API]
        MarketData[Market Data API]
    end
    
    UI --> Controllers
    Controllers --> UseCases
    UseCases --> Entities
    UseCases --> Ports
    Ports --> Repositories
    Ports --> AIService
    Repositories --> DB
    AIService --> AIProvider
    FileProcessor --> MarketData
```

### Arquitetura Hexagonal (Ports & Adapters)

O sistema implementa arquitetura hexagonal com clara separação de responsabilidades:

**Domain Layer (Centro)**:
- Entidades de negócio puras (Java puro, sem dependências externas)
- Value Objects para conceitos de domínio
- Regras de negócio e invariantes

**Application Layer (Orquestração)**:
- Use Cases que orquestram operações de negócio
- Ports (interfaces) para comunicação com infraestrutura
- DTOs para transferência de dados

**Infrastructure Layer (Adaptadores)**:
- Implementações concretas dos ports
- Persistência com JPA/Hibernate
- Integração com APIs externas

**Web Layer (Interface)**:
- Controllers REST
- Autenticação e autorização
- Serialização/deserialização

## Components and Interfaces

### Domain Layer

#### Core Entities

```java
// Usuario.java
public class Usuario {
    private UsuarioId id;
    private Email email;
    private SenhaHash senha;
    private Nome nome;
    private LocalDateTime criadoEm;
    private boolean ativo;
    private boolean dadosIniciais;
    
    // Métodos de negócio
    public void marcarDadosIniciaisCarregados();
    public boolean podeImportarPlanilha();
}

// Transacao.java
public class Transacao {
    private TransacaoId id;
    private UsuarioId usuarioId;
    private Valor valor;
    private Descricao descricao;
    private Categoria categoria;
    private LocalDate data;
    private TipoTransacao tipo; // RECEITA, DESPESA
    private boolean ativa;
    
    // Métodos de negócio
    public void validarTransacao();
    public boolean afetaOrcamento();
}

// Orcamento.java
public class Orcamento {
    private OrcamentoId id;
    private UsuarioId usuarioId;
    private Categoria categoria;
    private Valor limite;
    private PeriodoOrcamento periodo;
    private Valor gastoAtual;
    private StatusOrcamento status;
    
    // Métodos de negócio
    public void adicionarGasto(Valor valor);
    public boolean estaProximoDoLimite(); // 80%
    public boolean excedeuLimite();
}

// MetaFinanceira.java
public class MetaFinanceira {
    private MetaId id;
    private UsuarioId usuarioId;
    private Nome nome;
    private Valor valorAlvo;
    private Valor valorAtual;
    private LocalDate prazo;
    private TipoMeta tipo;
    private StatusMeta status;
    
    // Métodos de negócio
    public void adicionarProgresso(Valor valor);
    public BigDecimal calcularPercentualConclusao();
    public LocalDate estimarDataConclusao();
}
```

#### Value Objects

```java
public record Email(String valor) {
    public Email {
        if (!isValidEmail(valor)) {
            throw new IllegalArgumentException("Email inválido");
        }
    }
}

public record Valor(BigDecimal quantia, Moeda moeda) {
    public Valor {
        if (quantia == null || quantia.scale() > 2) {
            throw new IllegalArgumentException("Valor inválido");
        }
    }
    
    public Valor somar(Valor outro) {
        validarMoeda(outro);
        return new Valor(this.quantia.add(outro.quantia), this.moeda);
    }
}

public record Categoria(String nome, TipoCategoria tipo) {
    // Categorias predefinidas: ALIMENTACAO, TRANSPORTE, MORADIA, etc.
}
```

### Application Layer

#### Use Cases

```java
@Service
@Transactional
public class ImportarPlanilhaUseCase {
    private final UsuarioRepository usuarioRepository;
    private final TransacaoRepository transacaoRepository;
    private final ProcessadorPlanilhaPort processadorPort;
    private final NotificacaoPort notificacaoPort;
    
    public ResultadoImportacao executar(ComandoImportarPlanilha comando) {
        // 1. Validar usuário e arquivo
        // 2. Processar planilha
        // 3. Validar dados
        // 4. Detectar duplicatas
        // 5. Salvar transações
        // 6. Marcar dados iniciais como carregados
        // 7. Notificar usuário
    }
}

@Service
@Transactional
public class CriarTransacaoUseCase {
    public Transacao executar(ComandoTransacao comando) {
        // 1. Validar dados da transação
        // 2. Criar entidade Transacao
        // 3. Atualizar orçamentos afetados
        // 4. Atualizar metas relacionadas
        // 5. Salvar transação
        // 6. Publicar evento de transação criada
    }
}

@Service
public class GerarInsightsUseCase {
    private final IAAssessoraPort iaPort;
    private final TransacaoRepository transacaoRepository;
    
    public List<Insight> executar(UsuarioId usuarioId) {
        // 1. Coletar dados financeiros do usuário
        // 2. Solicitar análise à IA
        // 3. Validar recomendações
        // 4. Marcar insights como "requer confirmação"
        // 5. Retornar insights para apresentação
    }
}
```

#### Ports (Interfaces)

```java
public interface ProcessadorPlanilhaPort {
    DadosPlanilha processarArquivo(ArquivoPlanilha arquivo);
    List<TransacaoImportada> extrairTransacoes(DadosPlanilha dados);
    List<DuplicataPotencial> detectarDuplicatas(List<TransacaoImportada> transacoes);
}

public interface IAAssessoraPort {
    List<RecomendacaoIA> analisarGastos(DadosFinanceiros dados);
    List<OportunidadeEconomia> identificarEconomias(HistoricoTransacoes historico);
    RecomendacaoInvestimento avaliarCarteira(CarteiraInvestimentos carteira);
}

public interface NotificacaoPort {
    void notificarOrcamentoProximoLimite(Orcamento orcamento);
    void notificarMetaAlcancada(MetaFinanceira meta);
    void notificarImportacaoConcluida(ResultadoImportacao resultado);
}
```

### Infrastructure Layer

#### Repository Implementations

```java
@Repository
public class TransacaoRepositoryImpl implements TransacaoRepository {
    private final TransacaoJpaRepository jpaRepository;
    private final TransacaoMapper mapper;
    
    @Override
    public List<Transacao> buscarPorUsuarioEPeriodo(UsuarioId usuarioId, Periodo periodo) {
        return jpaRepository.findByUsuarioIdAndDataBetween(
            usuarioId.valor(), 
            periodo.inicio(), 
            periodo.fim()
        ).stream()
        .map(mapper::toDomain)
        .toList();
    }
}
```

#### AI Service Implementation

```java
@Component
public class IAAssessoraAdapter implements IAAssessoraPort {
    private final OpenAIClient openAIClient;
    private final PromptTemplate promptTemplate;
    
    @Override
    public List<RecomendacaoIA> analisarGastos(DadosFinanceiros dados) {
        String prompt = promptTemplate.criarPromptAnaliseGastos(dados);
        RespostaIA resposta = openAIClient.completar(prompt);
        
        return resposta.getRecomendacoes().stream()
            .map(r -> new RecomendacaoIA(
                r.getTipo(),
                r.getDescricao(),
                r.getJustificativa(),
                false // sempre requer confirmação
            ))
            .toList();
    }
}
```

## Data Models

### Database Schema

```sql
-- Usuários
CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    nome VARCHAR(255) NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    ativo BOOLEAN NOT NULL DEFAULT true,
    dados_iniciais_carregados BOOLEAN NOT NULL DEFAULT false
);

-- Transações
CREATE TABLE transacoes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    valor DECIMAL(15,2) NOT NULL,
    moeda VARCHAR(3) NOT NULL DEFAULT 'BRL',
    descricao TEXT NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('RECEITA', 'DESPESA')),
    data DATE NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    ativa BOOLEAN NOT NULL DEFAULT true,
    
    INDEX idx_usuario_data (usuario_id, data),
    INDEX idx_categoria (categoria),
    INDEX idx_tipo (tipo)
);

-- Orçamentos
CREATE TABLE orcamentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    categoria VARCHAR(50) NOT NULL,
    limite DECIMAL(15,2) NOT NULL,
    periodo VARCHAR(20) NOT NULL CHECK (periodo IN ('MENSAL', 'TRIMESTRAL', 'ANUAL')),
    gasto_atual DECIMAL(15,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    inicio_periodo DATE NOT NULL,
    fim_periodo DATE NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    
    UNIQUE(usuario_id, categoria, inicio_periodo)
);

-- Metas Financeiras
CREATE TABLE metas_financeiras (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    nome VARCHAR(255) NOT NULL,
    valor_alvo DECIMAL(15,2) NOT NULL,
    valor_atual DECIMAL(15,2) NOT NULL DEFAULT 0,
    prazo DATE NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVA',
    criado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Carteira de Investimentos
CREATE TABLE investimentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    ativo VARCHAR(20) NOT NULL, -- Código do ativo (PETR4, ITUB4, etc.)
    nome_ativo VARCHAR(255) NOT NULL,
    quantidade DECIMAL(15,6) NOT NULL,
    preco_compra DECIMAL(15,4) NOT NULL,
    data_compra DATE NOT NULL,
    tipo_ativo VARCHAR(30) NOT NULL, -- ACAO, FUNDO, CRIPTO, etc.
    ativo_registro BOOLEAN NOT NULL DEFAULT true
);

-- Insights da IA
CREATE TABLE insights_ia (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    tipo VARCHAR(50) NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT NOT NULL,
    justificativa TEXT NOT NULL,
    confirmado BOOLEAN NOT NULL DEFAULT false,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    valido_ate TIMESTAMP NOT NULL
);
```

### DTOs para API

```java
// Request DTOs
public record CriarTransacaoRequest(
    @NotNull @DecimalMin("0.01") BigDecimal valor,
    @NotBlank String descricao,
    @NotNull String categoria,
    @NotNull TipoTransacao tipo,
    @NotNull LocalDate data
) {}

public record CriarOrcamentoRequest(
    @NotNull String categoria,
    @NotNull @DecimalMin("0.01") BigDecimal limite,
    @NotNull PeriodoOrcamento periodo,
    @NotNull LocalDate inicioVigencia
) {}

// Response DTOs
public record TransacaoResponse(
    String id,
    BigDecimal valor,
    String moeda,
    String descricao,
    String categoria,
    TipoTransacao tipo,
    LocalDate data,
    LocalDateTime criadoEm
) {}

public record DashboardResponse(
    BigDecimal saldoAtual,
    BigDecimal receitaMensal,
    BigDecimal despesaMensal,
    List<OrcamentoStatusResponse> statusOrcamentos,
    List<MetaProgressoResponse> progressoMetas,
    List<TransacaoResponse> transacoesRecentes,
    ResumoInvestimentosResponse resumoInvestimentos
) {}
```

### Frontend Validation with Zod

O frontend utiliza Zod para validação de formulários e dados, garantindo type-safety e validação consistente antes do envio para o backend.

```typescript
// Schemas de validação Zod
import { z } from 'zod';

// Schema para criação de transação
export const criarTransacaoSchema = z.object({
  valor: z.number()
    .min(0.01, 'Valor deve ser maior que zero')
    .max(999999.99, 'Valor muito alto'),
  descricao: z.string()
    .min(1, 'Descrição é obrigatória')
    .max(255, 'Descrição muito longa'),
  categoria: z.enum(['ALIMENTACAO', 'TRANSPORTE', 'MORADIA', 'LAZER', 'SAUDE', 'OUTROS']),
  tipo: z.enum(['RECEITA', 'DESPESA']),
  data: z.string()
    .refine((date) => !isNaN(Date.parse(date)), 'Data inválida')
});

// Schema para criação de orçamento
export const criarOrcamentoSchema = z.object({
  categoria: z.string().min(1, 'Categoria é obrigatória'),
  limite: z.number()
    .min(0.01, 'Limite deve ser maior que zero')
    .max(999999.99, 'Limite muito alto'),
  periodo: z.enum(['MENSAL', 'TRIMESTRAL', 'ANUAL']),
  inicioVigencia: z.string()
    .refine((date) => !isNaN(Date.parse(date)), 'Data inválida')
});

// Schema para autenticação
export const loginSchema = z.object({
  email: z.string()
    .email('Email inválido')
    .min(1, 'Email é obrigatório'),
  senha: z.string()
    .min(8, 'Senha deve ter pelo menos 8 caracteres')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/, 
           'Senha deve conter maiúscula, minúscula, número e caractere especial')
});

// Schema para registro
export const registroSchema = z.object({
  nome: z.string()
    .min(2, 'Nome deve ter pelo menos 2 caracteres')
    .max(100, 'Nome muito longo'),
  email: z.string()
    .email('Email inválido')
    .min(1, 'Email é obrigatório'),
  senha: z.string()
    .min(8, 'Senha deve ter pelo menos 8 caracteres')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/, 
           'Senha deve conter maiúscula, minúscula, número e caractere especial'),
  confirmarSenha: z.string()
}).refine((data) => data.senha === data.confirmarSenha, {
  message: "Senhas não coincidem",
  path: ["confirmarSenha"],
});

// Types inferidos dos schemas
export type CriarTransacaoForm = z.infer<typeof criarTransacaoSchema>;
export type CriarOrcamentoForm = z.infer<typeof criarOrcamentoSchema>;
export type LoginForm = z.infer<typeof loginSchema>;
export type RegistroForm = z.infer<typeof registroSchema>;
```

### Form Validation Integration

```typescript
// Hook personalizado para formulários com Zod
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';

export function useValidatedForm<T extends z.ZodType>(schema: T) {
  return useForm<z.infer<T>>({
    resolver: zodResolver(schema),
    mode: 'onChange', // Validação em tempo real
  });
}

// Exemplo de uso em componente
export function CriarTransacaoForm() {
  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
  } = useValidatedForm(criarTransacaoSchema);

  const onSubmit = async (data: CriarTransacaoForm) => {
    try {
      // Dados já validados pelo Zod
      await criarTransacao(data);
      toast.success('Transação criada com sucesso!');
    } catch (error) {
      toast.error('Erro ao criar transação');
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <Input
        {...register('valor')}
        type="number"
        step="0.01"
        placeholder="Valor"
        error={errors.valor?.message}
      />
      <Input
        {...register('descricao')}
        placeholder="Descrição"
        error={errors.descricao?.message}
      />
      {/* Outros campos... */}
      <Button type="submit" disabled={!isValid}>
        Criar Transação
      </Button>
    </form>
  );
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Authentication and Security Properties

**Property 1: Valid credentials authentication**
*For any* valid user credentials, authentication should succeed and return both access and refresh JWT tokens with proper expiration times
**Validates: Requirements 1.1**

**Property 2: Invalid credentials rejection**
*For any* invalid credential combination (wrong email, wrong password, malformed input), authentication should fail and return appropriate error messages
**Validates: Requirements 1.2**

**Property 3: Token refresh round-trip**
*For any* valid refresh token, using it to refresh should produce a new valid access token that can be used for authenticated requests
**Validates: Requirements 1.3**

**Property 4: Logout token invalidation**
*For any* authenticated user session, logging out should invalidate both access and refresh tokens, making them unusable for subsequent requests
**Validates: Requirements 1.4**

### Data Import Properties

**Property 5: Valid file processing**
*For any* properly formatted Excel/CSV file with valid financial data, the import process should successfully parse all transactions and store them in the system
**Validates: Requirements 2.1**

**Property 6: Invalid file rejection**
*For any* file with invalid format, corrupted data, or missing required columns, the import should fail with descriptive error messages
**Validates: Requirements 2.2**

**Property 7: Required field validation**
*For any* imported transaction record, if it lacks required fields (date, amount, description, category), it should be rejected during validation
**Validates: Requirements 2.6**

### Transaction Management Properties

**Property 8: Transaction creation consistency**
*For any* valid transaction data, creating a transaction should store it with all provided information and generate appropriate system metadata (ID, timestamp)
**Validates: Requirements 3.1**

**Property 9: Transaction update audit preservation**
*For any* transaction update operation, the system should preserve the original transaction data in audit logs while updating the current record
**Validates: Requirements 3.2**

**Property 10: Running balance invariant**
*For any* sequence of transactions, the running balance should equal the sum of all transaction values up to that point, regardless of the order transactions were entered
**Validates: Requirements 3.6**

### Budget Management Properties

**Property 11: Budget parameter validation**
*For any* budget creation request, if it contains valid category, positive amount, and valid period, the budget should be created successfully
**Validates: Requirements 5.1**

**Property 12: Budget spending tracking accuracy**
*For any* budget and related transactions, the current spending amount should always equal the sum of all transactions in that category within the budget period
**Validates: Requirements 5.2**

### Goal Tracking Properties

**Property 13: Goal progress calculation**
*For any* financial goal and associated savings transactions, the progress percentage should accurately reflect the ratio of current savings to target amount
**Validates: Requirements 6.2**

### Investment Portfolio Properties

**Property 14: Portfolio value calculation**
*For any* set of investment positions and current market prices, the total portfolio value should equal the sum of (quantity × current_price) for all positions
**Validates: Requirements 7.2**

### AI Recommendations Properties

**Property 15: AI recommendation explanations**
*For any* AI-generated recommendation, it should include both the recommendation text and a clear explanation of the reasoning behind it
**Validates: Requirements 8.5**

### Confirmation System Properties

**Property 16: Financial action confirmation requirement**
*For any* state-changing financial operation, the system should present a confirmation dialog before execution and require explicit user approval
**Validates: Requirements 9.1**

### Security and Governance Properties

**Property 17: Data encryption consistency**
*For any* sensitive financial data (transactions, balances, personal information), it should be encrypted both when stored and when transmitted
**Validates: Requirements 10.1**

**Property 18: Backend state authority**
*For any* attempt to modify financial state, the change should only be accepted if it comes through proper backend validation, never directly from frontend
**Validates: Requirements 10.8**

## Error Handling

### Error Categories and Responses

**Validation Errors (400 Bad Request)**:
- Invalid input data format
- Missing required fields
- Business rule violations
- File format errors

```java
public class ValidationErrorResponse {
    private String message;
    private List<FieldError> fieldErrors;
    private String errorCode;
    private LocalDateTime timestamp;
}
```

**Authentication Errors (401 Unauthorized)**:
- Invalid credentials
- Expired tokens
- Missing authentication

**Authorization Errors (403 Forbidden)**:
- Insufficient permissions
- Account locked
- Resource access denied

**Business Logic Errors (422 Unprocessable Entity)**:
- Budget limit exceeded
- Duplicate transaction detection
- Goal deadline conflicts

**System Errors (500 Internal Server Error)**:
- Database connection failures
- External API failures
- Unexpected system errors

### Error Handling Strategy

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(ex.getMessage(), "VALIDATION_ERROR"));
    }
    
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex) {
        return ResponseEntity.unprocessableEntity()
            .body(new ErrorResponse(ex.getMessage(), ex.getErrorCode()));
    }
    
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurity(SecurityException ex) {
        // Log security incident
        securityLogger.warn("Security violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse("Access denied", "SECURITY_ERROR"));
    }
}
```

### Circuit Breaker for External Services

```java
@Component
public class MarketDataService {
    
    @CircuitBreaker(name = "market-data", fallbackMethod = "fallbackMarketData")
    @Retry(name = "market-data")
    public MarketPrice getMarketPrice(String symbol) {
        return marketDataClient.getPrice(symbol);
    }
    
    public MarketPrice fallbackMarketData(String symbol, Exception ex) {
        // Return cached price or default value
        return priceCache.getLastKnownPrice(symbol)
            .orElse(MarketPrice.unavailable(symbol));
    }
}
```

## Testing Strategy

### Dual Testing Approach

O sistema utiliza uma abordagem dupla de testes para garantir cobertura abrangente:

**Unit Tests**: Focam em exemplos específicos, casos extremos e condições de erro
- Testes de validação de entrada
- Testes de regras de negócio específicas
- Testes de integração entre componentes
- Casos extremos e condições de erro

**Property-Based Tests**: Verificam propriedades universais através de muitas entradas geradas
- Propriedades de correção do domínio
- Invariantes do sistema
- Comportamentos que devem ser consistentes
- Cobertura abrangente de entrada através de randomização

### Property-Based Testing Configuration

**Framework**: Utilizaremos **jqwik** para Java, que é uma biblioteca madura de property-based testing
- Mínimo de 100 iterações por teste de propriedade
- Cada teste de propriedade deve referenciar sua propriedade do documento de design
- Formato de tag: **Feature: gestao-financeira-domestica, Property {number}: {property_text}**

### Test Structure Examples

```java
// Property Test Example
@Property
@Label("Feature: gestao-financeira-domestica, Property 10: Running balance invariant")
void runningBalanceShouldEqualSumOfTransactions(@ForAll List<@Valid TransacaoData> transacoes) {
    // Arrange
    Usuario usuario = criarUsuarioTeste();
    
    // Act
    transacoes.forEach(t -> transacaoService.criarTransacao(usuario.getId(), t));
    BigDecimal saldoCalculado = transacaoService.calcularSaldoAtual(usuario.getId());
    
    // Assert
    BigDecimal somaEsperada = transacoes.stream()
        .map(t -> t.getTipo() == RECEITA ? t.getValor() : t.getValor().negate())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    assertThat(saldoCalculado).isEqualTo(somaEsperada);
}

// Unit Test Example
@Test
void deveRejeitarTransacaoComValorNegativo() {
    // Arrange
    Usuario usuario = criarUsuarioTeste();
    TransacaoData transacaoInvalida = new TransacaoData(
        new BigDecimal("-100.00"),
        "Transação inválida",
        Categoria.ALIMENTACAO,
        TipoTransacao.DESPESA,
        LocalDate.now()
    );
    
    // Act & Assert
    assertThatThrownBy(() -> 
        transacaoService.criarTransacao(usuario.getId(), transacaoInvalida))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Valor deve ser positivo");
}
```

### Integration Testing

```java
@SpringBootTest
@Testcontainers
class TransacaoIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("finance_test")
        .withUsername("test")
        .withPassword("test");
    
    @Test
    void deveProcessarFluxoCompletoDeTransacao() {
        // Teste de integração end-to-end
        // 1. Criar usuário
        // 2. Autenticar
        // 3. Criar transação via API
        // 4. Verificar persistência
        // 5. Verificar atualização de orçamento
    }
}
```

### Performance Testing

```java
@Test
@Timeout(value = 2, unit = TimeUnit.SECONDS)
void deveCalcularSaldoRapidamentePara1000Transacoes() {
    // Teste de performance para operações críticas
    List<Transacao> transacoes = gerarTransacoes(1000);
    
    long inicio = System.currentTimeMillis();
    BigDecimal saldo = calculadoraSaldo.calcular(transacoes);
    long duracao = System.currentTimeMillis() - inicio;
    
    assertThat(duracao).isLessThan(1000); // Menos de 1 segundo
    assertThat(saldo).isNotNull();
}
```

### Test Data Generators

```java
@Provide
Arbitrary<TransacaoData> transacoesValidas() {
    return Combinators.combine(
        Arbitraries.bigDecimals()
            .between(BigDecimal.valueOf(0.01), BigDecimal.valueOf(10000.00))
            .ofScale(2),
        Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(100),
        Arbitraries.of(Categoria.class),
        Arbitraries.of(TipoTransacao.class),
        Arbitraries.dates().between(
            LocalDate.now().minusYears(2),
            LocalDate.now()
        )
    ).as(TransacaoData::new);
}

@Provide
Arbitrary<Usuario> usuariosValidos() {
    return Combinators.combine(
        Arbitraries.emails(),
        Arbitraries.strings().alpha().ofMinLength(8).ofMaxLength(50),
        Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(100)
    ).as((email, senha, nome) -> 
        new Usuario(new Email(email), new SenhaHash(senha), new Nome(nome))
    );
}
```

### Continuous Testing Strategy

- **Unit tests**: Executados a cada commit
- **Integration tests**: Executados a cada pull request
- **Property tests**: Executados em pipeline de CI/CD
- **Performance tests**: Executados semanalmente
- **Security tests**: Executados antes de cada release

### Test Coverage Requirements

- **Line Coverage**: Mínimo 80% para código de produção
- **Branch Coverage**: Mínimo 70% para lógica de negócio
- **Property Coverage**: Todas as propriedades de correção devem ter testes correspondentes
- **Integration Coverage**: Todos os endpoints REST devem ter testes de integração