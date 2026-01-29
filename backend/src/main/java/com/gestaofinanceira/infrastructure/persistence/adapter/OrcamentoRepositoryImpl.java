package com.gestaofinanceira.infrastructure.persistence.adapter;

import com.gestaofinanceira.application.ports.repository.OrcamentoRepository;
import com.gestaofinanceira.domain.entities.Orcamento;
import com.gestaofinanceira.domain.valueobjects.OrcamentoId;
import com.gestaofinanceira.domain.valueobjects.StatusOrcamento;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;
import com.gestaofinanceira.infrastructure.persistence.entity.OrcamentoJpaEntity;
import com.gestaofinanceira.infrastructure.persistence.mapper.OrcamentoMapper;
import com.gestaofinanceira.infrastructure.persistence.repository.OrcamentoJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementação do repositório de orçamentos usando JPA.
 * Adapta as operações de domínio para operações JPA.
 */
@Repository
@Transactional
public class OrcamentoRepositoryImpl implements OrcamentoRepository {
    
    private final OrcamentoJpaRepository jpaRepository;
    private final OrcamentoMapper mapper;
    
    public OrcamentoRepositoryImpl(OrcamentoJpaRepository jpaRepository, OrcamentoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public Orcamento salvar(Orcamento orcamento) {
        OrcamentoJpaEntity jpaEntity = mapper.toJpaEntity(orcamento);
        OrcamentoJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Orcamento> buscarPorId(OrcamentoId id) {
        return jpaRepository.findById(id.valor())
            .map(mapper::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Orcamento> buscarAtivosPorUsuario(UsuarioId usuarioId) {
        return jpaRepository.findOrcamentosAtivosByUsuarioId(usuarioId.valor())
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Orcamento> buscarPorUsuario(UsuarioId usuarioId) {
        return jpaRepository.findByUsuarioId(usuarioId.valor())
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Orcamento> buscarAtivoPorUsuarioECategoria(UsuarioId usuarioId, String categoria) {
        return jpaRepository.findOrcamentoVigentePorCategoriaEData(
                usuarioId.valor(), categoria, LocalDate.now())
            .map(mapper::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Orcamento> buscarPorUsuarioEStatus(UsuarioId usuarioId, StatusOrcamento status) {
        OrcamentoJpaEntity.StatusOrcamentoEnum statusEnum = mapStatusToEnum(status);
        return jpaRepository.findByUsuarioIdAndStatus(usuarioId.valor(), statusEnum)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Orcamento> buscarProximosDoLimite(UsuarioId usuarioId) {
        return jpaRepository.findOrcamentosProximosLimite(usuarioId.valor())
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Orcamento> buscarExcedidos(UsuarioId usuarioId) {
        return jpaRepository.findByUsuarioIdAndStatus(
                usuarioId.valor(), OrcamentoJpaEntity.StatusOrcamentoEnum.EXCEDIDO)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Orcamento> buscarVencendoEm(LocalDate data) {
        return jpaRepository.findOrcamentosExpirados(null, data) // Busca todos os usuários
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public Orcamento atualizar(Orcamento orcamento) {
        Optional<OrcamentoJpaEntity> existingEntity = jpaRepository.findById(orcamento.getId().valor());
        
        if (existingEntity.isEmpty()) {
            throw new IllegalArgumentException("Orçamento não encontrado para atualização: " + orcamento.getId());
        }
        
        OrcamentoJpaEntity jpaEntity = existingEntity.get();
        mapper.updateJpaEntity(jpaEntity, orcamento);
        
        OrcamentoJpaEntity updatedEntity = jpaRepository.save(jpaEntity);
        return mapper.toDomain(updatedEntity);
    }
    
    @Override
    public void remover(OrcamentoId id) {
        jpaRepository.deleteById(id.valor());
    }
    
    @Override
    public int arquivarVencidos(LocalDate dataLimite) {
        List<OrcamentoJpaEntity> orcamentosVencidos = jpaRepository.findOrcamentosExpirados(null, dataLimite);
        
        int count = 0;
        for (OrcamentoJpaEntity orcamento : orcamentosVencidos) {
            orcamento.setStatus(OrcamentoJpaEntity.StatusOrcamentoEnum.ARQUIVADO);
            jpaRepository.save(orcamento);
            count++;
        }
        
        return count;
    }
    
    /**
     * Mapeia StatusOrcamento do domínio para enum JPA.
     */
    private OrcamentoJpaEntity.StatusOrcamentoEnum mapStatusToEnum(StatusOrcamento status) {
        return switch (status) {
            case ATIVO -> OrcamentoJpaEntity.StatusOrcamentoEnum.ATIVO;
            case EXCEDIDO -> OrcamentoJpaEntity.StatusOrcamentoEnum.EXCEDIDO;
            case PROXIMO_LIMITE -> OrcamentoJpaEntity.StatusOrcamentoEnum.PROXIMO_LIMITE;
            case ARQUIVADO -> OrcamentoJpaEntity.StatusOrcamentoEnum.ARQUIVADO;
        };
    }
}