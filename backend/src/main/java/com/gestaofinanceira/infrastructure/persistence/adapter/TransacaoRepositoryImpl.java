package com.gestaofinanceira.infrastructure.persistence.adapter;

import com.gestaofinanceira.application.ports.repository.TransacaoRepository;
import com.gestaofinanceira.domain.entities.Transacao;
import com.gestaofinanceira.domain.valueobjects.TransacaoId;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;
import com.gestaofinanceira.infrastructure.persistence.entity.TransacaoJpaEntity;
import com.gestaofinanceira.infrastructure.persistence.mapper.TransacaoMapper;
import com.gestaofinanceira.infrastructure.persistence.repository.TransacaoJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementação do repositório de transações usando JPA.
 * Adapta as operações de domínio para operações JPA.
 */
@Repository
@Transactional
public class TransacaoRepositoryImpl implements TransacaoRepository {
    
    private final TransacaoJpaRepository jpaRepository;
    private final TransacaoMapper mapper;
    
    public TransacaoRepositoryImpl(TransacaoJpaRepository jpaRepository, TransacaoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public Transacao salvar(Transacao transacao) {
        TransacaoJpaEntity jpaEntity = mapper.toJpaEntity(transacao);
        TransacaoJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Transacao> buscarPorId(TransacaoId id) {
        return jpaRepository.findById(id.valor())
            .map(mapper::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Transacao> buscarPorUsuarioEPeriodo(UsuarioId usuarioId, LocalDate dataInicio, LocalDate dataFim) {
        return jpaRepository.findByUsuarioIdAndDataBetweenAndAtivaTrue(usuarioId.valor(), dataInicio, dataFim)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Transacao> buscarPorUsuarioECategoria(UsuarioId usuarioId, String categoria) {
        return jpaRepository.findByUsuarioIdAndAtivaTrue(usuarioId.valor())
            .stream()
            .filter(t -> t.getCategoria().equals(categoria))
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Transacao> buscarPorUsuarioCategoriaEPeriodo(UsuarioId usuarioId, String categoria, 
                                                            LocalDate dataInicio, LocalDate dataFim) {
        return jpaRepository.findByUsuarioIdAndCategoriaAndDataBetweenAndAtivaTrue(
                usuarioId.valor(), categoria, dataInicio, dataFim)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Transacao> buscarRecentes(UsuarioId usuarioId, int limite) {
        PageRequest pageRequest = PageRequest.of(0, limite);
        return jpaRepository.findTransacoesRecentes(usuarioId.valor(), pageRequest)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoAtual(UsuarioId usuarioId) {
        BigDecimal saldo = jpaRepository.calcularSaldoTotal(usuarioId.valor());
        return saldo != null ? saldo : BigDecimal.ZERO;
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularReceitasPeriodo(UsuarioId usuarioId, LocalDate dataInicio, LocalDate dataFim) {
        BigDecimal receitas = jpaRepository.calcularReceitasPeriodo(usuarioId.valor(), dataInicio, dataFim);
        return receitas != null ? receitas : BigDecimal.ZERO;
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularDespesasPeriodo(UsuarioId usuarioId, LocalDate dataInicio, LocalDate dataFim) {
        BigDecimal despesas = jpaRepository.calcularDespesasPeriodo(usuarioId.valor(), dataInicio, dataFim);
        return despesas != null ? despesas : BigDecimal.ZERO;
    }
    
    @Override
    public Transacao atualizar(Transacao transacao) {
        Optional<TransacaoJpaEntity> existingEntity = jpaRepository.findById(transacao.getId().valor());
        
        if (existingEntity.isEmpty()) {
            throw new IllegalArgumentException("Transação não encontrada para atualização: " + transacao.getId());
        }
        
        TransacaoJpaEntity jpaEntity = existingEntity.get();
        mapper.updateJpaEntity(jpaEntity, transacao);
        
        TransacaoJpaEntity updatedEntity = jpaRepository.save(jpaEntity);
        return mapper.toDomain(updatedEntity);
    }
    
    @Override
    public void remover(TransacaoId id) {
        Optional<TransacaoJpaEntity> existingEntity = jpaRepository.findById(id.valor());
        
        if (existingEntity.isPresent()) {
            TransacaoJpaEntity jpaEntity = existingEntity.get();
            jpaEntity.setAtiva(false); // Soft delete
            jpaRepository.save(jpaEntity);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Transacao> buscarSimilares(UsuarioId usuarioId, BigDecimal valor, LocalDate data, String descricao) {
        // Busca transações com valor similar (±10%), data próxima (±3 dias) e descrição similar
        LocalDate dataInicio = data.minusDays(3);
        LocalDate dataFim = data.plusDays(3);
        
        return jpaRepository.findByUsuarioIdAndDataBetweenAndAtivaTrue(usuarioId.valor(), dataInicio, dataFim)
            .stream()
            .filter(t -> {
                // Verifica se o valor está dentro da margem de 10%
                BigDecimal diferenca = t.getValor().subtract(valor).abs();
                BigDecimal margem = valor.multiply(new BigDecimal("0.1"));
                boolean valorSimilar = diferenca.compareTo(margem) <= 0;
                
                // Verifica se a descrição é similar (contém palavras em comum)
                boolean descricaoSimilar = temPalavrasEmComum(t.getDescricao(), descricao);
                
                return valorSimilar && descricaoSimilar;
            })
            .map(mapper::toDomain)
            .toList();
    }
    
    /**
     * Verifica se duas descrições têm palavras em comum.
     */
    private boolean temPalavrasEmComum(String desc1, String desc2) {
        if (desc1 == null || desc2 == null) {
            return false;
        }
        
        String[] palavras1 = desc1.toLowerCase().split("\\s+");
        String[] palavras2 = desc2.toLowerCase().split("\\s+");
        
        for (String palavra1 : palavras1) {
            for (String palavra2 : palavras2) {
                if (palavra1.length() > 3 && palavra1.equals(palavra2)) {
                    return true;
                }
            }
        }
        
        return false;
    }
}