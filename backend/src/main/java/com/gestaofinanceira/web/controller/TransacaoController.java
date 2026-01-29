package com.gestaofinanceira.web.controller;

import com.gestaofinanceira.application.dto.command.ComandoAtualizarTransacao;
import com.gestaofinanceira.application.dto.command.ComandoCriarTransacao;
import com.gestaofinanceira.application.dto.request.AtualizarTransacaoRequest;
import com.gestaofinanceira.application.dto.request.CriarTransacaoRequest;
import com.gestaofinanceira.application.dto.response.TransacaoResponse;
import com.gestaofinanceira.application.usecases.transacao.AtualizarTransacaoUseCase;
import com.gestaofinanceira.application.usecases.transacao.CriarTransacaoUseCase;
import com.gestaofinanceira.application.usecases.transacao.ExcluirTransacaoUseCase;
import com.gestaofinanceira.application.usecases.transacao.ListarTransacoesUseCase;
import com.gestaofinanceira.domain.entities.Transacao;
import com.gestaofinanceira.domain.valueobjects.TipoTransacao;
import com.gestaofinanceira.domain.valueobjects.TransacaoId;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;
import com.gestaofinanceira.infrastructure.security.JwtAuthenticationFilter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller REST para operações de transações financeiras.
 * 
 * Fornece endpoints para:
 * - CRUD completo de transações
 * - Listagem com filtros e paginação
 * - Consultas por período, categoria e tipo
 * 
 * Requirements: 3.1, 3.2, 3.3, 3.5
 */
@RestController
@RequestMapping("/api/transacoes")
@Validated
public class TransacaoController {
    
    private final CriarTransacaoUseCase criarTransacaoUseCase;
    private final AtualizarTransacaoUseCase atualizarTransacaoUseCase;
    private final ExcluirTransacaoUseCase excluirTransacaoUseCase;
    private final ListarTransacoesUseCase listarTransacoesUseCase;
    
    public TransacaoController(CriarTransacaoUseCase criarTransacaoUseCase,
                              AtualizarTransacaoUseCase atualizarTransacaoUseCase,
                              ExcluirTransacaoUseCase excluirTransacaoUseCase,
                              ListarTransacoesUseCase listarTransacoesUseCase) {
        this.criarTransacaoUseCase = Objects.requireNonNull(criarTransacaoUseCase);
        this.atualizarTransacaoUseCase = Objects.requireNonNull(atualizarTransacaoUseCase);
        this.excluirTransacaoUseCase = Objects.requireNonNull(excluirTransacaoUseCase);
        this.listarTransacoesUseCase = Objects.requireNonNull(listarTransacoesUseCase);
    }
    
    /**
     * Cria uma nova transação financeira.
     * 
     * @param request dados da transação
     * @param authentication contexto de autenticação
     * @return transação criada
     */
    @PostMapping
    public ResponseEntity<TransacaoResponse> criarTransacao(
            @Valid @RequestBody CriarTransacaoRequest request,
            Authentication authentication) {
        
        try {
            UsuarioId usuarioId = extrairUsuarioId(authentication);
            
            ComandoCriarTransacao comando = new ComandoCriarTransacao(
                usuarioId,
                request.valor(),
                request.descricao(),
                request.categoria(),
                request.tipo(),
                request.data()
            );
            
            Transacao transacao = criarTransacaoUseCase.executar(comando);
            TransacaoResponse response = converterParaResponse(transacao);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Dados inválidos para criação de transação", e);
        } catch (IllegalStateException e) {
            throw new BusinessRuleException("Erro de regra de negócio", e);
        }
    }
    
    /**
     * Atualiza uma transação existente.
     * 
     * @param transacaoId ID da transação
     * @param request novos dados da transação
     * @param authentication contexto de autenticação
     * @return transação atualizada
     */
    @PutMapping("/{transacaoId}")
    public ResponseEntity<TransacaoResponse> atualizarTransacao(
            @PathVariable String transacaoId,
            @Valid @RequestBody AtualizarTransacaoRequest request,
            Authentication authentication) {
        
        try {
            UsuarioId usuarioId = extrairUsuarioId(authentication);
            TransacaoId id = new TransacaoId(UUID.fromString(transacaoId));
            
            ComandoAtualizarTransacao comando = new ComandoAtualizarTransacao(
                id,
                usuarioId,
                request.valor(),
                request.descricao(),
                request.categoria(),
                request.tipo(),
                request.data()
            );
            
            Transacao transacao = atualizarTransacaoUseCase.executar(comando);
            TransacaoResponse response = converterParaResponse(transacao);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Dados inválidos para atualização de transação", e);
        } catch (IllegalStateException e) {
            throw new BusinessRuleException("Erro de regra de negócio", e);
        }
    }
    
    /**
     * Exclui uma transação (soft delete).
     * 
     * @param transacaoId ID da transação
     * @param authentication contexto de autenticação
     * @return confirmação de exclusão
     */
    @DeleteMapping("/{transacaoId}")
    public ResponseEntity<Void> excluirTransacao(
            @PathVariable String transacaoId,
            Authentication authentication) {
        
        try {
            UsuarioId usuarioId = extrairUsuarioId(authentication);
            TransacaoId id = new TransacaoId(UUID.fromString(transacaoId));
            
            excluirTransacaoUseCase.executar(id, usuarioId);
            
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            throw new ValidationException("ID de transação inválido", e);
        } catch (IllegalStateException e) {
            throw new BusinessRuleException("Erro ao excluir transação", e);
        }
    }
    
    /**
     * Lista transações do usuário com filtros opcionais.
     * 
     * @param dataInicio data de início do período (opcional)
     * @param dataFim data de fim do período (opcional)
     * @param categoria categoria das transações (opcional)
     * @param tipo tipo de transação (opcional)
     * @param ordenacao campo para ordenação (data, valor, descricao, categoria, tipo)
     * @param direcao direção da ordenação (asc, desc)
     * @param page número da página (padrão: 0)
     * @param size tamanho da página (padrão: 20, máximo: 100)
     * @param authentication contexto de autenticação
     * @return página de transações
     */
    @GetMapping
    public ResponseEntity<Page<TransacaoResponse>> listarTransacoes(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            
            @RequestParam(required = false) String categoria,
            
            @RequestParam(required = false) TipoTransacao tipo,
            
            @RequestParam(required = false, defaultValue = "data") String ordenacao,
            
            @RequestParam(required = false, defaultValue = "desc") String direcao,
            
            @RequestParam(defaultValue = "0") @Min(0) int page,
            
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            
            Authentication authentication) {
        
        try {
            UsuarioId usuarioId = extrairUsuarioId(authentication);
            
            List<TransacaoResponse> transacoes;
            
            // Aplicar filtros baseados nos parâmetros fornecidos
            if (dataInicio != null && dataFim != null) {
                if (categoria != null && !categoria.trim().isEmpty()) {
                    // Filtro por período e categoria
                    transacoes = listarTransacoesUseCase.listarPorCategoriaEPeriodo(
                        usuarioId, categoria.trim(), dataInicio, dataFim);
                } else if (tipo != null) {
                    // Filtro por período e tipo
                    transacoes = listarTransacoesUseCase.listarPorTipo(
                        usuarioId, tipo, dataInicio, dataFim);
                } else {
                    // Filtro apenas por período
                    transacoes = listarTransacoesUseCase.listarPorPeriodo(
                        usuarioId, dataInicio, dataFim);
                }
            } else if (categoria != null && !categoria.trim().isEmpty()) {
                // Filtro apenas por categoria
                transacoes = listarTransacoesUseCase.listarPorCategoria(usuarioId, categoria.trim());
            } else {
                // Sem filtros específicos - buscar últimos 3 meses
                LocalDate hoje = LocalDate.now();
                LocalDate tresMesesAtras = hoje.minusMonths(3);
                transacoes = listarTransacoesUseCase.listarPorPeriodo(usuarioId, tresMesesAtras, hoje);
            }
            
            // Aplicar ordenação customizada
            transacoes = aplicarOrdenacao(transacoes, ordenacao, direcao);
            
            // Aplicar paginação manual (idealmente seria feita no repositório)
            Pageable pageable = PageRequest.of(page, size);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), transacoes.size());
            
            List<TransacaoResponse> paginatedList = start >= transacoes.size() ? 
                List.of() : transacoes.subList(start, end);
            
            Page<TransacaoResponse> pageResult = new PageImpl<>(
                paginatedList, pageable, transacoes.size());
            
            return ResponseEntity.ok(pageResult);
            
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Parâmetros de consulta inválidos", e);
        } catch (IllegalStateException e) {
            throw new BusinessRuleException("Erro ao listar transações", e);
        }
    }
    
    /**
     * Lista as transações mais recentes do usuário.
     * 
     * @param limite número máximo de transações (padrão: 10, máximo: 50)
     * @param authentication contexto de autenticação
     * @return lista de transações recentes
     */
    @GetMapping("/recentes")
    public ResponseEntity<List<TransacaoResponse>> listarTransacoesRecentes(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limite,
            Authentication authentication) {
        
        try {
            UsuarioId usuarioId = extrairUsuarioId(authentication);
            
            List<TransacaoResponse> transacoes = listarTransacoesUseCase.listarRecentes(usuarioId, limite);
            
            return ResponseEntity.ok(transacoes);
            
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Parâmetros inválidos", e);
        } catch (IllegalStateException e) {
            throw new BusinessRuleException("Erro ao listar transações recentes", e);
        }
    }
    
    /**
     * Reativa uma transação previamente excluída.
     * 
     * @param transacaoId ID da transação
     * @param authentication contexto de autenticação
     * @return transação reativada
     */
    @PatchMapping("/{transacaoId}/reativar")
    public ResponseEntity<TransacaoResponse> reativarTransacao(
            @PathVariable String transacaoId,
            Authentication authentication) {
        
        try {
            UsuarioId usuarioId = extrairUsuarioId(authentication);
            TransacaoId id = new TransacaoId(UUID.fromString(transacaoId));
            
            Transacao transacao = excluirTransacaoUseCase.reativar(id, usuarioId);
            TransacaoResponse response = converterParaResponse(transacao);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            throw new ValidationException("ID de transação inválido", e);
        } catch (IllegalStateException e) {
            throw new BusinessRuleException("Erro ao reativar transação", e);
        }
    }
    
    /**
     * Extrai o ID do usuário do contexto de autenticação.
     */
    private UsuarioId extrairUsuarioId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("Usuário não autenticado");
        }
        
        if (!(authentication.getPrincipal() instanceof JwtAuthenticationFilter.JwtUserPrincipal)) {
            throw new IllegalStateException("Tipo de principal inválido");
        }
        
        JwtAuthenticationFilter.JwtUserPrincipal principal = 
            (JwtAuthenticationFilter.JwtUserPrincipal) authentication.getPrincipal();
        
        return principal.getUsuarioId();
    }
    
    /**
     * Aplica ordenação customizada à lista de transações.
     * 
     * @param transacoes lista de transações
     * @param ordenacao campo para ordenação (data, valor, descricao, categoria, tipo)
     * @param direcao direção da ordenação (asc, desc)
     * @return lista ordenada
     */
    private List<TransacaoResponse> aplicarOrdenacao(List<TransacaoResponse> transacoes, 
                                                     String ordenacao, 
                                                     String direcao) {
        if (transacoes == null || transacoes.isEmpty()) {
            return transacoes;
        }
        
        boolean ascending = "asc".equalsIgnoreCase(direcao);
        
        return transacoes.stream()
            .sorted((t1, t2) -> {
                int comparison = 0;
                
                switch (ordenacao.toLowerCase()) {
                    case "data":
                        comparison = t1.data().compareTo(t2.data());
                        break;
                    case "valor":
                        comparison = t1.valor().compareTo(t2.valor());
                        break;
                    case "descricao":
                        comparison = t1.descricao().compareToIgnoreCase(t2.descricao());
                        break;
                    case "categoria":
                        comparison = t1.categoria().compareToIgnoreCase(t2.categoria());
                        break;
                    case "tipo":
                        comparison = t1.tipo().compareTo(t2.tipo());
                        break;
                    default:
                        // Default: ordenar por data
                        comparison = t1.data().compareTo(t2.data());
                }
                
                return ascending ? comparison : -comparison;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Converte uma entidade Transacao para TransacaoResponse.
     */
    private TransacaoResponse converterParaResponse(Transacao transacao) {
        return new TransacaoResponse(
            transacao.getId().valor().toString(),
            transacao.getValor().quantia(),
            transacao.getValor().moeda().name(),
            transacao.getDescricao().valor(),
            transacao.getCategoria().nome(),
            transacao.getTipo(),
            transacao.getData(),
            transacao.getCriadoEm(),
            transacao.isAtiva()
        );
    }
    
    // Exception classes para tratamento específico de erros
    
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static class BusinessRuleException extends RuntimeException {
        public BusinessRuleException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}