package com.gestaofinanceira.infrastructure.persistence.mapper;

import com.gestaofinanceira.domain.entities.Usuario;
import com.gestaofinanceira.domain.valueobjects.*;
import com.gestaofinanceira.infrastructure.persistence.entity.UsuarioJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para conversão entre entidade de domínio Usuario e entidade JPA UsuarioJpaEntity.
 */
@Component
public class UsuarioMapper {
    
    /**
     * Converte entidade de domínio para entidade JPA.
     */
    public UsuarioJpaEntity toJpaEntity(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        
        return new UsuarioJpaEntity(
            usuario.getId().valor(),
            usuario.getEmail().valor(),
            usuario.getSenha().hash(),
            usuario.getSenha().salt(),
            usuario.getNome().valor(),
            usuario.getCriadoEm(),
            usuario.isAtivo(),
            usuario.isDadosIniciaisCarregados()
        );
    }
    
    /**
     * Converte entidade JPA para entidade de domínio.
     */
    public Usuario toDomain(UsuarioJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        
        return new Usuario(
            new UsuarioId(jpaEntity.getId()),
            new Email(jpaEntity.getEmail()),
            new SenhaHash(jpaEntity.getSenhaHash(), jpaEntity.getSenhaSalt()),
            new Nome(jpaEntity.getNome()),
            jpaEntity.getCriadoEm(),
            jpaEntity.getAtivo(),
            jpaEntity.getDadosIniciaisCarregados()
        );
    }
    
    /**
     * Atualiza entidade JPA com dados da entidade de domínio.
     */
    public void updateJpaEntity(UsuarioJpaEntity jpaEntity, Usuario usuario) {
        if (jpaEntity == null || usuario == null) {
            return;
        }
        
        jpaEntity.setEmail(usuario.getEmail().valor());
        jpaEntity.setSenhaHash(usuario.getSenha().hash());
        jpaEntity.setSenhaSalt(usuario.getSenha().salt());
        jpaEntity.setNome(usuario.getNome().valor());
        jpaEntity.setAtivo(usuario.isAtivo());
        jpaEntity.setDadosIniciaisCarregados(usuario.isDadosIniciaisCarregados());
    }
}