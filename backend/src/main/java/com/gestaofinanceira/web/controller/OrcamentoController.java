package com.gestaofinanceira.web.controller;

import com.gestaofinanceira.application.dto.command.ComandoCriarOrcamento;
import com.gestaofinanceira.application.dto.request.CriarOrcamentoRequest;
import com.gestaofinanceira.application.dto.response.OrcamentoResponse;
import com.gestaofinanceira.application.usecases.orcamento.CriarOrcamentoUseCase;
import com.gestaofinanceira.application.usecases.orcamento.ListarOrcamentosUseCase;
import com.gestaofinanceira.domain.entities.Orcamento;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;
import com.gestaofinanceira.infrastructure.security.JwtAuthenticationFilter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Controller REST para operações de orçamentos.
 * 
 * Fornece endpoints para:
 * - Criação de orçamentos
 * - Listagem de orçamentos com filtros
 * - Monitoramento de progresso e status
 * 
 * Requirements: 5.1, 5.2
 */
@RestController
@RequestMapping("/api/orcamentos")
@Validated
public class OrcamentoController {
    
    private final CriarOrcamentoUseCase criarOrcamentoUseCase;
    private final ListarOrcamentosUseCase listarOrcamentosUseCase;
    
    public OrcamentoController(CriarOrcamentoUseCase criarOrcamentoUseCase,
                              ListarOrcamentosUseCase listarOrcamentosUseCase) {
        this.criarOrcamentoUseCase = Objects.requireNonNull(criarOrcamentoUseCase);
        this.listarOrcamentosUseCase = Objects.requireNonNull(listarOrcamentosUseCase);
    }
    
    /**
     * Cria um novo orçamento.
     * 
     * @param request dados do orçamento
     * @param authentication contexto de autenticação
     * @return orçamento criado
     */
    @PostMapping
    public ResponseEntity<OrcamentoResponse> criarOrcamento(
            @Valid @RequestBody CriarOrcamentoRequest request,
            Authentication authentication) {
        
        try {
            UsuarioId usuarioId = extrairUsuarioId(authentication);
            
            ComandoCriarOrcamento comando = new ComandoCriarOrcamento(
                usuarioId,
                request.categoria(),
                request.limite(),
                request.periodo(),
                request.inicioVigencia()
            );
            
            Orcamento orcamento = criarOrcamentoUseCase.executar(comando);
            OrcamentoResponse response = converterParaResponse(orcamento);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Dados inválidos para criação de orçamento", e);
        } catch (IllegalStateException e) {
            throw new BusinessRuleException("Erro de regra de negócio", e);
        }
    }
    
    /**
     * Lista orçamentos do usuário.
     * 
     * @param apenasAtivos se deve listar apenas orçamentos ativos (padrão: true)
     * @param authentication contexto de autenticação
     * @return lista de orçamentos
     */
    @GetMapping
    public ResponseEntity<List<OrcamentoResponse>> listarOrcamentos(
            @RequestParam(defaultValue = "true") boolean apenasAtivos,
            Authentication authentication) {
        
        try {
            UsuarioId usuarioId = extrairUsuarioId(authentication);
            
            List<OrcamentoResponse> orcamentos = apenasAtivos 
                ? listarOrcamentosUseCase.listarAtivos(usuarioId)
                : listarOrcamentosUseCase.listarTodos(usuarioId);
            
            return ResponseEntity.ok(orcamentos);
            
        } catch (IllegalStateException e) {
            throw new BusinessRuleException("Erro ao listar orçamentos", e);
        }
    }
    
    /**
     * Busca orçamento por categoria.
     * 
     * @param categoria categoria do orçamento
     * @param authentication contexto de autenticação
     * @return orçamento da categoria ou 404 se não existir
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<OrcamentoResponse> buscarPorCategoria(
            @PathVariable String categoria,
            Authentication authentication) {
        
        try {
            UsuarioId usuarioId = extrairUsuarioId(authentication);
            
            OrcamentoResponse orcamento = listarOrcamentosUseCase.buscarPorCategoria(usuarioId, categoria);
            
            if (orcamento == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(orcamento);
            
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Categoria inválida", e);
        } catch (IllegalStateException e) {
            throw new BusinessRuleException("Erro ao buscar orçamento", e);
        }
    }
    
    /**
     * Obtém resumo de status dos orçamentos do usuário.
     * 
     * @param authentication contexto de autenticação
     * @return resumo com estatísticas dos orçamentos
     */
    @GetMapping("/resumo")
    public ResponseEntity<ResumoOrcamentosResponse> obterResumo(Authentication authentication) {
        
        try {
            UsuarioId usuarioId = extrairUsuarioId(authentication);
            
            List<OrcamentoResponse> orcamentos = listarOrcamentosUseCase.listarAtivos(usuarioId);
            
            ResumoOrcamentosResponse resumo = calcularResumo(orcamentos);
            
            return ResponseEntity.ok(resumo);
            
        } catch (IllegalStateException e) {
            throw new BusinessRuleException("Erro ao calcular resumo", e);
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
     * Converte uma entidade Orcamento para OrcamentoResponse.
     */
    private OrcamentoResponse converterParaResponse(Orcamento orcamento) {
        BigDecimal percentualUtilizado = calcularPercentualUtilizado(
            orcamento.getGastoAtual().quantia(),
            orcamento.getLimite().quantia()
        );
        
        return new OrcamentoResponse(
            orcamento.getId().valor().toString(),
            orcamento.getCategoria().nome(),
            orcamento.getLimite().quantia(),
            orcamento.getPeriodo(),
            orcamento.getGastoAtual().quantia(),
            orcamento.getStatus(),
            orcamento.getInicioPeriodo(),
            orcamento.getFimPeriodo(),
            orcamento.getCriadoEm(),
            percentualUtilizado
        );
    }
    
    /**
     * Calcula o percentual utilizado do orçamento.
     */
    private BigDecimal calcularPercentualUtilizado(BigDecimal gastoAtual, BigDecimal limite) {
        if (limite.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return gastoAtual
            .divide(limite, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calcula resumo estatístico dos orçamentos.
     */
    private ResumoOrcamentosResponse calcularResumo(List<OrcamentoResponse> orcamentos) {
        int totalOrcamentos = orcamentos.size();
        int orcamentosExcedidos = 0;
        int orcamentosProximosLimite = 0;
        BigDecimal totalLimites = BigDecimal.ZERO;
        BigDecimal totalGastos = BigDecimal.ZERO;
        
        for (OrcamentoResponse orcamento : orcamentos) {
            totalLimites = totalLimites.add(orcamento.limite());
            totalGastos = totalGastos.add(orcamento.gastoAtual());
            
            if (orcamento.percentualUtilizado().compareTo(BigDecimal.valueOf(100)) >= 0) {
                orcamentosExcedidos++;
            } else if (orcamento.percentualUtilizado().compareTo(BigDecimal.valueOf(80)) >= 0) {
                orcamentosProximosLimite++;
            }
        }
        
        BigDecimal percentualGeralUtilizado = totalLimites.compareTo(BigDecimal.ZERO) == 0 
            ? BigDecimal.ZERO 
            : totalGastos.divide(totalLimites, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        
        return new ResumoOrcamentosResponse(
            totalOrcamentos,
            orcamentosExcedidos,
            orcamentosProximosLimite,
            totalLimites,
            totalGastos,
            percentualGeralUtilizado
        );
    }
    
    // DTOs e Exception classes
    
    public record ResumoOrcamentosResponse(
        int totalOrcamentos,
        int orcamentosExcedidos,
        int orcamentosProximosLimite,
        BigDecimal totalLimites,
        BigDecimal totalGastos,
        BigDecimal percentualGeralUtilizado
    ) {}
    
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