# Exemplos de Implementação

Este documento fornece exemplos práticos e templates de código para facilitar a implementação das funcionalidades pendentes.

## 📋 Índice

1. [Dashboard Controller - Exemplo Completo](#dashboard-controller)
2. [Import Controller - Exemplo Completo](#import-controller)
3. [Budget CRUD - Templates](#budget-crud)
4. [Goal CRUD - Templates](#goal-crud)
5. [Testes - Templates](#testes)

---

## Dashboard Controller

### 1. Use Case - ObterResumoDashboardUseCase.java

```java
package com.gestaofinanceira.application.usecases.dashboard;

import com.gestaofinanceira.application.dto.response.*;
import com.gestaofinanceira.application.ports.repository.*;
import com.gestaofinanceira.domain.entities.*;
import com.gestaofinanceira.domain.valueobjects.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class ObterResumoDashboardUseCase {
    
    private final TransacaoRepository transacaoRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final MetaFinanceiraRepository metaFinanceiraRepository;
    
    public ObterResumoDashboardUseCase(
            TransacaoRepository transacaoRepository,
            OrcamentoRepository orcamentoRepository,
            MetaFinanceiraRepository metaFinanceiraRepository) {
        this.transacaoRepository = Objects.requireNonNull(transacaoRepository);
        this.orcamentoRepository = Objects.requireNonNull(orcamentoRepository);
        this.metaFinanceiraRepository = Objects.requireNonNull(metaFinanceiraRepository);
    }
    
    public DashboardResponse executar(UsuarioId usuarioId) {
        Objects.requireNonNull(usuarioId, "UsuarioId não pode ser nulo");
        
        // Calcular métricas financeiras
        BigDecimal saldoAtual = calcularSaldoAtual(usuarioId);
        BigDecimal receitaMensal = calcularReceitaMensal(usuarioId);
        BigDecimal despesaMensal = calcularDespesaMensal(usuarioId);
        
        // Obter status de orçamentos
        List<OrcamentoStatusResponse> statusOrcamentos = obterStatusOrcamentos(usuarioId);
        
        // Obter progresso de metas
        List<MetaProgressoResponse> progressoMetas = obterProgressoMetas(usuarioId);
        
        // Obter transações recentes
        List<TransacaoResponse> transacoesRecentes = obterTransacoesRecentes(usuarioId);
        
        // Resumo de investimentos (null por enquanto)
        ResumoInvestimentosResponse resumoInvestimentos = null;
        
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
        LocalDate inicio = LocalDate.of(2000, 1, 1);
        LocalDate fim = LocalDate.now();
        
        List<Transacao> transacoes = transacaoRepository
            .buscarPorUsuarioEPeriodo(usuarioId, inicio, fim);
        
        return transacoes.stream()
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
        List<Orcamento> orcamentos = orcamentoRepository.buscarOrcamentosAtivos(usuarioId);
        
        return orcamentos.stream()
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
        List<MetaFinanceira> metas = metaFinanceiraRepository.buscarMetasAtivas(usuarioId);
        
        return metas.stream()
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
}
```

### 2. Controller - DashboardController.java

```java
package com.gestaofinanceira.web.controller;

import com.gestaofinanceira.application.dto.response.DashboardResponse;
import com.gestaofinanceira.application.usecases.dashboard.ObterResumoDashboardUseCase;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

/**
 * Controller REST para operações de dashboard.
 * 
 * Endpoints:
 * - GET /dashboard/resumo - Obtém resumo consolidado do dashboard
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    
    private final ObterResumoDashboardUseCase obterResumoDashboardUseCase;
    
    public DashboardController(ObterResumoDashboardUseCase obterResumoDashboardUseCase) {
        this.obterResumoDashboardUseCase = Objects.requireNonNull(obterResumoDashboardUseCase);
    }
    
    /**
     * Obtém resumo consolidado do dashboard do usuário autenticado.
     * 
     * @param userDetails dados do usuário autenticado
     * @return resumo do dashboard com métricas financeiras
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

### 3. Teste Unitário - ObterResumoDashboardUseCaseTest.java

```java
package com.gestaofinanceira.application.usecases.dashboard;

import com.gestaofinanceira.application.dto.response.DashboardResponse;
import com.gestaofinanceira.application.ports.repository.*;
import com.gestaofinanceira.domain.entities.*;
import com.gestaofinanceira.domain.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ObterResumoDashboardUseCase")
class ObterResumoDashboardUseCaseTest {
    
    @Mock
    private TransacaoRepository transacaoRepository;
    
    @Mock
    private OrcamentoRepository orcamentoRepository;
    
    @Mock
    private MetaFinanceiraRepository metaFinanceiraRepository;
    
    private ObterResumoDashboardUseCase useCase;
    
    @BeforeEach
    void setUp() {
        useCase = new ObterResumoDashboardUseCase(
            transacaoRepository,
            orcamentoRepository,
            metaFinanceiraRepository
        );
    }
    
    @Test
    @DisplayName("Deve retornar resumo com dados completos")
    void deveRetornarResumoComDadosCompletos() {
        // Arrange
        UsuarioId usuarioId = new UsuarioId(UUID.randomUUID());
        
        // Mock transações
        Transacao receita = criarTransacao(usuarioId, TipoTransacao.RECEITA, 1000);
        Transacao despesa = criarTransacao(usuarioId, TipoTransacao.DESPESA, 500);
        when(transacaoRepository.buscarPorUsuarioEPeriodo(any(), any(), any()))
            .thenReturn(List.of(receita, despesa));
        when(transacaoRepository.buscarTransacoesRecentes(any(), any()))
            .thenReturn(List.of(receita, despesa));
        
        // Mock orçamentos
        when(orcamentoRepository.buscarOrcamentosAtivos(any()))
            .thenReturn(List.of());
        
        // Mock metas
        when(metaFinanceiraRepository.buscarMetasAtivas(any()))
            .thenReturn(List.of());
        
        // Act
        DashboardResponse resumo = useCase.executar(usuarioId);
        
        // Assert
        assertThat(resumo).isNotNull();
        assertThat(resumo.saldoAtual()).isEqualTo(new BigDecimal("500.00"));
        assertThat(resumo.receitaMensal()).isNotNull();
        assertThat(resumo.despesaMensal()).isNotNull();
        assertThat(resumo.statusOrcamentos()).isNotNull();
        assertThat(resumo.progressoMetas()).isNotNull();
        assertThat(resumo.transacoesRecentes()).hasSize(2);
    }
    
    @Test
    @DisplayName("Deve retornar resumo vazio quando usuário não tem dados")
    void deveRetornarResumoVazioQuandoUsuarioNaoTemDados() {
        // Arrange
        UsuarioId usuarioId = new UsuarioId(UUID.randomUUID());
        
        when(transacaoRepository.buscarPorUsuarioEPeriodo(any(), any(), any()))
            .thenReturn(List.of());
        when(transacaoRepository.buscarTransacoesRecentes(any(), any()))
            .thenReturn(List.of());
        when(orcamentoRepository.buscarOrcamentosAtivos(any()))
            .thenReturn(List.of());
        when(metaFinanceiraRepository.buscarMetasAtivas(any()))
            .thenReturn(List.of());
        
        // Act
        DashboardResponse resumo = useCase.executar(usuarioId);
        
        // Assert
        assertThat(resumo).isNotNull();
        assertThat(resumo.saldoAtual()).isEqualTo(BigDecimal.ZERO);
        assertThat(resumo.statusOrcamentos()).isEmpty();
        assertThat(resumo.progressoMetas()).isEmpty();
        assertThat(resumo.transacoesRecentes()).isEmpty();
    }
    
    private Transacao criarTransacao(UsuarioId usuarioId, TipoTransacao tipo, double valor) {
        return Transacao.criar(
            usuarioId,
            new Valor(BigDecimal.valueOf(valor), Moeda.BRL),
            new Descricao("Teste"),
            "ALIMENTACAO",
            tipo,
            LocalDate.now()
        );
    }
}
```

---

## Import Controller

### 1. Controller - ImportController.java

```java
package com.gestaofinanceira.web.controller;

import com.gestaofinanceira.application.dto.command.ComandoImportarPlanilha;
import com.gestaofinanceira.application.dto.response.ResultadoImportacaoResponse;
import com.gestaofinanceira.application.usecases.importacao.ImportarPlanilhaUseCase;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/**
 * Controller REST para operações de importação de planilhas.
 * 
 * Endpoints:
 * - POST /importacao/upload - Faz upload e processa planilha
 */
@RestController
@RequestMapping("/importacao")
public class ImportController {
    
    private final ImportarPlanilhaUseCase importarPlanilhaUseCase;
    
    // Tamanho máximo de arquivo: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    public ImportController(ImportarPlanilhaUseCase importarPlanilhaUseCase) {
        this.importarPlanilhaUseCase = Objects.requireNonNull(importarPlanilhaUseCase);
    }
    
    /**
     * Faz upload e processa planilha de transações.
     * 
     * @param file arquivo Excel (.xlsx, .xls) ou CSV
     * @param userDetails dados do usuário autenticado
     * @return resultado da importação com estatísticas
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
        
        // Validar tamanho
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("Arquivo muito grande. Tamanho máximo: %d MB", 
                    MAX_FILE_SIZE / (1024 * 1024)));
        }
        
        // Validar extensão
        String filename = file.getOriginalFilename();
        if (filename == null || 
            (!filename.toLowerCase().endsWith(".xlsx") && 
             !filename.toLowerCase().endsWith(".xls") && 
             !filename.toLowerCase().endsWith(".csv"))) {
            throw new IllegalArgumentException(
                "Formato de arquivo inválido. Use .xlsx, .xls ou .csv");
        }
    }
}
```

### 2. Configuração - application.yml

```yaml
spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 10MB
      file-size-threshold: 2KB
```

---

## Budget CRUD

### Template: ObterOrcamentoPorIdUseCase.java

```java
package com.gestaofinanceira.application.usecases.orcamento;

import com.gestaofinanceira.application.dto.response.OrcamentoResponse;
import com.gestaofinanceira.application.ports.repository.OrcamentoRepository;
import com.gestaofinanceira.domain.entities.Orcamento;
import com.gestaofinanceira.domain.valueobjects.OrcamentoId;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class ObterOrcamentoPorIdUseCase {
    
    private final OrcamentoRepository orcamentoRepository;
    
    public ObterOrcamentoPorIdUseCase(OrcamentoRepository orcamentoRepository) {
        this.orcamentoRepository = Objects.requireNonNull(orcamentoRepository);
    }
    
    public OrcamentoResponse executar(UsuarioId usuarioId, OrcamentoId orcamentoId) {
        Objects.requireNonNull(usuarioId, "UsuarioId não pode ser nulo");
        Objects.requireNonNull(orcamentoId, "OrcamentoId não pode ser nulo");
        
        // Buscar orçamento
        Orcamento orcamento = orcamentoRepository.buscarPorId(orcamentoId)
            .orElseThrow(() -> new IllegalArgumentException("Orçamento não encontrado"));
        
        // Validar propriedade
        if (!orcamento.getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException("Orçamento não pertence ao usuário");
        }
        
        // Mapear para response
        return new OrcamentoResponse(
            orcamento.getId().valor().toString(),
            orcamento.getCategoria(),
            orcamento.getLimite().quantia(),
            orcamento.getPeriodo(),
            orcamento.getGastoAtual().quantia(),
            orcamento.getStatus(),
            orcamento.getInicioPeriodo(),
            orcamento.getFimPeriodo(),
            orcamento.getCriadoEm()
        );
    }
}
```

---

## Goal CRUD

### Template: AtualizarProgressoMetaUseCase.java

```java
package com.gestaofinanceira.application.usecases.meta;

import com.gestaofinanceira.application.dto.command.ComandoAtualizarProgressoMeta;
import com.gestaofinanceira.application.dto.response.MetaFinanceiraResponse;
import com.gestaofinanceira.application.ports.repository.MetaFinanceiraRepository;
import com.gestaofinanceira.domain.entities.MetaFinanceira;
import com.gestaofinanceira.domain.valueobjects.Valor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class AtualizarProgressoMetaUseCase {
    
    private final MetaFinanceiraRepository metaFinanceiraRepository;
    
    public AtualizarProgressoMetaUseCase(MetaFinanceiraRepository metaFinanceiraRepository) {
        this.metaFinanceiraRepository = Objects.requireNonNull(metaFinanceiraRepository);
    }
    
    public MetaFinanceiraResponse executar(ComandoAtualizarProgressoMeta comando) {
        Objects.requireNonNull(comando, "Comando não pode ser nulo");
        
        // Buscar meta
        MetaFinanceira meta = metaFinanceiraRepository.buscarPorId(comando.metaId())
            .orElseThrow(() -> new IllegalArgumentException("Meta não encontrada"));
        
        // Validar propriedade
        if (!meta.getUsuarioId().equals(comando.usuarioId())) {
            throw new IllegalArgumentException("Meta não pertence ao usuário");
        }
        
        // Adicionar progresso
        Valor valorAdicional = new Valor(comando.valorAdicional(), meta.getValorAlvo().moeda());
        meta.adicionarProgresso(valorAdicional);
        
        // Salvar
        MetaFinanceira metaAtualizada = metaFinanceiraRepository.atualizar(meta);
        
        // Mapear para response
        return new MetaFinanceiraResponse(
            metaAtualizada.getId().valor().toString(),
            metaAtualizada.getNome().valor(),
            metaAtualizada.getValorAlvo().quantia(),
            metaAtualizada.getValorAtual().quantia(),
            metaAtualizada.calcularPercentualConclusao(),
            metaAtualizada.getPrazo(),
            metaAtualizada.getTipo(),
            metaAtualizada.getStatus(),
            metaAtualizada.getCriadoEm()
        );
    }
}
```

---

## Testes

### Template: Integration Test

```java
package com.gestaofinanceira.web.controller;

import com.gestaofinanceira.application.dto.request.CriarOrcamentoRequest;
import com.gestaofinanceira.domain.valueobjects.PeriodoOrcamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("DashboardController Integration Test")
class DashboardControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    @DisplayName("Deve retornar resumo do dashboard")
    void deveRetornarResumoDashboard() throws Exception {
        mockMvc.perform(get("/dashboard/resumo"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.saldoAtual").exists())
            .andExpect(jsonPath("$.receitaMensal").exists())
            .andExpect(jsonPath("$.despesaMensal").exists())
            .andExpect(jsonPath("$.statusOrcamentos").isArray())
            .andExpect(jsonPath("$.progressoMetas").isArray())
            .andExpect(jsonPath("$.transacoesRecentes").isArray());
    }
    
    @Test
    @DisplayName("Deve retornar 401 quando não autenticado")
    void deveRetornar401QuandoNaoAutenticado() throws Exception {
        mockMvc.perform(get("/dashboard/resumo"))
            .andExpect(status().isUnauthorized());
    }
}
```

---

## 🎯 Checklist de Implementação

Para cada funcionalidade, siga este checklist:

### ✅ Use Case
- [ ] Criar classe do use case no pacote correto
- [ ] Implementar lógica de negócio
- [ ] Adicionar validações necessárias
- [ ] Injetar dependências via construtor
- [ ] Adicionar anotações (@Service, @Transactional)
- [ ] Documentar com JavaDoc

### ✅ Controller
- [ ] Criar ou atualizar controller
- [ ] Adicionar endpoint com anotações corretas
- [ ] Implementar autenticação (@AuthenticationPrincipal)
- [ ] Adicionar validações de entrada
- [ ] Implementar tratamento de erros
- [ ] Documentar com JavaDoc

### ✅ DTOs
- [ ] Criar request DTOs (se necessário)
- [ ] Criar command DTOs
- [ ] Adicionar validações (@NotNull, @Valid, etc.)
- [ ] Usar records para imutabilidade

### ✅ Testes Unitários
- [ ] Criar classe de teste
- [ ] Testar caso de sucesso
- [ ] Testar casos de erro
- [ ] Testar validações
- [ ] Usar mocks para dependências
- [ ] Atingir >80% de cobertura

### ✅ Testes de Integração
- [ ] Criar classe de teste de integração
- [ ] Testar endpoint com autenticação
- [ ] Testar endpoint sem autenticação (401)
- [ ] Testar com dados válidos
- [ ] Testar com dados inválidos
- [ ] Validar estrutura do JSON

### ✅ Validação Manual
- [ ] Testar via Postman/curl
- [ ] Testar via frontend
- [ ] Verificar logs
- [ ] Verificar banco de dados
- [ ] Confirmar que não há erros 500

---

**Dica**: Copie e adapte estes templates para acelerar a implementação!
