package com.gestaofinanceira.application.usecases.orcamento;

import com.gestaofinanceira.application.dto.response.OrcamentoResponse;
import com.gestaofinanceira.application.ports.repository.OrcamentoRepository;
import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
import com.gestaofinanceira.domain.entities.Orcamento;
import com.gestaofinanceira.domain.entities.Usuario;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Use Case para listagem de orçamentos.
 * 
 * Implementa o requirement 5.2: "THE Sistema SHALL track spending against budget limits in real-time"
 * 
 * Responsabilidades:
 * - Validar usuário
 * - Buscar orçamentos do usuário
 * - Calcular percentuais de utilização
 * - Converter para DTOs de resposta
 */
@Service
@Transactional(readOnly = true)
public class ListarOrcamentosUseCase {
    
    private final OrcamentoRepository orcamentoRepository;
    private final UsuarioRepository usuarioRepository;
    
    public ListarOrcamentosUseCase(OrcamentoRepository orcamentoRepository,
                                   UsuarioRepository usuarioRepository) {
        this.orcamentoRepository = Objects.requireNonNull(orcamentoRepository);
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository);
    }
    
    /**
     * Lista todos os orçamentos ativos do usuário.
     * 
     * @param usuarioId ID do usuário
     * @return lista de orçamentos ativos
     */
    public List<OrcamentoResponse> listarAtivos(UsuarioId usuarioId) {
        Objects.requireNonNull(usuarioId, "ID do usuário não pode ser nulo");
        
        // Validar usuário
        validarUsuario(usuarioId);
        
        // Buscar orçamentos ativos
        List<Orcamento> orcamentos = orcamentoRepository.buscarAtivosPorUsuario(usuarioId);
        
        // Converter para DTOs
        return orcamentos.stream()
            .map(this::converterParaResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista todos os orçamentos do usuário (ativos e inativos).
     * 
     * @param usuarioId ID do usuário
     * @return lista de todos os orçamentos
     */
    public List<OrcamentoResponse> listarTodos(UsuarioId usuarioId) {
        Objects.requireNonNull(usuarioId, "ID do usuário não pode ser nulo");
        
        // Validar usuário
        validarUsuario(usuarioId);
        
        // Buscar todos os orçamentos
        List<Orcamento> orcamentos = orcamentoRepository.buscarPorUsuario(usuarioId);
        
        // Converter para DTOs
        return orcamentos.stream()
            .map(this::converterParaResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Busca orçamento específico por categoria.
     * 
     * @param usuarioId ID do usuário
     * @param categoria categoria do orçamento
     * @return orçamento da categoria ou null se não existir
     */
    public OrcamentoResponse buscarPorCategoria(UsuarioId usuarioId, String categoria) {
        Objects.requireNonNull(usuarioId, "ID do usuário não pode ser nulo");
        Objects.requireNonNull(categoria, "Categoria não pode ser nula");
        
        if (categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("Categoria não pode estar vazia");
        }
        
        // Validar usuário
        validarUsuario(usuarioId);
        
        // Buscar orçamento ativo por categoria
        return orcamentoRepository.buscarAtivoPorUsuarioECategoria(usuarioId, categoria.trim())
            .map(this::converterParaResponse)
            .orElse(null);
    }
    
    /**
     * Valida se o usuário existe e está ativo.
     */
    private void validarUsuario(UsuarioId usuarioId) {
        Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
            .orElseThrow(() -> new IllegalStateException(
                "Usuário não encontrado: " + usuarioId));
        
        if (!usuario.isAtivo()) {
            throw new IllegalStateException(
                "Usuário inativo: " + usuarioId);
        }
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
}