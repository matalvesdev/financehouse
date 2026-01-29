package com.gestaofinanceira.application.ports.repository;

import com.gestaofinanceira.domain.entities.Orcamento;
import com.gestaofinanceira.domain.valueobjects.OrcamentoId;
import com.gestaofinanceira.domain.valueobjects.StatusOrcamento;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Port para persistência de orçamentos.
 * 
 * Define as operações de persistência necessárias para a entidade Orcamento,
 * incluindo consultas para monitoramento e controle de gastos.
 */
public interface OrcamentoRepository {
    
    /**
     * Salva um orçamento no repositório.
     * 
     * @param orcamento o orçamento a ser salvo
     * @return o orçamento salvo com ID gerado
     */
    Orcamento salvar(Orcamento orcamento);
    
    /**
     * Busca um orçamento por ID.
     * 
     * @param id o ID do orçamento
     * @return Optional contendo o orçamento se encontrado
     */
    Optional<Orcamento> buscarPorId(OrcamentoId id);
    
    /**
     * Busca orçamentos ativos de um usuário.
     * 
     * @param usuarioId o ID do usuário
     * @return lista de orçamentos ativos
     */
    List<Orcamento> buscarAtivosPorUsuario(UsuarioId usuarioId);
    
    /**
     * Busca todos os orçamentos de um usuário (ativos e inativos).
     * 
     * @param usuarioId o ID do usuário
     * @return lista de todos os orçamentos do usuário
     */
    List<Orcamento> buscarPorUsuario(UsuarioId usuarioId);
    
    /**
     * Busca orçamento ativo de um usuário por categoria.
     * 
     * @param usuarioId o ID do usuário
     * @param categoria a categoria do orçamento
     * @return Optional contendo o orçamento ativo da categoria
     */
    Optional<Orcamento> buscarAtivoPorUsuarioECategoria(UsuarioId usuarioId, String categoria);
    
    /**
     * Busca orçamentos por status.
     * 
     * @param usuarioId o ID do usuário
     * @param status o status dos orçamentos
     * @return lista de orçamentos com o status especificado
     */
    List<Orcamento> buscarPorUsuarioEStatus(UsuarioId usuarioId, StatusOrcamento status);
    
    /**
     * Busca orçamentos que estão próximos do limite (80% ou mais).
     * 
     * @param usuarioId o ID do usuário
     * @return lista de orçamentos próximos do limite
     */
    List<Orcamento> buscarProximosDoLimite(UsuarioId usuarioId);
    
    /**
     * Busca orçamentos que excederam o limite.
     * 
     * @param usuarioId o ID do usuário
     * @return lista de orçamentos que excederam o limite
     */
    List<Orcamento> buscarExcedidos(UsuarioId usuarioId);
    
    /**
     * Busca orçamentos que vencem em uma data específica.
     * 
     * @param data a data de vencimento
     * @return lista de orçamentos que vencem na data
     */
    List<Orcamento> buscarVencendoEm(LocalDate data);
    
    /**
     * Atualiza um orçamento existente.
     * 
     * @param orcamento o orçamento com dados atualizados
     * @return o orçamento atualizado
     */
    Orcamento atualizar(Orcamento orcamento);
    
    /**
     * Remove um orçamento do repositório.
     * 
     * @param id o ID do orçamento a ser removido
     */
    void remover(OrcamentoId id);
    
    /**
     * Arquiva orçamentos vencidos.
     * 
     * @param dataLimite data limite para arquivamento
     * @return número de orçamentos arquivados
     */
    int arquivarVencidos(LocalDate dataLimite);
}