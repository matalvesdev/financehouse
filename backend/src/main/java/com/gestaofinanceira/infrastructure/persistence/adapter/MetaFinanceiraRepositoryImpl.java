package com.gestaofinanceira.infrastructure.persistence.adapter;

import com.gestaofinanceira.application.ports.repository.MetaFinanceiraRepository;
import com.gestaofinanceira.domain.entities.MetaFinanceira;
import com.gestaofinanceira.domain.valueobjects.MetaId;
import com.gestaofinanceira.domain.valueobjects.StatusMeta;
import com.gestaofinanceira.domain.valueobjects.TipoMeta;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;
import com.gestaofinanceira.infrastructure.persistence.entity.MetaFinanceiraJpaEntity;
import com.gestaofinanceira.infrastructure.persistence.mapper.MetaFinanceiraMapper;
import com.gestaofinanceira.infrastructure.persistence.repository.MetaFinanceiraJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementação do repositório de metas financeiras usando JPA.
 * Adapta as operações de domínio para operações JPA.
 */
@Repository
@Transactional
public class MetaFinanceiraRepositoryImpl implements MetaFinanceiraRepository {
    
    private final MetaFinanceiraJpaRepository jpaRepository;
    private final MetaFinanceiraMapper mapper;
    
    public MetaFinanceiraRepositoryImpl(MetaFinanceiraJpaRepository jpaRepository, MetaFinanceiraMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public MetaFinanceira salvar(MetaFinanceira meta) {
        MetaFinanceiraJpaEntity jpaEntity = mapper.toJpaEntity(meta);
        MetaFinanceiraJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<MetaFinanceira> buscarPorId(MetaId id) {
        return jpaRepository.findById(id.valor())
            .map(mapper::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MetaFinanceira> buscarAtivasPorUsuario(UsuarioId usuarioId) {
        return jpaRepository.findMetasAtivasByUsuarioId(usuarioId.valor())
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MetaFinanceira> buscarPorUsuario(UsuarioId usuarioId) {
        return jpaRepository.findByUsuarioId(usuarioId.valor())
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MetaFinanceira> buscarPorUsuarioEStatus(UsuarioId usuarioId, StatusMeta status) {
        MetaFinanceiraJpaEntity.StatusMetaEnum statusEnum = mapStatusToEnum(status);
        return jpaRepository.findByUsuarioIdAndStatus(usuarioId.valor(), statusEnum)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MetaFinanceira> buscarPorUsuarioETipo(UsuarioId usuarioId, TipoMeta tipo) {
        MetaFinanceiraJpaEntity.TipoMetaEnum tipoEnum = mapTipoToEnum(tipo);
        return jpaRepository.findByUsuarioIdAndTipo(usuarioId.valor(), tipoEnum)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MetaFinanceira> buscarComPrazoProximo(UsuarioId usuarioId) {
        LocalDate hoje = LocalDate.now();
        LocalDate dataLimite = hoje.plusDays(30);
        
        return jpaRepository.findMetasProximasPrazo(usuarioId.valor(), hoje, dataLimite)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MetaFinanceira> buscarVencendoEm(LocalDate data) {
        return jpaRepository.findMetasVencendoNoPeriodo(null, data, data) // Busca todos os usuários
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MetaFinanceira> buscarAlcancadasRecentemente(UsuarioId usuarioId, int diasRecentes) {
        LocalDateTime dataLimite = LocalDateTime.now().minusDays(diasRecentes);
        
        return jpaRepository.findMetasConcluidasRecentemente(usuarioId.valor(), dataLimite)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public MetaFinanceira atualizar(MetaFinanceira meta) {
        Optional<MetaFinanceiraJpaEntity> existingEntity = jpaRepository.findById(meta.getId().valor());
        
        if (existingEntity.isEmpty()) {
            throw new IllegalArgumentException("Meta financeira não encontrada para atualização: " + meta.getId());
        }
        
        MetaFinanceiraJpaEntity jpaEntity = existingEntity.get();
        mapper.updateJpaEntity(jpaEntity, meta);
        
        MetaFinanceiraJpaEntity updatedEntity = jpaRepository.save(jpaEntity);
        return mapper.toDomain(updatedEntity);
    }
    
    @Override
    public void remover(MetaId id) {
        jpaRepository.deleteById(id.valor());
    }
    
    @Override
    public int arquivarVencidas(LocalDate dataLimite) {
        List<MetaFinanceiraJpaEntity> metasVencidas = jpaRepository.findMetasVencidas(null, dataLimite);
        
        int count = 0;
        for (MetaFinanceiraJpaEntity meta : metasVencidas) {
            meta.setStatus(MetaFinanceiraJpaEntity.StatusMetaEnum.VENCIDA);
            jpaRepository.save(meta);
            count++;
        }
        
        return count;
    }
    
    /**
     * Mapeia StatusMeta do domínio para enum JPA.
     */
    private MetaFinanceiraJpaEntity.StatusMetaEnum mapStatusToEnum(StatusMeta status) {
        return switch (status) {
            case ATIVA -> MetaFinanceiraJpaEntity.StatusMetaEnum.ATIVA;
            case CONCLUIDA -> MetaFinanceiraJpaEntity.StatusMetaEnum.CONCLUIDA;
            case CANCELADA -> MetaFinanceiraJpaEntity.StatusMetaEnum.CANCELADA;
            case PAUSADA -> MetaFinanceiraJpaEntity.StatusMetaEnum.PAUSADA;
            case VENCIDA -> MetaFinanceiraJpaEntity.StatusMetaEnum.VENCIDA;
        };
    }
    
    /**
     * Mapeia TipoMeta do domínio para enum JPA.
     */
    private MetaFinanceiraJpaEntity.TipoMetaEnum mapTipoToEnum(TipoMeta tipo) {
        return switch (tipo) {
            case RESERVA_EMERGENCIA -> MetaFinanceiraJpaEntity.TipoMetaEnum.EMERGENCIA;
            case FERIAS -> MetaFinanceiraJpaEntity.TipoMetaEnum.VIAGEM;
            case COMPRA -> MetaFinanceiraJpaEntity.TipoMetaEnum.COMPRA;
            case INVESTIMENTO -> MetaFinanceiraJpaEntity.TipoMetaEnum.INVESTIMENTO;
            case OUTROS -> MetaFinanceiraJpaEntity.TipoMetaEnum.OUTROS;
            default -> MetaFinanceiraJpaEntity.TipoMetaEnum.OUTROS;
        };
    }
}