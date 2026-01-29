package com.gestaofinanceira.application.ports.repository;

import com.gestaofinanceira.domain.entities.Transacao;
import com.gestaofinanceira.domain.valueobjects.TransacaoId;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Port para persistência de transações.
 * 
 * Define as operações de persistência necessárias para a entidade Transacao,
 * incluindo consultas específicas para relatórios e cálculos financeiros.
 */
public interface TransacaoRepository {
    
    /**
     * Salva uma transação no repositório.
     * 
     * @param transacao a transação a ser salva
     * @return a transação salva com ID gerado
     */
    Transacao salvar(Transacao transacao);
    
    /**
     * Busca uma transação por ID.
     * 
     * @param id o ID da transação
     * @return Optional contendo a transação se encontrada
     */
    Optional<Transacao> buscarPorId(TransacaoId id);
    
    /**
     * Busca transações de um usuário por período.
     * 
     * @param usuarioId o ID do usuário
     * @param dataInicio data de início do período
     * @param dataFim data de fim do período
     * @return lista de transações no período
     */
    List<Transacao> buscarPorUsuarioEPeriodo(UsuarioId usuarioId, LocalDate dataInicio, LocalDate dataFim);
    
    /**
     * Busca transações de um usuário por categoria.
     * 
     * @param usuarioId o ID do usuário
     * @param categoria a categoria das transações
     * @return lista de transações da categoria
     */
    List<Transacao> buscarPorUsuarioECategoria(UsuarioId usuarioId, String categoria);
    
    /**
     * Busca transações de um usuário por categoria e período.
     * 
     * @param usuarioId o ID do usuário
     * @param categoria a categoria das transações
     * @param dataInicio data de início do período
     * @param dataFim data de fim do período
     * @return lista de transações da categoria no período
     */
    List<Transacao> buscarPorUsuarioCategoriaEPeriodo(
        UsuarioId usuarioId, 
        String categoria, 
        LocalDate dataInicio, 
        LocalDate dataFim
    );
    
    /**
     * Busca as transações mais recentes de um usuário.
     * 
     * @param usuarioId o ID do usuário
     * @param limite número máximo de transações
     * @return lista das transações mais recentes
     */
    List<Transacao> buscarRecentes(UsuarioId usuarioId, int limite);
    
    /**
     * Calcula o saldo atual de um usuário.
     * 
     * @param usuarioId o ID do usuário
     * @return o saldo atual (receitas - despesas)
     */
    BigDecimal calcularSaldoAtual(UsuarioId usuarioId);
    
    /**
     * Calcula o total de receitas de um usuário em um período.
     * 
     * @param usuarioId o ID do usuário
     * @param dataInicio data de início do período
     * @param dataFim data de fim do período
     * @return total de receitas no período
     */
    BigDecimal calcularReceitasPeriodo(UsuarioId usuarioId, LocalDate dataInicio, LocalDate dataFim);
    
    /**
     * Calcula o total de despesas de um usuário em um período.
     * 
     * @param usuarioId o ID do usuário
     * @param dataInicio data de início do período
     * @param dataFim data de fim do período
     * @return total de despesas no período
     */
    BigDecimal calcularDespesasPeriodo(UsuarioId usuarioId, LocalDate dataInicio, LocalDate dataFim);
    
    /**
     * Atualiza uma transação existente.
     * 
     * @param transacao a transação com dados atualizados
     * @return a transação atualizada
     */
    Transacao atualizar(Transacao transacao);
    
    /**
     * Remove uma transação do repositório (soft delete).
     * 
     * @param id o ID da transação a ser removida
     */
    void remover(TransacaoId id);
    
    /**
     * Busca transações similares para detecção de duplicatas.
     * 
     * @param usuarioId o ID do usuário
     * @param valor o valor da transação
     * @param data a data da transação
     * @param descricao a descrição da transação
     * @return lista de transações similares
     */
    List<Transacao> buscarSimilares(UsuarioId usuarioId, BigDecimal valor, LocalDate data, String descricao);
}