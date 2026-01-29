package com.gestaofinanceira.infrastructure.persistence.mapper;

import com.gestaofinanceira.domain.entities.Orcamento;
import com.gestaofinanceira.domain.valueobjects.*;
import com.gestaofinanceira.infrastructure.persistence.entity.OrcamentoJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para conversão entre entidade de domínio Orcamento e entidade JPA OrcamentoJpaEntity.
 */
@Component
public class OrcamentoMapper {
    
    /**
     * Converte entidade de domínio para entidade JPA.
     */
    public OrcamentoJpaEntity toJpaEntity(Orcamento orcamento) {
        if (orcamento == null) {
            return null;
        }
        
        return new OrcamentoJpaEntity(
            orcamento.getId().valor(),
            orcamento.getUsuarioId().valor(),
            orcamento.getCategoria().nome(),
            orcamento.getLimite().quantia(),
            mapPeriodoToEnum(orcamento.getPeriodo()),
            orcamento.getGastoAtual().quantia(),
            mapStatusToEnum(orcamento.getStatus()),
            orcamento.getInicioPeriodo(),
            orcamento.getFimPeriodo(),
            orcamento.getCriadoEm(),
            orcamento.getAtualizadoEm()
        );
    }
    
    /**
     * Converte entidade JPA para entidade de domínio.
     */
    public Orcamento toDomain(OrcamentoJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        
        return new Orcamento(
            new OrcamentoId(jpaEntity.getId()),
            new UsuarioId(jpaEntity.getUsuarioId()),
            new Categoria(jpaEntity.getCategoria(), TipoCategoria.DESPESA), // Orçamentos são sempre para despesas
            new Valor(jpaEntity.getLimite(), Moeda.BRL), // Assumindo BRL por padrão
            mapEnumToPeriodo(jpaEntity.getPeriodo()),
            new Valor(jpaEntity.getGastoAtual(), Moeda.BRL),
            mapEnumToStatus(jpaEntity.getStatus()),
            jpaEntity.getInicioPeriodo(),
            jpaEntity.getFimPeriodo(),
            jpaEntity.getCriadoEm(),
            jpaEntity.getAtualizadoEm()
        );
    }
    
    /**
     * Atualiza entidade JPA com dados da entidade de domínio.
     */
    public void updateJpaEntity(OrcamentoJpaEntity jpaEntity, Orcamento orcamento) {
        if (jpaEntity == null || orcamento == null) {
            return;
        }
        
        jpaEntity.setCategoria(orcamento.getCategoria().nome());
        jpaEntity.setLimite(orcamento.getLimite().quantia());
        jpaEntity.setPeriodo(mapPeriodoToEnum(orcamento.getPeriodo()));
        jpaEntity.setGastoAtual(orcamento.getGastoAtual().quantia());
        jpaEntity.setStatus(mapStatusToEnum(orcamento.getStatus()));
        jpaEntity.setAtualizadoEm(orcamento.getAtualizadoEm());
    }
    
    /**
     * Mapeia PeriodoOrcamento do domínio para enum JPA.
     */
    private OrcamentoJpaEntity.PeriodoOrcamentoEnum mapPeriodoToEnum(PeriodoOrcamento periodo) {
        return switch (periodo) {
            case MENSAL -> OrcamentoJpaEntity.PeriodoOrcamentoEnum.MENSAL;
            case TRIMESTRAL -> OrcamentoJpaEntity.PeriodoOrcamentoEnum.TRIMESTRAL;
            case ANUAL -> OrcamentoJpaEntity.PeriodoOrcamentoEnum.ANUAL;
        };
    }
    
    /**
     * Mapeia enum JPA para PeriodoOrcamento do domínio.
     */
    private PeriodoOrcamento mapEnumToPeriodo(OrcamentoJpaEntity.PeriodoOrcamentoEnum periodoEnum) {
        return switch (periodoEnum) {
            case MENSAL -> PeriodoOrcamento.MENSAL;
            case TRIMESTRAL -> PeriodoOrcamento.TRIMESTRAL;
            case ANUAL -> PeriodoOrcamento.ANUAL;
        };
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
    
    /**
     * Mapeia enum JPA para StatusOrcamento do domínio.
     */
    private StatusOrcamento mapEnumToStatus(OrcamentoJpaEntity.StatusOrcamentoEnum statusEnum) {
        return switch (statusEnum) {
            case ATIVO -> StatusOrcamento.ATIVO;
            case PROXIMO_LIMITE -> StatusOrcamento.PROXIMO_LIMITE;
            case EXCEDIDO -> StatusOrcamento.EXCEDIDO;
            case ARQUIVADO -> StatusOrcamento.ARQUIVADO;
        };
    }
}