package com.gestaofinanceira.infrastructure.persistence.adapter;

import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
import com.gestaofinanceira.domain.entities.Usuario;
import com.gestaofinanceira.domain.valueobjects.Email;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;
import com.gestaofinanceira.infrastructure.persistence.entity.UsuarioJpaEntity;
import com.gestaofinanceira.infrastructure.persistence.mapper.UsuarioMapper;
import com.gestaofinanceira.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementação do repositório de usuários usando JPA.
 * Adapta as operações de domínio para operações JPA.
 */
@Repository
@Transactional
public class UsuarioRepositoryImpl implements UsuarioRepository {
    
    private final UsuarioJpaRepository jpaRepository;
    private final UsuarioMapper mapper;
    
    public UsuarioRepositoryImpl(UsuarioJpaRepository jpaRepository, UsuarioMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public Usuario salvar(Usuario usuario) {
        UsuarioJpaEntity jpaEntity = mapper.toJpaEntity(usuario);
        UsuarioJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(UsuarioId id) {
        return jpaRepository.findById(id.valor())
            .map(mapper::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(Email email) {
        return jpaRepository.findByEmailAndAtivo(email.valor())
            .map(mapper::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existePorEmail(Email email) {
        return jpaRepository.existsByEmail(email.valor());
    }
    
    @Override
    public Usuario atualizar(Usuario usuario) {
        Optional<UsuarioJpaEntity> existingEntity = jpaRepository.findById(usuario.getId().valor());
        
        if (existingEntity.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado para atualização: " + usuario.getId());
        }
        
        UsuarioJpaEntity jpaEntity = existingEntity.get();
        mapper.updateJpaEntity(jpaEntity, usuario);
        
        UsuarioJpaEntity updatedEntity = jpaRepository.save(jpaEntity);
        return mapper.toDomain(updatedEntity);
    }
    
    @Override
    public void remover(UsuarioId id) {
        Optional<UsuarioJpaEntity> existingEntity = jpaRepository.findById(id.valor());
        
        if (existingEntity.isPresent()) {
            UsuarioJpaEntity jpaEntity = existingEntity.get();
            jpaEntity.setAtivo(false); // Soft delete
            jpaRepository.save(jpaEntity);
        }
    }
}