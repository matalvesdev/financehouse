package com.gestaofinanceira.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade JPA para persistência de orçamentos.
 * Mapeia a tabela 'orcamentos' do banco de dados.
 */
@Entity
@Table(name = "orcamentos", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_orcamento_usuario_categoria_periodo", 
                           columnNames = {"usuario_id", "categoria", "inicio_periodo"})
       },
       indexes = {
           @Index(name = "idx_orcamentos_usuario", columnList = "usuario_id"),
           @Index(name = "idx_orcamentos_categoria", columnList = "categoria"),
           @Index(name = "idx_orcamentos_status", columnList = "status"),
           @Index(name = "idx_orcamentos_periodo", columnList = "inicio_periodo, fim_periodo")
       })
public class OrcamentoJpaEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "usuario_id", nullable = false, columnDefinition = "UUID")
    private UUID usuarioId;
    
    @Column(name = "categoria", nullable = false, length = 50)
    private String categoria;
    
    @Column(name = "limite", nullable = false, precision = 15, scale = 2)
    private BigDecimal limite;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "periodo", nullable = false, length = 20)
    private PeriodoOrcamentoEnum periodo;
    
    @Column(name = "gasto_atual", nullable = false, precision = 15, scale = 2)
    private BigDecimal gastoAtual = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusOrcamentoEnum status = StatusOrcamentoEnum.ATIVO;
    
    @Column(name = "inicio_periodo", nullable = false)
    private LocalDate inicioPeriodo;
    
    @Column(name = "fim_periodo", nullable = false)
    private LocalDate fimPeriodo;
    
    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
    
    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;
    
    // Enums para período e status
    public enum PeriodoOrcamentoEnum {
        MENSAL, TRIMESTRAL, ANUAL
    }
    
    public enum StatusOrcamentoEnum {
        ATIVO, EXCEDIDO, PROXIMO_LIMITE, ARQUIVADO
    }
    
    // Construtor padrão para JPA
    protected OrcamentoJpaEntity() {}
    
    public OrcamentoJpaEntity(UUID id, UUID usuarioId, String categoria, BigDecimal limite,
                             PeriodoOrcamentoEnum periodo, BigDecimal gastoAtual, 
                             StatusOrcamentoEnum status, LocalDate inicioPeriodo, 
                             LocalDate fimPeriodo, LocalDateTime criadoEm, 
                             LocalDateTime atualizadoEm) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.categoria = categoria;
        this.limite = limite;
        this.periodo = periodo;
        this.gastoAtual = gastoAtual;
        this.status = status;
        this.inicioPeriodo = inicioPeriodo;
        this.fimPeriodo = fimPeriodo;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }
    
    // Getters e Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public BigDecimal getLimite() {
        return limite;
    }
    
    public void setLimite(BigDecimal limite) {
        this.limite = limite;
    }
    
    public PeriodoOrcamentoEnum getPeriodo() {
        return periodo;
    }
    
    public void setPeriodo(PeriodoOrcamentoEnum periodo) {
        this.periodo = periodo;
    }
    
    public BigDecimal getGastoAtual() {
        return gastoAtual;
    }
    
    public void setGastoAtual(BigDecimal gastoAtual) {
        this.gastoAtual = gastoAtual;
    }
    
    public StatusOrcamentoEnum getStatus() {
        return status;
    }
    
    public void setStatus(StatusOrcamentoEnum status) {
        this.status = status;
    }
    
    public LocalDate getInicioPeriodo() {
        return inicioPeriodo;
    }
    
    public void setInicioPeriodo(LocalDate inicioPeriodo) {
        this.inicioPeriodo = inicioPeriodo;
    }
    
    public LocalDate getFimPeriodo() {
        return fimPeriodo;
    }
    
    public void setFimPeriodo(LocalDate fimPeriodo) {
        this.fimPeriodo = fimPeriodo;
    }
    
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
    
    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
    
    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
    
    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrcamentoJpaEntity that = (OrcamentoJpaEntity) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("OrcamentoJpaEntity{id=%s, usuarioId=%s, categoria='%s', limite=%s, status=%s}", 
            id, usuarioId, categoria, limite, status);
    }
}