package com.gestaofinanceira.application.usecases.orcamento;

import com.gestaofinanceira.application.dto.command.ComandoCriarOrcamento;
import com.gestaofinanceira.application.ports.repository.OrcamentoRepository;
import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
import com.gestaofinanceira.domain.entities.Orcamento;
import com.gestaofinanceira.domain.entities.Usuario;
import com.gestaofinanceira.domain.valueobjects.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Use Case para criação de orçamentos.
 * 
 * Implementa o requirement 5.1: "WHEN a user creates a budget, 
 * THE Sistema SHALL validate the budget parameters (category, amount, period)"
 * 
 * Responsabilidades:
 * - Validar dados do orçamento
 * - Verificar se já existe orçamento ativo para a categoria
 * - Criar entidade Orcamento
 * - Salvar no repositório
 */
@Service
@Transactional
public class CriarOrcamentoUseCase {
    
    private final OrcamentoRepository orcamentoRepository;
    private final UsuarioRepository usuarioRepository;
    
    public CriarOrcamentoUseCase(OrcamentoRepository orcamentoRepository,
                                 UsuarioRepository usuarioRepository) {
        this.orcamentoRepository = Objects.requireNonNull(orcamentoRepository);
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository);
    }
    
    /**
     * Executa a criação de um novo orçamento.
     * 
     * @param comando dados para criação do orçamento
     * @return o orçamento criado
     * @throws IllegalArgumentException se os dados forem inválidos
     * @throws IllegalStateException se já existir orçamento ativo para a categoria
     */
    public Orcamento executar(ComandoCriarOrcamento comando) {
        Objects.requireNonNull(comando, "Comando não pode ser nulo");
        
        // 1. Validar usuário
        Usuario usuario = validarUsuario(comando.usuarioId());
        
        // 2. Verificar se já existe orçamento ativo para a categoria
        verificarOrcamentoExistente(comando.usuarioId(), comando.categoria());
        
        // 3. Criar value objects
        Categoria categoria = criarCategoria(comando.categoria());
        Valor limite = Valor.reais(comando.limite());
        
        // 4. Criar orçamento
        Orcamento orcamento = Orcamento.criar(
            comando.usuarioId(),
            categoria,
            limite,
            comando.periodo(),
            comando.inicioVigencia()
        );
        
        // 5. Salvar orçamento
        return orcamentoRepository.salvar(orcamento);
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
     * Verifica se já existe orçamento ativo para a categoria.
     */
    private void verificarOrcamentoExistente(UsuarioId usuarioId, String nomeCategoria) {
        Optional<Orcamento> orcamentoExistente = orcamentoRepository.buscarAtivoPorUsuarioECategoria(usuarioId, nomeCategoria);
        if (orcamentoExistente.isPresent()) {
            throw new IllegalStateException(
                "Já existe um orçamento ativo para a categoria: " + nomeCategoria);
        }
    }
    
    /**
     * Cria a categoria apropriada.
     */
    private Categoria criarCategoria(String nomeCategoria) {
        // Primeiro tenta buscar categoria predefinida
        Categoria categoriaPredefinida = Categoria.buscarPredefinida(nomeCategoria);
        if (categoriaPredefinida != null) {
            return categoriaPredefinida;
        }
        
        // Se não encontrou, cria categoria personalizada para despesas
        return Categoria.despesaPersonalizada(nomeCategoria);
    }
}