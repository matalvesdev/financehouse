package com.gestaofinanceira.infrastructure.persistence.entity;

import com.gestaofinanceira.infrastructure.persistence.converter.EncryptedStringConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade JPA para persistência de usuários.
 * Mapeia a tabela 'usuarios' do banco de dados.
 */
@Entity
@Table(name = "usuarios")
public class UsuarioJpaEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;
    
    @Column(name = "senha_salt", nullable = false, length = 255)
    private String senhaSalt;
    
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "nome", nullable = false, length = 255)
    private String nome;
    
    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
    
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;
    
    @Column(name = "dados_iniciais_carregados", nullable = false)
    private Boolean dadosIniciaisCarregados = false;
    
    // Construtor padrão para JPA
    protected UsuarioJpaEntity() {}
    
    public UsuarioJpaEntity(UUID id, String email, String senhaHash, String senhaSalt, String nome, 
                           LocalDateTime criadoEm, Boolean ativo, Boolean dadosIniciaisCarregados) {
        this.id = id;
        this.email = email;
        this.senhaHash = senhaHash;
        this.senhaSalt = senhaSalt;
        this.nome = nome;
        this.criadoEm = criadoEm;
        this.ativo = ativo;
        this.dadosIniciaisCarregados = dadosIniciaisCarregados;
    }
    
    // Getters e Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getSenhaHash() {
        return senhaHash;
    }
    
    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }
    
    public String getSenhaSalt() {
        return senhaSalt;
    }
    
    public void setSenhaSalt(String senhaSalt) {
        this.senhaSalt = senhaSalt;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
    
    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
    
    public Boolean getAtivo() {
        return ativo;
    }
    
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
    
    public Boolean getDadosIniciaisCarregados() {
        return dadosIniciaisCarregados;
    }
    
    public void setDadosIniciaisCarregados(Boolean dadosIniciaisCarregados) {
        this.dadosIniciaisCarregados = dadosIniciaisCarregados;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioJpaEntity that = (UsuarioJpaEntity) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("UsuarioJpaEntity{id=%s, email='%s', nome='%s', ativo=%s}", 
            id, email, nome, ativo);
    }
}