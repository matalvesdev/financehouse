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
 * Entidade JPA para persistência de metas financeiras.
 * Mapeia a tabela 'metas_financeiras' do banco de dados.
 */
@Entity
@Table(name = "metas_financeiras", indexes = {
    @Index(name = "idx_metas_usuario", columnList = "usuario_id"),
    @Index(name = "idx_metas_status", columnList = "status"),
    @Index(name = "idx_metas_prazo", columnList = "prazo")
})
public class MetaFinanceiraJpaEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "usuario_id", nullable = false, columnDefinition = "UUID")
    private UUID usuarioId;
    
    @Column(name = "nome", nullable = false, length = 255)
    private String nome;
    
    @Column(name = "valor_alvo", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorAlvo;
    
    @Column(name = "valor_atual", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorAtual = BigDecimal.ZERO;
    
    @Column(name = "prazo", nullable = false)
    private LocalDate prazo;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoMetaEnum tipo;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusMetaEnum status = StatusMetaEnum.ATIVA;
    
    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
    
    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;
    
    // Enums para tipo e status
    public enum TipoMetaEnum {
        EMERGENCIA, VIAGEM, COMPRA, INVESTIMENTO, OUTROS
    }
    
    public enum StatusMetaEnum {
        ATIVA, CONCLUIDA, CANCELADA, PAUSADA, VENCIDA
    }
    
    // Construtor padrão para JPA
    protected MetaFinanceiraJpaEntity() {}
    
    public MetaFinanceiraJpaEntity(UUID id, UUID usuarioId, String nome, BigDecimal valorAlvo,
                                  BigDecimal valorAtual, LocalDate prazo, TipoMetaEnum tipo,
                                  StatusMetaEnum status, LocalDateTime criadoEm, 
                                  LocalDateTime atualizadoEm) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.valorAlvo = valorAlvo;
        this.valorAtual = valorAtual;
        this.prazo = prazo;
        this.tipo = tipo;
        this.status = status;
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
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public BigDecimal getValorAlvo() {
        return valorAlvo;
    }
    
    public void setValorAlvo(BigDecimal valorAlvo) {
        this.valorAlvo = valorAlvo;
    }
    
    public BigDecimal getValorAtual() {
        return valorAtual;
    }
    
    public void setValorAtual(BigDecimal valorAtual) {
        this.valorAtual = valorAtual;
    }
    
    public LocalDate getPrazo() {
        return prazo;
    }
    
    public void setPrazo(LocalDate prazo) {
        this.prazo = prazo;
    }
    
    public TipoMetaEnum getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoMetaEnum tipo) {
        this.tipo = tipo;
    }
    
    public StatusMetaEnum getStatus() {
        return status;
    }
    
    public void setStatus(StatusMetaEnum status) {
        this.status = status;
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
        MetaFinanceiraJpaEntity that = (MetaFinanceiraJpaEntity) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("MetaFinanceiraJpaEntity{id=%s, usuarioId=%s, nome='%s', valorAlvo=%s, status=%s}", 
            id, usuarioId, nome, valorAlvo, status);
    }
}