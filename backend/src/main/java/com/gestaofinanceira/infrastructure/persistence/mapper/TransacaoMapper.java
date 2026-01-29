package com.gestaofinanceira.infrastructure.persistence.mapper;

import com.gestaofinanceira.domain.entities.Transacao;
import com.gestaofinanceira.domain.valueobjects.*;
import com.gestaofinanceira.infrastructure.persistence.entity.TransacaoJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para conversão entre entidade de domínio Transacao e entidade JPA TransacaoJpaEntity.
 */
@Component
public class TransacaoMapper {
    
    /**
     * Converte entidade de domínio para entidade JPA.
     */
    public TransacaoJpaEntity toJpaEntity(Transacao transacao) {
        if (transacao == null) {
            return null;
        }
        
        return new TransacaoJpaEntity(
            transacao.getId().valor(),
            transacao.getUsuarioId().valor(),
            transacao.getValor().quantia(),
            transacao.getValor().moeda().getCodigo(),
            transacao.getDescricao().valor(),
            transacao.getCategoria().nome(),
            mapTipoToEnum(transacao.getTipo()),
            transacao.getData(),
            transacao.getCriadoEm(),
            transacao.getAtualizadoEm(),
            transacao.isAtiva()
        );
    }
    
    /**
     * Converte entidade JPA para entidade de domínio.
     */
    public Transacao toDomain(TransacaoJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        
        return new Transacao(
            new TransacaoId(jpaEntity.getId()),
            new UsuarioId(jpaEntity.getUsuarioId()),
            new Valor(jpaEntity.getValor(), Moeda.fromCodigo(jpaEntity.getMoeda())),
            new Descricao(jpaEntity.getDescricao()),
            new Categoria(jpaEntity.getCategoria(), mapEnumToTipoCategoria(jpaEntity.getTipo())),
            jpaEntity.getData(),
            mapEnumToTipo(jpaEntity.getTipo()),
            jpaEntity.getCriadoEm(),
            jpaEntity.getAtualizadoEm(),
            jpaEntity.getAtiva()
        );
    }
    
    /**
     * Atualiza entidade JPA com dados da entidade de domínio.
     */
    public void updateJpaEntity(TransacaoJpaEntity jpaEntity, Transacao transacao) {
        if (jpaEntity == null || transacao == null) {
            return;
        }
        
        jpaEntity.setValor(transacao.getValor().quantia());
        jpaEntity.setMoeda(transacao.getValor().moeda().getCodigo());
        jpaEntity.setDescricao(transacao.getDescricao().valor());
        jpaEntity.setCategoria(transacao.getCategoria().nome());
        jpaEntity.setTipo(mapTipoToEnum(transacao.getTipo()));
        jpaEntity.setAtiva(transacao.isAtiva());
        jpaEntity.setAtualizadoEm(transacao.getAtualizadoEm());
    }
    
    /**
     * Mapeia TipoTransacao do domínio para enum JPA.
     */
    private TransacaoJpaEntity.TipoTransacaoEnum mapTipoToEnum(TipoTransacao tipo) {
        return switch (tipo) {
            case RECEITA -> TransacaoJpaEntity.TipoTransacaoEnum.RECEITA;
            case DESPESA -> TransacaoJpaEntity.TipoTransacaoEnum.DESPESA;
        };
    }
    
    /**
     * Mapeia enum JPA para TipoTransacao do domínio.
     */
    private TipoTransacao mapEnumToTipo(TransacaoJpaEntity.TipoTransacaoEnum tipoEnum) {
        return switch (tipoEnum) {
            case RECEITA -> TipoTransacao.RECEITA;
            case DESPESA -> TipoTransacao.DESPESA;
        };
    }
    
    /**
     * Mapeia enum JPA para TipoCategoria do domínio baseado no tipo da transação.
     */
    private TipoCategoria mapEnumToTipoCategoria(TransacaoJpaEntity.TipoTransacaoEnum tipoEnum) {
        return switch (tipoEnum) {
            case RECEITA -> TipoCategoria.RECEITA;
            case DESPESA -> TipoCategoria.DESPESA;
        };
    }
}