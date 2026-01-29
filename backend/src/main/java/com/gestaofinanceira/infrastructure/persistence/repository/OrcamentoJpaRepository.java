package com.gestaofinanceira.infrastructure.persistence.repository;

import com.gestaofinanceira.infrastructure.persistence.entity.OrcamentoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório JPA para operações de persistência de orçamentos.
 */
@Repository
public interface OrcamentoJpaRepository extends JpaRepository<OrcamentoJpaEntity, UUID> {
    
    /**
     * Busca orçamentos ativos por usuário.
     */
    @Query("SELECT o FROM OrcamentoJpaEntity o WHERE o.usuarioId = :usuarioId AND o.status IN ('ATIVO', 'PROXIMO_LIMITE', 'EXCEDIDO') ORDER BY o.criadoEm DESC")
    List<OrcamentoJpaEntity> findOrcamentosAtivosByUsuarioId(@Param("usuarioId") UUID usuarioId);
    
    /**
     * Busca todos os orçamentos por usuário.
     */
    @Query("SELECT o FROM OrcamentoJpaEntity o WHERE o.usuarioId = :usuarioId ORDER BY o.criadoEm DESC")
    List<OrcamentoJpaEntity> findByUsuarioId(@Param("usuarioId") UUID usuarioId);
    
    /**
     * Busca orçamento por usuário, categoria e período específico.
     */
    @Query("SELECT o FROM OrcamentoJpaEntity o WHERE o.usuarioId = :usuarioId AND o.categoria = :categoria AND o.inicioPeriodo = :inicioPeriodo")
    Optional<OrcamentoJpaEntity> findByUsuarioIdAndCategoriaAndInicioPeriodo(
        @Param("usuarioId") UUID usuarioId,
        @Param("categoria") String categoria,
        @Param("inicioPeriodo") LocalDate inicioPeriodo
    );
    
    /**
     * Busca orçamentos por usuário e categoria.
     */
    @Query("SELECT o FROM OrcamentoJpaEntity o WHERE o.usuarioId = :usuarioId AND o.categoria = :categoria ORDER BY o.inicioPeriodo DESC")
    List<OrcamentoJpaEntity> findByUsuarioIdAndCategoria(
        @Param("usuarioId") UUID usuarioId,
        @Param("categoria") String categoria
    );
    
    /**
     * Busca orçamentos que contêm uma data específica.
     */
    @Query("SELECT o FROM OrcamentoJpaEntity o WHERE o.usuarioId = :usuarioId AND :data BETWEEN o.inicioPeriodo AND o.fimPeriodo AND o.status IN ('ATIVO', 'PROXIMO_LIMITE', 'EXCEDIDO')")
    List<OrcamentoJpaEntity> findOrcamentosVigentesPorData(
        @Param("usuarioId") UUID usuarioId,
        @Param("data") LocalDate data
    );
    
    /**
     * Busca orçamento vigente por usuário e categoria em uma data específica.
     */
    @Query("SELECT o FROM OrcamentoJpaEntity o WHERE o.usuarioId = :usuarioId AND o.categoria = :categoria AND :data BETWEEN o.inicioPeriodo AND o.fimPeriodo AND o.status IN ('ATIVO', 'PROXIMO_LIMITE', 'EXCEDIDO')")
    Optional<OrcamentoJpaEntity> findOrcamentoVigentePorCategoriaEData(
        @Param("usuarioId") UUID usuarioId,
        @Param("categoria") String categoria,
        @Param("data") LocalDate data
    );
    
    /**
     * Busca orçamentos por status.
     */
    @Query("SELECT o FROM OrcamentoJpaEntity o WHERE o.usuarioId = :usuarioId AND o.status = :status ORDER BY o.criadoEm DESC")
    List<OrcamentoJpaEntity> findByUsuarioIdAndStatus(
        @Param("usuarioId") UUID usuarioId,
        @Param("status") OrcamentoJpaEntity.StatusOrcamentoEnum status
    );
    
    /**
     * Busca orçamentos próximos do limite.
     */
    @Query("SELECT o FROM OrcamentoJpaEntity o WHERE o.usuarioId = :usuarioId AND o.status IN ('PROXIMO_LIMITE', 'EXCEDIDO') ORDER BY o.atualizadoEm DESC")
    List<OrcamentoJpaEntity> findOrcamentosProximosLimite(@Param("usuarioId") UUID usuarioId);
    
    /**
     * Busca orçamentos que expiraram (fim do período passou).
     */
    @Query("SELECT o FROM OrcamentoJpaEntity o WHERE o.usuarioId = :usuarioId AND o.fimPeriodo < :dataAtual AND o.status IN ('ATIVO', 'PROXIMO_LIMITE', 'EXCEDIDO')")
    List<OrcamentoJpaEntity> findOrcamentosExpirados(
        @Param("usuarioId") UUID usuarioId,
        @Param("dataAtual") LocalDate dataAtual
    );
    
    /**
     * Busca orçamentos por período.
     */
    @Query("SELECT o FROM OrcamentoJpaEntity o WHERE o.usuarioId = :usuarioId AND o.periodo = :periodo ORDER BY o.inicioPeriodo DESC")
    List<OrcamentoJpaEntity> findByUsuarioIdAndPeriodo(
        @Param("usuarioId") UUID usuarioId,
        @Param("periodo") OrcamentoJpaEntity.PeriodoOrcamentoEnum periodo
    );
    
    /**
     * Verifica se existe orçamento conflitante (mesma categoria e período sobreposto).
     */
    @Query("""
        SELECT COUNT(o) > 0 
        FROM OrcamentoJpaEntity o 
        WHERE o.usuarioId = :usuarioId 
        AND o.categoria = :categoria 
        AND o.id != :orcamentoId
        AND (
            (:inicioPeriodo BETWEEN o.inicioPeriodo AND o.fimPeriodo) OR
            (:fimPeriodo BETWEEN o.inicioPeriodo AND o.fimPeriodo) OR
            (o.inicioPeriodo BETWEEN :inicioPeriodo AND :fimPeriodo)
        )
        AND o.status IN ('ATIVO', 'PROXIMO_LIMITE', 'EXCEDIDO')
    """)
    boolean existeOrcamentoConflitante(
        @Param("usuarioId") UUID usuarioId,
        @Param("categoria") String categoria,
        @Param("inicioPeriodo") LocalDate inicioPeriodo,
        @Param("fimPeriodo") LocalDate fimPeriodo,
        @Param("orcamentoId") UUID orcamentoId
    );
    
    /**
     * Conta orçamentos ativos do usuário.
     */
    @Query("SELECT COUNT(o) FROM OrcamentoJpaEntity o WHERE o.usuarioId = :usuarioId AND o.status IN ('ATIVO', 'PROXIMO_LIMITE', 'EXCEDIDO')")
    long countOrcamentosAtivos(@Param("usuarioId") UUID usuarioId);
}