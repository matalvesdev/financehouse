package com.gestaofinanceira.infrastructure.persistence.repository;

import com.gestaofinanceira.infrastructure.persistence.entity.UsuarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório JPA para operações de persistência de usuários.
 */
@Repository
public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, UUID> {
    
    /**
     * Busca usuário por email.
     */
    Optional<UsuarioJpaEntity> findByEmail(String email);
    
    /**
     * Verifica se existe usuário com o email especificado.
     */
    boolean existsByEmail(String email);
    
    /**
     * Busca usuários ativos.
     */
    @Query("SELECT u FROM UsuarioJpaEntity u WHERE u.ativo = true")
    java.util.List<UsuarioJpaEntity> findAllAtivos();
    
    /**
     * Busca usuário ativo por email.
     */
    @Query("SELECT u FROM UsuarioJpaEntity u WHERE u.email = :email AND u.ativo = true")
    Optional<UsuarioJpaEntity> findByEmailAndAtivo(@Param("email") String email);
    
    /**
     * Busca usuários que ainda não carregaram dados iniciais.
     */
    @Query("SELECT u FROM UsuarioJpaEntity u WHERE u.dadosIniciaisCarregados = false AND u.ativo = true")
    java.util.List<UsuarioJpaEntity> findUsuariosSemDadosIniciais();
    
    /**
     * Conta usuários ativos.
     */
    @Query("SELECT COUNT(u) FROM UsuarioJpaEntity u WHERE u.ativo = true")
    long countUsuariosAtivos();
}