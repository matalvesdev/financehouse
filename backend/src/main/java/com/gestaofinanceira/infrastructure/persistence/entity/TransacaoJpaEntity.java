package com.gestaofinanceira.infrastructure.persistence.entity;

import com.gestaofinanceira.infrastructure.persistence.converter.EncryptedStringConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade JPA para persistência de transações.
 * Mapeia a tabela 'transacoes' do banco de dados.
 */
@Entity
@Table(name = "transacoes", indexes = {
    @Index(name = "idx_transacoes_usuario_data", columnList = "usuario_id, data"),
    @Index(name = "idx_transacoes_categoria", columnList = "categoria"),
    @Index(name = "idx_transacoes_tipo", columnList = "tipo"),
    @Index(name = "idx_transacoes_ativa", columnList = "ativa")
})
public class TransacaoJpaEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "usuario_id", nullable = false, columnDefinition = "UUID")
    private UUID usuarioId;
    
    @Column(name = "valor", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;
    
    @Column(name = "moeda", nullable = false, length = 3)
    private String moeda = "BRL";
    
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;
    
    @Column(name = "categoria", nullable = false, length = 50)
    private String categoria;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoTransacaoEnum tipo;
    
    @Column(name = "data", nullable = false)
    private LocalDate data;
    
    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
    
    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;
    
    @Column(name = "ativa", nullable = false)
    private Boolean ativa = true;
    
    // Enum para tipo de transação
    public enum TipoTransacaoEnum {
        RECEITA, DESPESA
    }
    
    // Construtor padrão para JPA
    protected TransacaoJpaEntity() {}
    
    public TransacaoJpaEntity(UUID id, UUID usuarioId, BigDecimal valor, String moeda,
                             String descricao, String categoria, TipoTransacaoEnum tipo,
                             LocalDate data, LocalDateTime criadoEm, LocalDateTime atualizadoEm,
                             Boolean ativa) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.valor = valor;
        this.moeda = moeda;
        this.descricao = descricao;
        this.categoria = categoria;
        this.tipo = tipo;
        this.data = data;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
        this.ativa = ativa;
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
    
    public BigDecimal getValor() {
        return valor;
    }
    
    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
    
    public String getMoeda() {
        return moeda;
    }
    
    public void setMoeda(String moeda) {
        this.moeda = moeda;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public TipoTransacaoEnum getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoTransacaoEnum tipo) {
        this.tipo = tipo;
    }
    
    public LocalDate getData() {
        return data;
    }
    
    public void setData(LocalDate data) {
        this.data = data;
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
    
    public Boolean getAtiva() {
        return ativa;
    }
    
    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransacaoJpaEntity that = (TransacaoJpaEntity) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("TransacaoJpaEntity{id=%s, usuarioId=%s, valor=%s, tipo=%s, data=%s}", 
            id, usuarioId, valor, tipo, data);
    }
}