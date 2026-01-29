package com.gestaofinanceira.infrastructure.persistence.mapper;

import com.gestaofinanceira.domain.entities.MetaFinanceira;
import com.gestaofinanceira.domain.valueobjects.*;
import com.gestaofinanceira.infrastructure.persistence.entity.MetaFinanceiraJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para conversão entre entidade de domínio MetaFinanceira e entidade JPA MetaFinanceiraJpaEntity.
 */
@Component
public class MetaFinanceiraMapper {
    
    /**
     * Converte entidade de domínio para entidade JPA.
     */
    public MetaFinanceiraJpaEntity toJpaEntity(MetaFinanceira meta) {
        if (meta == null) {
            return null;
        }
        
        return new MetaFinanceiraJpaEntity(
            meta.getId().valor(),
            meta.getUsuarioId().valor(),
            meta.getNome().valor(),
            meta.getValorAlvo().quantia(),
            meta.getValorAtual().quantia(),
            meta.getPrazo(),
            mapTipoToEnum(meta.getTipo()),
            mapStatusToEnum(meta.getStatus()),
            meta.getCriadoEm(),
            meta.getAtualizadoEm()
        );
    }
    
    /**
     * Converte entidade JPA para entidade de domínio.
     */
    public MetaFinanceira toDomain(MetaFinanceiraJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        
        return new MetaFinanceira(
            new MetaId(jpaEntity.getId()),
            new UsuarioId(jpaEntity.getUsuarioId()),
            new Nome(jpaEntity.getNome()),
            new Valor(jpaEntity.getValorAlvo(), Moeda.BRL), // Assumindo BRL por padrão
            new Valor(jpaEntity.getValorAtual(), Moeda.BRL),
            jpaEntity.getPrazo(),
            mapEnumToTipo(jpaEntity.getTipo()),
            mapEnumToStatus(jpaEntity.getStatus()),
            jpaEntity.getCriadoEm(),
            jpaEntity.getAtualizadoEm()
        );
    }
    
    /**
     * Atualiza entidade JPA com dados da entidade de domínio.
     */
    public void updateJpaEntity(MetaFinanceiraJpaEntity jpaEntity, MetaFinanceira meta) {
        if (jpaEntity == null || meta == null) {
            return;
        }
        
        jpaEntity.setNome(meta.getNome().valor());
        jpaEntity.setValorAlvo(meta.getValorAlvo().quantia());
        jpaEntity.setValorAtual(meta.getValorAtual().quantia());
        jpaEntity.setPrazo(meta.getPrazo());
        jpaEntity.setTipo(mapTipoToEnum(meta.getTipo()));
        jpaEntity.setStatus(mapStatusToEnum(meta.getStatus()));
        jpaEntity.setAtualizadoEm(meta.getAtualizadoEm());
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
    
    /**
     * Mapeia enum JPA para TipoMeta do domínio.
     */
    private TipoMeta mapEnumToTipo(MetaFinanceiraJpaEntity.TipoMetaEnum tipoEnum) {
        return switch (tipoEnum) {
            case EMERGENCIA -> TipoMeta.RESERVA_EMERGENCIA;
            case VIAGEM -> TipoMeta.FERIAS;
            case COMPRA -> TipoMeta.COMPRA;
            case INVESTIMENTO -> TipoMeta.INVESTIMENTO;
            case OUTROS -> TipoMeta.OUTROS;
        };
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
     * Mapeia enum JPA para StatusMeta do domínio.
     */
    private StatusMeta mapEnumToStatus(MetaFinanceiraJpaEntity.StatusMetaEnum statusEnum) {
        return switch (statusEnum) {
            case ATIVA -> StatusMeta.ATIVA;
            case CONCLUIDA -> StatusMeta.CONCLUIDA;
            case CANCELADA -> StatusMeta.CANCELADA;
            case PAUSADA -> StatusMeta.PAUSADA;
            case VENCIDA -> StatusMeta.VENCIDA;
        };
    }
}