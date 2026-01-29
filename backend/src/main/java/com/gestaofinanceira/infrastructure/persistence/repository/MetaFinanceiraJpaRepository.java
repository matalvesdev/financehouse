package com.gestaofinanceira.infrastructure.persistence.repository;

import com.gestaofinanceira.infrastructure.persistence.entity.MetaFinanceiraJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repositório JPA para operações de persistência de metas financeiras.
 */
@Repository
public interface MetaFinanceiraJpaRepository extends JpaRepository<MetaFinanceiraJpaEntity, UUID> {
    
    /**
     * Busca metas ativas por usuário.
     */
    @Query("SELECT m FROM MetaFinanceiraJpaEntity m WHERE m.usuarioId = :usuarioId AND m.status = 'ATIVA' ORDER BY m.prazo ASC")
    List<MetaFinanceiraJpaEntity> findMetasAtivasByUsuarioId(@Param("usuarioId") UUID usuarioId);
    
    /**
     * Busca todas as metas por usuário.
     */
    @Query("SELECT m FROM MetaFinanceiraJpaEntity m WHERE m.usuarioId = :usuarioId ORDER BY m.criadoEm DESC")
    List<MetaFinanceiraJpaEntity> findByUsuarioId(@Param("usuarioId") UUID usuarioId);
    
    /**
     * Busca metas por usuário e status.
     */
    @Query("SELECT m FROM MetaFinanceiraJpaEntity m WHERE m.usuarioId = :usuarioId AND m.status = :status ORDER BY m.criadoEm DESC")
    List<MetaFinanceiraJpaEntity> findByUsuarioIdAndStatus(
        @Param("usuarioId") UUID usuarioId,
        @Param("status") MetaFinanceiraJpaEntity.StatusMetaEnum status
    );
    
    /**
     * Busca metas por usuário e tipo.
     */
    @Query("SELECT m FROM MetaFinanceiraJpaEntity m WHERE m.usuarioId = :usuarioId AND m.tipo = :tipo ORDER BY m.prazo ASC")
    List<MetaFinanceiraJpaEntity> findByUsuarioIdAndTipo(
        @Param("usuarioId") UUID usuarioId,
        @Param("tipo") MetaFinanceiraJpaEntity.TipoMetaEnum tipo
    );
    
    /**
     * Busca metas que vencem em um período específico.
     */
    @Query("SELECT m FROM MetaFinanceiraJpaEntity m WHERE m.usuarioId = :usuarioId AND m.prazo BETWEEN :dataInicio AND :dataFim AND m.status = 'ATIVA' ORDER BY m.prazo ASC")
    List<MetaFinanceiraJpaEntity> findMetasVencendoNoPeriodo(
        @Param("usuarioId") UUID usuarioId,
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim
    );
    
    /**
     * Busca metas próximas do prazo (próximos 30 dias).
     */
    @Query("SELECT m FROM MetaFinanceiraJpaEntity m WHERE m.usuarioId = :usuarioId AND m.prazo BETWEEN :hoje AND :dataLimite AND m.status = 'ATIVA' ORDER BY m.prazo ASC")
    List<MetaFinanceiraJpaEntity> findMetasProximasPrazo(
        @Param("usuarioId") UUID usuarioId,
        @Param("hoje") LocalDate hoje,
        @Param("dataLimite") LocalDate dataLimite
    );
    
    /**
     * Busca metas vencidas (prazo passou e não foram concluídas).
     */
    @Query("SELECT m FROM MetaFinanceiraJpaEntity m WHERE m.usuarioId = :usuarioId AND m.prazo < :dataAtual AND m.status = 'ATIVA'")
    List<MetaFinanceiraJpaEntity> findMetasVencidas(
        @Param("usuarioId") UUID usuarioId,
        @Param("dataAtual") LocalDate dataAtual
    );
    
    /**
     * Busca metas concluídas recentemente (últimos 30 dias).
     */
    @Query("SELECT m FROM MetaFinanceiraJpaEntity m WHERE m.usuarioId = :usuarioId AND m.status = 'CONCLUIDA' AND m.atualizadoEm >= :dataLimite ORDER BY m.atualizadoEm DESC")
    List<MetaFinanceiraJpaEntity> findMetasConcluidasRecentemente(
        @Param("usuarioId") UUID usuarioId,
        @Param("dataLimite") java.time.LocalDateTime dataLimite
    );
    
    /**
     * Busca metas por nome (busca textual).
     */
    @Query("SELECT m FROM MetaFinanceiraJpaEntity m WHERE m.usuarioId = :usuarioId AND LOWER(m.nome) LIKE LOWER(CONCAT('%', :termo, '%')) ORDER BY m.criadoEm DESC")
    List<MetaFinanceiraJpaEntity> findByUsuarioIdAndNomeContainingIgnoreCase(
        @Param("usuarioId") UUID usuarioId,
        @Param("termo") String termo
    );
    
    /**
     * Calcula progresso médio das metas ativas do usuário.
     */
    @Query("""
        SELECT AVG(m.valorAtual * 100.0 / m.valorAlvo) 
        FROM MetaFinanceiraJpaEntity m 
        WHERE m.usuarioId = :usuarioId 
        AND m.status = 'ATIVA' 
        AND m.valorAlvo > 0
    """)
    Double calcularProgressoMedio(@Param("usuarioId") UUID usuarioId);
    
    /**
     * Conta metas por status.
     */
    @Query("SELECT COUNT(m) FROM MetaFinanceiraJpaEntity m WHERE m.usuarioId = :usuarioId AND m.status = :status")
    long countByUsuarioIdAndStatus(
        @Param("usuarioId") UUID usuarioId,
        @Param("status") MetaFinanceiraJpaEntity.StatusMetaEnum status
    );
    
    /**
     * Conta metas ativas do usuário.
     */
    @Query("SELECT COUNT(m) FROM MetaFinanceiraJpaEntity m WHERE m.usuarioId = :usuarioId AND m.status = 'ATIVA'")
    long countMetasAtivas(@Param("usuarioId") UUID usuarioId);
    
    /**
     * Busca metas que precisam de verificação de vencimento.
     */
    @Query("SELECT m FROM MetaFinanceiraJpaEntity m WHERE m.status = 'ATIVA' AND m.prazo < :dataAtual")
    List<MetaFinanceiraJpaEntity> findMetasParaVerificacaoVencimento(@Param("dataAtual") LocalDate dataAtual);
}