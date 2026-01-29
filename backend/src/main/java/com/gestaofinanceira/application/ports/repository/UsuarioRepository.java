package com.gestaofinanceira.application.ports.repository;

import com.gestaofinanceira.domain.entities.Usuario;
import com.gestaofinanceira.domain.valueobjects.Email;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;

import java.util.Optional;

/**
 * Port para persistência de usuários.
 * 
 * Define as operações de persistência necessárias para a entidade Usuario,
 * abstraindo os detalhes de implementação da camada de infraestrutura.
 */
public interface UsuarioRepository {
    
    /**
     * Salva um usuário no repositório.
     * 
     * @param usuario o usuário a ser salvo
     * @return o usuário salvo com ID gerado
     */
    Usuario salvar(Usuario usuario);
    
    /**
     * Busca um usuário por ID.
     * 
     * @param id o ID do usuário
     * @return Optional contendo o usuário se encontrado
     */
    Optional<Usuario> buscarPorId(UsuarioId id);
    
    /**
     * Busca um usuário por email.
     * 
     * @param email o email do usuário
     * @return Optional contendo o usuário se encontrado
     */
    Optional<Usuario> buscarPorEmail(Email email);
    
    /**
     * Verifica se existe um usuário com o email especificado.
     * 
     * @param email o email a ser verificado
     * @return true se existe um usuário com o email
     */
    boolean existePorEmail(Email email);
    
    /**
     * Atualiza um usuário existente.
     * 
     * @param usuario o usuário com dados atualizados
     * @return o usuário atualizado
     */
    Usuario atualizar(Usuario usuario);
    
    /**
     * Remove um usuário do repositório (soft delete).
     * 
     * @param id o ID do usuário a ser removido
     */
    void remover(UsuarioId id);
}