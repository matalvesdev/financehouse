# Design Document: Funcionalidades Pendentes

## Overview

Este documento detalha o design técnico para implementar as funcionalidades pendentes no Sistema de Gestão Financeira Doméstica. O foco principal está em completar os controllers REST faltantes (Dashboard e Import) e adicionar operações CRUD completas para orçamentos e metas.

## Architecture

### Existing Architecture (Maintained)

O sistema já possui arquitetura hexagonal bem estabelecida:

```
Web Layer (Controllers) → Application Layer (Use Cases) → Domain Layer (Entities) → Infrastructure Layer (Repositories)
```

As novas implementações seguirão exatamente o mesmo padrão arquitetural.

## Components and Interfaces

### 1. Dashboard Controller

#### 1.1 DashboardController (Web Layer)

```java
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final ObterResumoDashboardUseCase obterResumoDashboardUseCase;
    
    /**
     * Obtém resumo consolidado do dashboard do usuário.
     * 
     * GET /dashboard/resumo
     * 
     * @return DashboardResponse com dados consolidados
     */
    @GetMapping("/resumo")
    public ResponseEntity<DashboardResponse> obterResumo(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UsuarioId usuarioId = new UsuarioId(UUID.fromString(userDetails.getUsername()));
        DashboardResponse resumo = obterResumoDashboardUseCase.executar(usuarioId);
        
        return ResponseEntity.ok(resumo);
    }
}
```

#### 1.2 ObterResumoDashboardUseCase (Application Layer)

```java
@Service
@Transactional(readOnly = true)
public class ObterResumoDashboardUseCase {
    
    private final TransacaoRepository transacaoRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final MetaFinanceiraRepository metaFinanceiraRepository;
    
    public DashboardResponse executar(UsuarioId usuarioId) {
        // 1. Calcular saldo atual (soma de todas as transações ativas)
        BigDecimal saldoAtual = calcularSaldoAtual(usuarioId);
        
        // 2. Calcular receita mensal (mês atual)
        BigDecimal receitaMensal = calcularReceitaMensal(usuarioId);
        
        // 3. Calcular despesa mensal (mês atual)
        BigDecimal despesaMensal = calcularDespesaMensal(usuarioId);
        
        // 4. Obter status de orçamentos ativos
        List<OrcamentoStatusResponse> statusOrcamentos = obterStatusOrcamentos(usuarioId);
        
        // 5. Obter progresso de metas ativas
        List<MetaProgressoResponse> progressoMetas = obterProgressoMetas(usuarioId);
        
        // 6. Obter transações recentes (últimas 10)
        List<TransacaoResponse> transacoesRecentes = obterTransacoesRecentes(usuarioId);
        
        // 7. Obter resumo de investimentos (se existir)
        ResumoInvestimentosResponse resumoInvestimentos = obterResumoInvestimentos(usuarioId);
        
        return new DashboardResponse(
            saldoAtual,
            receitaMensal,
            despesaMensal,
            statusOrcamentos,
            progressoMetas,
            transacoesRecentes,
            resumoInvestimentos
        );
    }
    
    private BigDecimal calcularSaldoAtual(UsuarioId usuarioId) {
        LocalDate inicio = LocalDate.of(2000, 1, 1); // Data muito antiga
        LocalDate fim = LocalDate.now();
        
        List<Transacao> todasTransacoes = transacaoRepository
            .buscarPorUsuarioEPeriodo(usuarioId, inicio, fim);
        
        return todasTransacoes.stream()
            .map(t -> t.getTipo() == TipoTransacao.RECEITA 
                ? t.getValor().quantia() 
                : t.getValor().quantia().negate())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calcularReceitaMensal(UsuarioId usuarioId) {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate fimMes = LocalDate.now().withDayOfMonth(
            LocalDate.now().lengthOfMonth());
        
        List<Transacao> transacoes = transacaoRepository
            .buscarPorUsuarioEPeriodo(usuarioId, inicioMes, fimMes);
        
        return transacoes.stream()
            .filter(t -> t.getTipo() == TipoTransacao.RECEITA)
            .map(t -> t.getValor().quantia())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calcularDespesaMensal(UsuarioId usuarioId) {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate fimMes = LocalDate.now().withDayOfMonth(
            LocalDate.now().lengthOfMonth());
        
        List<Transacao> transacoes = transacaoRepository
            .buscarPorUsuarioEPeriodo(usuarioId, inicioMes, fimMes);
        
        return transacoes.stream()
            .filter(t -> t.getTipo() == TipoTransacao.DESPESA)
            .map(t -> t.getValor().quantia())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private List<OrcamentoStatusResponse> obterStatusOrcamentos(UsuarioId usuarioId) {
        List<Orcamento> orcamentosAtivos = orcamentoRepository
            .buscarOrcamentosAtivos(usuarioId);
        
        return orcamentosAtivos.stream()
            .map(o -> new OrcamentoStatusResponse(
                o.getId().valor().toString(),
                o.getCategoria(),
                o.getLimite().quantia(),
                o.getGastoAtual().quantia(),
                calcularPercentualGasto(o),
                o.getStatus()
            ))
            .toList();
    }
    
    private BigDecimal calcularPercentualGasto(Orcamento orcamento) {
        if (orcamento.getLimite().quantia().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return orcamento.getGastoAtual().quantia()
            .divide(orcamento.getLimite().quantia(), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
    
    private List<MetaProgressoResponse> obterProgressoMetas(UsuarioId usuarioId) {
        List<MetaFinanceira> metasAtivas = metaFinanceiraRepository
            .buscarMetasAtivas(usuarioId);
        
        return metasAtivas.stream()
            .map(m -> new MetaProgressoResponse(
                m.getId().valor().toString(),
                m.getNome().valor(),
                m.getValorAlvo().quantia(),
                m.getValorAtual().quantia(),
                m.calcularPercentualConclusao(),
                m.getPrazo(),
                m.estimarDataConclusao(),
                m.getStatus()
            ))
            .toList();
    }
    
    private List<TransacaoResponse> obterTransacoesRecentes(UsuarioId usuarioId) {
        List<Transacao> transacoes = transacaoRepository
            .buscarTransacoesRecentes(usuarioId, 10);
        
        return transacoes.stream()
            .map(t -> new TransacaoResponse(
                t.getId().valor().toString(),
                t.getValor().quantia(),
                t.getValor().moeda().getCodigo(),
                t.getDescricao().valor(),
                t.getCategoria(),
                t.getTipo(),
                t.getData(),
                t.getCriadoEm()
            ))
            .toList();
    }
    
    private ResumoInvestimentosResponse obterResumoInvestimentos(UsuarioId usuarioId) {
        // Por enquanto, retorna null (investimentos não implementados)
        // Quando implementado, buscar investimentos e calcular resumo
        return null;
    }
}
```

### 2. Import Controller

#### 2.1 ImportController (Web Layer)

```java
@RestController
@RequestMapping("/importacao")
@RequiredArgsConstructor
public class ImportController {
    
    private final ImportarPlanilhaUseCase importarPlanilhaUseCase;
    
    /**
     * Faz upload e processa planilha de transações.
     * 
     * POST /importacao/upload
     * 
     * @param file arquivo Excel ou CSV
     * @return ResultadoImportacaoResponse com estatísticas da importação
     */
    @PostMapping("/upload")
    public ResponseEntity<ResultadoImportacaoResponse> uploadPlanilha(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // Validar arquivo
            validarArquivo(file);
            
            // Criar comando
            UsuarioId usuarioId = new UsuarioId(UUID.fromString(userDetails.getUsername()));
            ComandoImportarPlanilha comando = new ComandoImportarPlanilha(
                usuarioId,
                file.getOriginalFilename(),
                file.getBytes(),
                file.getContentType()
            );
            
            // Executar importação
            ResultadoImportacaoResponse resultado = importarPlanilhaUseCase.executar(comando);
            
            return ResponseEntity.ok(resultado);
            
        } catch (IOException e) {
            throw new IllegalArgumentException("Erro ao ler arquivo: " + e.getMessage());
        }
    }
    
    private void validarArquivo(MultipartFile file) {
        // Validar se arquivo foi enviado
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode ser vazio");
        }
        
        // Validar tamanho (máximo 10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB em bytes
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException(
                "Arquivo muito grande. Tamanho máximo: 10MB");
        }
        
        // Validar extensão
        String filename = file.getOriginalFilename();
        if (filename == null || 
            (!filename.endsWith(".xlsx") && 
             !filename.endsWith(".xls") && 
             !filename.endsWith(".csv"))) {
            throw new IllegalArgumentException(
                "Formato de arquivo inválido. Use .xlsx, .xls ou .csv");
        }
    }
}
```

#### 2.2 ImportarPlanilhaUseCase (Application Layer)

```java
@Service
@Transactional
public class ImportarPlanilhaUseCase {
    
    private final UsuarioRepository usuarioRepository;
    private final TransacaoRepository transacaoRepository;
    private final ProcessadorPlanilhaPort processadorPlanilhaPort;
    private final AuditLogger auditLogger;
    
    public ResultadoImportacaoResponse executar(ComandoImportarPlanilha comando) {
        // 1. Validar usuário
        Usuario usuario = usuarioRepository.buscarPorId(comando.usuarioId())
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        
        // 2. Processar planilha
        DadosPlanilha dadosPlanilha = processadorPlanilhaPort.processarArquivo(
            new ArquivoPlanilha(
                comando.nomeArquivo(),
                comando.conteudo(),
                comando.contentType()
            )
        );
        
        // 3. Extrair transações
        List<TransacaoImportada> transacoesImportadas = 
            processadorPlanilhaPort.extrairTransacoes(dadosPlanilha);
        
        // 4. Detectar duplicatas
        List<DuplicataPotencial> duplicatas = 
            processadorPlanilhaPort.detectarDuplicatas(transacoesImportadas);
        
        // 5. Validar e salvar transações
        List<TransacaoResponse> transacoesSalvas = new ArrayList<>();
        List<ErroImportacaoResponse> erros = new ArrayList<>();
        
        for (int i = 0; i < transacoesImportadas.size(); i++) {
            TransacaoImportada importada = transacoesImportadas.get(i);
            
            try {
                // Validar dados
                validarTransacaoImportada(importada);
                
                // Criar transação
                Transacao transacao = criarTransacaoDeImportacao(
                    usuario.getId(), importada);
                
                // Salvar
                Transacao salva = transacaoRepository.salvar(transacao);
                
                // Adicionar à lista de sucesso
                transacoesSalvas.add(mapearParaResponse(salva));
                
            } catch (Exception e) {
                // Adicionar à lista de erros
                erros.add(new ErroImportacaoResponse(
                    i + 1, // número da linha
                    importada.descricao(),
                    e.getMessage()
                ));
            }
        }
        
        // 6. Marcar dados iniciais como carregados (se primeira importação)
        if (!usuario.isDadosIniciaisCarregados() && !transacoesSalvas.isEmpty()) {
            usuario.marcarDadosIniciaisCarregados();
            usuarioRepository.atualizar(usuario);
        }
        
        // 7. Registrar auditoria
        auditLogger.logImportacao(
            usuario.getId(),
            comando.nomeArquivo(),
            transacoesImportadas.size(),
            transacoesSalvas.size(),
            erros.size()
        );
        
        // 8. Retornar resultado
        return new ResultadoImportacaoResponse(
            transacoesImportadas.size(),
            transacoesSalvas.size(),
            erros.size(),
            duplicatas.stream()
                .map(d -> new DuplicataPotencialResponse(
                    d.linha(),
                    d.descricao(),
                    d.valor(),
                    d.data(),
                    d.motivoSimilaridade()
                ))
                .toList(),
            erros,
            transacoesSalvas
        );
    }
    
    private void validarTransacaoImportada(TransacaoImportada importada) {
        if (importada.data() == null) {
            throw new IllegalArgumentException("Data é obrigatória");
        }
        
        if (importada.valor() == null || 
            importada.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser positivo");
        }
        
        if (importada.descricao() == null || 
            importada.descricao().trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição é obrigatória");
        }
        
        if (importada.categoria() == null || 
            importada.categoria().trim().isEmpty()) {
            throw new IllegalArgumentException("Categoria é obrigatória");
        }
    }
    
    private Transacao criarTransacaoDeImportacao(
            UsuarioId usuarioId, 
            TransacaoImportada importada) {
        
        return Transacao.criar(
            usuarioId,
            new Valor(importada.valor(), Moeda.BRL),
            new Descricao(importada.descricao()),
            importada.categoria(),
            importada.tipo(),
            importada.data()
        );
    }
    
    private TransacaoResponse mapearParaResponse(Transacao transacao) {
        return new TransacaoResponse(
            transacao.getId().valor().toString(),
            transacao.getValor().quantia(),
            transacao.getValor().moeda().getCodigo(),
            transacao.getDescricao().valor(),
            transacao.getCategoria(),
            transacao.getTipo(),
            transacao.getData(),
            transacao.getCriadoEm()
        );
    }
}
```

### 3. Complete Budget CRUD Operations

#### 3.1 Additional Endpoints in OrcamentoController

```java
@RestController
@RequestMapping("/orcamentos")
@RequiredArgsConstructor
public class OrcamentoController {
    
    // Existing: POST /orcamentos (criar)
    // Existing: GET /orcamentos (listar)
    
    private final ObterOrcamentoPorIdUseCase obterOrcamentoPorIdUseCase;
    private final AtualizarOrcamentoUseCase atualizarOrcamentoUseCase;
    private final ExcluirOrcamentoUseCase excluirOrcamentoUseCase;
    
    /**
     * Obtém orçamento por ID.
     * 
     * GET /orcamentos/{id}
     */
    @GetMapping("/{orcamentoId}")
    public ResponseEntity<OrcamentoResponse> obterPorId(
            @PathVariable String orcamentoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UsuarioId usuarioId = new UsuarioId(UUID.fromString(userDetails.getUsername()));
        OrcamentoId id = new OrcamentoId(UUID.fromString(orcamentoId));
        
        OrcamentoResponse orcamento = obterOrcamentoPorIdUseCase.executar(usuarioId, id);
        
        return ResponseEntity.ok(orcamento);
    }
    
    /**
     * Atualiza orçamento existente.
     * 
     * PUT /orcamentos/{id}
     */
    @PutMapping("/{orcamentoId}")
    public ResponseEntity<OrcamentoResponse> atualizar(
            @PathVariable String orcamentoId,
            @Valid @RequestBody AtualizarOrcamentoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UsuarioId usuarioId = new UsuarioId(UUID.fromString(userDetails.getUsername()));
        OrcamentoId id = new OrcamentoId(UUID.fromString(orcamentoId));
        
        ComandoAtualizarOrcamento comando = new ComandoAtualizarOrcamento(
            id,
            usuarioId,
            request.categoria(),
            request.limite(),
            request.periodo()
        );
        
        OrcamentoResponse orcamento = atualizarOrcamentoUseCase.executar(comando);
        
        return ResponseEntity.ok(orcamento);
    }
    
    /**
     * Exclui orçamento (soft delete).
     * 
     * DELETE /orcamentos/{id}
     */
    @DeleteMapping("/{orcamentoId}")
    public ResponseEntity<Void> excluir(
            @PathVariable String orcamentoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UsuarioId usuarioId = new UsuarioId(UUID.fromString(userDetails.getUsername()));
        OrcamentoId id = new OrcamentoId(UUID.fromString(orcamentoId));
        
        excluirOrcamentoUseCase.executar(usuarioId, id);
        
        return ResponseEntity.noContent().build();
    }
}
```

### 4. Complete Goal CRUD Operations

#### 4.1 Additional Endpoints in MetaFinanceiraController

```java
@RestController
@RequestMapping("/metas")
@RequiredArgsConstructor
public class MetaFinanceiraController {
    
    // Existing: POST /metas (criar)
    // Existing: GET /metas (listar)
    
    private final ObterMetaPorIdUseCase obterMetaPorIdUseCase;
    private final AtualizarMetaUseCase atualizarMetaUseCase;
    private final ExcluirMetaUseCase excluirMetaUseCase;
    private final AtualizarProgressoMetaUseCase atualizarProgressoMetaUseCase;
    
    /**
     * Obtém meta por ID.
     * 
     * GET /metas/{id}
     */
    @GetMapping("/{metaId}")
    public ResponseEntity<MetaFinanceiraResponse> obterPorId(
            @PathVariable String metaId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UsuarioId usuarioId = new UsuarioId(UUID.fromString(userDetails.getUsername()));
        MetaId id = new MetaId(UUID.fromString(metaId));
        
        MetaFinanceiraResponse meta = obterMetaPorIdUseCase.executar(usuarioId, id);
        
        return ResponseEntity.ok(meta);
    }
    
    /**
     * Atualiza meta existente.
     * 
     * PUT /metas/{id}
     */
    @PutMapping("/{metaId}")
    public ResponseEntity<MetaFinanceiraResponse> atualizar(
            @PathVariable String metaId,
            @Valid @RequestBody AtualizarMetaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UsuarioId usuarioId = new UsuarioId(UUID.fromString(userDetails.getUsername()));
        MetaId id = new MetaId(UUID.fromString(metaId));
        
        ComandoAtualizarMeta comando = new ComandoAtualizarMeta(
            id,
            usuarioId,
            request.nome(),
            request.valorAlvo(),
            request.prazo(),
            request.tipo()
        );
        
        MetaFinanceiraResponse meta = atualizarMetaUseCase.executar(comando);
        
        return ResponseEntity.ok(meta);
    }
    
    /**
     * Atualiza progresso da meta.
     * 
     * PATCH /metas/{id}/progresso
     */
    @PatchMapping("/{metaId}/progresso")
    public ResponseEntity<MetaFinanceiraResponse> atualizarProgresso(
            @PathVariable String metaId,
            @Valid @RequestBody AtualizarProgressoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UsuarioId usuarioId = new UsuarioId(UUID.fromString(userDetails.getUsername()));
        MetaId id = new MetaId(UUID.fromString(metaId));
        
        ComandoAtualizarProgressoMeta comando = new ComandoAtualizarProgressoMeta(
            id,
            usuarioId,
            request.valorAdicional()
        );
        
        MetaFinanceiraResponse meta = atualizarProgressoMetaUseCase.executar(comando);
        
        return ResponseEntity.ok(meta);
    }
    
    /**
     * Exclui meta (soft delete).
     * 
     * DELETE /metas/{id}
     */
    @DeleteMapping("/{metaId}")
    public ResponseEntity<Void> excluir(
            @PathVariable String metaId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UsuarioId usuarioId = new UsuarioId(UUID.fromString(userDetails.getUsername()));
        MetaId id = new MetaId(UUID.fromString(metaId));
        
        excluirMetaUseCase.executar(usuarioId, id);
        
        return ResponseEntity.noContent().build();
    }
}
```

## Data Models

### New DTOs

```java
// Request DTOs
public record AtualizarOrcamentoRequest(
    @NotNull String categoria,
    @NotNull @DecimalMin("0.01") BigDecimal limite,
    @NotNull PeriodoOrcamento periodo
) {}

public record AtualizarMetaRequest(
    @NotBlank String nome,
    @NotNull @DecimalMin("0.01") BigDecimal valorAlvo,
    @NotNull LocalDate prazo,
    @NotNull TipoMeta tipo
) {}

public record AtualizarProgressoRequest(
    @NotNull @DecimalMin("0.01") BigDecimal valorAdicional
) {}

// Command DTOs
public record ComandoAtualizarOrcamento(
    OrcamentoId id,
    UsuarioId usuarioId,
    String categoria,
    BigDecimal limite,
    PeriodoOrcamento periodo
) {}

public record ComandoAtualizarMeta(
    MetaId id,
    UsuarioId usuarioId,
    String nome,
    BigDecimal valorAlvo,
    LocalDate prazo,
    TipoMeta tipo
) {}

public record ComandoAtualizarProgressoMeta(
    MetaId id,
    UsuarioId usuarioId,
    BigDecimal valorAdicional
) {}
```

## Error Handling

All new endpoints follow existing error handling patterns:

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(ex.getMessage(), "VALIDATION_ERROR"));
    }
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage(), "NOT_FOUND"));
    }
    
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse("Acesso negado", "FORBIDDEN"));
    }
}
```

## Testing Strategy

### Unit Tests

Each new use case requires unit tests:

```java
@ExtendWith(MockitoExtension.class)
class ObterResumoDashboardUseCaseTest {
    
    @Mock
    private TransacaoRepository transacaoRepository;
    
    @Mock
    private OrcamentoRepository orcamentoRepository;
    
    @Mock
    private MetaFinanceiraRepository metaFinanceiraRepository;
    
    @InjectMocks
    private ObterResumoDashboardUseCase useCase;
    
    @Test
    void deveRetornarResumoComDadosCompletos() {
        // Arrange
        UsuarioId usuarioId = new UsuarioId(UUID.randomUUID());
        // Mock repositories...
        
        // Act
        DashboardResponse resumo = useCase.executar(usuarioId);
        
        // Assert
        assertThat(resumo).isNotNull();
        assertThat(resumo.saldoAtual()).isNotNull();
        assertThat(resumo.receitaMensal()).isNotNull();
        assertThat(resumo.despesaMensal()).isNotNull();
    }
    
    @Test
    void deveRetornarResumoVazioQuandoUsuarioNaoTemDados() {
        // Test empty state
    }
}
```

### Integration Tests

Each new controller requires integration tests:

```java
@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser
    void deveRetornarResumoDashboard() throws Exception {
        mockMvc.perform(get("/dashboard/resumo")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.saldoAtual").exists())
            .andExpect(jsonPath("$.receitaMensal").exists())
            .andExpect(jsonPath("$.despesaMensal").exists());
    }
}
```

## Security Considerations

1. All endpoints require JWT authentication
2. All operations validate user ownership of resources
3. File uploads are limited to 10MB
4. File types are validated (only .xlsx, .xls, .csv)
5. All database operations are transactional
6. Audit logs are maintained for import operations

## Performance Considerations

1. Dashboard queries should be optimized with proper indexes
2. File processing should handle large files efficiently
3. Duplicate detection should use efficient algorithms
4. Consider caching dashboard data for frequently accessed users
5. Use pagination for large result sets

## Deployment Considerations

1. Ensure file upload limits are configured in application.yml
2. Configure multipart file size limits in Spring Boot
3. Ensure database has proper indexes for dashboard queries
4. Monitor file processing performance
5. Set up alerts for failed imports
