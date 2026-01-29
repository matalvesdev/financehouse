package com.gestaofinanceira.application.usecases.transacao;

import com.gestaofinanceira.application.ports.repository.MetaFinanceiraRepository;
import com.gestaofinanceira.application.ports.repository.OrcamentoRepository;
import com.gestaofinanceira.application.ports.repository.TransacaoRepository;
import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
import com.gestaofinanceira.domain.entities.MetaFinanceira;
import com.gestaofinanceira.domain.entities.Orcamento;
import com.gestaofinanceira.domain.entities.Transacao;
import com.gestaofinanceira.domain.entities.Usuario;
import com.gestaofinanceira.domain.valueobjects.StatusMeta;
import com.gestaofinanceira.domain.valueobjects.TransacaoId;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Use Case para exclusão de transações financeiras.
 * 
 * Implementa o requirement 3.3: "WHEN a user deletes a transaction, 
 * THE Sistema SHALL require confirmation and soft-delete the record"
 * 
 * Responsabilidades:
 * - Validar propriedade da transação
 * - Reverter impactos nos orçamentos e metas
 * - Realizar soft delete (desativar transação)
 * - Preservar audit trail
 * - Manter integridade dos dados financeiros
 */
@Service
@Transactional
public class ExcluirTransacaoUseCase {
    
    private final TransacaoRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final MetaFinanceiraRepository metaFinanceiraRepository;
    
    public ExcluirTransacaoUseCase(TransacaoRepository transacaoRepository,
                                   UsuarioRepository usuarioRepository,
                                   OrcamentoRepository orcamentoRepository,
                                   MetaFinanceiraRepository metaFinanceiraRepository) {
        this.transacaoRepository = Objects.requireNonNull(transacaoRepository, 
            "TransacaoRepository não pode ser nulo");
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, 
            "UsuarioRepository não pode ser nulo");
        this.orcamentoRepository = Objects.requireNonNull(orcamentoRepository, 
            "OrcamentoRepository não pode ser nulo");
        this.metaFinanceiraRepository = Objects.requireNonNull(metaFinanceiraRepository, 
            "MetaFinanceiraRepository não pode ser nulo");
    }
    
    /**
     * Executa a exclusão (soft delete) de uma transação.
     * 
     * @param transacaoId ID da transação a ser excluída
     * @param usuarioId ID do usuário proprietário
     * @throws IllegalArgumentException se os parâmetros forem inválidos
     * @throws IllegalStateException se a transação não existir ou não pertencer ao usuário
     */
    public void executar(TransacaoId transacaoId, UsuarioId usuarioId) {
        Objects.requireNonNull(transacaoId, "ID da transação não pode ser nulo");
        Objects.requireNonNull(usuarioId, "ID do usuário não pode ser nulo");
        
        // 1. Validar usuário
        validarUsuario(usuarioId);
        
        // 2. Buscar e validar transação
        Transacao transacao = buscarEValidarTransacao(transacaoId, usuarioId);
        
        // 3. Reverter impactos nos orçamentos e metas
        reverterImpactos(transacao);
        
        // 4. Desativar transação (soft delete)
        transacao.desativar();
        
        // 5. Salvar transação desativada
        transacaoRepository.atualizar(transacao);
    }
    
    /**
     * Executa a exclusão permanente de uma transação (hard delete).
     * 
     * ATENÇÃO: Esta operação é irreversível e deve ser usada apenas
     * em casos específicos como limpeza de dados de teste.
     * 
     * @param transacaoId ID da transação a ser excluída permanentemente
     * @param usuarioId ID do usuário proprietário
     */
    public void excluirPermanentemente(TransacaoId transacaoId, UsuarioId usuarioId) {
        Objects.requireNonNull(transacaoId, "ID da transação não pode ser nulo");
        Objects.requireNonNull(usuarioId, "ID do usuário não pode ser nulo");
        
        // 1. Validar usuário
        validarUsuario(usuarioId);
        
        // 2. Buscar e validar transação
        Transacao transacao = buscarTransacao(transacaoId, usuarioId);
        
        // 3. Reverter impactos se a transação estiver ativa
        if (transacao.isAtiva()) {
            reverterImpactos(transacao);
        }
        
        // 4. Remover transação permanentemente
        transacaoRepository.remover(transacaoId);
    }
    
    /**
     * Reativa uma transação previamente desativada.
     * 
     * @param transacaoId ID da transação a ser reativada
     * @param usuarioId ID do usuário proprietário
     * @return a transação reativada
     */
    public Transacao reativar(TransacaoId transacaoId, UsuarioId usuarioId) {
        Objects.requireNonNull(transacaoId, "ID da transação não pode ser nulo");
        Objects.requireNonNull(usuarioId, "ID do usuário não pode ser nulo");
        
        // 1. Validar usuário
        validarUsuario(usuarioId);
        
        // 2. Buscar e validar transação
        Transacao transacao = buscarTransacao(transacaoId, usuarioId);
        
        if (transacao.isAtiva()) {
            throw new IllegalStateException("Transação já está ativa: " + transacaoId);
        }
        
        // 3. Reativar transação
        transacao.reativar();
        
        // 4. Aplicar impactos novamente
        aplicarImpactos(transacao);
        
        // 5. Salvar transação reativada
        return transacaoRepository.atualizar(transacao);
    }
    
    /**
     * Valida se o usuário existe e está ativo.
     */
    private void validarUsuario(UsuarioId usuarioId) {
        Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
            .orElseThrow(() -> new IllegalStateException(
                "Usuário não encontrado: " + usuarioId));
        
        if (!usuario.isAtivo()) {
            throw new IllegalStateException(
                "Usuário inativo: " + usuarioId);
        }
    }
    
    /**
     * Busca e valida se a transação existe, está ativa e pertence ao usuário.
     */
    private Transacao buscarEValidarTransacao(TransacaoId transacaoId, UsuarioId usuarioId) {
        Transacao transacao = buscarTransacao(transacaoId, usuarioId);
        
        if (!transacao.isAtiva()) {
            throw new IllegalStateException(
                "Transação já está inativa: " + transacaoId);
        }
        
        return transacao;
    }
    
    /**
     * Busca e valida se a transação existe e pertence ao usuário.
     */
    private Transacao buscarTransacao(TransacaoId transacaoId, UsuarioId usuarioId) {
        Transacao transacao = transacaoRepository.buscarPorId(transacaoId)
            .orElseThrow(() -> new IllegalStateException(
                "Transação não encontrada: " + transacaoId));
        
        if (!transacao.getUsuarioId().equals(usuarioId)) {
            throw new IllegalStateException(
                "Transação não pertence ao usuário: " + transacaoId);
        }
        
        return transacao;
    }
    
    /**
     * Reverte os impactos da transação nos orçamentos e metas.
     */
    private void reverterImpactos(Transacao transacao) {
        // Reverter impacto no orçamento (apenas para despesas)
        if (transacao.afetaOrcamento()) {
            reverterImpactoOrcamento(transacao);
        }
        
        // Reverter impacto nas metas (apenas para receitas)
        if (transacao.getTipo().ehReceita()) {
            reverterImpactoMetas(transacao);
        }
    }
    
    /**
     * Aplica os impactos da transação nos orçamentos e metas (usado na reativação).
     */
    private void aplicarImpactos(Transacao transacao) {
        // Aplicar impacto no orçamento (apenas para despesas)
        if (transacao.afetaOrcamento()) {
            aplicarImpactoOrcamento(transacao);
        }
        
        // Aplicar impacto nas metas (apenas para receitas)
        if (transacao.getTipo().ehReceita()) {
            aplicarImpactoMetas(transacao);
        }
    }
    
    /**
     * Reverte o impacto da transação no orçamento.
     */
    private void reverterImpactoOrcamento(Transacao transacao) {
        Optional<Orcamento> orcamentoOpt = orcamentoRepository
            .buscarAtivoPorUsuarioECategoria(
                transacao.getUsuarioId(), 
                transacao.getCategoria().nome()
            );
        
        if (orcamentoOpt.isPresent()) {
            Orcamento orcamento = orcamentoOpt.get();
            orcamento.removerGasto(transacao.getValor());
            orcamentoRepository.atualizar(orcamento);
        }
    }
    
    /**
     * Aplica o impacto da transação no orçamento.
     */
    private void aplicarImpactoOrcamento(Transacao transacao) {
        Optional<Orcamento> orcamentoOpt = orcamentoRepository
            .buscarAtivoPorUsuarioECategoria(
                transacao.getUsuarioId(), 
                transacao.getCategoria().nome()
            );
        
        if (orcamentoOpt.isPresent()) {
            Orcamento orcamento = orcamentoOpt.get();
            orcamento.adicionarGasto(transacao.getValor());
            orcamentoRepository.atualizar(orcamento);
        }
    }
    
    /**
     * Reverte o impacto da transação nas metas.
     */
    private void reverterImpactoMetas(Transacao transacao) {
        if (contribuiParaMetas(transacao)) {
            var metasAtivas = metaFinanceiraRepository
                .buscarAtivasPorUsuario(transacao.getUsuarioId());
            
            for (MetaFinanceira meta : metasAtivas) {
                if (meta.getStatus() == StatusMeta.ATIVA) {
                    meta.removerProgresso(transacao.getValor());
                    metaFinanceiraRepository.atualizar(meta);
                }
            }
        }
    }
    
    /**
     * Aplica o impacto da transação nas metas.
     */
    private void aplicarImpactoMetas(Transacao transacao) {
        if (contribuiParaMetas(transacao)) {
            var metasAtivas = metaFinanceiraRepository
                .buscarAtivasPorUsuario(transacao.getUsuarioId());
            
            for (MetaFinanceira meta : metasAtivas) {
                if (meta.getStatus() == StatusMeta.ATIVA) {
                    meta.adicionarProgresso(transacao.getValor());
                    metaFinanceiraRepository.atualizar(meta);
                }
            }
        }
    }
    
    /**
     * Verifica se a transação contribui para metas financeiras.
     */
    private boolean contribuiParaMetas(Transacao transacao) {
        return transacao.getCategoria().nome().equals("INVESTIMENTOS") ||
               transacao.getDescricao().contem("poupança") ||
               transacao.getDescricao().contem("meta") ||
               transacao.getDescricao().contem("reserva");
    }
}