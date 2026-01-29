package com.gestaofinanceira.application.usecases.meta;

import com.gestaofinanceira.application.dto.response.MetaFinanceiraResponse;
import com.gestaofinanceira.application.ports.repository.MetaFinanceiraRepository;
import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
import com.gestaofinanceira.domain.entities.MetaFinanceira;
import com.gestaofinanceira.domain.entities.Usuario;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Use Case para listagem de metas financeiras.
 * 
 * Implementa o requirement 6.2: "THE Sistema SHALL track progress toward goals 
 * based on designated savings transactions"
 * 
 * Responsabilidades:
 * - Validar usuário
 * - Buscar metas do usuário
 * - Calcular percentuais de conclusão
 * - Estimar datas de conclusão
 * - Converter para DTOs de resposta
 */
@Service
@Transactional(readOnly = true)
public class ListarMetasFinanceirasUseCase {
    
    private final MetaFinanceiraRepository metaFinanceiraRepository;
    private final UsuarioRepository usuarioRepository;
    
    public ListarMetasFinanceirasUseCase(MetaFinanceiraRepository metaFinanceiraRepository,
                                         UsuarioRepository usuarioRepository) {
        this.metaFinanceiraRepository = Objects.requireNonNull(metaFinanceiraRepository);
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository);
    }
    
    /**
     * Lista todas as metas ativas do usuário.
     * 
     * @param usuarioId ID do usuário
     * @return lista de metas ativas
     */
    public List<MetaFinanceiraResponse> listarAtivas(UsuarioId usuarioId) {
        Objects.requireNonNull(usuarioId, "ID do usuário não pode ser nulo");
        
        // Validar usuário
        validarUsuario(usuarioId);
        
        // Buscar metas ativas
        List<MetaFinanceira> metas = metaFinanceiraRepository.buscarAtivasPorUsuario(usuarioId);
        
        // Converter para DTOs
        return metas.stream()
            .map(this::converterParaResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista todas as metas do usuário (ativas e inativas).
     * 
     * @param usuarioId ID do usuário
     * @return lista de todas as metas
     */
    public List<MetaFinanceiraResponse> listarTodas(UsuarioId usuarioId) {
        Objects.requireNonNull(usuarioId, "ID do usuário não pode ser nulo");
        
        // Validar usuário
        validarUsuario(usuarioId);
        
        // Buscar todas as metas
        List<MetaFinanceira> metas = metaFinanceiraRepository.buscarPorUsuario(usuarioId);
        
        // Converter para DTOs
        return metas.stream()
            .map(this::converterParaResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista metas por tipo.
     * 
     * @param usuarioId ID do usuário
     * @param tipo tipo da meta
     * @return lista de metas do tipo especificado
     */
    public List<MetaFinanceiraResponse> listarPorTipo(UsuarioId usuarioId, 
                                                      com.gestaofinanceira.domain.valueobjects.TipoMeta tipo) {
        Objects.requireNonNull(usuarioId, "ID do usuário não pode ser nulo");
        Objects.requireNonNull(tipo, "Tipo da meta não pode ser nulo");
        
        // Validar usuário
        validarUsuario(usuarioId);
        
        // Buscar metas por tipo
        List<MetaFinanceira> metas = metaFinanceiraRepository.buscarPorUsuarioETipo(usuarioId, tipo);
        
        // Converter para DTOs
        return metas.stream()
            .map(this::converterParaResponse)
            .collect(Collectors.toList());
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
}