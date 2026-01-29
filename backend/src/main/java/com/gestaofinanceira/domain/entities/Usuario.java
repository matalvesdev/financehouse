package com.gestaofinanceira.domain.entities;

import com.gestaofinanceira.domain.valueobjects.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade que representa um usuário do sistema de gestão financeira.
 * 
 * Responsabilidades:
 * - Manter dados básicos do usuário (email, nome, senha)
 * - Controlar estado de ativação e dados iniciais
 * - Validar regras de negócio relacionadas ao usuário
 */
public class Usuario {
    
    private final UsuarioId id;
    private final Email email;
    private SenhaHash senha;
    private Nome nome;
    private final LocalDateTime criadoEm;
    private boolean ativo;
    private boolean dadosIniciaisCarregados;
    
    /**
     * Construtor para criação de novo usuário.
     */
    public Usuario(UsuarioId id, Email email, SenhaHash senha, Nome nome) {
        this.id = Objects.requireNonNull(id, "ID do usuário não pode ser nulo");
        this.email = Objects.requireNonNull(email, "Email não pode ser nulo");
        this.senha = Objects.requireNonNull(senha, "Senha não pode ser nula");
        this.nome = Objects.requireNonNull(nome, "Nome não pode ser nulo");
        this.criadoEm = LocalDateTime.now();
        this.ativo = true;
        this.dadosIniciaisCarregados = false;
    }
    
    /**
     * Construtor para reconstrução de usuário existente (usado por repositórios).
     */
    public Usuario(UsuarioId id, Email email, SenhaHash senha, Nome nome, 
                   LocalDateTime criadoEm, boolean ativo, boolean dadosIniciaisCarregados) {
        this.id = Objects.requireNonNull(id, "ID do usuário não pode ser nulo");
        this.email = Objects.requireNonNull(email, "Email não pode ser nulo");
        this.senha = Objects.requireNonNull(senha, "Senha não pode ser nula");
        this.nome = Objects.requireNonNull(nome, "Nome não pode ser nulo");
        this.criadoEm = Objects.requireNonNull(criadoEm, "Data de criação não pode ser nula");
        this.ativo = ativo;
        this.dadosIniciaisCarregados = dadosIniciaisCarregados;
    }
    
    /**
     * Factory method para criar um novo usuário.
     */
    public static Usuario criar(Email email, SenhaHash senha, Nome nome) {
        return new Usuario(UsuarioId.gerar(), email, senha, nome);
    }
    
    /**
     * Marca que os dados iniciais do usuário foram carregados via planilha.
     * Esta operação só pode ser feita uma vez.
     */
    public void marcarDadosIniciaisCarregados() {
        if (dadosIniciaisCarregados) {
            throw new IllegalStateException("Dados iniciais já foram carregados para este usuário");
        }
        
        if (!ativo) {
            throw new IllegalStateException("Não é possível carregar dados para usuário inativo");
        }
        
        this.dadosIniciaisCarregados = true;
    }
    
    /**
     * Verifica se o usuário pode importar planilha de dados iniciais.
     */
    public boolean podeImportarPlanilha() {
        return ativo && !dadosIniciaisCarregados;
    }
    
    /**
     * Atualiza a senha do usuário.
     */
    public void atualizarSenha(SenhaHash novaSenha) {
        Objects.requireNonNull(novaSenha, "Nova senha não pode ser nula");
        
        if (!ativo) {
            throw new IllegalStateException("Não é possível atualizar senha de usuário inativo");
        }
        
        this.senha = novaSenha;
    }
    
    /**
     * Atualiza o nome do usuário.
     */
    public void atualizarNome(Nome novoNome) {
        Objects.requireNonNull(novoNome, "Novo nome não pode ser nulo");
        
        if (!ativo) {
            throw new IllegalStateException("Não é possível atualizar nome de usuário inativo");
        }
        
        this.nome = novoNome;
    }
    
    /**
     * Desativa o usuário.
     */
    public void desativar() {
        if (!ativo) {
            throw new IllegalStateException("Usuário já está inativo");
        }
        
        this.ativo = false;
    }
    
    /**
     * Reativa o usuário.
     */
    public void reativar() {
        if (ativo) {
            throw new IllegalStateException("Usuário já está ativo");
        }
        
        this.ativo = true;
    }
    
    /**
     * Verifica se a senha fornecida está correta.
     */
    public boolean verificarSenha(String senhaTexto) {
        Objects.requireNonNull(senhaTexto, "Senha não pode ser nula");
        return senha.verificarSenha(senhaTexto);
    }
    
    /**
     * Verifica se o usuário está ativo e pode realizar operações.
     */
    public boolean podeRealizarOperacoes() {
        return ativo;
    }
    
    /**
     * Verifica se o usuário é novo (sem dados iniciais carregados).
     */
    public boolean ehNovoUsuario() {
        return !dadosIniciaisCarregados;
    }
    
    // Getters
    public UsuarioId getId() {
        return id;
    }
    
    public Email getEmail() {
        return email;
    }
    
    public SenhaHash getSenha() {
        return senha;
    }
    
    public Nome getNome() {
        return nome;
    }
    
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
    
    public boolean isAtivo() {
        return ativo;
    }
    
    public boolean isDadosIniciaisCarregados() {
        return dadosIniciaisCarregados;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Usuario{id=%s, email=%s, nome=%s, ativo=%s, dadosIniciaisCarregados=%s}", 
            id, email, nome, ativo, dadosIniciaisCarregados);
    }
}