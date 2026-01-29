package com.gestaofinanceira.application.usecases.transacao;

import com.gestaofinanceira.application.dto.command.ComandoCriarTransacao;
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
 * Use Case para criação de transações financeiras.
 * 
 * Implementa o requirement 3.1: "WHEN a user creates a new transaction, 
 * THE Sistema SHALL validate and store the transaction data"
 * 
 * Responsabilidades:
 * - Validar dados da transação
 * - Criar entidade Transacao
 * - Atualizar orçamentos afetados (requirement 3.7)
 * - Atualizar metas relacionadas
 * - Salvar transação
 * - Enviar notificações quando necessário
 */
@Service
@Transactional
public class CriarTransacaoUseCase {
    
    private final TransacaoRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final MetaFinanceiraRepository metaFinanceiraRepository;
    private final NotificacaoPort notificacaoPort;
    
    public CriarTransacaoUseCase(TransacaoRepository transacaoRepository,
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
     * Executa a criação de uma nova transação.
     * 
     * @param comando dados para criação da transação
     * @return a transação criada
     * @throws IllegalArgumentException se os dados forem inválidos
     * @throws IllegalStateException se o usuário não existir ou não puder criar transações
     */
    public Transacao executar(ComandoCriarTransacao comando) {
        Objects.requireNonNull(comando, "Comando não pode ser nulo");
        
        // 1. Validar usuário
        Usuario usuario = validarUsuario(comando.usuarioId());
        
        // 2. Criar value objects
        Valor valor = Valor.reais(comando.valor());
        Descricao descricao = new Descricao(comando.descricao());
        Categoria categoria = criarCategoria(comando.categoria(), comando.tipo());
        
        // 3. Criar transação
        Transacao transacao = Transacao.criar(
            comando.usuarioId(),
            valor,
            descricao,
            categoria,
            comando.data(),
            comando.tipo()
        );
        
        // 4. Salvar transação
        Transacao transacaoSalva = transacaoRepository.salvar(transacao);
        
        // 5. Atualizar orçamentos afetados (apenas para despesas)
        if (transacao.afetaOrcamento()) {
            atualizarOrcamentoAfetado(transacao);
        }
        
        // 6. Atualizar metas relacionadas (apenas para receitas de poupança/investimento)
        if (transacao.getTipo().ehReceita()) {
            atualizarMetasRelacionadas(transacao);
        }
        
        return transacaoSalva;
    }
    
    /**
     * Valida se o usuário existe e pode criar transações.
     */
    private Usuario validarUsuario(UsuarioId usuarioId) {
        return usuarioRepository.buscarPorId(usuarioId)
            .filter(Usuario::isAtivo)
            .orElseThrow(() -> new IllegalStateException(
                "Usuário não encontrado ou inativo: " + usuarioId));
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
     * Atualiza o orçamento afetado pela transação de despesa.
     */
    private void atualizarOrcamentoAfetado(Transacao transacao) {
        Optional<Orcamento> orcamentoOpt = orcamentoRepository
            .buscarAtivoPorUsuarioECategoria(
                transacao.getUsuarioId(), 
                transacao.getCategoria().nome()
            );
        
        if (orcamentoOpt.isPresent()) {
            Orcamento orcamento = orcamentoOpt.get();
            
            // Adiciona o gasto ao orçamento
            orcamento.adicionarGasto(transacao.getValor());
            
            // Salva o orçamento atualizado
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
     * Atualiza metas financeiras relacionadas à transação de receita.
     * Considera receitas de categorias específicas como progresso em metas.
     */
    private void atualizarMetasRelacionadas(Transacao transacao) {
        // Apenas receitas de investimento ou poupança contribuem para metas
        if (transacao.getCategoria().equals(Categoria.INVESTIMENTOS) ||
            transacao.getDescricao().contem("poupança") ||
            transacao.getDescricao().contem("meta") ||
            transacao.getDescricao().contem("reserva")) {
            
            // Busca metas ativas do usuário
            var metasAtivas = metaFinanceiraRepository
                .buscarAtivasPorUsuario(transacao.getUsuarioId());
            
            // Para cada meta ativa, adiciona o progresso
            for (MetaFinanceira meta : metasAtivas) {
                if (meta.getStatus() == StatusMeta.ATIVA) {
                    meta.adicionarProgresso(transacao.getValor());
                    metaFinanceiraRepository.atualizar(meta);
                    
                    // Notifica se a meta foi alcançada
                    if (meta.foiAlcancada()) {
                        notificacaoPort.notificarMetaAlcancada(meta);
                    }
                }
            }
        }
    }
}