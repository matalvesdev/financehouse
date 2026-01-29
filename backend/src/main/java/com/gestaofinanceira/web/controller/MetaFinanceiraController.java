package com.gestaofinanceira.web.controller;

import com.gestaofinanceira.application.dto.command.ComandoCriarMetaFinanceira;
import com.gestaofinanceira.application.dto.request.CriarMetaFinanceiraRequest;
import com.gestaofinanceira.application.dto.response.MetaFinanceiraResponse;
import com.gestaofinanceira.application.usecases.meta.CriarMetaFinanceiraUseCase;
import com.gestaofinanceira.application.usecases.meta.ListarMetasFinanceirasUseCase;
import com.gestaofinanceira.domain.entities.MetaFinanceira;
import com.gestaofinanceira.domain.valueobjects.TipoMeta;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Controller REST para operações de metas financeiras.
 * 
 * Fornece endpoints para:
 * - Criação de metas financeiras
 * - Listagem de metas com filtros
 * - Monitoramento de progresso
 * 
 * Requirements: 6.1, 6.2
 */
@RestController
@RequestMapping("/api/metas")
@Validated
public class MetaFinanceiraController {
    
    private final CriarMetaFinanceiraUseCase criarMetaFinanceiraUseCase;
    private final ListarMetasFinanceirasUseCase listarMetasFinanceirasUseCase;
    
    public MetaFinanceiraController(CriarMetaFinanceiraUseCase criarMetaFinanceiraUseCase,
                                   ListarMetasFinanceirasUseCase listarMetasFinanceirasUseCase) {
        this.criarMetaFinanceiraUseCase = Objects.requireNonNull(criarMetaFinanceiraUseCase);
        this.listarMetasFinanceirasUseCase = Objects.requireNonNull(listarMetasFinanceirasUseCase);
    }
    
    /**
     * Cria uma nova meta financeira.
     * 
     * @param request dados da meta
     * @param authentication contexto de autenticação
     * @return meta criada
     */
    @PostMapping
    public ResponseEntity<MetaFinanceiraResponse> criarMeta(
            @Valid @RequestBody CriarMetaFinanceiraRequest request,
            Authentication authentication) {
        
        try {
            UsuarioId usuarioId = extrairUsuarioId(authentication);
            
            ComandoCriarMetaFinanceira comando = new ComandoCriarMetaFinanceira(
                usuarioId,
                request.nome(),
                request.valorAlvo(),
                request.prazo(),
                request.tipo()
            );
            
            MetaFinanceira meta = criarMetaFinanceiraUseCase.executar(comando);
            MetaFinanceiraResponse response = converterParaResponse(meta);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Dados inválidos para criação de meta", e);
        } catch (IllegalStateException e) {
            throw new BusinessRuleException("Erro de regra de negócio", e);
        }
    }
    
    /**
     * Lista metas do usuário.
     * 
     * @param apenasAtivas se deve listar apenas metas ativas (padrão: true)
     * @param tipo tipo específico de meta (opcional)
     * @param authentication contexto de autenticação
     * @return lista de metas
     */
    @GetMapping
    public ResponseEntity<List<MetaFinanceiraResponse>> listarMetas(
            @RequestParam(defaultValue = "true") boolean apenasAtivas,
            @RequestParam(required = false) TipoMeta tipo,
            Authentication authentication) {
        
        try {
            UsuarioId usuarioId = extrairUsuarioId(authentication);
            
            List<MetaFinanceiraResponse> metas;
            
            if (tipo != null) {
                // Filtrar por tipo
                metas = listarMetasFinanceirasUseCase.listarPorTipo(usuarioId, tipo);
            } else if (apenasAtivas) {
                // Apenas metas ativas
                metas = listarMetasFinanceirasUseCase.listarAtivas(usuarioId);
            } else {
                // Todas as metas
                metas = listarMetasFinanceirasUseCase.listarTodas(usuarioId);
            }
            
            return ResponseEntity.ok(metas);
            
        } catch (IllegalStateException e) {
            throw new BusinessRuleException("Erro ao listar metas", e);
        }
    }
    
    /**
     * Obtém resumo de progresso das metas do usuário.
     * 
     * @param authentication contexto de autenticação
     * @return resumo com estatísticas das metas
     */
    @GetMapping("/resumo")
    public ResponseEntity<ResumoMetasResponse> obterResumo(Authentication authentication) {
        
        try {
            UsuarioId usuarioId = extrairUsuarioId(authentication);
            
            List<MetaFinanceiraResponse> metas = listarMetasFinanceirasUseCase.listarAtivas(usuarioId);
            
            ResumoMetasResponse resumo = calcularResumo(metas);
            
            return ResponseEntity.ok(resumo);
            
        } catch (IllegalStateException e) {
            throw new BusinessRuleException("Erro ao calcular resumo", e);
        }
    }
    
    /**
     * Lista metas próximas do vencimento.
     * 
     * @param dias número de dias para considerar "próximo" (padrão: 30)
     * @param authentication contexto de autenticação
     * @return lista de metas próximas do vencimento
     */
    @GetMapping("/proximas-vencimento")
    public ResponseEntity<List<MetaFinanceiraResponse>> listarProximasVencimento(
            @RequestParam(defaultValue = "30") int dias,
            Authentication authentication) {
        
        try {
            UsuarioId usuarioId = extrairUsuarioId(authentication);
            
            List<MetaFinanceiraResponse> todasMetas = listarMetasFinanceirasUseCase.listarAtivas(usuarioId);
            
            LocalDate dataLimite = LocalDate.now().plusDays(dias);
            
            List<MetaFinanceiraResponse> metasProximas = todasMetas.stream()
                .filter(meta -> meta.prazo().isBefore(dataLimite) || meta.prazo().isEqual(dataLimite))
                .toList();
            
            return ResponseEntity.ok(metasProximas);
            
        } catch (IllegalStateException e) {
            throw new BusinessRuleException("Erro ao listar metas próximas do vencimento", e);
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
     * Converte uma entidade MetaFinanceira para MetaFinanceiraResponse.
     */
    private MetaFinanceiraResponse converterParaResponse(MetaFinanceira meta) {
        BigDecimal percentualConclusao = meta.calcularPercentualConclusao();
        LocalDate estimativaConclusao = meta.estimarDataConclusao();
        
        return new MetaFinanceiraResponse(
            meta.getId().valor().toString(),
            meta.getNome().valor(),
            meta.getValorAlvo().quantia(),
            meta.getValorAtual().quantia(),
            meta.getPrazo(),
            meta.getTipo(),
            meta.getStatus(),
            meta.getCriadoEm(),
            percentualConclusao,
            estimativaConclusao
        );
    }
    
    /**
     * Calcula resumo estatístico das metas.
     */
    private ResumoMetasResponse calcularResumo(List<MetaFinanceiraResponse> metas) {
        int totalMetas = metas.size();
        int metasAlcancadas = 0;
        int metasEmAndamento = 0;
        int metasAtrasadas = 0;
        BigDecimal totalValorAlvo = BigDecimal.ZERO;
        BigDecimal totalValorAtual = BigDecimal.ZERO;
        
        LocalDate hoje = LocalDate.now();
        
        for (MetaFinanceiraResponse meta : metas) {
            totalValorAlvo = totalValorAlvo.add(meta.valorAlvo());
            totalValorAtual = totalValorAtual.add(meta.valorAtual());
            
            if (meta.percentualConclusao().compareTo(BigDecimal.valueOf(100)) >= 0) {
                metasAlcancadas++;
            } else if (meta.prazo().isBefore(hoje)) {
                metasAtrasadas++;
            } else {
                metasEmAndamento++;
            }
        }
        
        BigDecimal percentualGeralConclusao = totalValorAlvo.compareTo(BigDecimal.ZERO) == 0 
            ? BigDecimal.ZERO 
            : totalValorAtual.divide(totalValorAlvo, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        
        return new ResumoMetasResponse(
            totalMetas,
            metasAlcancadas,
            metasEmAndamento,
            metasAtrasadas,
            totalValorAlvo,
            totalValorAtual,
            percentualGeralConclusao
        );
    }
    
    // DTOs e Exception classes
    
    public record ResumoMetasResponse(
        int totalMetas,
        int metasAlcancadas,
        int metasEmAndamento,
        int metasAtrasadas,
        BigDecimal totalValorAlvo,
        BigDecimal totalValorAtual,
        BigDecimal percentualGeralConclusao
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