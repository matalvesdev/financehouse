package com.gestaofinanceira.application.usecases.transacao;

import com.gestaofinanceira.application.dto.command.ComandoAtualizarTransacao;
import com.gestaofinanceira.application.ports.repository.MetaFinanceiraRepository;
import com.gestaofinanceira.application.ports.repository.OrcamentoRepository;
import com.gestaofinanceira.application.ports.repository.TransacaoRepository;
import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
import com.gestaofinanceira.application.ports.service.NotificacaoPort;
import com.gestaofinanceira.domain.entities.MetaFinanceira;
import com.gestaofinanceira.domain.entities.Orcamento;
import com.gestaofinanceira.domain.entities.Transacao;
import com.gestaofinanceira.domain.entities.Usuario;
import com.gestaofinanceira.domain.valueobjects.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Use Case para atualização de transações financeiras.
 * 
 * Implementa o requirement 3.2: "WHEN a user updates an existing transaction, 
 * THE Sistema SHALL preserve audit trail and update the record"
 * 
 * Responsabilidades:
 * - Validar propriedade da transação
 * - Preservar audit trail (data de atualização)
 * - Reverter impactos da transação original
 * - Aplicar novos impactos nos orçamentos e metas
 * - Atualizar a transação
 * - Enviar notificações quando necessário
 */
@Service
@Transactional
public class AtualizarTransacaoUseCase {
    
    private final TransacaoRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final MetaFinanceiraRepository metaFinanceiraRepository;
    private final NotificacaoPort notificacaoPort;
    
    public AtualizarTransacaoUseCase(TransacaoRepository transacaoRepository,
                                     UsuarioRepository usuarioRepository,
                                     OrcamentoRepository orcamentoRepository,
                                     MetaFinanceiraRepository metaFinanceiraRepository,
                                     NotificacaoPort notificacaoPort) {
        this.transacaoRepository = Objects.requireNonNull(transacaoRepository, 
            "TransacaoRepository não pode ser nulo");
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, 
            "UsuarioRepository não pode ser nulo");
        this.orcamentoRepository = Objects.requireNonNull(orcamentoRepository, 
            "OrcamentoRepository não pode ser nulo");
        this.metaFinanceiraRepository = Objects.requireNonNull(metaFinanceiraRepository, 
            "MetaFinanceiraRepository não pode ser nulo");
        this.notificacaoPort = Objects.requireNonNull(notificacaoPort, 
            "NotificacaoPort não pode ser nulo");
    }
    
    /**
     * Executa a atualização de uma transação existente.
     * 
     * @param comando dados para atualização da transação
     * @return a transação atualizada
     * @throws IllegalArgumentException se os dados forem inválidos
     * @throws IllegalStateException se a transação não existir ou não pertencer ao usuário
     */
    public Transacao executar(ComandoAtualizarTransacao comando) {
        Objects.requireNonNull(comando, "Comando não pode ser nulo");
        
        // 1. Validar usuário
        Usuario usuario = validarUsuario(comando.usuarioId());
        
        // 2. Buscar e validar transação existente
        Transacao transacaoOriginal = buscarEValidarTransacao(comando.transacaoId(), comando.usuarioId());
        
        // 3. Reverter impactos da transação original
        reverterImpactosOriginais(transacaoOriginal);
        
        // 4. Criar novos value objects
        Valor novoValor = Valor.reais(comando.valor());
        Descricao novaDescricao = new Descricao(comando.descricao());
        Categoria novaCategoria = criarCategoria(comando.categoria(), comando.tipo());
        
        // 5. Atualizar campos da transação (preserva audit trail)
        transacaoOriginal.atualizarValor(novoValor);
        transacaoOriginal.atualizarDescricao(novaDescricao);
        transacaoOriginal.atualizarCategoria(novaCategoria);
        
        // 6. Salvar transação atualizada
        Transacao transacaoAtualizada = transacaoRepository.atualizar(transacaoOriginal);
        
        // 7. Aplicar novos impactos
        if (transacaoAtualizada.afetaOrcamento()) {
            atualizarOrcamentoAfetado(transacaoAtualizada);
        }
        
        if (transacaoAtualizada.getTipo().ehReceita()) {
            atualizarMetasRelacionadas(transacaoAtualizada);
        }
        
        return transacaoAtualizada;
    }
    
    /**
     * Valida se o usuário existe e está ativo.
     */
    private Usuario validarUsuario(UsuarioId usuarioId) {
        return usuarioRepository.buscarPorId(usuarioId)
            .filter(Usuario::isAtivo)
            .orElseThrow(() -> new IllegalStateException(
                "Usuário não encontrado ou inativo: " + usuarioId));
    }
    
    /**
     * Busca e valida se a transação existe e pertence ao usuário.
     */
    private Transacao buscarEValidarTransacao(TransacaoId transacaoId, UsuarioId usuarioId) {
        Transacao transacao = transacaoRepository.buscarPorId(transacaoId)
            .orElseThrow(() -> new IllegalStateException(
                "Transação não encontrada: " + transacaoId));
        
        if (!transacao.getUsuarioId().equals(usuarioId)) {
            throw new IllegalStateException(
                "Transação não pertence ao usuário: " + transacaoId);
        }
        
        if (!transacao.isAtiva()) {
            throw new IllegalStateException(
                "Não é possível atualizar transação inativa: " + transacaoId);
        }
        
        return transacao;
    }
    
    /**
     * Cria a categoria apropriada baseada no nome e tipo.
     */
    private Categoria criarCategoria(String nomeCategoria, TipoTransacao tipo) {
        // Primeiro tenta buscar categoria predefinida
        Categoria categoriaPredefinida = Categoria.buscarPredefinida(nomeCategoria);
        if (categoriaPredefinida != null) {
            return categoriaPredefinida;
        }
        
        // Se não encontrou, cria categoria personalizada
        return tipo.ehReceita() 
            ? Categoria.receitaPersonalizada(nomeCategoria)
            : Categoria.despesaPersonalizada(nomeCategoria);
    }
    
    /**
     * Reverte os impactos da transação original nos orçamentos e metas.
     */
    private void reverterImpactosOriginais(Transacao transacaoOriginal) {
        // Reverter impacto no orçamento (apenas para despesas)
        if (transacaoOriginal.afetaOrcamento()) {
            reverterImpactoOrcamento(transacaoOriginal);
        }
        
        // Reverter impacto nas metas (apenas para receitas)
        if (transacaoOriginal.getTipo().ehReceita()) {
            reverterImpactoMetas(transacaoOriginal);
        }
    }
    
    /**
     * Reverte o impacto da transação original no orçamento.
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
     * Reverte o impacto da transação original nas metas.
     */
    private void reverterImpactoMetas(Transacao transacao) {
        if (transacao.getCategoria().equals(Categoria.INVESTIMENTOS) ||
            transacao.getDescricao().contem("poupança") ||
            transacao.getDescricao().contem("meta") ||
            transacao.getDescricao().contem("reserva")) {
            
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
     * Atualiza o orçamento afetado pela transação atualizada.
     */
    private void atualizarOrcamentoAfetado(Transacao transacao) {
        Optional<Orcamento> orcamentoOpt = orcamentoRepository
            .buscarAtivoPorUsuarioECategoria(
                transacao.getUsuarioId(), 
                transacao.getCategoria().nome()
            );
        
        if (orcamentoOpt.isPresent()) {
            Orcamento orcamento = orcamentoOpt.get();
            orcamento.adicionarGasto(transacao.getValor());
            orcamentoRepository.atualizar(orcamento);
            
            // Envia notificações se necessário
            if (orcamento.estaProximoDoLimite()) {
                notificacaoPort.notificarOrcamentoProximoLimite(orcamento);
            }
            
            if (orcamento.excedeuLimite()) {
                notificacaoPort.notificarOrcamentoExcedido(orcamento);
            }
        }
    }
    
    /**
     * Atualiza metas financeiras relacionadas à transação atualizada.
     */
    private void atualizarMetasRelacionadas(Transacao transacao) {
        if (transacao.getCategoria().equals(Categoria.INVESTIMENTOS) ||
            transacao.getDescricao().contem("poupança") ||
            transacao.getDescricao().contem("meta") ||
            transacao.getDescricao().contem("reserva")) {
            
            var metasAtivas = metaFinanceiraRepository
                .buscarAtivasPorUsuario(transacao.getUsuarioId());
            
            for (MetaFinanceira meta : metasAtivas) {
                if (meta.getStatus() == StatusMeta.ATIVA) {
                    meta.adicionarProgresso(transacao.getValor());
                    metaFinanceiraRepository.atualizar(meta);
                    
                    if (meta.foiAlcancada()) {
                        notificacaoPort.notificarMetaAlcancada(meta);
                    }
                }
            }
        }
    }
}