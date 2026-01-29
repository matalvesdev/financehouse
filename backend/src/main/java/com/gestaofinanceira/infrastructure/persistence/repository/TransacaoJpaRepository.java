package com.gestaofinanceira.infrastructure.persistence.repository;

import com.gestaofinanceira.infrastructure.persistence.entity.TransacaoJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repositório JPA para operações de persistência de transações.
 */
@Repository
public interface TransacaoJpaRepository extends JpaRepository<TransacaoJpaEntity, UUID> {
    
    /**
     * Busca transações ativas por usuário.
     */
    @Query("SELECT t FROM TransacaoJpaEntity t WHERE t.usuarioId = :usuarioId AND t.ativa = true ORDER BY t.data DESC")
    List<TransacaoJpaEntity> findByUsuarioIdAndAtivaTrue(@Param("usuarioId") UUID usuarioId);
    
    /**
     * Busca transações ativas por usuário com paginação.
     */
    @Query("SELECT t FROM TransacaoJpaEntity t WHERE t.usuarioId = :usuarioId AND t.ativa = true")
    Page<TransacaoJpaEntity> findByUsuarioIdAndAtivaTrue(@Param("usuarioId") UUID usuarioId, Pageable pageable);
    
    /**
     * Busca transações por usuário e período.
     */
    @Query("SELECT t FROM TransacaoJpaEntity t WHERE t.usuarioId = :usuarioId AND t.data BETWEEN :dataInicio AND :dataFim AND t.ativa = true ORDER BY t.data DESC")
    List<TransacaoJpaEntity> findByUsuarioIdAndDataBetweenAndAtivaTrue(
        @Param("usuarioId") UUID usuarioId,
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim
    );
    
    /**
     * Busca transações por usuário, categoria e período.
     */
    @Query("SELECT t FROM TransacaoJpaEntity t WHERE t.usuarioId = :usuarioId AND t.categoria = :categoria AND t.data BETWEEN :dataInicio AND :dataFim AND t.ativa = true ORDER BY t.data DESC")
    List<TransacaoJpaEntity> findByUsuarioIdAndCategoriaAndDataBetweenAndAtivaTrue(
        @Param("usuarioId") UUID usuarioId,
        @Param("categoria") String categoria,
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim
    );
    
    /**
     * Busca transações por usuário e tipo.
     */
    @Query("SELECT t FROM TransacaoJpaEntity t WHERE t.usuarioId = :usuarioId AND t.tipo = :tipo AND t.ativa = true ORDER BY t.data DESC")
    List<TransacaoJpaEntity> findByUsuarioIdAndTipoAndAtivaTrue(
        @Param("usuarioId") UUID usuarioId,
        @Param("tipo") TransacaoJpaEntity.TipoTransacaoEnum tipo
    );
    
    /**
     * Calcula saldo total do usuário.
     */
    @Query("""
        SELECT COALESCE(
            SUM(CASE WHEN t.tipo = 'RECEITA' THEN t.valor ELSE -t.valor END), 
            0
        ) 
        FROM TransacaoJpaEntity t 
        WHERE t.usuarioId = :usuarioId AND t.ativa = true
    """)
    BigDecimal calcularSaldoTotal(@Param("usuarioId") UUID usuarioId);
    
    /**
     * Calcula total de receitas no período.
     */
    @Query("""
        SELECT COALESCE(SUM(t.valor), 0) 
        FROM TransacaoJpaEntity t 
        WHERE t.usuarioId = :usuarioId 
        AND t.tipo = 'RECEITA' 
        AND t.data BETWEEN :dataInicio AND :dataFim 
        AND t.ativa = true
    """)
    BigDecimal calcularReceitasPeriodo(
        @Param("usuarioId") UUID usuarioId,
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim
    );
    
    /**
     * Calcula total de despesas no período.
     */
    @Query("""
        SELECT COALESCE(SUM(t.valor), 0) 
        FROM TransacaoJpaEntity t 
        WHERE t.usuarioId = :usuarioId 
        AND t.tipo = 'DESPESA' 
        AND t.data BETWEEN :dataInicio AND :dataFim 
        AND t.ativa = true
    """)
    BigDecimal calcularDespesasPeriodo(
        @Param("usuarioId") UUID usuarioId,
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim
    );
    
    /**
     * Calcula total de gastos por categoria no período.
     */
    @Query("""
        SELECT COALESCE(SUM(t.valor), 0) 
        FROM TransacaoJpaEntity t 
        WHERE t.usuarioId = :usuarioId 
        AND t.categoria = :categoria 
        AND t.tipo = 'DESPESA'
        AND t.data BETWEEN :dataInicio AND :dataFim 
        AND t.ativa = true
    """)
    BigDecimal calcularGastosPorCategoriaPeriodo(
        @Param("usuarioId") UUID usuarioId,
        @Param("categoria") String categoria,
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim
    );
    
    /**
     * Busca transações recentes (últimas N transações).
     */
    @Query("SELECT t FROM TransacaoJpaEntity t WHERE t.usuarioId = :usuarioId AND t.ativa = true ORDER BY t.criadoEm DESC")
    List<TransacaoJpaEntity> findTransacoesRecentes(@Param("usuarioId") UUID usuarioId, Pageable pageable);
    
    /**
     * Busca transações por descrição (busca textual).
     */
    @Query("SELECT t FROM TransacaoJpaEntity t WHERE t.usuarioId = :usuarioId AND LOWER(t.descricao) LIKE LOWER(CONCAT('%', :termo, '%')) AND t.ativa = true ORDER BY t.data DESC")
    List<TransacaoJpaEntity> findByUsuarioIdAndDescricaoContainingIgnoreCaseAndAtivaTrue(
        @Param("usuarioId") UUID usuarioId,
        @Param("termo") String termo
    );
    
    /**
     * Conta transações do usuário.
     */
    @Query("SELECT COUNT(t) FROM TransacaoJpaEntity t WHERE t.usuarioId = :usuarioId AND t.ativa = true")
    long countByUsuarioIdAndAtivaTrue(@Param("usuarioId") UUID usuarioId);
}