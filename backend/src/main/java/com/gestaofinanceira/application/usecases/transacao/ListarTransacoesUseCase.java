package com.gestaofinanceira.application.usecases.transacao;

import com.gestaofinanceira.application.dto.response.TransacaoResponse;
import com.gestaofinanceira.application.ports.repository.TransacaoRepository;
import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
import com.gestaofinanceira.domain.entities.Transacao;
import com.gestaofinanceira.domain.entities.Usuario;
import com.gestaofinanceira.domain.valueobjects.TipoTransacao;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Use Case para listagem de transações financeiras.
 * 
 * Implementa o requirement 3.5: "WHEN displaying transactions, 
 * THE Sistema SHALL show them in chronological order with pagination"
 * 
 * Responsabilidades:
 * - Validar usuário
 * - Buscar transações com filtros opcionais
 * - Ordenar transações cronologicamente
 * - Converter para DTOs de resposta
 * - Suportar diferentes tipos de consulta (período, categoria, recentes)
 */
@Service
@Transactional(readOnly = true)
public class ListarTransacoesUseCase {
    
    private final TransacaoRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;
    
    public ListarTransacoesUseCase(TransacaoRepository transacaoRepository,
                                   UsuarioRepository usuarioRepository) {
        this.transacaoRepository = Objects.requireNonNull(transacaoRepository, 
            "TransacaoRepository não pode ser nulo");
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, 
            "UsuarioRepository não pode ser nulo");
    }
    
    /**
     * Lista transações de um usuário por período.
     * 
     * @param usuarioId ID do usuário
     * @param dataInicio data de início do período
     * @param dataFim data de fim do período
     * @return lista de transações no período ordenadas cronologicamente
     */
    public List<TransacaoResponse> listarPorPeriodo(UsuarioId usuarioId, 
                                                    LocalDate dataInicio, 
                                                    LocalDate dataFim) {
        Objects.requireNonNull(usuarioId, "ID do usuário não pode ser nulo");
        Objects.requireNonNull(dataInicio, "Data de início não pode ser nula");
        Objects.requireNonNull(dataFim, "Data de fim não pode ser nula");
        
        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Data de início deve ser anterior à data de fim");
        }
        
        // Validar usuário
        validarUsuario(usuarioId);
        
        // Buscar transações
        List<Transacao> transacoes = transacaoRepository
            .buscarPorUsuarioEPeriodo(usuarioId, dataInicio, dataFim);
        
        // Ordenar cronologicamente (mais recentes primeiro) e converter para DTO
        return transacoes.stream()
            .sorted((t1, t2) -> t2.getData().compareTo(t1.getData()))
            .map(this::converterParaResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista transações de um usuário por categoria.
     * 
     * @param usuarioId ID do usuário
     * @param categoria categoria das transações
     * @return lista de transações da categoria ordenadas cronologicamente
     */
    public List<TransacaoResponse> listarPorCategoria(UsuarioId usuarioId, String categoria) {
        Objects.requireNonNull(usuarioId, "ID do usuário não pode ser nulo");
        Objects.requireNonNull(categoria, "Categoria não pode ser nula");
        
        if (categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("Categoria não pode estar vazia");
        }
        
        // Validar usuário
        validarUsuario(usuarioId);
        
        // Buscar transações
        List<Transacao> transacoes = transacaoRepository
            .buscarPorUsuarioECategoria(usuarioId, categoria.trim().toUpperCase());
        
        // Ordenar cronologicamente (mais recentes primeiro) e converter para DTO
        return transacoes.stream()
            .sorted((t1, t2) -> t2.getData().compareTo(t1.getData()))
            .map(this::converterParaResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista transações de um usuário por categoria e período.
     * 
     * @param usuarioId ID do usuário
     * @param categoria categoria das transações
     * @param dataInicio data de início do período
     * @param dataFim data de fim do período
     * @return lista de transações da categoria no período
     */
    public List<TransacaoResponse> listarPorCategoriaEPeriodo(UsuarioId usuarioId, 
                                                              String categoria,
                                                              LocalDate dataInicio, 
                                                              LocalDate dataFim) {
        Objects.requireNonNull(usuarioId, "ID do usuário não pode ser nulo");
        Objects.requireNonNull(categoria, "Categoria não pode ser nula");
        Objects.requireNonNull(dataInicio, "Data de início não pode ser nula");
        Objects.requireNonNull(dataFim, "Data de fim não pode ser nula");
        
        if (categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("Categoria não pode estar vazia");
        }
        
        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Data de início deve ser anterior à data de fim");
        }
        
        // Validar usuário
        validarUsuario(usuarioId);
        
        // Buscar transações
        List<Transacao> transacoes = transacaoRepository
            .buscarPorUsuarioCategoriaEPeriodo(
                usuarioId, 
                categoria.trim().toUpperCase(), 
                dataInicio, 
                dataFim
            );
        
        // Ordenar cronologicamente (mais recentes primeiro) e converter para DTO
        return transacoes.stream()
            .sorted((t1, t2) -> t2.getData().compareTo(t1.getData()))
            .map(this::converterParaResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista as transações mais recentes de um usuário.
     * 
     * Implementa o requirement 4.6: "THE Sistema SHALL display recent transactions (last 10) on the dashboard"
     * 
     * @param usuarioId ID do usuário
     * @param limite número máximo de transações (padrão: 10)
     * @return lista das transações mais recentes
     */
    public List<TransacaoResponse> listarRecentes(UsuarioId usuarioId, int limite) {
        Objects.requireNonNull(usuarioId, "ID do usuário não pode ser nulo");
        
        if (limite <= 0) {
            throw new IllegalArgumentException("Limite deve ser maior que zero");
        }
        
        if (limite > 100) {
            throw new IllegalArgumentException("Limite não pode ser maior que 100");
        }
        
        // Validar usuário
        validarUsuario(usuarioId);
        
        // Buscar transações recentes
        List<Transacao> transacoes = transacaoRepository.buscarRecentes(usuarioId, limite);
        
        // Converter para DTO (já vem ordenado do repositório)
        return transacoes.stream()
            .map(this::converterParaResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista as transações mais recentes de um usuário (padrão: 10).
     */
    public List<TransacaoResponse> listarRecentes(UsuarioId usuarioId) {
        return listarRecentes(usuarioId, 10);
    }
    
    /**
     * Lista transações de um usuário por tipo (RECEITA ou DESPESA).
     * 
     * @param usuarioId ID do usuário
     * @param tipo tipo da transação
     * @param dataInicio data de início do período
     * @param dataFim data de fim do período
     * @return lista de transações do tipo especificado
     */
    public List<TransacaoResponse> listarPorTipo(UsuarioId usuarioId, 
                                                 TipoTransacao tipo,
                                                 LocalDate dataInicio, 
                                                 LocalDate dataFim) {
        Objects.requireNonNull(usuarioId, "ID do usuário não pode ser nulo");
        Objects.requireNonNull(tipo, "Tipo da transação não pode ser nulo");
        Objects.requireNonNull(dataInicio, "Data de início não pode ser nula");
        Objects.requireNonNull(dataFim, "Data de fim não pode ser nula");
        
        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Data de início deve ser anterior à data de fim");
        }
        
        // Validar usuário
        validarUsuario(usuarioId);
        
        // Buscar transações por período e filtrar por tipo
        List<Transacao> transacoes = transacaoRepository
            .buscarPorUsuarioEPeriodo(usuarioId, dataInicio, dataFim);
        
        // Filtrar por tipo, ordenar cronologicamente e converter para DTO
        return transacoes.stream()
            .filter(t -> t.getTipo() == tipo)
            .sorted((t1, t2) -> t2.getData().compareTo(t1.getData()))
            .map(this::converterParaResponse)
            .collect(Collectors.toList());
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
     * Converte uma entidade Transacao para TransacaoResponse.
     */
    private TransacaoResponse converterParaResponse(Transacao transacao) {
        return new TransacaoResponse(
            transacao.getId().valor().toString(),
            transacao.getValor().quantia(),
            transacao.getValor().moeda().name(),
            transacao.getDescricao().valor(),
            transacao.getCategoria().nome(),
            transacao.getTipo(),
            transacao.getData(),
            transacao.getCriadoEm(),
            transacao.isAtiva()
        );
    }
}